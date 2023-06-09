package verifica_bilanci_gaja;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;

import simpleBucket.WaterBudget;

//0.07278611961063386

public class TestRunoff {

	@Test
	public void testLinear() throws Exception {

		String startDate = "2015-10-01 00:00";
		String endDate = "2018-06-01 00:00";
		int timeStepMinutes = 60;
		String fId = "ID";

		String inPathToRec = "/home/andreisd/Downloads/OMS_Project_Brenta_GWS2023_gaja/OMS_Project_Brenta_GWS2023/GEOframe VdA/output_daniele/RZ_quick_mm_853.csv";

		String pathToDischargeMM = "/home/andreisd/Downloads/wetransfer_c_aet_853-csv_2023-02-17_1512/storage.csv";
		String pathToDischarge = "/home/andreisd/Downloads/wetransfer_c_aet_853-csv_2023-02-17_1512/AET.csv";
		String pathToStorage = "/home/andreisd/Downloads/wetransfer_c_aet_853-csv_2023-02-17_1512/Throughfall.csv";

		OmsTimeSeriesIteratorReader DischargeReader = getTimeseriesReader(inPathToRec, fId, startDate, endDate,
				timeStepMinutes);

		OmsTimeSeriesIteratorWriter writerDischargeMM = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerDischarge = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerStorage = new OmsTimeSeriesIteratorWriter();

		writerDischargeMM.file = pathToDischargeMM;
		writerDischargeMM.tStart = startDate;
		writerDischargeMM.tTimestep = timeStepMinutes;
		writerDischargeMM.fileNovalue = "-9999";

		writerDischarge.file = pathToDischarge;
		writerDischarge.tStart = startDate;
		writerDischarge.tTimestep = timeStepMinutes;
		writerDischarge.fileNovalue = "-9999";

		writerStorage.file = pathToStorage;
		writerStorage.tStart = startDate;
		writerStorage.tTimestep = timeStepMinutes;
		writerStorage.fileNovalue = "-9999";

		Integer ID = 853;
		// canopyOUT.WaterBudgetCanopyOUT waterBudget= new
		// canopyOUT.WaterBudgetCanopyOUT();
		// simpleBucket.WaterBudget waterBudget = new simpleBucket.WaterBudget();
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
			System.out.println(DischargeReader.tCurrent);
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
				balance = balance + Math.abs(tmpBalance);
				//	System.out.println(waterBudget.outHMError.get(ID)[0]);

					System.out.println(tmpBalance);

					System.out.println("********************");
				

			}

			s = outHMStorage.get(ID)[0];
			t = t + 1;
		}
		System.out.println(balance);

		DischargeReader.close();

	}

	private OmsTimeSeriesIteratorReader getTimeseriesReader(String inPath, String id, String startDate, String endDate,
			int timeStepMinutes) throws URISyntaxException {
		OmsTimeSeriesIteratorReader reader = new OmsTimeSeriesIteratorReader();
		reader.file = inPath;
		reader.idfield = "ID";
		reader.tStart = startDate;
		reader.tTimestep = 60 * 24;
		reader.tEnd = endDate;
		reader.fileNovalue = "-9999";
		reader.initProcess();
		return reader;
	}
}
