package waterBudgetTest;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.geotools.coverage.grid.GridCoverage2D;
import org.jgrasstools.gears.io.rasterreader.OmsRasterReader;
import org.jgrasstools.gears.io.rasterwriter.OmsRasterWriter;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.junit.*;

import saturationDegree.PathGenerator;
//import pathGenerator.PathGenerator;
import saturationDegree.SaturationDegree;

/**
 * Test PrestleyTaylorModel.
 * 
 */
@SuppressWarnings("nls")
public class TestSaturationDegree{

	GridCoverage2D outDataGrid = null;

	@Test
	public void Test() throws Exception {


		OmsRasterReader subReader = new OmsRasterReader();
		subReader.file = "resources/Input/subbasins_cut_basento_mary.asc";
		subReader.fileNovalue = -9999.0;
		subReader.geodataNovalue = Double.NaN;
		subReader.process();
		GridCoverage2D sub = subReader.outRaster;

		String startDate = "2014-01-07 19:00" ;
		String endDate = "2014-01-07 20:00";
		int timeStepMinutes = 60;
		String fId = "ID";

		String inPathToStorage ="resources/Input/Storage_10_Bas.csv";

		OmsTimeSeriesIteratorReader storageReader = getTimeseriesReader(inPathToStorage, fId, startDate, endDate, timeStepMinutes);


		SaturationDegree test =new SaturationDegree();
		PathGenerator path=new PathGenerator();

		test.inSubbasins=sub;
		test.Smax_saturation_degree=1;

		while( storageReader.doProcess) { 




			storageReader.nextRecord();	
			HashMap<Integer, double[]> id2ValueMap = storageReader.outData;
			test.inHMStorage= id2ValueMap;




			test.process();

			path.pathToOutDir="resources/Output/Storage";
			path.basin="Bas";
			path.tCurrent=storageReader.tCurrent;
			path.process();




			outDataGrid  =  test.outSaturationDataGrid;


			GridCoverage2D SRaster = test.outSaturationDataGrid;

			OmsRasterWriter writerRaster = new OmsRasterWriter();
			writerRaster.inRaster = SRaster;
			writerRaster.file = path.pathOutDataComplete;
			writerRaster.process();

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