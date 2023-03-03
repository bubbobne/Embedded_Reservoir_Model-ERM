package it.geoframe.blogspot.canopy;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;

import canopyOut.WaterBudgetCanopyOUT;
import utils.Constants;
import utils.TestUtility;

/**
 * 
 * @author Giuseppe Formetta, Daniele Andreis
 *
 */

public class TestCanopy {

	@Test
	public void testMassConservation() throws Exception {
		String startDate = "2015-10-01 00:00";
		String endDate = "2016-09-30 00:00";
		int timeStepMinutes = 1440;
		String fId = "ID";
		String inPathToPrec = "resources/input/md.csv";
		String inPathToET = "resources/input/et_pt.csv";

		OmsTimeSeriesIteratorReader ETReader = TestUtility.getTimeseriesReader(inPathToET, fId, startDate, endDate,
				timeStepMinutes);
		OmsTimeSeriesIteratorReader RainReader = TestUtility.getTimeseriesReader(inPathToPrec, fId, startDate, endDate,
				timeStepMinutes);

		Integer ID = 853;
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
			double inRain = id2ValueMap.get(ID)[0];
			ETReader.nextRecord();
			id2ValueMap = ETReader.outData;
			double inEt = ETReader.outData.get(ID)[0];
			waterBudget.inHMETp = id2ValueMap;
			id2ValueMap = new HashMap<Integer, double[]>();
			id2ValueMap.put(853, new double[] { Double.NaN });
			waterBudget.inHMLAI = id2ValueMap;
			waterBudget.tTimestep = timeStepMinutes;
			waterBudget.process();
			HashMap<Integer, double[]> outHMStorage = waterBudget.outHMStorage;
			HashMap<Integer, double[]> outHMET = waterBudget.outHMAET;
			HashMap<Integer, double[]> outHThroughfall = waterBudget.outHMThroughfall;
			double sto = outHMStorage.get(ID)[0];
			if (t > 1) {
				double tmpBalance = sto - s - inRain + outHThroughfall.get(ID)[0] + outHMET.get(ID)[0];
				assertEquals(tmpBalance, 0, Constants.TOLLERANCE);
				balance = balance + Math.abs(tmpBalance);
			}
			assertEquals(balance, 0, Constants.TOLLERANCE);
			s = sto;
			t = t + 1;
		}

		ETReader.close();
		RainReader.close();

	}

}
