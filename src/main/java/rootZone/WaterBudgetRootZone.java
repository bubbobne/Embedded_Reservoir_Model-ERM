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

package rootZone;


import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;
import oms3.annotations.Unit;

import org.geotools.feature.SchemaException;

import rootZone.SimpleETModelFactory;

import java.io.IOException;

import org.apache.commons.math3.ode.*;


/**
 * The Class WaterBudget solves the water budget equation, according to
 * the models chosen for the simulation of the discharge and of the AET. 
 * The outputs of the class are 4 hashmaps with the water storage values,
 * simulated discharge values, simulated AET and the quick discharge (if there
 * is any). The simulated discharge is partitioned in two flows: a flow which drains
 * to the lower layer and the quick discharge.
 * For the lower layer, we don't have nor ET processes and the 
 * partitioning of the discharge in quick and drainage to other layers, so the third and
 * fourth columns of the first hashmap and the second hashmap will be equal to zero.
 * @author Marialaura Bancheri
 */
public class WaterBudgetRootZone{


	@Description("Input rain Hashmap")
	@In
	public HashMap<Integer, double[]> inHMRain;
	
	
	@Description("Input rain Hashmap")
	@In
	public HashMap<Integer, double[]> inHMSnow;


	@Description("Input ET Hashmap")
	@In
	public HashMap<Integer, double[]> inHMETp;


	@Description("Integration time")
	double dt ;


	@Description("Parameter of the non-linear Reservoir model "
			+ "for the considered layer")
	@In
	public double a ;


	@Description("Parameter of non-linear reservoir, for the upper layer")
	@In
	public double b;


	@Description("Maximum percolation rate")
	@In
	public double Pmax;
	
	@Unit("-")
	@In
  	public Double pB;
	
	
	@Description("partitioning coefficient between the reserovir")
	@Unit("-")
	@In
	@Out
	public double alpha;


	@Description("Maximum value of the water storage, needed for the"
			+ "computation of the Actual EvapoTraspiration")
	@In
	public double s_RootZoneMax;
	
	@Description("")
	@In
	public boolean connectTOcanopy;
	
	
	@Description("Discharge model: NonLinearReservoir, Clapp-H")
	@In
	public String UpTake_model;
	
	
	@Description("Alpha model: Hymod, Value ")
	@In
	public String Alpha_model;

	@Description("ET model: AET")
	@In
	public String ET_model;

	@Description("ODE solver ")
	@In
	public String solver_model;

	UpTakeModel model;
	ETModel ETModel;
	AlphaModel alphaModel;
	
	@Description("The output HashMap with the Water Storage  ")
	@Out
	public HashMap<Integer, double[]> outHMStorage= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the discharge ")
	@Out
	public HashMap<Integer, double[]> outHMRootUpTake= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the AET ")
	@Out
	public HashMap<Integer, double[]> outHMEvaporation = new HashMap<Integer, double[]>() ;


	@Description("The output HashMap with the outflow "
			+ "which drains to the lower layer")
	@Out
	public HashMap<Integer, double[]> outHMR= new HashMap<Integer, double[]>() ;

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
			
			double snow = 0;					
			if (inHMSnow != null) snow=inHMSnow.get(ID)[0];
			if (isNovalue(snow)) snow= 0;

			if (pB==null)pB=0.0;
			alphaModel=SimpleAlphaModelFactory.createModel(Alpha_model,pB, s_RootZoneMax, alpha, rain+snow,initialConditionS_i.get(ID)[0]);
			double alpha=(rain==0)?0:alphaModel.alphaValues();
					
			
			//System.out.println(alpha);
			
			
			double totalInputFluxes=(1-alpha)*(rain+snow);

			double ETp=0;
			if (inHMETp != null) ETp = inHMETp.get(ID)[0];
			if (isNovalue(ETp)) ETp= 0;

			double waterStorage=computeS(totalInputFluxes,initialConditionS_i.get(ID)[0], ETp);
			
			double upTake=(connectTOcanopy)?computeUpTake(waterStorage):0;
			double evapotranspiration=computeAET(waterStorage, ETp);
			double drainage=computeR(waterStorage);
			

			/** Save the result in  hashmaps for each station*/
			storeResult_series(ID,waterStorage,upTake,evapotranspiration,drainage);
			
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
	public double computeS(double totalInputFluxes, double S_i, double ETp) throws IOException {
		/**integration time*/
		dt=1E-4;

		double upTake=0;
		/** SimpleFactory for the computation of Q, according to the model*/
		if (connectTOcanopy){
		model=SimpleDischargeModelFactory.createModel(UpTake_model, a, S_i, b);
		upTake=model.dischargeValues();
		} 

		/** SimpleFactory for the computation of ET, according to the model*/
		ETModel=SimpleETModelFactory.createModel(ET_model, S_i, s_RootZoneMax);	
		double Emod=ETModel.ETcoefficient()*ETp;
		
		
		double Rg=Pmax*S_i/s_RootZoneMax;
		//double Rg=Pmax*S_i;

		/** Creation of the differential equation*/
		FirstOrderDifferentialEquations ode=new waterBudgetODE(totalInputFluxes,upTake, Emod, Rg);			

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

	// computation of the discharge according to the mode: 
	// mode external --> external value
	// else --> model
	/**
	 * Compute computation of the discharge according to the mode:
	 * mode external --> external value
	 * else --> non-linear reservoir model
	 *
	 * @param Qinput: input discharge value
	 * @return the double value of the simulated discharge
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeUpTake( double S_i) throws IOException {
		model=SimpleDischargeModelFactory.createModel(UpTake_model, a, S_i, b);
		double upTake=model.dischargeValues();
		return upTake;
	}


	/**
	 * Compute the outflow toward the lower layer
	 *
	 * @param Q: simulated discharge for the considered layer
	 * @return the double value of the outflow toward the lower layer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeR(double S_i) throws IOException {
		//double Rg=Pmax*S_i;
		double Rg=Pmax*S_i/s_RootZoneMax;
		return Rg;
	}


	/**
	 * Compute the AET
	 *
	 * @param ETinput: the input potential ET
	 * @return the double value od the AET
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeAET(double S_i, double ETp) throws IOException {
		/** SimpleFactory for the computation of ET, according to the model*/
		ETModel=SimpleETModelFactory.createModel(ET_model, S_i, s_RootZoneMax);	
		double Emod=ETModel.ETcoefficient()*ETp;
		return Emod;
	}
	



	/**
	 * Store of the results in hashmaps 
	 *
	 * @param waterStorage is the water storage
	 * @param discharge is the discharge
	 * @param evapotranspiration is the evapotranspiration
	 * @param quickRunoff is the water quick runoff from the layer
	 * @param drainage is drainage toward the lower layer
	 * @throws SchemaException the schema exception
	 */
	
	private void storeResult_series(int ID, double waterStorage,double upTake,
			double evapotranspiration,double drainage) throws SchemaException {

		outHMStorage.put(ID, new double[]{waterStorage});
		outHMRootUpTake.put(ID, new double[]{upTake});
		outHMEvaporation.put(ID, new double[]{evapotranspiration});
		outHMR.put(ID, new double[]{drainage});

	}
	

}