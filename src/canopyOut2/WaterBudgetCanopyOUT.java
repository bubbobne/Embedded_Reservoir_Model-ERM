package canopyOut2;

import static org.hortonmachine.gears.libs.modules.HMConstants.isNovalue;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;

/**
 * The component solves the budget for the outer part of the canopy layer.
 * Inputs are: the rain and the potential evapotranspiration Outputs are: the
 * storage and the throughfall.
 * 
 * @author Marialaura Bancheri, Riccardo Busti
 * 
 */

public class WaterBudgetCanopyOUT {

	@Description("Input rain Hashmap")
	@In
	public HashMap<Integer, double[]> inHMRain;

	@Description("Input ETp Hashmap")
	@In
	public HashMap<Integer, double[]> inHMETp;

	@Description("Input CI Hashmap")
	@In
	public HashMap<Integer, double[]> initialConditionS_i;

	@Description("Leaf Area Index Hashmap")
	@In
	public HashMap<Integer, double[]> inHMLAI;

	@Description("coefficient canopy out")
	@In
	public double kc;

	@Description("Time step")
	@In
	public double tTimestep;

	@Description("Partitioning coefficient free throughfall")
	@In
	public double p;

	@Description("RK iterations")
	@In
	public double RKiter = 100;

	// @Description("ODE solver model:dp853, Eulero ")
	// @In
	// public String solver_model;

	@Description("The HashMap with the Actual input of the layer ")
	@Out
	public HashMap<Integer, double[]> outHMActualInput = new HashMap<Integer, double[]>();

	@Description("The HashMap with the Actual input of the layer ")
	@Out
	public HashMap<Integer, double[]> outHMActualOutput = new HashMap<Integer, double[]>();

	@Description("The output HashMap with the Water Storage  ")
	@Out
	public HashMap<Integer, double[]> outHMStorage = new HashMap<Integer, double[]>();

	@Description("The output HashMap with the Throughfall ")
	@Out
	public HashMap<Integer, double[]> outHMThroughfall = new HashMap<Integer, double[]>();

	@Description("The output HashMap with the AET ")
	@Out
	public HashMap<Integer, double[]> outHMAET = new HashMap<Integer, double[]>();

	@Description("The output HashMap with the AET ")
	@Out
	public HashMap<Integer, double[]> outHMError = new HashMap<Integer, double[]>();

	int step;
	double rain;
	double CI;
	double ETp;
	double s_CanopyMax;

	/**
	 * Process: reading of the data, computation of the storage and outflows
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception {

		// reading the ID of all the stations
		Set<Entry<Integer, double[]>> entrySet = inHMRain.entrySet();

		// iterate over the station
		for (Entry<Integer, double[]> entry : entrySet) {
			Integer ID = entry.getKey();

			/** Input data reading */
			rain = inHMRain.get(ID)[0];
			if (isNovalue(rain))
				rain = 0.0;

			double LAI = inHMLAI.get(ID)[0];
			if (isNovalue(LAI))
				LAI = 0.6;
			LAI = (LAI == 0) ? 0.6 : LAI;

			ETp = inHMETp.get(ID)[0];
			if (isNovalue(ETp))
				ETp = 0.0;

			if (step == 0) {
				System.out.println("C--kc:" + kc + "-p:" + p);

				if (initialConditionS_i != null) {
					CI = initialConditionS_i.get(ID)[0];
					if (isNovalue(CI))
						CI = kc * LAI / 2;
				} else {
					CI = kc * LAI / 2;
				}
			}

			s_CanopyMax = kc * LAI;

			double actualInput = (1 - p) * rain;

			// solve S at t^n+1
			double[] out = RK4(CI, actualInput);
			double waterStorage = out[0];
			if (waterStorage < 0)
				waterStorage = 0;
			double error = out[1];

			// update variables at t^n+1
			double actualOutput = out[3];
			double throughfall = actualOutput + p * rain;
			double AET = out[2];
			// export to timeseries
			System.out.println(CI - waterStorage + rain - throughfall - AET);
			storeResult_series(ID, waterStorage, throughfall, AET, actualInput, actualOutput, error);

			// set new IC
			CI = waterStorage;

		}
		step++;
	}

	// compute dS/dt
	public double computeFunction(double Sn, double in) {
		double actualOut = computeActualOutput(Sn);
		return in - computeAET(Sn, in, actualOut) - actualOut;
	}

	// compute AET
	public double computeAET(double Sn, double in, double out) {
		return Math.min(Sn + in - out, ETp * Math.min(1, Sn / s_CanopyMax));
	}

	// compute actual output
	public double computeActualOutput(double Sn) {
		return Math.max(0, Sn - s_CanopyMax);
	}

	// RK4
	public double[] RK4(double Sn, double in) {
		double k1 = 0;
		double k2 = 0;
		double k3 = 0;
		double k4 = 0;
		double balance = 0;
		double min = 60;
		double aet = 0;
		double actualOut = 0;
		double dt = 1.0 / RKiter;
		for (int k = 0; k < RKiter; k++) {

			k1 = computeFunction(Sn, in);
			k2 = computeFunction(Sn + 0.5 * dt * k1, in);
			k3 = computeFunction(Sn + 0.5 * dt * k2, in);
			k4 = computeFunction(Sn + dt * k3, in);
			double Sn1 = Sn + dt * (k1 + 2 * k2 + 2 * k3 + k4) / 6;
			double deltaActualOut = computeActualOutput(Sn);
			double deltaAET = dt * computeAET(Sn, in, deltaActualOut);
			aet = aet + deltaAET;
			deltaActualOut = dt * deltaActualOut;
			actualOut = actualOut + deltaActualOut;
			balance = balance + Sn - Sn1 + dt * in - deltaAET - deltaActualOut;
			Sn = Sn1;
		}


		return new double[] { Sn, balance, aet, actualOut };
	}

	// store results
	private void storeResult_series(int ID, double S, double tr, double aet, double in, double out, double err) {

		outHMStorage.put(ID, new double[] { S });
		outHMThroughfall.put(ID, new double[] { tr });
		outHMAET.put(ID, new double[] { aet });
		outHMActualInput.put(ID, new double[] { in });
		outHMActualOutput.put(ID, new double[] { out });
		outHMError.put(ID, new double[] { err });

	}

}