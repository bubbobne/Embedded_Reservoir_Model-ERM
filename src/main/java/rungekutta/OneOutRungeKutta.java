package rungekutta;

/**
 * 
 * Extension for single-out reservoir.
 * 
 * @author Giuseppe Formetta, Daniele Andreis
 *
 */
public class OneOutRungeKutta extends RungeKutta {
	private double d;
	private double c;
	private double maxOut;

	public OneOutRungeKutta(double c, double d, double maxOut) {
		this.c = c;
		this.d = d;
		this.maxOut = maxOut;
	}

	public double[] computeFunction(double storageN, double in) {
		if (storageN < 0) {
			storageN = 0;
		}
		double out = computeOut(storageN, in);
		double fun = in - out;
		return new double[] { fun, out };
	}

	private double computeOut(double storageN, double in) {
		double out = c * Math.pow(Math.min(1, storageN / maxOut), d);
		out = out + Math.max(0, storageN - maxOut + in - out);
		out = Math.min(storageN + in, out);
		return out;
	}

	@Override
	protected int getOutDimension() {
		return 2;
	}
}
