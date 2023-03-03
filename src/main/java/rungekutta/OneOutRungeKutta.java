package rungekutta;

/**
 * 
 * @author Giuseppe Formetta, Daniele Andreis
 *
 */
public class OneOutRungeKutta extends RungeKutta {
	double d;
	double c;
	double maxOut;

	public OneOutRungeKutta(double c, double d, double maxOut) {
		// TODO Auto-generated constructor stub
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
//		if (in - fun - out > 0) {
//			System.out.println(in - fun - out);
//			System.out.println(in - in+out - out);
//
//		}
		return new double[] { fun, out };
	}

	private double computeOut(double storageN, double in) {
		// double out = Math.max(c,recharge) * Math.pow(Sn / s_RunoffMax, d);
		double out = c * Math.pow(Math.min(1, storageN / maxOut), d);
		out = out + Math.max(0, storageN - maxOut + in - out);
		out = Math.min(storageN + in, out);
		return out;
	}

	@Override
	public int getOutDimension() {
		// TODO Auto-generated method stub
		return 2;
	}
}
