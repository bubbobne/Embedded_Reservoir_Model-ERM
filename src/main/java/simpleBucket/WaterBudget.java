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
import rungekutta.OneOutRungeKutta;
import rungekutta.RungeKutta;
import rungekutta.Utils;

/**
 * The Class WaterBudget solves the water budget equation for the runoff layer.
 * The input s the recharge from the root zone and the output is the discharge,
 * modeled with a non linear reservoir model.
 * 
 * @author Marialaura Bancheri, Riccardo Busti, Giuseppe Formetta, Daniele
 *         Andreis
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
	@Deprecated
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
	@Deprecated
	public double RKiter = 100;

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
	RungeKutta rk = null;
	double m3s = 0;

	/**
	 * Process: reading of the data, computation of the storage and outflows
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception {

		Set<Entry<Integer, double[]>> entrySet = inHMRechargeValues.entrySet();

		for (Entry<Integer, double[]> entry : entrySet) {
			Integer ID = entry.getKey();

			/** Input data reading */
			recharge = inHMRechargeValues.get(ID)[0];
			if (isNovalue(recharge))
				recharge = 0;
			if (step == 0) {
				init(ID);
			}

			double[] output = rk.run(CI, recharge, 0.01);

			storeResult_series(ID, output);

		}
		step++;
	}

	private void init(Integer ID) {
		System.out.println("RU--c:" + c + "-d:" + d + "-s_RunoffMax:" + s_RunoffMax);
		rk = new OneOutRungeKutta(c, d, s_RunoffMax);
		m3s = A * Math.pow(10, 3) / (tTimestep * 60);
		if (initialConditionS_i != null) {
			CI = initialConditionS_i.get(ID)[0];
			if (isNovalue(CI))
				CI = 0.5 * s_RunoffMax;
		} else {
			CI = 0.5 * s_RunoffMax;
		}
	}

	private void storeResult_series(int ID, double[] output) {
		double waterStorage = output[0];
		if (waterStorage < 0)
			waterStorage = 0;
		double error = output[2];
		double runoff_mm = output[1];
		double runoff = runoff_mm * m3s;
		CI = waterStorage;
		outHMStorage.put(ID, new double[] { waterStorage });
		outHMDischarge.put(ID, new double[] { runoff });
		outHMDischarge_mm.put(ID, new double[] { runoff_mm });
		outHMError.put(ID, new double[] { error });

	}
}