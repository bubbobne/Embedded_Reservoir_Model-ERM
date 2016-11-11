package waterBudgetTest;


import java.net.URISyntaxException;
import java.util.HashMap;

import org.geotools.coverage.grid.GridCoverage2D;
import org.jgrasstools.gears.io.rasterreader.OmsRasterReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;

import rootZone.WaterBudgetRootZone;
import runoff.WaterBudgetRunoff;
import canopy.WaterBudgetCanopy;
import groundWater.WaterBudgetGroundWater;

public class TestReservoirsConnection{

	@Test
	public void testLinear() throws Exception {

		String startDate = "1994-01-01 00:00";
		String endDate = "1994-01-02 00:00";
		int timeStepMinutes = 60;
		String fId = "ID";

		
		String inPathToPrec = "resources/Input/rainfall.csv";
		String inPathToET ="resources/Input/ET.csv";
		
		
		String pathToSc= "resources/Output/reservoirsConnection/Sc.csv";
		//String pathToUpTake= "resources/Output/rootZone/UpTake.csv";
		String pathToETc= "resources/Output/reservoirsConnection/ETc.csv";
		String inPathToLAI= "resources/Input/LAI.csv";
		
		String pathToSrz= "resources/Output/reservoirsConnection/Srz.csv";
		//String pathTroughfall= "resources/Output/canopy/Throughfall.csv";
		String pathToETrz= "resources/Output/reservoirsConnection/ETrz.csv";
		
		
		String pathToSg= "resources/Output/reservoirsConnection/Sg.csv";
		String pathToQg= "resources/Output/reservoirsConnection/Qg.csv";
		
		String pathToSrunoff= "resources/Output/reservoirsConnection/Srunoff.csv";
		String pathToETrunoff= "resources/Output/reservoirsConnection/ETrunoff.csv";
		String pathToQrunoff= "resources/Output/reservoirsConnection/Qrunoff.csv";

		
		OmsTimeSeriesIteratorReader JReader = getTimeseriesReader(inPathToPrec, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader ETReader = getTimeseriesReader(inPathToET, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader LAIReader = getTimeseriesReader(inPathToLAI, fId, startDate, endDate, timeStepMinutes);

		
		OmsTimeSeriesIteratorWriter writerSc = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerSrz = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerSg = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerSrunoff = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerETc = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerETrz = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerETrunoff = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerQg = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerQrunoff = new OmsTimeSeriesIteratorWriter();


		writerSc.file = pathToSc;
		writerSc.tStart = startDate;
		writerSc.tTimestep = timeStepMinutes;
		writerSc.fileNovalue="-9999";
		
		writerSrz.file = pathToSrz;
		writerSrz.tStart = startDate;
		writerSrz.tTimestep = timeStepMinutes;
		writerSrz.fileNovalue="-9999";
		
		writerSg.file = pathToSg;
		writerSg.tStart = startDate;
		writerSg.tTimestep = timeStepMinutes;
		writerSg.fileNovalue="-9999";
		
		writerSrunoff.file = pathToSrunoff;
		writerSrunoff.tStart = startDate;
		writerSrunoff.tTimestep = timeStepMinutes;
		writerSrunoff.fileNovalue="-9999";
		
		
		writerETc.file = pathToETc;
		writerETc.tStart = startDate;
		writerETc.tTimestep = timeStepMinutes;
		writerETc.fileNovalue="-9999";
	
		
		writerETrz.file = pathToETrz;
		writerETrz.tStart = startDate;
		writerETrz.tTimestep = timeStepMinutes;
		writerETrz.fileNovalue="-9999";
		
		
		writerETrunoff.file = pathToETrunoff;
		writerETrunoff.tStart = startDate;
		writerETrunoff.tTimestep = timeStepMinutes;
		writerETrunoff.fileNovalue="-9999";
		
		
		writerQg.file = pathToQg;
		writerQg.tStart = startDate;
		writerQg.tTimestep = timeStepMinutes;
		writerQg.fileNovalue="-9999";
		
		writerQrunoff.file = pathToQrunoff;
		writerQrunoff.tStart = startDate;
		writerQrunoff.tTimestep = timeStepMinutes;
		writerQrunoff.fileNovalue="-9999";
		
		
		OmsRasterReader Wsup = new OmsRasterReader();
		Wsup.file = "resources/Input/width_10.asc";
		Wsup.fileNovalue = -9999.0;
		Wsup.geodataNovalue = Double.NaN;
		Wsup.process();
		GridCoverage2D width_sup = Wsup.outRaster;
		
		
		OmsRasterReader topindex = new OmsRasterReader();
		topindex.file = "resources/Input/topIndex.asc";
		topindex.fileNovalue = -9999.0;
		topindex.geodataNovalue = Double.NaN;
		topindex.process();
		GridCoverage2D topIndex = topindex.outRaster;
		

		
		WaterBudgetRootZone waterBudgetRZ= new WaterBudgetRootZone();
		
		WaterBudgetCanopy waterBudgetCanopy= new WaterBudgetCanopy();
		
		WaterBudgetGroundWater waterBudgetGW= new WaterBudgetGroundWater();
		
		WaterBudgetRunoff waterBudgetRunoff= new WaterBudgetRunoff();
		
		


		while( JReader.doProcess ) {
			
			waterBudgetCanopy.solver_model="dp853";
			waterBudgetCanopy.ET_model="AET";
			// 0<Imax<3
			waterBudgetCanopy.Imax=0.2;
			waterBudgetCanopy.k=0.463;
			waterBudgetCanopy.s_CanopyMax=0.001;
		
			waterBudgetRZ.solver_model="dp853";
			waterBudgetRZ.UpTake_model="NonLinearReservoir";
			waterBudgetRZ.ET_model="AET";
			waterBudgetRZ.a=752.3543670;
			waterBudgetRZ.b=1;
			waterBudgetRZ.s_RootZoneMax=0.005704;
			waterBudgetRZ.Pmax=0.5;
			waterBudgetRZ.pB=4.5;
			waterBudgetRZ.connectTOcanopy=false;
			
			waterBudgetGW.solver_model="dp853";
			waterBudgetGW.Q_model="NonLinearReservoir";
			waterBudgetGW.a=5;
			waterBudgetGW.b=1;
			waterBudgetGW.A=115;
			
			
			waterBudgetRunoff.solver_model="dp853";
			waterBudgetRunoff.ET_model="AET";
			waterBudgetRunoff.inRescaledDistance=width_sup;
			waterBudgetRunoff.pCelerity=2;
			waterBudgetRunoff.inTopindex=topIndex;
			waterBudgetRunoff.pSat=2;
			waterBudgetRunoff.inTimestep=timeStepMinutes;
			waterBudgetRunoff.tStartDate=startDate;
			waterBudgetRunoff.tEndDate=endDate;
			waterBudgetRunoff.ID=209;
			waterBudgetRunoff.s_RunoffMax=0.001;
		
			
			JReader.nextRecord();
			
			HashMap<Integer, double[]> id2ValueMap = JReader.outData;
			waterBudgetCanopy.inHMRain = id2ValueMap;
			
            
            ETReader.nextRecord();
            id2ValueMap = ETReader.outData;
            waterBudgetCanopy.inHMETp = id2ValueMap;
            waterBudgetRZ.inHMETp=id2ValueMap;
            waterBudgetRunoff.inHMETp=id2ValueMap;
            
            
            LAIReader.nextRecord();
            id2ValueMap = LAIReader.outData;
            waterBudgetCanopy.inHMLAI = id2ValueMap;
            
            
            //waterBudgetCanopy.inHMRootUpTake=waterBudgetRZ.outHMRootUpTake;
            waterBudgetCanopy.process();
                       
         

            waterBudgetRZ.inHMRain=waterBudgetCanopy.outHMThroughfall;
            waterBudgetRZ.process();
            
            waterBudgetRunoff.inRainValues=waterBudgetCanopy.outHMThroughfall; 
            waterBudgetRunoff.alpha=waterBudgetRZ.alpha; 
            waterBudgetRunoff.process();        
 
            
            waterBudgetGW.inHMRechargeValues=waterBudgetRZ.outHMR;            
            waterBudgetGW.process();
                  
            
            HashMap<Integer, double[]> outHMStorageRZ = waterBudgetRZ.outHMStorage;          
            HashMap<Integer, double[]> outHMStorageCanopy=waterBudgetCanopy.outHMStorage;           
            HashMap<Integer, double[]> outHMStorageGround=waterBudgetGW.outHMStorage;           
            HashMap<Integer, double[]> outHMStorageRunoff=waterBudgetRunoff.outHMStorage;
            
            HashMap<Integer, double[]> outHMETrz = waterBudgetRZ.outHMEvaporation;
            HashMap<Integer, double[]> outHMETc = waterBudgetCanopy.outHMTranspiration;
            HashMap<Integer, double[]> outHMETrunoff = waterBudgetRunoff.outHMET;
            
            
            waterBudgetGW.inHMRechargeValues=waterBudgetRZ.outHMR;
            
            HashMap<Integer, double[]> outHMQ = waterBudgetGW.outHMDischarge;
            HashMap<Integer, double[]> outHMQrunoff = waterBudgetRunoff.outHMDischarge;

            
			writerSc.inData = outHMStorageCanopy ;
			writerSc.writeNextLine();			
			if (pathToSc != null) {
				writerSc.close();
			}
			
			writerSrz.inData = outHMStorageRZ ;
			writerSrz.writeNextLine();			
			if (pathToSrz!= null) {
				writerSrz.close();
			}
			
			writerSg.inData = outHMStorageGround ;
			writerSg.writeNextLine();			
			if (pathToSg!= null) {
				writerSg.close();
			}
			
			
			writerSrunoff.inData = outHMStorageRunoff ;
			writerSrunoff.writeNextLine();			
			if (pathToSrunoff!= null) {
				writerSrunoff.close();
			}
			
			writerETc.inData = outHMETc;
			writerETc.writeNextLine();			
			if (pathToETc!= null) {
				writerETc.close();
			}
			

			writerETrz.inData = outHMETrz;
			writerETrz.writeNextLine();		
			if (pathToETrz!= null) {
				writerETrz.close();
			}
			
			
			writerETrunoff.inData = outHMETrunoff;
			writerETrunoff.writeNextLine();			
			if (pathToETrunoff!= null) {
				writerETrunoff.close();
			}
			
			
			writerQg.inData = outHMQ;
			writerQg.writeNextLine();		
			if (pathToQg!= null) {
				writerQg.close();
			}
			
			
			writerQrunoff.inData = outHMQrunoff;
			writerQrunoff.writeNextLine();			
			if (pathToQrunoff!= null) {
				writerQrunoff.close();
			}

						
			
            
		}
		JReader.close();
        LAIReader.close();
        ETReader.close();

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