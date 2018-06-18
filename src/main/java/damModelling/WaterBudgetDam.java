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

package damModelling;

import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import java.util.HashMap;
import java.util.Map;
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
public class WaterBudgetDam{


	@Description("Input recharge Hashmap")
	@In
	public HashMap<Integer, double[]> inHMRechargeValues;
	
/*	@Description("Input recharge Hashmap")
	@In
	public HashMap<Integer, double[]> inHMPrecipValues;*/

	@Description("Input CI Hashmap")
	@In
	public HashMap<Integer, double[]>initialConditionS_i;
	
	@Description("Input dam trasfers Hashmap ")
	@In
	public HashMap<Integer, double[]> inHMerogazioni;

	
	@Description("Dam stage CI")
	@In
	public double h_CI;
	
	@Description("Stage of spillway activation")
	@In
	public double h_sfioro;
	

	
	@Description("Spillway equation coefficient")
	@In
	public double mu;
	
	@Description("Spillway length")
	@In
	public double l;


	@Description("Coefficient of the linear equation beteween the dam stage and the surface area")
	@In
	public double a_surface;	
	
	@Description("Known term of the linear equation beteween the dam stage and the surface area")
	@In
	public double b_surface;
	

	@Description("ODE solver model: dp853, Eulero ")
	@In
	public String solver_model;

	@Description("The output HashMap with the dam stage")
	@Out
	public HashMap<Integer, double[]> outHMLevel= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the total discharge (trasfer+spillway) ")
	@Out
	public HashMap<Integer, double[]> outHMDischarge ;
	
	@Description("The output HashMap with the spillway discharge")
	@Out
	public HashMap<Integer, double[]> outHMSfiori ;

	@Description("The output HashMap with the surface area")
	@Out
	public HashMap<Integer, double[]> outHMSurface;

	
	int step;

	double CI;


	/**
	 * Process: reading of the data, computation of the
	 * stage in the dam and outflows
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception {
		//checkNull(inHMRechargeValues);

		outHMSurface= new HashMap<Integer, double[]>() ;
		outHMDischarge= new HashMap<Integer, double[]>() ;
		outHMSfiori= new HashMap<Integer, double[]>() ;
		
		// reading the ID of all the stations 
		Set<Entry<Integer, double[]>> entrySet = inHMRechargeValues.entrySet();




		// iterate over the station
		for( Entry<Integer, double[]> entry : entrySet ) {
			Integer ID = entry.getKey();

			if(step==0){

				if(initialConditionS_i!=null){
					CI=initialConditionS_i.get(ID)[0];
					if (isNovalue(CI)) CI= h_CI;
				}else{
					CI=h_CI;
				}
				System.out.println("XXXXXXXXXXXXXXXXXXXXX"+CI);
			}

					
			
			
			/**Input data reading*/
			double recharge = inHMRechargeValues.get(ID)[0];
			if (isNovalue(recharge)) recharge= 0;
			
			/**Input data reading*/
			double erogazioni = inHMerogazioni.get(ID)[0];
			if (isNovalue(erogazioni)) erogazioni= 0;
			
/*			double precip = inHMPrecipValues.get(ID)[0];
			if (isNovalue(precip)) recharge= 0;*/
			
			//precip=precip/1000*A*Math.pow(10, 6)/(60*timeStep);
			
			double precip=0;

			double level=computeS(recharge,CI,precip,erogazioni,l);
			double surface=computeSurface(level);
			double Q_sfioro=computeQ(level);
			double discharge=Q_sfioro+erogazioni;
	

			/** Save the result in  hashmaps for each station*/
			storeResult_series(ID,level,discharge,Q_sfioro,surface);

			CI=level;

		}


		step++;

	}


	public double computeS(double recharge, double h_i, double precip,double erogazioni, double l) throws IOException {


		/** Creation of the differential equation*/
		FirstOrderDifferentialEquations ode=new waterBudgetODE(recharge,precip,a_surface,b_surface,mu,l, erogazioni, h_sfioro);			

		/** Boundaries conditions*/
		double[] y = new double[] { h_i, h_CI };

		/** Choice of the ODE solver */	
		SolverODE solver;
		solver=SimpleIntegratorFactory.createSolver(solver_model, 1, ode, y);

		/** result of the resolution of the ODE*/
		h_i=solver.integrateValues();


		return h_i;
	}



	public double computeQ(double h_i) throws IOException {
		double Q=mu*l*Math.pow(2*9.81,0.5)*Math.pow(Math.max(0,h_i-h_sfioro),1.5);
		return Q;
	}


	public double computeSurface(double h_i) throws IOException {
		double A=(a_surface*h_i-b_surface);
		return A;
	}


	private void storeResult_series(int ID, double level,double discharge, double Q_sfioro,double surface)
			throws SchemaException {

		outHMLevel.put(ID, new double[]{level});
		outHMDischarge.put(ID, new double[]{discharge});
		outHMSfiori.put(ID, new double[]{Q_sfioro});
		outHMSurface.put(ID, new double[]{surface});


	}


}