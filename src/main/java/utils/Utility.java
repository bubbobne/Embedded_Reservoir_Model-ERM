package utils;

public class Utility {

	/**
	 * Get the conversion factor, from mm/dt to m3/s.
	 * 
	 * @param A area
	 * @param dt time in minutes
	 * @return
	 */
	
	public final static double getCOnversionToM3SCoeff(double A, double dt ) {
		 return A * Math.pow(10, 3) / (dt * 60);
	}
	
}
