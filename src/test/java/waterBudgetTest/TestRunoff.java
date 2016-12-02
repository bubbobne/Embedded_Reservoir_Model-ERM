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

		String startDate = "1996-09-17 20:00";
		String endDate = "1996-09-18 20:00";
		int timeStepMinutes = 60;
		String fId = "ID";

		String inPathToPrec = "resources/Input/rainfall.csv";
		String pathToQ= "resources/Output/runoff/Q_runoff_2.csv";


		
		OmsTimeSeriesIteratorReader JReader = getTimeseriesReader(inPathToPrec, fId, startDate, endDate, timeStepMinutes);

		OmsTimeSeriesIteratorWriter writerQ = new OmsTimeSeriesIteratorWriter();

		
		writerQ.file = pathToQ;
		writerQ.tStart = startDate;
		writerQ.tTimestep = timeStepMinutes;
		writerQ.fileNovalue="-9999";
		
		

		
		WaterBudgetRunoff waterBudgetRunoff= new WaterBudgetRunoff();
		
		OmsRasterReader Wsup = new OmsRasterReader();
		Wsup.file = "resources/Input/rescaled_4.asc";
		Wsup.fileNovalue = -9999.0;
		Wsup.geodataNovalue = Double.NaN;
		Wsup.process();
		GridCoverage2D width_sup = Wsup.outRaster;
		
		
		OmsRasterReader topindex = new OmsRasterReader();
		topindex.file = "resources/Input/top_4.asc";
		topindex.fileNovalue = -9999.0;
		topindex.geodataNovalue = Double.NaN;
		topindex.process();
		GridCoverage2D topIndex = topindex.outRaster;


		while( JReader.doProcess ) {
		
			waterBudgetRunoff.solver_model="dp853";
			waterBudgetRunoff.ET_model="AET";
			waterBudgetRunoff.inRescaledDistance=width_sup;
			waterBudgetRunoff.pCelerity=2;
			waterBudgetRunoff.inTopindex=topIndex;
			waterBudgetRunoff.pSat=40;
			waterBudgetRunoff.inTimestep=timeStepMinutes;
			waterBudgetRunoff.tStartDate=startDate;
			waterBudgetRunoff.tEndDate=endDate;
			waterBudgetRunoff.ID=209;
			waterBudgetRunoff.alpha=1;
			waterBudgetRunoff.s_RunoffMax=4.60;
			
			JReader.nextRecord();
			
			HashMap<Integer, double[]> id2ValueMap = JReader.outData;
			waterBudgetRunoff.inRainValues = id2ValueMap;

			waterBudgetRunoff.process();
            
            HashMap<Integer, double[]> outHMDischarge = waterBudgetRunoff.outHMDischarge;

			
			writerQ.inData = outHMDischarge;
			writerQ.writeNextLine();
			
			if (pathToQ != null) {
				writerQ.close();
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