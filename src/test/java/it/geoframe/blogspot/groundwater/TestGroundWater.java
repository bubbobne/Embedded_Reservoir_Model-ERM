package it.geoframe.blogspot.groundwater;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.junit.Test;

import groundWater.WaterBudgetGroundWater;
import utils.TestUtility;

/**
 * Test for ground water module.
 * 
 * @author Giuseppe Formetta, Daniele Andreis
 *
 */

public class TestGroundWater extends TestUtility {
	/**
	 * Verify that mass is conserved, for each time step and globally.
	 * Constants
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMassConservation() throws Exception {

		String inPathToRecharge = "resources/input/rz_outflow.csv";

		OmsTimeSeriesIteratorReader rechargeReader = getTimeseriesReader(inPathToRecharge, FID, START_DATE, END_DATE,
				MINUTES_TIME_STEP);

		WaterBudgetGroundWater waterBudget = new WaterBudgetGroundWater();

		int t = 1;
		double s = 0;
		double balance = 0;
		while (rechargeReader.doProcess) {
			waterBudget.f = 1.6898317789112696;

			waterBudget.s_GroundWaterMax = 109.10940439050844;
			waterBudget.e = 1.8329108592995276;

			
			rechargeReader.nextRecord();
			HashMap<Integer, double[]> id2ValueMap = rechargeReader.outData;
			waterBudget.inHMRechargeValues = id2ValueMap;
			double inRecharge = id2ValueMap.get(BASIN_ID)[0];

			waterBudget.tTimestep = MINUTES_TIME_STEP;
			waterBudget.process();
			HashMap<Integer, double[]> outHMStorage = waterBudget.outHMStorage;
			HashMap<Integer, double[]> outHMDischarge = waterBudget.outHMDischarge_mm;
			double sto = outHMStorage.get(BASIN_ID)[0];
			if (t > 1) {
				double tmpBalance = sto - s - inRecharge + outHMDischarge.get(BASIN_ID)[0];
				assertEquals(0, tmpBalance, TOLLERANCE);
				balance = balance + Math.abs(tmpBalance);
			}
			assertEquals(balance, 0, TOLLERANCE);
			s = sto;
			t = t + 1;
		}

		rechargeReader.close();

	}

}
