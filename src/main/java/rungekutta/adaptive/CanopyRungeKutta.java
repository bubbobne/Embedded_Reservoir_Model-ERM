/*
 * GNU GPL v3 License
 *
 * Copyright 2015 Marialaura Bancheri
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package rungekutta.adaptive;

/**
 * Canopy
 * 
 * @author Giuseppe Formetta, Daniele Andreis
 *
 */
public class CanopyRungeKutta extends AdaptiveRungeKutta4 {
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
