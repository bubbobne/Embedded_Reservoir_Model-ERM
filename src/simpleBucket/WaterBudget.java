package simpleBucket;

import static org.hortonmachine.gears.libs.modules.HMConstants.isNovalue;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.text.Utilities;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;

import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;
import oms3.annotations.Unit;
import utility.Utils;

/**
 * The Class WaterBudget solves the water budget equation for the runoff layer.
 * The input s the recharge from the root zone and the output is the discharge,
 * modeled with a non linear reservoir model.
 * 
 * @author Marialaura Bancheri, Riccardo Busti
 */
public class WaterBudget {

	@Description("Input recharge Hashmap")
	@In
	public HashMap<Integer, double[]> inHMRechargeValues;

	@Description("Input CI Hashmap")
	@In
	public HashMap<Integer, double[]> initialConditionS_i;

	@Description("Time Step simulation")
	@Unit("minutes")
	@In
	public double tTimestep;

	@Description("Coefficient of the non-linear Reservoir model ")
	@In
	public double c;

	@Description("Exponent of non-linear reservoir")
	@In
	public double d;

	@Description("The area of the HRUs in km2")
	@In
	public double A;

	@Description("s_RunoffMax")
	@In
	public double s_RunoffMax;

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
				System.out.println("RU--c:" + c + "-d:" + d + "-s_RunoffMax:" + s_RunoffMax);

				if (initialConditionS_i != null) {
					CI = initialConditionS_i.get(ID)[0];
					if (isNovalue(CI))
						CI = 0.5 * s_RunoffMax;
				} else {
					CI = 0.5 * s_RunoffMax;
				}
			}

			double m3s = A * Math.pow(10, 3) / (tTimestep * 60);

			// solve S at t^n+1

			double[] output = RK4(CI);
			double waterStorage = output[0];
			if (waterStorage < 0)
				waterStorage = 0;
			double error = output[1];

			// update variables at t^n+1
			double runoff_mm = output[2];
			double runoff = runoff_mm * m3s;

			// save results
			storeResult_series(ID, waterStorage, runoff_mm, runoff, error);

			// update storage
			CI = waterStorage;
		}
		step++;
	}

	// compute dS/dt
	public double[] computeFunction(double Sn) {
		if (Sn < 0) {
			Sn = 0;
		}
		double out = computeRunoff(Sn);
		double fun = recharge - out;
		return new double[] { fun, out };
	}

	// compute deep discharge
	public double computeRunoff(double Sn) {
		// double out = Math.max(c,recharge) * Math.pow(Sn / s_RunoffMax, d);
		double out = c * Math.pow(Math.min(1, Sn / s_RunoffMax), d);
		out = out + Math.max(0, Sn - s_RunoffMax + recharge - out);
		out = Math.min(Sn + recharge, out);
		return out;
	}

	// RK4
	public double[] RK4(double Sn) {
		double Sn0 = Sn;
		double t = 0;
		double dt = 0.01;
		double dtMin = 0.01;
		double dtMax = 0.1;
		double dSMax = 0.1;
		double dSMin = 0.01;
		double dSToll = 0.01;
		double runoff = 0;
		double balance = 0;
		double Sn1 = 0;
		double test = 0;

		while (t <= 1.0) {

			double[] k1 = computeFunction(Sn);
			double[] k2 = computeFunction(Sn + 0.5 * dt * k1[0]);
			double[] k3 = computeFunction(Sn + 0.5 * dt * k2[0]);
			double[] k4 = computeFunction(Sn + dt * k3[0]);

			double Sn1OneStep = Utils.getRKMean(k1, k2, k3, k4, 0);
			double runoffOneStep = Utils.getRKMean(k1, k2, k3, k4, 1);
			// double deltaRunoff = runoffOneStep;
			k2 = computeFunction(Sn + 0.25 * dt * k1[0]);
			k3 = computeFunction(Sn + 0.25 * dt * k2[0]);
			k4 = computeFunction(Sn + 0.5 * dt * k3[0]);
			double Sn1HalfStep = Utils.getRKMean(k1, k2, k3, k4, 0);
			double runoffHalfStep = Utils.getRKMean(k1, k2, k3, k4, 1);

			k2 = computeFunction(Sn + dt * k1[0]);
			k3 = computeFunction(Sn + dt * k2[0]);
			k4 = computeFunction(Sn + 2 * dt * k3[0]);
			double Sn1DoubleStep = Utils.getRKMean(k1, k2, k3, k4, 0);
			double runoffDpubleStep = Utils.getRKMean(k1, k2, k3, k4, 1);

			double deltaRunoff = 0;
			if (Math.abs(dt * Sn1OneStep) < dSToll) {
				deltaRunoff = runoffOneStep;
				Sn1 = Sn + dt * Sn1OneStep;
				t = t + dt;
				if (dt != dtMin) {
					dt = dtMin;
				}
			} else {
				if (Math.abs(dt * Sn1OneStep) > dSToll
						&& Math.abs(Sn1OneStep - Sn1HalfStep) / Math.abs(Sn1OneStep) > dSMax) {
					dt = dt / 2; 
					Sn1 = Sn + dt * Sn1HalfStep;
					deltaRunoff = runoffHalfStep;

				} else if (dt*2<dtMax &&   Math.abs(dt * Sn1OneStep) > dSToll
						&& Math.abs(Sn1OneStep - Sn1DoubleStep) / Math.abs(Sn1OneStep) < dSMin) {
					dt = dt * 2; 
					Sn1 = Sn + dt * Sn1DoubleStep;
					deltaRunoff = runoffDpubleStep;

				} else {
					Sn1 = Sn + dt * Sn1OneStep;
					deltaRunoff = runoffOneStep;

				}
				t = t + dt;
			}

			runoff = runoff + dt * deltaRunoff;
			balance = balance + Sn - Sn1 + dt * recharge - dt * deltaRunoff;
			Sn = Sn1;
			if(t+dt>1.0) {
				dt = 1.00000000001-t;
			}
			System.out.println(t);
		}
		return new double[] { Sn1, balance, runoff };
	}

	private void storeResult_series(int ID, double S, double r_mm, double r, double err) {

		outHMStorage.put(ID, new double[] { S });
		outHMDischarge.put(ID, new double[] { r });
		outHMDischarge_mm.put(ID, new double[] { r_mm });
		outHMError.put(ID, new double[] { err });

	}

	class runoffODE implements FirstOrderDifferentialEquations {

		private double in;

		public runoffODE(double in) {
			this.in = in;
		}

		public int getDimension() {
			return 1;
		}

		public void computeDerivatives(double t, double[] y, double[] yDot) {
			yDot[0] = in - c * Math.pow(Math.min(1, y[0] / s_RunoffMax), d);

		}

	}
}