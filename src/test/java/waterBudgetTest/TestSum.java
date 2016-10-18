package waterBudgetTest;


import java.net.URISyntaxException;
import java.util.HashMap;

import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;

import sumSeries.SumSeries;

public class TestSum {

	@Test
	public void testLinear() throws Exception {

		String startDate = "1994-01-01 00:00";
		String endDate = "1994-01-01 04:00";
		int timeStepMinutes = 60;
		String fId = "ID";


		String inPathToQ = "resources/Output/waterBudget/Q.csv";
		String pathToSum= "resources/Output/waterBudget/Sum.csv";

		
		OmsTimeSeriesIteratorReader Q1 = getTimeseriesReader(inPathToQ, fId, startDate, endDate, timeStepMinutes);

		OmsTimeSeriesIteratorWriter writerSum = new OmsTimeSeriesIteratorWriter();


		writerSum.file = pathToSum;
		writerSum.tStart = startDate;
		writerSum.tTimestep = timeStepMinutes;
		writerSum.fileNovalue="-9999";
		
		SumSeries sum= new SumSeries();


		while( Q1.doProcess ) {
		

			
			Q1.nextRecord();
			
			HashMap<Integer, double[]> id2ValueMap = Q1.outData;
			sum.inHMDischarge = id2ValueMap;
			sum.inHMDischarge2 = id2ValueMap;
			
			


            sum.process();
            
            HashMap<Integer, double[]> outHMSum = sum.outHMQtot;

            
			writerSum.inData = outHMSum ;
			writerSum.writeNextLine();
			
			if (pathToSum != null) {
				writerSum.close();
			}
			
		}
		Q1.close();


	}


	private OmsTimeSeriesIteratorReader getTimeseriesReader( String inPath, String id, String startDate, String endDate,
			int timeStepMinutes ) throws URISyntaxException {
		OmsTimeSeriesIteratorReader reader = new OmsTimeSeriesIteratorReader();
		reader.file = inPath;
		reader.idfield = "ID";
		reader.tStart = startDate;
		reader.tTimestep = 60;
		reader.tEnd = endDate;
		reader.fileNovalue = "-9999";
		reader.initProcess();
		return reader;
	}
}