package it.geoframe.blogspot.runkekutta;

import org.junit.Test;

import rungekutta.RungeKutta;

/**
 * Check the method with exact solution from:
 * 
 * Demonstrating Reservoir Routing in the Classroom: Physical and Mathematical
 * Modeling James Kilduff, Assistant Professor
 * 
 * 
 * 
 * 
 * 
 * @author Giuseppe Formetta, Daniele Andreis
 *
 */

public class TestRK {
	@Test
	public void RK() {
		RoutingRk rk = new RoutingRk();
		double CI = 0;

		for (int t = 0; t <= 800; t = t + 10) {
			double[] output = rk.run(CI, 12.3*10, 0.01);
			System.out.println(output[0]/rk.area + " "+rk.getExactH(t, 0));
			CI = output[0];
		}
	}

	public class RoutingRk extends RungeKutta {

		public double area = 71;
		double cc = 0.78;
		double cd = 5.59 * 0.78;

		@Override
		protected double[] computeFunction(double storage, double in) {
			// TODO Auto-generated method stub

			if (storage < 0) {
				storage = 0;
			}

			double out1 = computeOut(storage, in);
			return new double[] { in - out1, out1 };
		}

		private double computeOut(double storage, double in) {
			double out = cd * Math.sqrt(storage / area);
			out = Math.min(storage + in, out);
			return out;
		}

		@Override
		protected int getOutDimension() {
			// TODO Auto-generated method stub
			return 2;
		}

		/**
		 * Get the exact soulution (equation 12)
		 * 
		 * @param t
		 * @param h0
		 * @return
		 */
		public double getExactH(double t, double h0) {
			return (Math.sqrt(h0) - cd * t / (1 * area));
		}

		/**
		 * Get the exact soulution (equation 12)
		 * 
		 * @param t
		 * @param h0
		 * @return
		 */
		public double getExactQ(double h) {
			return cd * Math.sqrt(h);
		}

	}

}
