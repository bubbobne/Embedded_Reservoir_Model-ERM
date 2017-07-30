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
package runoff;

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

	public double Q;
	public double AET;
	public double inputFluxes;

	/**
	 * Instantiates the first layer parameters .
	 *
	 * @param Q: discharge value
	 * @param inputFluxes: input fluxes value
	 * @param AET: the AET value
	 */
	public waterBudgetODE(double inputFluxes, double Q, double AET) {
		this.Q=Q;
		this.inputFluxes=inputFluxes;
		this.AET=AET;

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
		yDot[0] =inputFluxes-Q-AET;
		

	
	}

}
