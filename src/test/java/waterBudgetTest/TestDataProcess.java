package waterBudgetTest;


import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.coverage.grid.GridCoverage2D;
import org.jgrasstools.gears.io.rasterreader.OmsRasterReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;


import runoff.WaterBudgetRunoff;
import saturationDegree.DataProcess;

public class TestDataProcess{

	@Test
	public void testLinear() throws Exception {

		String startDate = "2013-12-15 00:00" ;
		String endDate = "2013-12-15 02:00";
		int timeStepMinutes = 60;
		String fId = "ID";
		
		String inPathToStorage ="/Users/marialaura/Dropbox/dati_NewAge/EsercitazioniIdrologia2017/output/Basento/Storage/Srz_2.csv";
		String inPathToStorage1 ="/Users/marialaura/Dropbox/dati_NewAge/EsercitazioniIdrologia2017/output/Basento/Storage/Srz_3.csv";
		String inPathToStorage2 ="/Users/marialaura/Dropbox/dati_NewAge/EsercitazioniIdrologia2017/output/Basento/Storage/Srz_4.csv";
		String outPathToStorageTotal = "/Users/marialaura/Dropbox/dati_NewAge/EsercitazioniIdrologia2017/output/Basento/Storage/Storage_total.csv";



		
		OmsTimeSeriesIteratorReader storageReader = getTimeseriesReader(inPathToStorage, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader storageReader1 = getTimeseriesReader(inPathToStorage1, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader storageReader2 = getTimeseriesReader(inPathToStorage2, fId, startDate, endDate, timeStepMinutes);


		OmsTimeSeriesIteratorWriter writerStotal = new OmsTimeSeriesIteratorWriter();


		
		writerStotal.file = outPathToStorageTotal;
		writerStotal.tStart = startDate;
		writerStotal.tTimestep = timeStepMinutes;
		writerStotal.fileNovalue="-9999";
		

		DataProcess test =new DataProcess();
		



		while(storageReader.doProcess ) {


			
			storageReader.nextRecord();
			
			HashMap<Integer, double[]> id2ValueMap = storageReader.outData;
			test.inHMStorage = id2ValueMap;
			
			storageReader1.nextRecord();
            id2ValueMap = storageReader1.outData;
            test.inHMStorageFromAboveVert1 = id2ValueMap;
            test.ID1=3;
            
			storageReader2.nextRecord();
           id2ValueMap = storageReader2.outData;
           test.inHMStorageFromAboveVert2 = id2ValueMap;
          test.ID2=4;

			test.process();
            
            HashMap<Integer, double[]> outHMDischarge = test.inHMStorage;

			
            writerStotal.inData = outHMDischarge;
            writerStotal.writeNextLine();
			
			if (outPathToStorageTotal != null) {
				writerStotal.close();
			}
			

          
		}
		storageReader.close();


	}


	private OmsTimeSeriesIteratorReader getTimeseriesReader( String inPath, String id, String startDate, String endDate,
			int timeStepMinutes ) throws URISyntaxException {
		OmsTimeSeriesIteratorReader reader = new OmsTimeSeriesIteratorReader();
		reader.file = inPath;
		reader.idfield = "ID";
		reader.tStart = startDate;
		reader.tTimestep = timeStepMinutes;
		reader.tEnd = endDate;
		reader.fileNovalue = "-9999";
		reader.initProcess();
		return reader;
	}
}