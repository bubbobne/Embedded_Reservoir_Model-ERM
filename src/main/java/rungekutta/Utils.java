
package rungekutta;

/**
 * 
 * @author Giuseppe Formetta, Daniele Andreis
 *
 */
public class Utils {

	/**
	 * Evaluate the Runge-Kutta 4 mean;
	 * 
	 * @param k1
	 * @param k2
	 * @param k3
	 * @param k4
	 * @param index
	 * @return
	 */
	public final static double getRKMean(double[] k1, double k2[], double[] k3, double[] k4, int index) {
		return (k1[index] + 2 * k2[index] + 2 * k3[index] + k4[index]) / 6;
	}

	/**
	 * Evaluate the Runge-Kutta 4 mean;
	 * 
	 * @param k1
	 * @param k2
	 * @param k3
	 * @param k4
	 * @param index
	 * @param den   denominator if is not 6.
	 * @return
	 */
	public final static double getRKMean(double[] k1, double k2[], double[] k3, double[] k4, int index, double den) {
		return (k1[index] + 2 * k2[index] + 2 * k3[index] + k4[index]) / den;
	}
}
