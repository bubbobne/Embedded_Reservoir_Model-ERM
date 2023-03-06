package rungekutta;

/**
 * Canopy
 * 
 * @author Giuseppe Formetta, Daniele Andreis
 *
 */
public class CanopyRungeKutta extends RungeKutta {
	private double evapoT;
	private double storageMax;

	public CanopyRungeKutta(double maxOut, double maxOut2) {
		this.evapoT = maxOut;
		this.storageMax = maxOut2;

	}		// TODO Auto-generated method stub


	public double[] computeFunction(double storageN, double in) {
		if (storageN < 0) {
			storageN = 0;
		}

		double out1 = computeOut1(storageN, in);
		double out2 = computeOut2(storageN, in, out1);
		return new double[] { in - out1 - out2, out1, out2 };

	}

	private double computeOut2(double storageN, double in, double out2) {
		return Math.max(0, storageN + in - out2 - storageMax);

	}

	// compute AET
	private double computeOut1(double storageN, double in) {
		return Math.min(Math.max(0, storageN + in), evapoT * Math.min(1, storageN / storageMax));
	}

	@Override
	protected int getOutDimension() {
		return 3;
	}
}
