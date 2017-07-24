package prova;

import java.util.HashMap;

import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;

import oms3.annotations.Description;
import oms3.annotations.Out;



public class prova_equazione {

	
	public double s_RootZoneMax;
	public double S_i=0.1;
	
	@Description("The output HashMap with the Water Storage  ")
	@Out
	public HashMap<Integer, double[]> outHMStorage= new HashMap<Integer, double[]>() ;
	
	
	public void process(){
		

		
		/** Creation of the differential equation*/
		FirstOrderDifferentialEquations ode=new ODE();			

		/** Boundaries conditions*/
		double[] y = new double[] { S_i, s_RootZoneMax };

		/** Choice of the ODE solver */	
		SolverODE solver;
		solver=SimpleIntegratorFactory.createSolver("dp853", 1, ode, y);
		
		S_i=solver.integrateValues();
		
		outHMStorage.put(1, new double[]{S_i});
		
	}
	
	
	
	
	
	
}
