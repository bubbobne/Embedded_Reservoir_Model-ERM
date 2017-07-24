package waterBudgetTest;


import java.net.URISyntaxException;
import java.util.HashMap;

import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;

import canopyOUT.WaterBudgetCanopyOUT;

public class TestCanopyOUT{

	@Test
	public void testLinear() throws Exception {

		String startDate = "1998-10-03 00:00";
		String endDate = "1998-10-05 00:00";
		int timeStepMinutes = 60*24;
		String fId = "ID";
		



		String inPathToPrec = "resources/Input/Melting_1.csv";
		String inPathToET ="resources/Input/etp_1_daily.csv";
		String inPathToLAI= "resources/Input/LAI_1_daily.csv";		
		
		String pathToS= "resources/Output/canopy/S_Canopy.csv";
		String pathToET= "resources/Output/canopy/ET_Canopy.csv";
		String pathToThroughfall= "resources/Output/canopy/Q_Canopy.csv";

		
		OmsTimeSeriesIteratorReader ETReader = getTimeseriesReader(inPathToET, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader RainReader = getTimeseriesReader(inPathToPrec, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader LAIReader = getTimeseriesReader(inPathToLAI, fId, startDate, endDate, timeStepMinutes);

		
		OmsTimeSeriesIteratorWriter writerS = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerAET = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerThroughfall = new OmsTimeSeriesIteratorWriter();


		writerS.file = pathToS;
		writerS.tStart = startDate;
		writerS.tTimestep = timeStepMinutes;
		writerS.fileNovalue="-9999";

		
		writerAET.file = pathToET;
		writerAET.tStart = startDate;
		writerAET.tTimestep = timeStepMinutes;
		writerAET.fileNovalue="-9999";
		
		
		writerThroughfall.file = pathToThroughfall;
		writerThroughfall.tStart = startDate;
		writerThroughfall.tTimestep = timeStepMinutes;
		writerThroughfall.fileNovalue="-9999";
	
		
		WaterBudgetCanopyOUT waterBudget= new WaterBudgetCanopyOUT();


		while( RainReader.doProcess ) {
			

		    waterBudget.p=0.65;	
			waterBudget.solver_model="dp853";
			waterBudget.kc_canopy_out= 0.25;
			waterBudget.IntialConditionStorage=0.001;


			
			RainReader.nextRecord();
			
			HashMap<Integer, double[]> id2ValueMap = RainReader.outData;
			waterBudget.inHMRain= id2ValueMap;
			
            
            ETReader.nextRecord();
            id2ValueMap = ETReader.outData;
            waterBudget.inHMETp = id2ValueMap;
            

            LAIReader.nextRecord();
            id2ValueMap = LAIReader.outData;
            waterBudget.inHMLAI = id2ValueMap;

            waterBudget.process();
            
            HashMap<Integer, double[]> outHMStorage = waterBudget.outHMStorage;
            HashMap<Integer, double[]> outHMET = waterBudget.outHMAET;
            HashMap<Integer, double[]> outHThroughfall = waterBudget.outHMThroughfall;
            
            
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
			

		}

        LAIReader.close();
        ETReader.close();
        RainReader.close();

	}


	private OmsTimeSeriesIteratorReader getTimeseriesReader( String inPath, String id, String startDate, String endDate,
			int timeStepMinutes ) throws URISyntaxException {
		OmsTimeSeriesIteratorReader reader = new OmsTimeSeriesIteratorReader();
		reader.file = inPath;
		reader.idfield = "ID";
		reader.tStart = startDate;
		reader.tTimestep = 60*24;
		reader.tEnd = endDate;
		reader.fileNovalue = "-9999";
		reader.initProcess();
		return reader;
	}
}