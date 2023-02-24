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

package groundWater2;

import static org.hortonmachine.gears.libs.modules.HMConstants.isNovalue;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;

/**
 * The Class WaterBudget solves the water budget equation for the groudwater
 * layer. The input s the recharge from the root zone and the output is the
 * discharge, modeled with a non linear reservoir model.
 * 
 * @author Marialaura Bancheri, Riccardo Busti
 */
public class WaterBudgetGround {

	@Description("Input recharge Hashmap")
	@In
	public HashMap<Integer, double[]> inHMRechargeValues;

	@Description("Input CI Hashmap")
	@In
	public HashMap<Integer, double[]> initialConditionS_i;

	@Description("Time Step simulation")
	@In
	public double tTimestep;

	@Description("Coefficient of the non-linear Reservoir model ")
	@In
	public double e;

	@Description("Exponent of non-linear reservoir")
	@In
	public double f;

	@Description("The area of the HRUs in km2")
	@In
	public double A;

	@Description("s_GroundWaterMax")
	@In
	public double s_GroundWaterMax;

	@Description("RK iterations")
	@In
	public double RKiter = 100;

	// @Description("ODE solver model: dp853, Eulero ")
	// @In
	// public String solver_model;

	@Description("The output HashMap with the Water Storage")
	@Out
	public HashMap<Integer, double[]> outHMStorage = new HashMap<Integer, double[]>();

	@Description("The output HashMap with the discharge")
	@Out
	public HashMap<Integer, double[]> outHMDischarge = new HashMap<Integer, double[]>();

	@Description("The output HashMap with the discharge in mm")
	@Out
	public HashMap<Integer, double[]> outHMDischarge_mm = new HashMap<Integer, double[]>();

	@Description("The output HashMap with error")
	@Out
	public HashMap<Integer, double[]> outHMError = new HashMap<Integer, double[]>();

	int step;
	double recharge;
	double CI;

	/**
	 * Process: reading of the data, computation of the storage and outflows
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception {

		// reading the ID of all the stations
		Set<Entry<Integer, double[]>> entrySet = inHMRechargeValues.entrySet();

		// iterate over the station
		for (Entry<Integer, double[]> entry : entrySet) {
			Integer ID = entry.getKey();

			/** Input data reading */
			recharge = inHMRechargeValues.get(ID)[0];
			if (isNovalue(recharge))
				recharge = 0;

			if (step == 0) {
				System.out.println("GW--e:" + e + "-f:" + f + "-s_GroundWaterMax:" + s_GroundWaterMax);

				if (initialConditionS_i != null) {
					CI = initialConditionS_i.get(ID)[0];
					if (isNovalue(CI))
						CI = 0.01 * s_GroundWaterMax;
				} else {
					CI = 0.01 * s_GroundWaterMax;
				}
			}

			double m3s = A * Math.pow(10, 3) / (tTimestep * 60);

			// solve S at t^n+1
			double[] out = RK4(CI);
			double waterStorage = out[0];
			if (waterStorage < 0)
				waterStorage = 0;
			double error = out[1];

			// update variables at t^n+1
			double deep_mm = out[2];
			double deep = deep_mm * m3s;

			// save results
			storeResult_series(ID, waterStorage, deep_mm, deep, error);

			// update storage
			CI = waterStorage;
		}
		step++;
	}

	// compute dS/dt
	public double computeFunction(double Sn) {
		double fun = recharge - computeDeep(Sn);
		return fun;
	}

	// compute deep discharge
	public double computeDeep(double Sn) {
		return e * Math.pow(Sn / s_GroundWaterMax, f);
	}

	// RK4
	public double[] RK4(double Sn) {
		double k1 = 0;
		double k2 = 0;
		double k3 = 0;
		double k4 = 0;
		double balance = 0;
		double min = 60;
		double deep = 0;
		double dt = 1.0 / RKiter;
		for (int k = 0; k < RKiter; k++) {
			k1 = computeFunction(Sn);
			k2 = computeFunction(Sn + 0.5 * dt * k1);
			k3 = computeFunction(Sn + 0.5 * dt * k2);
			k4 = computeFunction(Sn + dt * k3);
			double Sn1 = Sn + dt * (k1 + 2 * k2 + 2 * k3 + k4) / 6;
			double deltaDeep = dt * computeDeep(Sn);
			balance = balance + Sn - Sn1 + dt * recharge - deltaDeep;
			Sn = Sn1;
			deep = deep + deltaDeep;
		}
		return new double[] { Sn, balance, deep };
	}

	private void storeResult_series(int ID, double S, double d_mm, double d, double err) {

		outHMStorage.put(ID, new double[] { S });
		outHMDischarge.put(ID, new double[] { d });
		outHMDischarge_mm.put(ID, new double[] { d_mm });
		outHMError.put(ID, new double[] { err });

	}

}