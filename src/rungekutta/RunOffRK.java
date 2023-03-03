package rungekutta;

public class RunOffRK extends RungeKutta {
	double d;
	double c;
	double sRunoffMax;

	public RunOffRK(double c, double d, double sRunoffMax) {
		// TODO Auto-generated constructor stub
		this.c = c;
		this.d = d;
		this.sRunoffMax = sRunoffMax;
	}

	public double[] computeFunction(double Sn, double in) {
		if (Sn < 0) {
			Sn = 0;
		}
		double out = computeRunoff(Sn, in);
		double fun = in - out;
//		if (in - fun - out > 0) {
//			System.out.println(in - fun - out);
//			System.out.println(in - in+out - out);
//
//		}
		return new double[] { fun, out };
	}

	private double computeRunoff(double Sn, double in) {
		// double out = Math.max(c,recharge) * Math.pow(Sn / s_RunoffMax, d);
		double out = c * Math.pow(Math.min(1, Sn / sRunoffMax), d);
		out = out + Math.max(0, Sn - sRunoffMax + in - out);
		out = Math.min(Sn + in, out);
		return out;
	}

	@Override
	public int getOutDimension() {
		// TODO Auto-generated method stub
		return 2;
	}
}
