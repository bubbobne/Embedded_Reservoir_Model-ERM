package utility;

public class RungeKutta {

	private RKFunction rkFunction = null;

	public RungeKutta(RKFunction rkFunctoin) {

		this.rkFunction = rkFunctoin;

	}

	// RK4
	public double[] RK4(double Sn, double in) {
		double Sn0 = Sn;
		double t = 0;
		double dt = 0.01;
		double dtMin = 0.0001;
		double dtMax = 0.1;
		double dSMax = 0.1;
		double dSMin = 0.01;
		double dSToll = 0.01;
		double runoff = 0;
		double balance = 0;
		double Sn1 = 0;
		double test = 0;

		while (t < 1.0) {

			double[] k1 = rkFunction.computeFunction(Sn, in);
			double[] k2 = rkFunction.computeFunction(Sn + 0.5 * dt * k1[0], in);
			double[] k3 = rkFunction.computeFunction(Sn + 0.5 * dt * k2[0], in);
			double[] k4 = rkFunction.computeFunction(Sn + dt * k3[0], in);

			double Sn1OneStep = Utils.getRKMean(k1, k2, k3, k4, 0);
			double runoffOneStep = Utils.getRKMean(k1, k2, k3, k4, 1);
			// double deltaRunoff = runoffOneStep;
			k2 = computeFunction(Sn + 0.25 * dt * k1[0]);
			k3 = computeFunction(Sn + 0.25 * dt * k2[0]);
			k4 = computeFunction(Sn + 0.5 * dt * k3[0]);
			double Sn1HalfStep = Utils.getRKMean(k1, k2, k3, k4, 0);
			double runoffHalfStep = Utils.getRKMean(k1, k2, k3, k4, 1);

			k2 = computeFunction(Sn + dt * k1[0]);
			k3 = computeFunction(Sn + dt * k2[0]);
			k4 = computeFunction(Sn + 2 * dt * k3[0]);
			double Sn1DoubleStep = Utils.getRKMean(k1, k2, k3, k4, 0);
			double runoffDpubleStep = Utils.getRKMean(k1, k2, k3, k4, 1);

			double deltaRunoff = 0;
			if (Math.abs(dt * Sn1OneStep) < dSToll) {
				deltaRunoff = runoffOneStep;
				Sn1 = Sn + dt * Sn1OneStep;
				t = t + dt;
				if (dt != dtMin) {
					dt = dtMin;
				}
			} else {
				if (Math.abs(dt * Sn1OneStep) > dSToll
						&& Math.abs(Sn1OneStep - Sn1HalfStep) / Math.abs(Sn1OneStep) > dSMax) {
					dt = dt / 2;
					Sn1 = Sn + dt * Sn1HalfStep;
					deltaRunoff = runoffHalfStep;

				} else if (dt * 2 < dtMax && Math.abs(dt * Sn1OneStep) > dSToll
						&& Math.abs(Sn1OneStep - Sn1DoubleStep) / Math.abs(Sn1OneStep) < dSMin) {
					dt = dt * 2;
					Sn1 = Sn + dt * Sn1DoubleStep;
					deltaRunoff = runoffDpubleStep;

				} else {
					Sn1 = Sn + dt * Sn1OneStep;
					deltaRunoff = runoffOneStep;

				}
				t = t + dt;
			}

			runoff = runoff + dt * deltaRunoff;
			balance = balance + Sn - Sn1 + dt * recharge - dt * deltaRunoff;
			Sn = Sn1;
			if (t + dt > 1.0) {
				dt = 1.0 - t;
			}
		}
		return new double[] { Sn1, balance, runoff };
	}

}
