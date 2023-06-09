package verifica_bilanci_gaja;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;

import canopyOut.WaterBudgetCanopyOUT;

//0.07278611961063386

public class TestCanopy {

	@Test
	public void testLinear() throws Exception {

		String startDate = "2015-10-01 00:00";
		String endDate = "2016-09-30 00:00";
		int timeStepMinutes = 1440;
		String fId = "ID";

		String inPathToPrec = "/home/andreisd/Downloads/wetransfer_c_aet_853-csv_2023-02-17_1512/Md_853.csv";
		String inPathToET = "/home/andreisd/Downloads/853/ET_PT_853.csv";

		String pathToS = "/home/andreisd/Downloads/wetransfer_c_aet_853-csv_2023-02-17_1512/storage.csv";
		String pathToET = "/home/andreisd/Downloads/wetransfer_c_aet_853-csv_2023-02-17_1512/AET.csv";
		String pathToThroughfall = "/home/andreisd/Downloads/wetransfer_c_aet_853-csv_2023-02-17_1512/Throughfall.csv";

		OmsTimeSeriesIteratorReader ETReader = getTimeseriesReader(inPathToET, fId, startDate, endDate,
				timeStepMinutes);
		OmsTimeSeriesIteratorReader RainReader = getTimeseriesReader(inPathToPrec, fId, startDate, endDate,
				timeStepMinutes);

		OmsTimeSeriesIteratorWriter writerS = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerAET = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerThroughfall = new OmsTimeSeriesIteratorWriter();

		writerS.file = pathToS;
		writerS.tStart = startDate;
		writerS.tTimestep = timeStepMinutes;
		writerS.fileNovalue = "-9999";

		writerAET.file = pathToET;
		writerAET.tStart = startDate;
		writerAET.tTimestep = timeStepMinutes;
		writerAET.fileNovalue = "-9999";

		writerThroughfall.file = pathToThroughfall;
		writerThroughfall.tStart = startDate;
		writerThroughfall.tTimestep = timeStepMinutes;
		writerThroughfall.fileNovalue = "-9999";

		Integer ID = 853;
		// canopyOUT.WaterBudgetCanopyOUT waterBudget= new
		// canopyOUT.WaterBudgetCanopyOUT();
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
			double inEt =  ETReader.outData.get(ID)[0];
			waterBudget.inHMETp = id2ValueMap;

			id2ValueMap = new HashMap<Integer, double[]>();
			id2ValueMap.put(853, new double[] { Double.NaN });
			waterBudget.inHMLAI = id2ValueMap;
			waterBudget.tTimestep = timeStepMinutes;
			System.out.println(RainReader.tCurrent);

			waterBudget.process();

			HashMap<Integer, double[]> outHMStorage = waterBudget.outHMStorage;
			HashMap<Integer, double[]> outHMET = waterBudget.outHMAET;
			HashMap<Integer, double[]> outHThroughfall = waterBudget.outHMThroughfall;

			double sto = outHMStorage.get(ID)[0];
			if (t > 1) {

				double tmpBalance = sto - s - inRain + outHThroughfall.get(ID)[0] + outHMET.get(ID)[0];
				balance = balance + Math.abs(tmpBalance);
		//		if (Math.abs(tmpBalance) > 0.001) {

//				/	System.out.println(waterBudget.outHMError.get(ID)[0]);
//
//				System.out.println(sto);

				System.out.println(outHMET.get(ID)[0]);
				double t2 = 0.2754773505506556*0.6/2;
//				System.out.println(inEt * (sto/t2));

				
//					System.out.println(s);
//					System.out.println(inRain);
//					System.out.println(outHThroughfall.get(ID)[0]);
//					System.out.println(outHMET.get(ID)[0]);
//
//					System.out.println(balance);
				//	System.out.println(tmpBalance);

					System.out.println("********************");
	//			}
			}

			s = sto;

//            
            
			writerS.inData = outHMStorage ;
			writerS.writeNextLine();
			
			if (pathToS != null) {
				writerS.close();
			}
			

			
			writerAET.inData = outHMET;
			writerAET.writeNextLine();
			
			if (pathToET != null) {
				writerAET.close();
			}
			
			
			writerThroughfall.inData =outHThroughfall ;
			writerThroughfall.writeNextLine();
			
			if (pathToThroughfall != null) {
				writerThroughfall.close();
			}
			


			t = t + 1;
		}
	//	System.out.println(balance);

		ETReader.close();
		RainReader.close();

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
