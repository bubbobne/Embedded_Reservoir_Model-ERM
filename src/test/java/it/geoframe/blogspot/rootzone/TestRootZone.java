package it.geoframe.blogspot.rootzone;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.junit.Test;

import rootZone.WaterBudgetRootZone;
import utils.TestUtility;

/**
 * Test for root zone module
 * 
 * @author Giuseppe Formetta, Daniele Andreis
 *
 */

public class TestRootZone extends TestUtility {
	/**
	 * Verify that mass is conserved, for each time step and globally.
	 * 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMassConservation() throws Exception {

		String inPathToPrec = "resources/input/throughfall.csv";
		String inPathToET = "resources/input/aet.csv";

		OmsTimeSeriesIteratorReader ETReader = getTimeseriesReader(inPathToET, FID, START_DATE, END_DATE,
				MINUTES_TIME_STEP);
		OmsTimeSeriesIteratorReader rainReader = getTimeseriesReader(inPathToPrec, FID, START_DATE, END_DATE,
				MINUTES_TIME_STEP);
		WaterBudgetRootZone waterBudget = new WaterBudgetRootZone();

		int t = 1;
		double s = 0;
		double balance = 0;
		while (rainReader.doProcess) {

			rainReader.nextRecord();
			HashMap<Integer, double[]> id2ValueMap = rainReader.outData;
			waterBudget.inHMRain = id2ValueMap;
			waterBudget.s_RootZoneMax = 36.03329620172442;
			waterBudget.g = 3.5693446772151898;
			waterBudget.h = 3.5034142032095836;
			waterBudget.pB_soil = 6.324193266645169;

			double inRain = id2ValueMap.get(BASIN_ID)[0];
			ETReader.nextRecord();
			id2ValueMap = ETReader.outData;

			waterBudget.inHMETp = id2ValueMap;
			id2ValueMap = new HashMap<Integer, double[]>();
			id2ValueMap.put(BASIN_ID, new double[] { Double.NaN });
			waterBudget.tTimestep = MINUTES_TIME_STEP;
			waterBudget.process();
			HashMap<Integer, double[]> outHMStorage = waterBudget.outHMStorage;
			HashMap<Integer, double[]> outHMET = waterBudget.outHMEvaporation;
			HashMap<Integer, double[]> outHFast = waterBudget.outHMquick_mm;
			HashMap<Integer, double[]> outHMRecharge = waterBudget.outHMR;

			double sto = outHMStorage.get(BASIN_ID)[0];
			if (t > 1) {
				double tmpBalance = sto - s - inRain + outHFast.get(BASIN_ID)[0] + outHMRecharge.get(BASIN_ID)[0]
						+ outHMET.get(BASIN_ID)[0];
				assertEquals(tmpBalance, 0, TOLLERANCE);
				balance = balance + Math.abs(tmpBalance);
			}
			assertEquals(balance, 0, TOLLERANCE);
			s = sto;
			t = t + 1;
		}

		ETReader.close();
		rainReader.close();

	}

}
