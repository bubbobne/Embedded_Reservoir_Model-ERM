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

package groundWater;

public class NonLinearReservoir implements DischargeModel{
	
	double a;
	double S_i;
	double b;
	double Q;
	
	public NonLinearReservoir(double a,double S_i, double b){
		this.a=a;
		this.S_i=S_i;
		this.b=b;		
	}

	public double dischargeValues() {
		Q=a*Math.pow(S_i, b);
		return Q;
	}

}
