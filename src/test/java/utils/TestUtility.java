package utils;

import java.net.URISyntaxException;

import org.hortonmachine.gears.io.timedependent.OmsTimeSeriesIteratorReader;

public class TestUtility {

	public static OmsTimeSeriesIteratorReader getTimeseriesReader(String inPath, String id, String startDate, String endDate,
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
