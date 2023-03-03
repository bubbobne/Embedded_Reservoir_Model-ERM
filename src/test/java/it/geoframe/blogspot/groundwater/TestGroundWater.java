package it.geoframe.blogspot.groundwater;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;

import canopyOut.WaterBudgetCanopyOUT;
import groundWater.WaterBudgetGround;
import utils.Constants;
import utils.TestUtility;

/**
 * 
 * @author Giuseppe Formetta, Daniele Andreis
 *
 */

public class TestGroundWater {

	@Test
	public void testMassConservation() throws Exception {
		String startDate = "2015-10-01 00:00";
		String endDate = "2016-09-30 00:00";
		int timeStepMinutes = 1440;
		String fId = "ID";
		String inPathToRecharge = "resources/input/rz_outflow.csv";

		OmsTimeSeriesIteratorReader rechargeReader = TestUtility.getTimeseriesReader(inPathToRecharge, fId, startDate,
				endDate, timeStepMinutes);

		Integer ID = 853;
		WaterBudgetGround waterBudget = new WaterBudgetGround();

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
			double inRecharge = id2ValueMap.get(ID)[0];

			waterBudget.tTimestep = timeStepMinutes;
			waterBudget.process();
			HashMap<Integer, double[]> outHMStorage = waterBudget.outHMStorage;
			HashMap<Integer, double[]> outHMDischarge = waterBudget.outHMDischarge_mm;
			double sto = outHMStorage.get(ID)[0];
			if (t > 1) {
				double tmpBalance = sto - s - inRecharge + outHMDischarge.get(ID)[0];
				assertEquals(0, tmpBalance, Constants.TOLLERANCE);
				balance = balance + Math.abs(tmpBalance);
			}
			assertEquals(balance, 0, Constants.TOLLERANCE);
			s = sto;
			t = t + 1;
		}

		rechargeReader.close();

	}

}
