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
package damModelling;

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

	public double a_surface;
	
	public double b_surface;

	public double recharge;
	
	public double precip;
	
	public double erogazioni;
	
	public double l;
	
	public double mu;
	
	public double h_sfioro;

	
	



	/**
	 * Instantiates the first layer parameters .
	 *
	 * @param recharge: input recharge value
	 * @param Qmod: the modeled input discharge
	 */
	public waterBudgetODE(double recharge, double precip, double a_surface, double b_surface, double mu,double l, double erogazioni,double h_sfioro) {
		this.a_surface=a_surface;
		this.b_surface=b_surface;
		this.recharge=recharge;
		this.precip=precip;
		this.erogazioni=erogazioni;
		this.l=l;
		this.mu=mu;
		this.h_sfioro=h_sfioro;

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
		yDot[0] =(recharge+precip-mu*l*Math.pow(2*9.81,0.5)*Math.pow(Math.max(0, y[0]-h_sfioro),1.5)- erogazioni)/(a_surface*y[0]-b_surface)*3600;
		
		//1/(a_surface*y[0]-b_surface)*
			
	}

}
