package hydrometricStage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;
import oms3.annotations.Unit;
import oms3.io.CSTable;
import oms3.io.DataIO;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


public class ReaderParameterFRC {

	@Description("Path to the params file")
	@In
	public String inParamsFile;
	
	
	@Description("Path to the params file")
	@In
	public String IDsubs;
	
	
	@Description("The double value with the hydrometric zero")
	@Out
	@Unit ("m")
	public double H0=0;

	@Description("The double value of the coefficient")
	@Out
	public double a;

	@Description("The double value of the coefficient")
	@Out
	public double b;

	@Description("The double value of the second coefficient")
	@Out
	public double c;

	@Description("The double value of the coefficient")
	@Out
	public double d;
	
	@Description("String containing the name of the model: "
			+ " FRC_Qh: a*(h+H0)^b;"
			+ " FRC_VA: (a*(h+H0)^b)*(c*(h+H0)+d"
			+ " NO_FRC")
	@Out
	public String model;


	Logger logger = LogManager.getLogger(ReaderParameterFRC.class);


	/**
	 * Process.
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception {

	
		String [] ID_basin=readDataString(inParamsFile, "ID_basin");
		
		int indexOfsubs = Arrays.asList(ID_basin).indexOf(IDsubs);
		
		
		model=readDataString(inParamsFile, "formula")[indexOfsubs];
				
		if(model.equals("")||model.equals("NO_FRC")){
			model="NO_FRC";
		}else{
		
		a=readData(inParamsFile, "a")[indexOfsubs];
		b=readData(inParamsFile, "b")[indexOfsubs];
		c=readData(inParamsFile, "c")[indexOfsubs];
		d=readData(inParamsFile, "d")[indexOfsubs];
		H0=readData(inParamsFile, "H0")[indexOfsubs];
		}
		
		System.out.println("");
	
	}
	
	
	
	/**
	 * Read data: reader input Data: all values from the time series.
	 *
	 * @param inPath the  path of the  input file
	 * @param i the index of the column
	 * @return the double[] vector with the input values
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Double [] readData(String inPath, String column) throws IOException {	
		CSTable Reader = DataIO.table(new File(inPath));
		Double[] vector =DataIO.getColumnDoubleValues(Reader, column );
		return vector;		
	}
	
	
	public String [] readDataString(String inPath, String column) throws IOException {	
		CSTable Reader = DataIO.table(new File(inPath));
		String [] vector =getColumnStringValues(Reader, column );
		return vector;		
	}
	
	
	
    public static String [] getColumnStringValues(CSTable t, String columnName) {
        int col = columnIndex(t, columnName);
        if (col == -1) {
            throw new IllegalArgumentException("No such column: " + columnName);
        }
        List<String> l = new ArrayList<String>();
        for (String[] s : t.rows()) {
            l.add(new String(s[col]));
        }
        return l.toArray(new String[0]);
    }
    
    
    /** Gets a column index by name
     * 
     * @param table The table to check
     * @param name the column name
     * @return the index of the column
     */
    public static int columnIndex(CSTable table, String name) {
        for (int i = 1; i <= table.getColumnCount(); i++) {
            if (table.getColumnName(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }
}
