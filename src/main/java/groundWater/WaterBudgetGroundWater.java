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

package groundWater;

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
public class WaterBudgetGroundWater{


	@Description("Input rain Hashmap")
	@In
	public HashMap<Integer, double[]> inHMRechargeValues;
	

	@Description("Integration time")
	double dt ;


	@Description("Parameter of the non-linear Reservoir model "
			+ "for the considered layer")
	@In
	public static double a ;


	@Description("Parameter of non-linear reservoir, for the upper layer")
	@In
	public static double b;

	@Description("The area of the HRUs in km2")
	@In
	public static double A;


	@Description("Discharge model: NonLinearReservoir, Clapp-H")
	@In
	public String Q_model;


	@Description("ODE solver ")
	@In
	public String solver_model;

	DischargeModel model;

	
	@Description("The output HashMap with the Water Storage  ")
	@Out
	public HashMap<Integer, double[]> outHMStorage= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the discharge ")
	@Out
	public HashMap<Integer, double[]> outHMDischarge= new HashMap<Integer, double[]>() ;

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
		//checkNull(inHMRechargeValues);
		
       
		// reading the ID of all the stations 
		Set<Entry<Integer, double[]>> entrySet = inHMRechargeValues.entrySet();

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
			double recharge = inHMRechargeValues.get(ID)[0];
			if (isNovalue(recharge)) recharge= 0;
			
			
			

			double waterStorage=computeS(recharge,initialConditionS_i.get(ID)[0]);
			double discharge=computeQ(waterStorage);


			/** Save the result in  hashmaps for each station*/
			storeResult_series(ID,waterStorage,discharge);
			
			initialConditionS_i.put(ID,new double[]{waterStorage});

		}


		step++;

	}

	/**
	 * Compute the water storage
	 *
	 * @param J: input rain 
	 * @return the water storage, according to the model and the layer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeS(double totalInputFluxes, double S_i) throws IOException {
		/**integration time*/
		dt=1E-4;

		/** SimpleFactory for the computation of Q, according to the model*/
		model=SimpleDischargeModelFactory.createModel(Q_model, a, S_i, b);
		double Qmod=model.dischargeValues();


		/** Creation of the differential equation*/
		FirstOrderDifferentialEquations ode=new waterBudgetODE(totalInputFluxes,Qmod);			

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


	/**
	 *
	 * @param Qinput: input discharge value
	 * @return the double value of the simulated discharge
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeQ(double S_i) throws IOException {
		model=SimpleDischargeModelFactory.createModel(Q_model, a, S_i, b);
		double Q=model.dischargeValues();
		// the discharge is converted from mm3/mm2/h to m3/s
		Q=Q/1000*A*Math.pow(10, 6)/3600;
		return Q;
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
	
	private void storeResult_series(int ID, double waterStorage,double discharge) throws SchemaException {

		outHMStorage.put(ID, new double[]{waterStorage});
		outHMDischarge.put(ID, new double[]{discharge});


	}
	

}