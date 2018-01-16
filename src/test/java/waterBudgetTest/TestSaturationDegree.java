package waterBudgetTest;

import java.net.URISyntaxException;
import java.util.HashMap;

import org.geotools.coverage.grid.GridCoverage2D;
import org.jgrasstools.gears.io.rasterreader.OmsRasterReader;
import org.jgrasstools.gears.io.rasterwriter.OmsRasterWriter;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.junit.*;

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
		subReader.file = "/Users/marialaura/Dropbox/dati_NewAge/EsercitazioniIdrologia2017/output/geomorphology/subbasins_cut_basento_mary.asc";
		subReader.fileNovalue = -9999.0;
		subReader.geodataNovalue = Double.NaN;
		subReader.process();
		GridCoverage2D sub = subReader.outRaster;

		String startDate = "2014-01-07 19:00" ;
		String endDate = "2014-01-07 20:00";
		int timeStepMinutes = 60;
		String fId = "ID";

		String inPathToStorage ="/Users/marialaura/Dropbox/dati_NewAge/EsercitazioniIdrologia2017/output/Basento/Storage/Stot_musk_10.csv";

		OmsTimeSeriesIteratorReader storageReader = getTimeseriesReader(inPathToStorage, fId, startDate, endDate, timeStepMinutes);


		SaturationDegree test =new SaturationDegree();
		//PathGenerator path=new PathGenerator();

		test.inSubbasins=sub;
		test.Smax=1;

		while( storageReader.doProcess) { 




			storageReader.nextRecord();	
			HashMap<Integer, double[]> id2ValueMap = storageReader.outData;
			test.inHMStorage= id2ValueMap;




			test.process();

			//path.pathToOutData="/Users/marialaura/Dropbox/dati_NewAge/EsercitazioniIdrologia2017/output/Basento/Storage/S.asc";
			//path.tCurrent=storageReader.tCurrent;
			//path.process();




			outDataGrid  =  test.outSaturationDataGrid;


			GridCoverage2D krigingRaster = test.outSaturationDataGrid;

			OmsRasterWriter writerRaster = new OmsRasterWriter();
			writerRaster.inRaster = krigingRaster;
			//writerRaster.file = path.pathOutDataComplete;
			writerRaster.process();

		}

		storageReader.close();
		// writer.close();


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
