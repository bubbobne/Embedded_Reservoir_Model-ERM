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

package canopyIN;

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


/**
 * The component solves the budget for the inner part of the canopy layer.
 * Inputs are: the uptake of the roots and the potential evapotranspiration
 * Outputs are: the storage and the actual ET .
 */

public class WaterBudgetCanopyIN{


	@Description("Input root uptake hashmap")
	@In
	public HashMap<Integer, double[]> inHMRootUpTake;


	@Description("Input potential evapotranspiration Hashmap")
	@In
	public HashMap<Integer, double[]> inHMETp;
	
	@Description("ETp: Potential evapotranspiration value for the given time considered")
	double ETp;
	
	@Description("ET model: AET,LAI")
	@In
	public String ET_model;
	
	ETModel ETmodel;

	
	@Description("Leaf Area Index")
	@In
	public  HashMap<Integer, double[]> inHMLAI;

	@Description("SCF parameter in case of AET simulated with LAI")
	@In
	public static double k ;

	@Description("crop coefficient canopy in")
	@In
	public static double kc_canopy_in;

	@Description("Initial condition storage")
	@In
	public static double IntialConditionStorage;

	@Description("ODE solver model: dp853, Eulero ")
	@In
	public String solver_model;


	@Description("The output HashMap with the Water Storage  ")
	@Out
	public HashMap<Integer, double[]> outHMStorage= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the AET ")
	@Out
	public HashMap<Integer, double[]> outHMAET= new HashMap<Integer, double[]>() ;


	HashMap<Integer, double[]>initialConditionS_i= new HashMap<Integer, double[]>();
	int step;

	@Description("Integration time")
	double dt=1E-4; ;



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
		Set<Entry<Integer, double[]>> entrySet = inHMRootUpTake.entrySet();

		if(step==0){
			for (Entry<Integer, double[]> entry : entrySet){
				Integer ID = entry.getKey();
				initialConditionS_i.put(ID,new double[]{IntialConditionStorage});
			}
		}

		// iterate over the station
		for( Entry<Integer, double[]> entry : entrySet ) {
			Integer ID = entry.getKey();

			/**Input data reading*/
			double rootUpTake = inHMRootUpTake.get(ID)[0];
			if (isNovalue(rootUpTake)) rootUpTake= 0;


			double LAI= inHMLAI.get(ID)[0];
			if (isNovalue(LAI)) LAI= 0;


			ETp=0;
			if (inHMETp != null) ETp = inHMETp.get(ID)[0];
			if (isNovalue(ETp)) ETp= 0;

			double waterStorage=computeS(rootUpTake,initialConditionS_i.get(ID)[0],LAI);

			double evapotranspiration=computeAET(waterStorage, LAI);

			/** Save the result in  hashmaps for each station*/
			storeResult_series(ID,waterStorage,evapotranspiration);

			/** Updates the initial conditions for the following time step*/
			initialConditionS_i.put(ID,new double[]{waterStorage});

		}


		step++;

	}

	/**
	 * Compute the water storage
	 *
	 * @param rootUpTake: root uptake
	 * @param S_i : initial condition
	 * @param LAI: leaf area index
	 * @return the water storage, according to the model and the layer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeS(double rootUpTake, double S_i,  double LAI) throws IOException {

		//(Brisson et al., 1998):
		double s_CanopyMax=kc_canopy_in*LAI;

		/** SimpleFactory for the computation of ET, according to the model*/
		ETmodel=SimpleETModelFactory.createModel(ET_model,S_i,s_CanopyMax,k,LAI);
		double AETmod=ETp*ETmodel.ETcoefficient();


		/** Creation of the differential equation*/
		FirstOrderDifferentialEquations ode=new waterBudgetODE(rootUpTake, AETmod);			

		/** Boundaries conditions*/
		double[] y = new double[] { S_i, 0 };

		/** Choice of the ODE solver */	
		SolverODE solver;
		solver=SimpleIntegratorFactory.createSolver(solver_model, dt, ode, y);

		/** result of the resolution of the ODE*/
		S_i=solver.integrateValues();

		/** Check of the Storage values: they cannot be negative*/
		if (S_i<0) S_i=0;

		return S_i;
	}


	/**
	 * Compute the AET
	 *
	 * @param S_i: storage at the considered time step
	 * @param LAI: leaf area index
	 * @return the double value of the AET
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeAET(double S_i,double LAI) throws IOException {
		/** SimpleFactory for the computation of ET, according to the model*/
		ETmodel=SimpleETModelFactory.createModel(ET_model,S_i,kc_canopy_in*LAI,k,LAI);
		double AETmod=ETp*ETmodel.ETcoefficient();
		return AETmod;
	}




	/**
	 * Store of the results in hashmaps 
	 *
	 * @param waterStorage is the water storage
	 * @param evapotranspiration is the evapotranspiration
	 * @throws SchemaException the schema exception
	 */

	private void storeResult_series(int ID, double waterStorage,double evapotranspiration) throws SchemaException {

		outHMStorage.put(ID, new double[]{waterStorage});
		outHMAET.put(ID, new double[]{evapotranspiration});

	}


}