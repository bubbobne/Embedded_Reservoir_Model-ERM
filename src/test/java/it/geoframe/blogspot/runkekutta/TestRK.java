package it.geoframe.blogspot.runkekutta;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import rungekutta.RungeKutta4;
import rungekutta.adaptive.AdaptiveRungeKutta4;
import utils.TestUtility;

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

	public static double DELTA_T = 10;

	/**
	 * output evaluate with: https://gist.github.com/bubbobne/06c15e4a0dc1104bfbf458e7cf60db11
	 */
	private final static double[] solution = new double[] { 1.2543261182170111, 2.18017645278028, 2.929554696185594,
			3.5550666963585438, 4.086236819424352, 4.542381430549469, 4.937227977946122, 5.281055311377312,
			5.581842148323699, 5.845946403356595, 6.078536194392336, 6.283878204712432, 6.46553882104708,
			6.626529229735589, 6.769413026236427, 6.8963878967034375, 7.009348852118379, 7.1099380153882645,
			7.199584398305937, 7.279536088507692, 7.350886587205579, 7.414596573677519, 7.471512047678362,
			7.522379569640137, 7.567859151004008, 7.608535223812762, 7.644926026793103, 7.677491675730076,
			7.706641132864849 };

	@Test
	public void RK() {

		RoutingRk rk = new RoutingRk();
		double CI = 0;
		int i = 0;
		for (double t = 10; t < 300; t = t + DELTA_T) {
			double[] output = rk.run(CI, 12.3 * DELTA_T, 100);
			assertEquals(output[0] - CI - 12.3 * DELTA_T + output[1], 0, TestUtility.TOLLERANCE);
			assertEquals(output[0] / rk.area, solution[i], 0.0001);
			// System.out.print(output[0] / rk.area);
			CI = output[0];
			i = i + 1;
		}

		double h0 = CI / rk.area;
		for (double t = 310; t < 500; t = t + DELTA_T) {
			double[] output = rk.run(CI, 0, 100);
			assertEquals(output[0] - CI + output[1], 0, TestUtility.TOLLERANCE);
			double h = rk.getExactH(t, h0);
			System.out.println(t);
			System.out.println(h);

			assertEquals(output[0] / rk.area, h, 0.001);
			CI = output[0];
			i = i + 1;

		}

	}

	public class RoutingRk extends AdaptiveRungeKutta4 {

		public double area = 71;
		double cc = 0.78;
		double cd = 5.59 * 0.78;

		@Override
		protected double[] computeFunction(double storage, double in) {
			if (storage < 0) {
				storage = 0;
			}
			double out1 = computeOut(storage, in);
			return new double[] { in - out1, out1 };
		}

		public double getTime(double h0, double h) {
			h0 = Math.sqrt(h0);
			h = Math.sqrt(h);

			return 2 * area / cd * ((h0 - h) + 1 / cd * Math.log(Math.abs((1 - cd * h0) / (1 - cd * h))));
		}

		private double computeOut(double storage, double in) {
			return Math.min(storage + in, cd * Math.sqrt(storage / area)) * DELTA_T;
		}

		@Override
		protected int getOutDimension() {
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
			if (t >= 300) {
				double h = (Math.sqrt(h0) - cd * (t - 300) / (2 * area));
				if (h > 0) {
					return h * h;
				} else {
					return 0;
				}
			} else {
				return h0;
			}
		}

		/**
		 * Get the exact soulution (equation 12)
		 * 
		 * @param t
		 * @param h0
		 * @return
		 */
		public double getExactQ(double t, double h) {
			if (t > 300) {
				return cd * Math.sqrt(h);
			} else {
				return 0;
			}
		}

	}

}
