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

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;
import oms3.annotations.Unit;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.SchemaException;
import org.jgrasstools.gears.libs.modules.JGTModel;
import org.jgrasstools.gears.utils.coverage.CoverageUtilities;
import org.jgrasstools.hortonmachine.modules.statistics.cb.OmsCb;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BrentSolver;
import org.apache.commons.math3.analysis.solvers.IllinoisSolver;
import org.apache.commons.math3.analysis.solvers.NewtonRaphsonSolver;
import org.apache.commons.math3.ode.*;


/**
 * The Class WaterBudget solves the water budget equation, according to
 * the models chosen for the simulation of the discharge and of the AET. 
 * The outputs of the class are 4 hashmaps with the water storage values,
 * simulated discharge values, simulated AET and the quick discharge (if there
 * is any). The simulated discharge is partitioned in two flows: a flow which drains
 * to the lower layer and the quick discharge.
 * For the lower layer, we don't have nor ET processes and the 
 * partitioning of the discharge in quick and drainage to other layers, so the third and
 * fourth columns of the first hashmap and the second hashmap will be equal to zero.
 * @author Marialaura Bancheri
 */
public class WaterBudget extends JGTModel{


	@Description("Input rain Hashmap")
	@In
	public HashMap<Integer, double[]> inRainValues;


	@Description("Input rain Hashmap")
	@In
	public HashMap<Integer, double[]> inSnowValues;

	@Description("ET: ET value for the given time considered")
	double ET;

	@Description("Input ET Hashmap")
	@In
	public HashMap<Integer, double[]> inETvalues;

	@Description("Input Discharge Hashmap: first contribution from other HRUs")
	@In
	public HashMap<Integer, double[]> inDischargevalues1;

	@Description("Input Discharge Hashmap: second contribution from other HRUs")
	@In
	public HashMap<Integer, double[]> inDischargevalues2;


	@Description("time step of the simulation")
	@In
	public int inTimestep;


	@Description("Integration time")
	double dt ;

	@Description("ID of the basin")
	@In
	public static int ID ;



	@Description("partitioning coefficient between the reserovir")
	@Unit("-")
	@In
	@Out
	public double alpha;


	@Description("Degree of spatial variability of the soil moisture capacity.")
	@In
	@Unit("-")
	public Double pB;

	@Description("Simluted value of AET"
			+ "at a given time step")
	double AET;

	@Description("The output HashMap with the Water Storage  ")
	@Out
	public HashMap<Integer, double[]> outHMStorage= new HashMap<Integer, double[]>() ;


	@Description("The output HashMap with the AET ")
	@Out
	public HashMap<Integer, double[]> outHMEvapotranspiration = new HashMap<Integer, double[]>() ;


	@Description("The output HashMap with the outflow "
			+ "which drains to the lower layer")
	@Out
	public HashMap<Integer, double[]> outHMR= new HashMap<Integer, double[]>() ;

	double eta_i;
	int step;
	
	public double zeta;
	public double zeta_rz;
	public double psiB;
	public double beta;
	public double theta_s;



	/**
	 * Process: reading of the data, computation of the
	 * storage and outflows
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception {
		checkNull(inRainValues);

		/**Input data reading*/
		double rain = inRainValues.get(ID)[0];
		if (isNovalue(rain)) rain= 0;

		double snow = 0;					
		if (inSnowValues != null) snow=inSnowValues.get(ID)[0];
		if (isNovalue(snow)) snow= 0;

		double Qinput1=0;
		if (inDischargevalues1 != null) Qinput1 =inDischargevalues1.get(ID)[0];
		if (isNovalue(Qinput1)) Qinput1= 0;

		double Qinput2=0;
		if (inDischargevalues2 != null) Qinput2 =inDischargevalues2.get(ID)[0];
		if (isNovalue(Qinput2)) Qinput2= 0;


		double totalInputFluxes=rain+snow+Qinput1+Qinput2;

		ET=0;
		if (inETvalues != null) ET = inETvalues.get(ID)[0];
		if (isNovalue(ET)) ET= 0;

		double S_max=theta_s*(zeta-zeta_rz);
		
		double storage_rz=computeS(eta_i);
		
		double alpha=alpha(storage_rz,totalInputFluxes,S_max);
	
		double evapotranspiration=computeAET(storage_rz,S_max);
		
		double eta_rz=computeEta((1-alpha)*totalInputFluxes,eta_i,evapotranspiration,S_max);
		
		double discharge=computeQ(eta_rz);
		
		


		/** Save the result in  hashmaps for each station*/
		storeResult_series(ID,eta_rz,discharge,evapotranspiration);

		eta_i=eta_rz;
		step++;

	}

	private double computeS(double eta_i) {
		double S_g=((zeta-eta_i)>psiB)?Math.pow(psiB/(zeta-eta_i),beta):1;
		double S_rz=((zeta_rz-eta_i)>psiB)?Math.pow(psiB/(zeta_rz-eta_i),beta):1;
		return S_g-S_rz;
	}
	
	private double alpha( double S_rz, double Pval, double S_max) {
		double pCmax=S_max *(pB+1);
		double coeff1 = ((1.0 - ((pB + 1.0) * (S_rz) / pCmax)));
		double exp = 1.0 / (pB + 1.0);
		double ct_prev = pCmax * (1.0 - pow(coeff1, exp));
		double UT1 = max((Pval - pCmax + ct_prev), 0.0);     
		return alpha=UT1/Pval;
	}
	
	
	/**
	 * Compute the AET
	 *
	 * @param ETinput: the input potential ET
	 * @return the double value od the AET
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private double computeAET(double S_rz, double S_max) throws IOException {
		AET=(S_rz*ET/S_max);
		return AET;
	}




	/**
	 * Compute the water storage
	 *
	 * @param J: input rain 
	 * @param Qinput : input discharge
	 * @param ET: input potential ET
	 * @return the water storage, according to the model and the layer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeEta(double totalInputFluxes, double eta, double evapotranspiration,double S_max) throws IOException {
		/**integration time*/
		dt=1E-4;

		
		UnivariateFunction univariateFunction = new equation(zeta, zeta_rz,eta, psiB, beta,totalInputFluxes, dt,evapotranspiration);
		BrentSolver brentSolver = new BrentSolver();

		double eta_rz=brentSolver.solve(1000, univariateFunction,-1,S_max);
		return eta_rz;
	}

	/**
	 * Compute computation of the discharge according to the mode:
	 * mode external --> external value
	 * else --> non-linear reservoir model
	 *
	 * @param Qinput: input discharge value
	 * @return the double value of the simulated discharge
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeQ( double eta_rz) throws IOException {
		double Q=eta_rz-eta_i+(Math.pow(psiB/(zeta_rz-eta_rz),beta)-Math.pow(psiB/(zeta_rz-eta_i),beta))/dt;		
		return Q;
	}





	/**
	 * Store of the results in hashmaps 
	 *
	 * @param waterStorage is the water storage
	 * @param discharge is the discharge
	 * @param evapotranspiration is the evapotranspiration
	 * @param quickRunoff is the water quick runoff from the layer
	 * @param drainage is drainage toward the lower layer
	 * @throws SchemaException the schema exception
	 */

	private void storeResult_series(int ID, double waterStorage,double discharge,
			double evapotranspiration) throws SchemaException {

		outHMStorage.put(ID, new double[]{waterStorage});
		outHMEvapotranspiration.put(ID, new double[]{evapotranspiration});
		outHMR.put(ID, new double[]{discharge});

	}


}