package utils;

import java.net.URISyntaxException;

import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorReader;

public class TestUtility {

	
	public final static double TOLLERANCE = Math.exp(-9);
	protected final static String START_DATE = "2015-10-01 00:00";
	protected final static String END_DATE = "2018-09-30 00:00";
	protected final static String FID = "ID";

	protected final static int MINUTES_TIME_STEP = 1440;
	protected final static int BASIN_ID = 853;

	
	public  OmsTimeSeriesIteratorReader getTimeseriesReader(String inPath, String id, String startDate, String endDate,
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
