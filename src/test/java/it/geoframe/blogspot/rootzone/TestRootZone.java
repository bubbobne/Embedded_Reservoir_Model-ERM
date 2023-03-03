package it.geoframe.blogspot.rootzone;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;

import canopyOut.WaterBudgetCanopyOUT;
import rootZone.WaterBudgetRootZone;
import utils.Constants;
import utils.TestUtility;

/**
 * 
 * @author Giuseppe Formetta, Daniele Andreis
 *
 */

public class TestRootZone {

	@Test
	public void testMassConservation() throws Exception {
		String startDate = "2015-10-01 00:00";
		String endDate = "2016-09-30 00:00";
		int timeStepMinutes = 1440;
		String fId = "ID";
		String inPathToPrec = "resources/input/throughfall.csv";
		String inPathToET = "resources/input/aet.csv";

		OmsTimeSeriesIteratorReader ETReader = TestUtility.getTimeseriesReader(inPathToET, fId, startDate, endDate,
				timeStepMinutes);
		OmsTimeSeriesIteratorReader rainReader = TestUtility.getTimeseriesReader(inPathToPrec, fId, startDate, endDate,
				timeStepMinutes);

		Integer ID = 853;
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

			double inRain = id2ValueMap.get(ID)[0];
			ETReader.nextRecord();
			id2ValueMap = ETReader.outData;
			double inEt = ETReader.outData.get(ID)[0];

			waterBudget.inHMETp = id2ValueMap;
			id2ValueMap = new HashMap<Integer, double[]>();
			id2ValueMap.put(853, new double[] { Double.NaN });
			waterBudget.tTimestep = timeStepMinutes;
			waterBudget.process();
			HashMap<Integer, double[]> outHMStorage = waterBudget.outHMStorage;
			HashMap<Integer, double[]> outHMET = waterBudget.outHMEvaporation;
			HashMap<Integer, double[]> outHFast = waterBudget.outHMquick_mm;
			HashMap<Integer, double[]> outHMRecharge = waterBudget.outHMR;
			HashMap<Integer, double[]> outHMActualInput = waterBudget.outHMR;


			double sto = outHMStorage.get(ID)[0];
			if (t > 1) {
				double tmpBalance = sto - s -inRain  + outHFast.get(ID)[0]+ outHMRecharge.get(ID)[0] + outHMET.get(ID)[0];
				assertEquals(tmpBalance, 0, Constants.TOLLERANCE);
				balance = balance + Math.abs(tmpBalance);
			}
			assertEquals(balance, 0, Constants.TOLLERANCE);
			s = sto;
			t = t + 1;
		}

		ETReader.close();
		rainReader.close();

	}

}
