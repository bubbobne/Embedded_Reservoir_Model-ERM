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

import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

// TODO: Auto-generated Javadoc
/**
 * The Class FirstLayer implements the FirstOrderDifferentialEquations interface
 * and solves the water budget equation.
 * @author Marialaura Bancheri
 */
public class waterBudgetODE implements FirstOrderDifferentialEquations{

	public double actualInput;

	public double ETp;
	
	public double s_RootZoneMax;
	
	public double Pmax;
	
	public double b_rz;


	/**
	 * Instantiates the first layer parameters .
	 *
	 * @param actualInput: actual input value after the partition
	 * @param ET: the modeled ET value
	 * @param uptake: the modeled value of the uptake
	 * @param Rg : the modeled value of the recharge of the lower layer
	 */
	public waterBudgetODE (double actualInput,double s_RootZoneMax, double Pmax,double b_rz, double ETp) {
		this.actualInput=actualInput;
		this.ETp=ETp;
		this.s_RootZoneMax=s_RootZoneMax;
		this.Pmax=Pmax;
		this.b_rz=b_rz;

	}
	
	/* (non-Javadoc)
	 * @see org.apache.commons.math3.ode.FirstOrderDifferentialEquations#getDimension()
	 */
	public int getDimension() {
		return 2;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.commons.math3.ode.FirstOrderDifferentialEquations#computeDerivatives(double, double[], double[])
	 */
	public void computeDerivatives(double t, double[] y, double[] yDot)
			throws MaxCountExceededException, DimensionMismatchException {
		yDot[0] =actualInput-Math.max(0, (ETp*Math.min(1,1.33*(y[0]/s_RootZoneMax))))-Pmax*Math.pow(y[0]/s_RootZoneMax, b_rz);
		
	
	}

}
