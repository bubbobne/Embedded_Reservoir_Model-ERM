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
package rootZone;


public class BeC implements SwrcModel {
	
	double zeta;
	double eta;
	double psiB;
	double beta;

	public BeC(double zeta, double eta, double psiB, double beta) {
		this.zeta=zeta;
		this.eta=eta;
		this.psiB=psiB;
		this.beta=beta;
	}

	@Override
	public double values() {
		double s_e=((zeta-eta)>psiB)?Math.pow(psiB/(zeta-eta),beta):1;
		return s_e;
	}
	
	

}
