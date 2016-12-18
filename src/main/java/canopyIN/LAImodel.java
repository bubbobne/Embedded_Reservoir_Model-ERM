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
package canopyIN;

// TODO: Auto-generated Javadoc
/**
 * The Class LAImodel.
 */
public class LAImodel implements ETModel{
	
	/** The  maximum storage. */
	public static double s_max;
	
	/** The actual storage. */
	public static double S_i;
	
	/** The multiplicative coefficient for the computation of actual ET. */
	public static double AETcoefficient;
	
	/** The leaf area index. */
	public static double LAI;
	
	/** The parameter of the SCF formulation */
	public static double k;
	

	
	/**
	 * Instantiates a new ET model.
	 *
	 * @param S_i is the actual storage
	 * @param s_max is the maximum storage
	 * @param k is the parameter of the SCF formulation
	 * @param LAI is the leaf area index
	 */
	public LAImodel(double S_i, double s_max,double k, double LAI){
		this.s_max=s_max;
		this.S_i=S_i;
		this.k=k;
		this.LAI=LAI;
	}

	/* (non-Javadoc)
	 * @see canopyIN.ETModel#ETcoefficient()
	 */
	public double ETcoefficient() {
		
		double SCF=1-Math.exp(-k*LAI);	
		AETcoefficient=S_i/s_max*(1-SCF);
		return AETcoefficient;
	}




}
