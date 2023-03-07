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

import static org.hortonmachine.gears.libs.modules.HMConstants.isNovalue;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;
import rungekutta.adaptive.RootZoneRungeKutta;
import utils.Utility;;

/**
 * The Class WaterBudget solves the water budget equation for the root zone
 * layer.
 * 
 * @author Marialaura Bancheri, Riccardo Busti, Giuseppe Formetta, Daniele
 *         Andreis
 */
public class WaterBudgetRootZone {

	@Description("Input rain Hashmap")
	@In
	public HashMap<Integer, double[]> inHMRain;

	@Description("Input ET wet canopy Hashmap")
	@In
	public HashMap<Integer, double[]> inHMEwc;

	@Description("Input ET Hashmap")
	@In
	public HashMap<Integer, double[]> inHMETp;

	@Description("Input CI Hashmap")
	@In
	public HashMap<Integer, double[]> initialConditionS_i;

	@Description("The maximum storage capacity")
	@In
	public double pCmax;

	@Description("Maximum percolation rate")
	@In
	public double g;

	@Description("Exponential of non-linear reservoir")
	@In
	public double h;

	@Description("Degree of spatial variability of the soil moisture capacity")
	@In
	public double pB_soil;

	@Description("Maximum value of the water storage, needed for the computation of the Actual EvapoTraspiration")
	@In
	@Out
	public double s_RootZoneMax;

	@Description("CI of the water storage")
	@In
	@Out
	public double s_RootZoneCI;

	@Description("Initial saturation_degree")
	@In
	public double sat_degree = 0.5;

	@Description("RK iterations")
	@In
	@Deprecated
	public double RKiter = 100;

	@Description("The area of the HRUs in km2")
	@In
	public double A;

	@Description("Time step")
	@In
	@Deprecated
	/**
	 * only for back-compatibility
	 */
	public double tTimestep;

	@Description("The HashMap with the Actual input of the layer ")
	@Out
	public HashMap<Integer, double[]> outHMActualInput = new HashMap<Integer, double[]>();

	@Description("The output HashMap with the Water Storage  ")
	@Out
	public HashMap<Integer, double[]> outHMStorage = new HashMap<Integer, double[]>();

	@Description("The output HashMap with the AET ")
	@Out
	public HashMap<Integer, double[]> outHMEvaporation = new HashMap<Integer, double[]>();

	@Description("The output HashMap with the outflow which drains to the lower layer")
	@Out
	public HashMap<Integer, double[]> outHMR = new HashMap<Integer, double[]>();

	@Description("The output HashMap with the quick outflow ")
	@Out
	public HashMap<Integer, double[]> outHMquick = new HashMap<Integer, double[]>();

	@Description("The output HashMap with the quick outflow ")
	@Out
	public HashMap<Integer, double[]> outHMquick_mm = new HashMap<Integer, double[]>();

	@Description("The output HashMap with alpha ")
	@Out
	public HashMap<Integer, double[]> outHMalpha = new HashMap<Integer, double[]>();

	@Description("The output HashMap with mass balance ")
	@Out
	public HashMap<Integer, double[]> outHMError = new HashMap<Integer, double[]>();

	int step;
	double CI;
	RootZoneRungeKutta rk = null;
	double m3s = 0;

	/**
	 * Process: reading of the data, computation of the storage and outflows
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception {
		double ETp = 0;
		double Ewc = 0;
		double ETpNet = 0;
		double rain = 0;

		// reading the ID of all the stations
		Set<Entry<Integer, double[]>> entrySet = inHMRain.entrySet();

		// iterate over the station
		for (Entry<Integer, double[]> entry : entrySet) {
			Integer ID = entry.getKey();

			/** Input data reading */
			rain = inHMRain.get(ID)[0];
			if (isNovalue(rain))
				rain = 0;

			if (inHMETp != null)
				ETp = inHMETp.get(ID)[0];
			if (isNovalue(ETp))
				ETp = 0;

			if (inHMEwc != null)
				Ewc = inHMEwc.get(ID)[0];
			if (isNovalue(Ewc))
				Ewc = 0;

			if (step == 0) {
				init(ID);
			}

			ETpNet = ETp - Ewc;

			double[] out = rk.run(CI, rain, ETpNet, RKiter);

			storeResultAndUpdate(ID, out);

		}
		step++;
	}

	private void init(Integer ID) {
		System.out.println("RZ--grz:" + g + "-hrz:" + h + "-Smax:" + s_RootZoneMax + "-pB_soil:" + pB_soil);
		rk = new RootZoneRungeKutta(g, h, s_RootZoneMax, pB_soil);
		m3s = Utility.getCOnversionToM3SCoeff(A, tTimestep);

		if (initialConditionS_i != null) {
			CI = initialConditionS_i.get(ID)[0];
			if (isNovalue(CI))
				CI = s_RootZoneMax * sat_degree;

		} else {
			CI = s_RootZoneMax * sat_degree;
		}
	}

	// store results
	private void storeResultAndUpdate(int ID, double[] out) {

		double waterStorage = out[0];
		if (waterStorage < 0)
			waterStorage = 0;
		double error = out[6];
		double alfa = out[4];
		double quick_mm = out[5];
		double quick = quick_mm * m3s;
		double actualInput = out[1];
		double recharge = out[2];
		double aet = out[3];

		CI = waterStorage;

		outHMActualInput.put(ID, new double[] { actualInput });
		outHMStorage.put(ID, new double[] { waterStorage });
		outHMEvaporation.put(ID, new double[] { aet });
		outHMR.put(ID, new double[] { recharge });
		outHMquick.put(ID, new double[] { quick });
		outHMquick_mm.put(ID, new double[] { quick_mm });
		outHMalpha.put(ID, new double[] { alfa });
		outHMError.put(ID, new double[] { error });

	}

}