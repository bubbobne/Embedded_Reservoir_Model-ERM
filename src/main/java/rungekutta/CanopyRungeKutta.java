package rungekutta;


/**
 * 
 * @author Giuseppe Formetta, Daniele Andreis
 *
 */
public class CanopyRungeKutta extends RungeKutta {
	double maxOut1;
	double maxOut2;

	public CanopyRungeKutta(double maxOut, double maxOut2) {

		this.maxOut1 = maxOut;
		this.maxOut2 = maxOut2;

	}

	public double[] computeFunction(double storageN, double in) {
		if (storageN < 0) {
			storageN = 0;
		}

		double out1 = computeOut1(storageN, in);
		double out2 = computeOut2(storageN, in, out1);
		return new double[] { in - out1 - out2, out1, out2 };

	}

	private double computeOut2(double storageN, double in, double out2) {
		return Math.max(0, storageN + in - out2 - maxOut1);

	}

	// compute AET
	public double computeOut1(double storageN, double in) {
		return Math.min(Math.max(0, storageN + in), maxOut1 * Math.min(1, storageN / maxOut1));
	}

	@Override
	public int getOutDimension() {
		// TODO Auto-generated method stub
		return 3;
	}
}
