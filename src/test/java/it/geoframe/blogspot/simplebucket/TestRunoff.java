package it.geoframe.blogspot.simplebucket;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;

import simpleBucket.WaterBudget;
import utils.Constants;
import utils.TestUtility;

/**
 * 
 * @author Giuseppe Formetta, Daniele Andreis
 *
 */

public class TestRunoff {

	@Test
	public void testMassConservation() throws Exception {

		String startDate = "2015-10-01 00:00";
		String endDate = "2018-06-01 00:00";
		int timeStepMinutes = 60;
		String fId = "ID";
		String inPathToRec = "resources/input/rz_quick_mm.csv";
		OmsTimeSeriesIteratorReader DischargeReader = TestUtility.getTimeseriesReader(inPathToRec, fId, startDate,
				endDate, timeStepMinutes);
		OmsTimeSeriesIteratorWriter writerDischargeMM = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerDischarge = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerStorage = new OmsTimeSeriesIteratorWriter();
		Integer ID = 853;
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
			double inRain = id2ValueMap.get(ID)[0];
			waterBudget.process();

			HashMap<Integer, double[]> outHMStorage = waterBudget.outHMStorage;
			HashMap<Integer, double[]> outHMDischarge = waterBudget.outHMDischarge;
			HashMap<Integer, double[]> outHMDischargeMM = waterBudget.outHMDischarge_mm;

			if (t > 1) {

				double sto = outHMStorage.get(ID)[0];
				double tmpBalance = sto - s - inRain + outHMDischargeMM.get(ID)[0];
				assertEquals(tmpBalance, 0, Constants.TOLLERANCE);
				balance = balance + Math.abs(tmpBalance);
			}
			assertEquals(balance, 0, Constants.TOLLERANCE);
			s = outHMStorage.get(ID)[0];
			t = t + 1;
		}

		DischargeReader.close();

	}

}
