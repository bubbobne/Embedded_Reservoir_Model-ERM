package it.geoframe.blogspot.runkekutta;



public class TestArticolo {
	private double inflowDeltaT = 12.3 ;
	private static int RKiter = 30;
	private double k = 1 / (2 * 0.13473);
	private double epsilon = 2;;
	private double dt;

	public static void main(String[] args) {
		double Sn = 0;
		TestArticolo ta = new TestArticolo();
		double dt = 300 / RKiter;

		double recharge = 12.3*10;

		ta.RK4(Sn, recharge,dt);

	}

	// compute dS/dt
	public double computeFunction(double Sn, double recharge) {
		return recharge - computeQ(Sn, recharge);
	}



	// RK4
	public double[] RK4(double Sn, double recharge,double dt) {
		double k1 = 0;
		double k2 = 0;
		double k3 = 0;
		double k4 = 0;
		double balance = 0;
		double min = 60;
		double q = 0;
		double t = 0;
		for (int k = 0; k < RKiter; k++) {
			k1 = computeFunction(Sn, recharge);
			k2 = computeFunction(Sn + 0.5 * dt* k1, recharge);
			k3 = computeFunction(Sn + 0.5 *dt* k2, recharge);
			k4 = computeFunction(Sn + dt* k3, recharge);
			double Sn1 = Sn +  dt*(k1 + 2 * k2 + 2 * k3 + k4) / 6;
			double deltaQ = computeQ(Sn, recharge);
			balance = balance + Sn - Sn1 + dt*recharge - dt*deltaQ;
			Sn = Sn1;
			q = q + deltaQ;
			t = t + dt;
			System.out.println("tempo: " + k);
			System.out.println("h: " + Sn / 71);
			System.out.println("q: " + deltaQ);
			System.out.println("q: " + balance);

		}
		return new double[] { Sn, balance, q };
	}

	private double computeQ(double sn, double recharge) {

		return Math.min(sn + recharge, Math.pow(sn / k, 1 / epsilon));
	}
}