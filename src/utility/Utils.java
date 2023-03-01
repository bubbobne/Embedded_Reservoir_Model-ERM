

package utility;


public class Utils {
	public final static double getRKMean(double[] k1, double k2[], double[] k3, double[] k4, int index) {
		return (k1[index] + 2 * k2[index] + 2 * k3[index] + k4[index]) / 6;
	}
	
	
	
	public final static double getRKMean(double[] k1, double k2[], double[] k3, double[] k4, int index, double den) {
		return (k1[index] + 2 * k2[index] + 2 * k3[index] + k4[index]) / den;
	}
}
