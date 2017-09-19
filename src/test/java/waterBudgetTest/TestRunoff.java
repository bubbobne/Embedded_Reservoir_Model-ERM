package waterBudgetTest;


import java.net.URISyntaxException;
import java.util.HashMap;

import org.geotools.coverage.grid.GridCoverage2D;
import org.jgrasstools.gears.io.rasterreader.OmsRasterReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;


import runoff.WaterBudgetRunoff;

public class TestRunoff{

	@Test
	public void testLinear() throws Exception {

		String startDate = "1997-01-01 00:00";
		String endDate = "1997-01-01 15:00";
		int timeStepMinutes = 60;
		String fId = "ID";

		String inPathToPrec = "resources/Input/InputRO_1.csv";
		String pathToQ= "resources/Output/runoff/Q_runoff.csv";
		String pathToQmm= "resources/Output/runoff/Q_runoff_mm.csv";
		


		
		OmsTimeSeriesIteratorReader JReader = getTimeseriesReader(inPathToPrec, fId, startDate, endDate, timeStepMinutes);

		OmsTimeSeriesIteratorWriter writerQ = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerQint = new OmsTimeSeriesIteratorWriter();

		
		writerQ.file = pathToQ;
		writerQ.tStart = startDate;
		writerQ.tTimestep = timeStepMinutes;
		writerQ.fileNovalue="-9999";
		
		
		writerQint.file = pathToQmm;
		writerQint.tStart = startDate;
		writerQint.tTimestep = timeStepMinutes;
		writerQint.fileNovalue="-9999";
		

		
		WaterBudgetRunoff waterBudgetRunoff= new WaterBudgetRunoff();
		
		OmsRasterReader Wsup = new OmsRasterReader();
		Wsup.file = "resources/Input/rescaled_1.asc";
		Wsup.fileNovalue = -9999.0;
		Wsup.geodataNovalue = Double.NaN;
		Wsup.process();
		GridCoverage2D width_sup = Wsup.outRaster;
		
		
		OmsRasterReader topindex = new OmsRasterReader();
		topindex.file = "resources/Input/top_1.asc";
		topindex.fileNovalue = -9999.0;
		topindex.geodataNovalue = Double.NaN;
		topindex.process();
		GridCoverage2D topIndex = topindex.outRaster;


		while( JReader.doProcess ) {

			waterBudgetRunoff.inRescaledDistance=width_sup;
			waterBudgetRunoff.pCelerity=0.4;
			waterBudgetRunoff.inTopindex=topIndex;
			waterBudgetRunoff.pSat=20;
			waterBudgetRunoff.inTimestep=timeStepMinutes;
			waterBudgetRunoff.ID=1;
			waterBudgetRunoff.alpha=1;
			
			JReader.nextRecord();
			
			HashMap<Integer, double[]> id2ValueMap = JReader.outData;
			waterBudgetRunoff.inRainValues = id2ValueMap;

			waterBudgetRunoff.process();
            
            HashMap<Integer, double[]> outHMDischarge = waterBudgetRunoff.outHMDischarge;
            HashMap<Integer, double[]> outHMDischarge_mm = waterBudgetRunoff.outHMDischarge_mm;

			
			writerQ.inData = outHMDischarge;
			writerQ.writeNextLine();
			
			if (pathToQ != null) {
				writerQ.close();
			}
			
			writerQint.inData = outHMDischarge_mm;
			writerQint.writeNextLine();
			
			if (pathToQmm != null) {
				writerQint.close();
			}
          
		}
		JReader.close();


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