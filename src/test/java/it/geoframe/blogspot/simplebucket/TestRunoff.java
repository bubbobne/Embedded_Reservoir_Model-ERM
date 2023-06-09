package it.geoframe.blogspot.simplebucket;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.junit.Test;

import simpleBucket.WaterBudget;
import utils.TestUtility;

/**
 * 
 * Test for runoff module.
 * 
 * @author Giuseppe Formetta, Daniele Andreis
 *
 */

public class TestRunoff extends TestUtility{

	/**
	 * Verify that mass is conserved, for each time step and globally.
	 * 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMassConservation() throws Exception {
		String fId = "ID";
		String inPathToRec = "resources/input/rz_quick_mm.csv";
		OmsTimeSeriesIteratorReader DischargeReader = getTimeseriesReader(inPathToRec, fId, START_DATE,
				END_DATE, MINUTES_TIME_STEP);
		WaterBudget waterBudget = new WaterBudget();

		int t = 1;
		double s = 0;
		double balance = 0;
		while (DischargeReader.doProcess) {
			waterBudget.s_RunoffMax = 34.90;
			waterBudget.c = 1.580;
			waterBudget.d = 1.00013;
			waterBudget.A = 11.39;
			waterBudget.tTimestep = 1440;
			waterBudget.RKiter = 300;

			DischargeReader.nextRecord();
			HashMap<Integer, double[]> id2ValueMap = DischargeReader.outData;
			waterBudget.inHMRechargeValues = id2ValueMap;
			double inRain = id2ValueMap.get(BASIN_ID)[0];
			waterBudget.process();

			HashMap<Integer, double[]> outHMStorage = waterBudget.outHMStorage;
			HashMap<Integer, double[]> outHMDischargeMM = waterBudget.outHMDischarge_mm;

			if (t > 1) {

				double sto = outHMStorage.get(BASIN_ID)[0];
				double tmpBalance = sto - s - inRain + outHMDischargeMM.get(BASIN_ID)[0];
				assertEquals(tmpBalance, 0, TOLLERANCE);
				balance = balance + Math.abs(tmpBalance);
			}
			assertEquals(balance, 0, TOLLERANCE);
			s = outHMStorage.get(BASIN_ID)[0];
			t = t + 1;
		}

		DischargeReader.close();

	}

}
