package canopyOut;

import static org.hortonmachine.gears.libs.modules.HMConstants.isNovalue;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;
import rungekutta.CanopyRungeKutta;
import rungekutta.RungeKutta;;

/**
 * The component solves the budget for the outer part of the canopy layer.
 * Inputs are: the rain and the potential evapotranspiration Outputs are: the
 * storage and the throughfall.
 * 
 * @author Marialaura Bancheri, Riccardo Busti, Giuseppe Formetta, Daniele Andreis
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
	RungeKutta rk = null;

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
			if (isNovalue(ETp) || ETp < 0)
				ETp = 0.0;

			if (step == 0) {
				System.out.println("C--kc:" + kc + "-p:" + p);
				rk = new CanopyRungeKutta(ETp, s_CanopyMax);

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
			double[] out = rk.run(CI,actualInput, 0.01);
			double waterStorage = out[0];
			if (waterStorage < 0)
				waterStorage = 0;
			double aet = out[1];
			
			// update variables at t^n+1
			double actualOutput = out[2];
			double throughfall = actualOutput + p * rain;
			double error = out[3];
			// export to timeseries
			storeResult_series(ID, waterStorage, throughfall, aet, actualInput, actualOutput, error);

			// set new IC
			CI = waterStorage;

		}
		step++;
	}



	private void storeResult_series(int ID, double S, double tr, double aet, double in, double out, double err) {

		outHMStorage.put(ID, new double[] { S });
		outHMThroughfall.put(ID, new double[] { tr });
		outHMAET.put(ID, new double[] { aet });
		outHMActualInput.put(ID, new double[] { in });
		outHMActualOutput.put(ID, new double[] { out });
		outHMError.put(ID, new double[] { err });

	}

}