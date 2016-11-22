/*
 * GNU GPL v3 License
 *
 * Copyright 2015 Marialaura Bancheri
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package canopy;

import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;


import org.geotools.feature.SchemaException;


import java.io.IOException;

import org.apache.commons.math3.ode.*;



public class WaterBudgetCanopy{


	@Description("Input rain Hashmap")
	@In
	public HashMap<Integer, double[]> inHMRain;


	@Description("T: Transpiration value for the given time considered")
	double ETp;

	@Description("Input ETp Hashmap")
	@In
	public HashMap<Integer, double[]> inHMETp;


	@Description("Input root uptake HM")
	@In
	public HashMap<Integer, double[]> inHMRootUpTake;
	
	@Description("Leaf Area Index")
	@In
	public  HashMap<Integer, double[]> inHMLAI;


	@Description("Maximum interception capacity")
	@In
	public double Imax;	


	@Description("Integration time")
	double dt ;


	@Description("SCF parameter")
	@In
	public static double k ;
	
	@Description("Throughfall paramter")
	@In
	public static double a_c ;
	
	
	
	@Description("Throughfall paramter")
	@In
	public static double b_c ;


	@Description("Maximum value of the water storage, needed for the"
			+ "computation of the Actual EvapoTraspiration")
	@In
	@Out
	public static double s_CanopyMax;


	@Description("ET model: AET,ExternalValues")
	@In
	public String ET_model;

	@Description("ODE solver ")
	@In
	public String solver_model;

	@Description("Simluted value of AET"
			+ "at a given time step")
	double AET;

	ETModel ETmodel;


	@Description("The output HashMap with the Water Storage  ")
	@Out
	public HashMap<Integer, double[]> outHMStorage= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the discharge ")
	@Out
	public HashMap<Integer, double[]> outHMThroughfall= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the AET ")
	@Out
	public HashMap<Integer, double[]> outHMTranspiration = new HashMap<Integer, double[]>() ;


	HashMap<Integer, double[]>initialConditionS_i= new HashMap<Integer, double[]>();
	int step;




	/**
	 * Process: reading of the data, computation of the
	 * storage and outflows
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception {
		//checkNull(inHMRain);



		// reading the ID of all the stations 
		Set<Entry<Integer, double[]>> entrySet = inHMRain.entrySet();

		if(step==0){
			for (Entry<Integer, double[]> entry : entrySet){
				Integer ID = entry.getKey();
				initialConditionS_i.put(ID,new double[]{0.0});
			}
		}

		// iterate over the station
		for( Entry<Integer, double[]> entry : entrySet ) {
			Integer ID = entry.getKey();

			/**Input data reading*/
			double rain = inHMRain.get(ID)[0];
			if (isNovalue(rain)) rain= 0;

			double rootUpTake;
			if (inHMRootUpTake==null) rootUpTake=0;
			else if (inHMRootUpTake.isEmpty()) rootUpTake=0;
			else {
				 rootUpTake = inHMRootUpTake.get(ID)[0];
					if (isNovalue(rootUpTake)) rootUpTake= 0;
			}
			
			
			double LAI= inHMLAI.get(ID)[0];
			if (isNovalue(LAI)) LAI= 0;


			ETp=0;
			if (inHMETp != null) ETp = inHMETp.get(ID)[0];
			if (isNovalue(ETp)) ETp= 0;

			double waterStorage=computeS(rain,initialConditionS_i.get(ID)[0],rootUpTake,LAI);
			double throughfall=computeThroughfall(rain, waterStorage);
			double evapotranspiration=computeAET(waterStorage, LAI);


			/** Save the result in  hashmaps for each station*/
			storeResult_series(ID,waterStorage,throughfall,evapotranspiration);

			initialConditionS_i.put(ID,new double[]{waterStorage});

		}


		step++;

	}

	/**
	 * Compute the water storage
	 *
	 * @param J: input rain 
	 * @param Qinput : input discharge
	 * @param ET: input potential ET
	 * @return the water storage, according to the model and the layer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeS(double rain, double S_i, double rootUpTake, double LAI) throws IOException {
		/**integration time*/
		dt=1E-4;

		//Gomez et al. (2001)
		//s_CanopyMax=1.184+0.49*LAI;

		/** SimpleFactory for the computation of ET, according to the model*/
		ETmodel=SimpleETModelFactory.createModel(ET_model,S_i,s_CanopyMax,k,LAI);
		double Tmod=ETp*ETmodel.ETcoefficient();


		/** Creation of the differential equation*/
		FirstOrderDifferentialEquations ode=new waterBudgetODE(rain,computeThroughfall(rain,S_i), Tmod,rootUpTake);			

		/** Boundaries conditions*/
		double[] y = new double[] { S_i, 0 };

		/** Choice of the ODE solver */	
		SolverODE solver;
		solver=SimpleIntegratorFactory.createSolver(solver_model, dt, ode, y);

		/** result of the resolution of the ODE, if nZ=1, S_i=S_t
		 * and setting of the new initial condition (S_i)*/
		S_i=solver.integrateValues();

		/** Check of the Storage values: they cannot be negative*/
		if (S_i<0) S_i=0;

		return S_i;
	}


	public double computeThroughfall(double rain, double S_i) throws IOException {
		double throughfall=(S_i>Imax)?Math.pow(a_c*S_i,b_c):0;
		//double throughfall=rain-Math.min(Imax-S_i, rain);
		return throughfall;
	}



	/**
	 * Compute the AET
	 *
	 * @param ETinput: the input potential ET
	 * @return the double value od the AET
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeAET(double S_i,double LAI) throws IOException {
		/** SimpleFactory for the computation of ET, according to the model*/
		ETmodel=SimpleETModelFactory.createModel(ET_model,S_i,s_CanopyMax,k,LAI);
		double Tmod=ETp*ETmodel.ETcoefficient();
		return Tmod;
	}




	/**
	 * Store of the results in hashmaps 
	 *
	 * @param waterStorage is the water storage
	 * @param discharge is the discharge
	 * @param evapotranspiration is the evapotranspiration
	 * @throws SchemaException the schema exception
	 */

	private void storeResult_series(int ID, double waterStorage,double throughfall,
			double evapotranspiration) throws SchemaException {

		outHMStorage.put(ID, new double[]{waterStorage});
		outHMThroughfall.put(ID, new double[]{throughfall});
		outHMTranspiration.put(ID, new double[]{evapotranspiration});

	}


}