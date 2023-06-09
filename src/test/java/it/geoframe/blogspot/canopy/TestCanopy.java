package it.geoframe.blogspot.canopy;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.junit.Test;

import canopyOut.WaterBudgetCanopyOUT;
import utils.TestUtility;

/**
 * 
 * Test for Canopy module.
 * 
 * 
 * 
 * @author Giuseppe Formetta, Daniele Andreis
 *
 */

public class TestCanopy extends TestUtility {

	/**
	 * Verify that mass is conserved, for each time step and globally.
	 * 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMassConservation() throws Exception {

		String inPathToPrec = "resources/input/md.csv";
		String inPathToET = "resources/input/et_pt.csv";

		OmsTimeSeriesIteratorReader ETReader = getTimeseriesReader(inPathToET, FID, START_DATE, END_DATE,
				MINUTES_TIME_STEP);
		OmsTimeSeriesIteratorReader RainReader = getTimeseriesReader(inPathToPrec, FID, START_DATE, END_DATE,
				MINUTES_TIME_STEP);

		WaterBudgetCanopyOUT waterBudget = new WaterBudgetCanopyOUT();

		int t = 1;
		double s = 0;
		double balance = 0;
		while (RainReader.doProcess) {
			waterBudget.RKiter = 100;
			waterBudget.p = 0.8880331046728323;
			waterBudget.kc = 0.2754773505506556;
			RainReader.nextRecord();
			HashMap<Integer, double[]> id2ValueMap = RainReader.outData;
			waterBudget.inHMRain = id2ValueMap;
			double inRain = id2ValueMap.get(BASIN_ID)[0];
			ETReader.nextRecord();
			id2ValueMap = ETReader.outData;
			double inEt = ETReader.outData.get(BASIN_ID)[0];
			waterBudget.inHMETp = id2ValueMap;
			id2ValueMap = new HashMap<Integer, double[]>();
			id2ValueMap.put(853, new double[] { Double.NaN });
			waterBudget.inHMLAI = id2ValueMap;
			waterBudget.tTimestep = MINUTES_TIME_STEP;
			waterBudget.process();
			HashMap<Integer, double[]> outHMStorage = waterBudget.outHMStorage;
			HashMap<Integer, double[]> outHMET = waterBudget.outHMAET;
			HashMap<Integer, double[]> outHThroughfall = waterBudget.outHMThroughfall;
			double sto = outHMStorage.get(BASIN_ID)[0];
			if (t > 1) {
				double tmpBalance = sto - s - inRain + outHThroughfall.get(BASIN_ID)[0] + outHMET.get(BASIN_ID)[0];
				assertEquals(tmpBalance, 0, TOLLERANCE);
				balance = balance + Math.abs(tmpBalance);
			}
			assertEquals(balance, 0, TOLLERANCE);
			s = sto;
			t = t + 1;
		}

		ETReader.close();
		RainReader.close();

	}

}
