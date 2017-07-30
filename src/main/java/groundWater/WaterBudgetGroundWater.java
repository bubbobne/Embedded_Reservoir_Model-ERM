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
 * The Class WaterBudget solves the water budget equation for the groudwater layer.
 * The input s the recharge from the root zone and the output is the discharge, 
 * modeled with a non linear reservoir model.
 * 
 * @author Marialaura Bancheri
 */
public class WaterBudgetGroundWater{


	@Description("Input recharge Hashmap")
	@In
	public HashMap<Integer, double[]> inHMRechargeValues;
	
	
	@Description("Time Step simulation")
	@In
	public int timeStep;


	@Description("Coefficient of the non-linear Reservoir model ")
	@In
	public double a ;


	@Description("Exponent of non-linear reservoir")
	@In
	public double b;

	@Description("The area of the HRUs in km2")
	@In
	public double A;
	
	@Description("Smax")
	@In
	public double Smax;


	@Description("ODE solver model: dp853, Eulero ")
	@In
	public String solver_model;
	
	@Description("The output HashMap with the Water Storage")
	@Out
	public HashMap<Integer, double[]> outHMStorage= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the discharge")
	@Out
	public HashMap<Integer, double[]> outHMDischarge= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the discharge in mm")
	@Out
	public HashMap<Integer, double[]> outHMDischarge_mm= new HashMap<Integer, double[]>() ;

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
				initialConditionS_i.put(ID,new double[]{Smax/2});
			}
		}

		// iterate over the station
		for( Entry<Integer, double[]> entry : entrySet ) {
			Integer ID = entry.getKey();

			/**Input data reading*/
			double recharge = inHMRechargeValues.get(ID)[0];
			if (isNovalue(recharge)) recharge= 0;
			if(step==0&recharge==0)recharge= 1;


			double waterStorage=computeS(recharge,initialConditionS_i.get(ID)[0]);
			double discharge_mm=computeQ(waterStorage);
			
			double discharge=discharge_mm/1000*A*Math.pow(10, 6)/(60*timeStep);		

			/** Save the result in  hashmaps for each station*/
			storeResult_series(ID,waterStorage,discharge,discharge_mm);

			initialConditionS_i.put(ID,new double[]{waterStorage});

		}


		step++;

	}

	/**
	 * Compute the water storage
	 *
	 * @param totalInputFluxes: input total input fluxes
	 * @param the initial condition of the storage
	 * @return the water storage, according to the model and the layer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeS(double recharge, double S_i) throws IOException {


		/** Creation of the differential equation*/
		FirstOrderDifferentialEquations ode=new waterBudgetODE(recharge,a,b, Smax);			

		/** Boundaries conditions*/
		double[] y = new double[] { S_i, Smax };

		/** Choice of the ODE solver */	
		SolverODE solver;
		solver=SimpleIntegratorFactory.createSolver(solver_model, 1, ode, y);

		/** result of the resolution of the ODE*/
		S_i=solver.integrateValues();

		/** Check of the Storage values: they cannot be negative*/
		if (S_i<0) S_i=0;
		
		//System.out.println("GW:"+S_i);
		//System.out.println("a:"+a+"b:"+b+"Smax:"+Smax);

		return S_i;
	}


	/**
	 *
	 * @param S_i: the actual storage 
	 * @return the double value of the simulated discharge
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeQ(double S_i) throws IOException {
		double Q=a*Math.pow(S_i/Smax, b);
		return Q;
	}




	/**
	 * Store of the results in hashmaps 
	 *
	 * @param waterStorage is the water storage
	 * @param discharge is the discharge
	 * @param discharge is the discharge in mm
	 * @throws SchemaException the schema exception
	 */

	private void storeResult_series(int ID, double waterStorage,double discharge, double discharge_mm)
			throws SchemaException {

		outHMStorage.put(ID, new double[]{waterStorage});
		outHMDischarge.put(ID, new double[]{discharge});
		outHMDischarge_mm.put(ID, new double[]{discharge_mm});


	}


}