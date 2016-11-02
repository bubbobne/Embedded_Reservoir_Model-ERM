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

package sumSeries;

import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import java.util.HashMap;
import java.util.Map;

import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.Finalize;
import oms3.annotations.In;
import oms3.annotations.Initialize;
import oms3.annotations.Out;

import org.jgrasstools.gears.libs.modules.JGTModel;

/**
 * The Class SumSeries compute the sum of two time series
 *
 * @author sidereus <francesco.serafin.3@gmail.com>
 * @date Nov 2nd, 2016
 */
public class SumSeries extends JGTModel {

	@Description("Input first discharge Hashmap")
	@In
	public HashMap<Integer, double[]> inHMDischarge;

	@Description("Input second discharge Hashmap")
	@In
	public HashMap<Integer, double[]> inHMDischarge2;


	@Description("The output HashMap with the sum"
			+ "for the considered layer ")
	@Out
	public static HashMap<Integer, double[]> outHMQtot;

	@In
	public static Integer id;

	private static int timeSeriesCounter = 0;

	@Initialize
	public void init() {
		outHMQtot = new HashMap<>();
        timeSeriesCounter = 0;
	}

	/**
	 * Process.
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception {

		System.out.println(id + ": processing sum series");

		checkNull(inHMDischarge);
		double[] sum = new double[1];

		sumInHMDischarge(sum, inHMDischarge, timeSeriesCounter);
        sumInHMDischarge(sum, inHMDischarge2, 0);

		outHMQtot.put(id, sum);
		timeSeriesCounter += 1;

	}

	@Finalize
	public void finalize() {
		timeSeriesCounter = 0;
		outHMQtot.clear();
	}

	private void sumInHMDischarge(double[] sum, final HashMap<Integer,
			double[]> discharge, int index) {

        if (discharge != null) {
			for (Map.Entry<Integer, double[]> me : discharge.entrySet()) {
				double tmpDischarge = me.getValue()[index];
				if (isNovalue(tmpDischarge)) tmpDischarge = 0.0;
				sum[0] += tmpDischarge;
			}
		}

	}

}

