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

package runoffOLD;

import static org.jgrasstools.gears.libs.modules.JGTConstants.doubleNovalue;
import static org.jgrasstools.gears.libs.modules.JGTConstants.isNovalue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import oms3.annotations.Description;
import oms3.annotations.Execute;
import oms3.annotations.In;
import oms3.annotations.Out;
import oms3.annotations.Unit;
import runoff.ETModel;
import runoff.SimpleETModelFactory;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.feature.SchemaException;
import org.jgrasstools.gears.utils.coverage.CoverageUtilities;
import org.jgrasstools.gears.utils.math.interpolation.LinearListInterpolator;
import org.jgrasstools.hortonmachine.modules.statistics.cb.OmsCb;

import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

import org.apache.commons.math3.ode.*;



/**
 * The Class WaterBudget of the package runoff solves the water budget equation, 
 * according to the WFIUH theory. 
 * The outputs of the class are are the simulated discharge values 
 * and the water storage values.
 * @author Marialaura Bancheri
 */
public class WaterBudgetRunoff{


	@Description("Input rain Hashmap")
	@In
	public HashMap<Integer, double[]> inRainValues;


	@Description("Input ET Hashmap")
	@In
	public HashMap<Integer, double[]> inHMETp;
	
	@Description("ET model: AET")
	@In
	public String ET_model;
	
	ETModel ETModel;

	@Description("The ID of the investigated station")
	@In
	public int ID;

	@Description("Time step of the simulation")
	@In
	public int inTimestep;

	@Description("ODE solver model: dp853, Eulero")
	@In
	public String solver_model;


	@Description("Rescaled distance raster map")
	@In
	public GridCoverage2D inRescaledDistance = null;

	@Description("Topographic index raster map")
	@In
	public GridCoverage2D inTopindex = null;

	@Description("Channel celerity")
	@Unit("m/s")
	@In
	public double pCelerity;

	@Description("Maximum value of the water storage, needed for the"
			+ "computation of the Actual EvapoTraspiration")
	@In
	public double s_RunoffMax;

	@Description("partitioning coefficient between the runoff layer and the root zone layer")
	@In
	@Out
	public double alpha;

	@Description("Soil saturation percentage ")
	@Unit("%")
	@In
	public double pSat;
	
	@Description("Initial condition storage")
	@In
	public double IntialConditionStorage;

	@Description("First date of the simulation")
	@In
	public String tStartDate;

	@Description("Last date of the simulation")
	@In
	public String tEndDate;

	@Description("The HashMap with the Actual input of the layer ")
	@Out
	public HashMap<Integer, double[]> outHMActualInput= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the discharge values")
	@Out
	public HashMap<Integer, double[]> outHMDischarge= new HashMap<Integer, double[]>() ;
	
	@Description("The output HashMap with the discharge in mm")
	@Out
	public HashMap<Integer, double[]> outHMDischarge_mm= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the storage values")
	@Out
	public HashMap<Integer, double[]> outHMStorage= new HashMap<Integer, double[]>() ;

	@Description("The output HashMap with the AET values")
	@Out
	public HashMap<Integer, double[]> outHMET= new HashMap<Integer, double[]>() ;

	@Description("Initial condition for the computation of the storage")
	double S_i;

	@Description("step of the simulation")
	int step;
	@Description("Integration time")
	double dt ;

	@Description("parameter of the input rescaled distance raster map")
	private double xRes;
	private double yRes;
	private int cols;
	private int rows;

	@Description("parameter of the input width function")
	private double[][] widthFunction;
	private double[] timeArray;
	private double area;
	private double delta;
	private double pixelTotal;
	private double[] pixelArray;   


	@Description("length of the input rain time series")
	int dim;

	@Description("Vector with the runoff values for each time step")
	double []runoff;

	/**
	 * Process: reading of the data, computation of the
	 * storage and outflows
	 *
	 * @throws Exception the exception
	 */
	@Execute
	public void process() throws Exception {
		//checkNull(inRainValues);



		/** setting of the initial conditions*/
		if(step==0){

			// computation of the WFIUH considering the TopIndex and the % of saturation
			computeWFIUH();

			// the runoff output is in a vector whose dimensions are given by the product of length of the input series 
			// and its timeStep,  plus the length of the width function/60, since we want minute intervals

			// now is in minutes
			runoff=new double [(int)widthFunction[widthFunction.length - 1][0]/60+1];
			
			S_i=IntialConditionStorage;
		}

		/**Input data reading*/
		double rain = inRainValues.get(ID)[0];
		if (isNovalue(rain)) rain= 0;

		double ETp=0;
		if (inHMETp != null) ETp = inHMETp.get(ID)[0];
		if (isNovalue(ETp)) ETp= 0;

		/**
		 * The conversion in needed for input fluxes, since the rescaled distance is in
		 * 1/m, the celerity is in m/s, the area is in m^2 and the input are in mm/h
		 * with the conversion we obtain m/sec
		 **/
		double conversion=(inTimestep==60)?(1000*3600):(1000*3600*24);
		

		/** Computation of the runoff, considering the output of the previous time step */
		runoff=computeQ(alpha*rain/conversion, runoff);

		/** The output of the previous computation in the average discharge in 1 minute in m^3/s 
		 * so we need the compute the hourly average */
		double Q=computeMean(runoff,inTimestep);
		

		/** computation of the storage, the disharge must be converted from m^3/s to mm/h*/
		double waterStorage=computeS(alpha*rain,Q*1000/area*3600, ETp);

		double ET=computeAET(waterStorage, ETp);

		/** Save the result in  hashmaps for each station*/
		storeResult_series(ID,alpha*rain,waterStorage,Q,ET);

		runoff=computeNewVector(runoff,inTimestep);

		step++;

	}

	/**
	 * Compute the water storage
	 *
	 * @param J: input rain 
	 * @param Qmod : the discahrge obtained with the WFIUH
	 * @return the water storage, according to the model and the layer
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeS(double rain, double Qmod, double ETp) throws IOException {
		/**integration time*/
		dt=1E-4;

		double Emod=computeAET(S_i, ETp);

		/** Creation of the differential equation*/
		FirstOrderDifferentialEquations ode=new waterBudgetODE(rain,Qmod, Emod);			

		/** Boundaries conditions*/
		double[] y = new double[] { S_i, 0 };

		/** Choice of the ODE solver */	
		SolverODE solver;
		solver=SimpleIntegratorFactory.createSolver(solver_model, dt, ode, y);

		/** result of the resolution of the ODE, if nZ=1, S_i=S_t
		 * and setting of the new initial condition (S_i)*/
		S_i=solver.integrateValues();
		S_i=(S_i<0)?0:S_i;
		return S_i;
	}


	/**
	 * Compute computation of the discharge according to the WFIUH model
	 *
	 * @param rain : input fluxes in m/s
	 * @param widthFunction is the width function 
	 * @param inTimestep is the time step of the input
	 * @param area is the area of the saturated part of the basin
	 * @param pCelerity is the celerity in the channel
	 * @param runoff is the output of the previous time step computation
	 * @param dim is the length of the time series 
	 * @param step is the computation step we are considering 
	 * @return the double vector with the values of the simulated discharge
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double [] computeQ(double rain, double[] runoff) throws IOException {
		WFIUHKinematic wfiuh= new WFIUHKinematic(widthFunction,rain,inTimestep, area, pCelerity, runoff, dim,step);
		double [] Qfin=wfiuh.calculateQ();
		return Qfin;
	}


	public double computeAET(double S_i, double ETp) throws IOException {
		/** SimpleFactory for the computation of ET, according to the model*/
		ETModel=SimpleETModelFactory.createModel(ET_model, S_i, s_RunoffMax);	
		double Emod=ETModel.ETcoefficient()*ETp;
		return Emod;
	}

	/**
	 * Compute the mean of the runoff vector given by the previous method
	 * this is needed since the runoff is given as the average over 60 seconds and
	 * we need it as average over the input time step
	 *
	 * @param runoff is the the vector with the discharge values to be averaged 
	 * @param inTimeStep is the input time step
	 * @return the double value averaged over the in time step in m3/s in one hour
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double computeMean (double [] runoff, int inTimeStep ){

		int dim=(inTimeStep>runoff.length)?runoff.length:inTimeStep;
		for (int i=0;i<dim;i++){
			runoff[0]=runoff[0]+runoff[i];	
		}
		double mean=runoff[0]/dim;
		return mean;	
	}

	/**
	 * ComputeNewVector does a shift in the time series, since in the 
	 * first n values (where n is equal to the time step) the discharge was already 
	 * computed
	 *
	 * @param runoff is the the vector with the discharge values to be averaged 
	 * @param inTimeStep is the input time step
	 * @return the new vector with the runoff values
	 * @throws IOException Signals that an I/O exception has occurred.
	 */

	public double [] computeNewVector (double [] runoff,int inTimeStep){

		for (int i=0;i<runoff.length;i++){
			if (i<runoff.length-inTimeStep){
			runoff[i]=runoff[inTimeStep+i];	
			}else{
				runoff[i]=0;
			}
		}
		return runoff;	
	}


	/**
	 * computeWFIUH computes the width function starting from the rescaled distance and top index raster maps
	 *
	 * @return the width function
	 * @throws IOException Signals that an I/O exception has occurred.
	 */

	private void computeWFIUH () throws Exception{

		HashMap<String, Double> regionMap = CoverageUtilities.getRegionParamsFromGridCoverage(inRescaledDistance);
		cols = regionMap.get(CoverageUtilities.COLS).intValue();
		rows = regionMap.get(CoverageUtilities.ROWS).intValue();
		xRes = regionMap.get(CoverageUtilities.XRES);
		yRes = regionMap.get(CoverageUtilities.YRES);

		RenderedImage rescaledRI = inRescaledDistance.getRenderedImage();
		WritableRaster rescaledWR = CoverageUtilities.renderedImage2WritableRaster(rescaledRI, false);

		//processWithTopIndex(rescaledWR);

		GridCoverage2D widthfunctionSupCoverage = CoverageUtilities.buildCoverage("sup", rescaledWR, regionMap,
				inRescaledDistance.getCoordinateReferenceSystem());


		double[][] widthfunctionCb = doCb(widthfunctionSupCoverage);

		setWidthFunction(widthfunctionCb);

	}

	private void processWithTopIndex( WritableRaster rescaledWR) throws Exception {
		double[][] topindexCb = doCb(inTopindex);

		// cumulate topindex
		for( int i = 0; i < topindexCb.length; i++ ) {
			if (i > 0) {
				topindexCb[i][1] = topindexCb[i][1] + topindexCb[i - 1][1];
			}
		}
		double max = topindexCb[topindexCb.length - 1][1];
		// normalize
		for( int i = 0; i < topindexCb.length; i++ ) {
			topindexCb[i][1] = topindexCb[i][1] / max;
		}

		List<Double> meanValueList = new ArrayList<Double>();
		List<Double> cumulatedValueList = new ArrayList<Double>();
		for( int i = 0; i < topindexCb.length; i++ ) {
			meanValueList.add(topindexCb[i][0]);
			cumulatedValueList.add(topindexCb[i][1]);
		}

		LinearListInterpolator interpolator = new LinearListInterpolator(meanValueList, cumulatedValueList);
		double topindexThreshold = interpolator.linearInterpolateX(1 - pSat / 100);

		RenderedImage topindexRI = inTopindex.getRenderedImage();
		RandomIter topindexIter = RandomIterFactory.create(topindexRI, null);

		for( int c = 0; c < cols; c++ ) {
			for( int r = 0; r < rows; r++ ) {
				double topindex = topindexIter.getSampleDouble(c, r, 0);               
				if (topindex >= topindexThreshold) {
				}else{
					rescaledWR.setSample(c, r, 0, doubleNovalue);
				}
			}
		}
	}


	/**
	 * OmsCb computes the first two moments of the given input raster map
	 *
	 * @return the vector with the first two moments 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private double[][] doCb( GridCoverage2D coverage ) throws Exception {
		OmsCb cb = new OmsCb();
		cb.inRaster1 = coverage;
		cb.pFirst = 1;
		cb.pLast = 2;
		cb.pBins = 100;
		//cb.pm = pm;
		cb.process();
		double[][] moments = cb.outCb;
		return moments;
	}

	/**
	 * setWidthFunction computes the the with functions
	 *
	 * @param widthfunctionCb is the matrix with the moment of the rescaled distance map
	 * @return the matrix with the width function
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void setWidthFunction( double[][] widthfunctionCb ) {
		int widthFunctionLength = widthfunctionCb.length;
		pixelTotal = 0.0;
		double timeTotalNum = 0.0;

		timeArray = new double[widthFunctionLength];
		pixelArray = new double[widthFunctionLength];

		for( int i = 0; i < widthfunctionCb.length; i++ ) {
			timeArray[i] = widthfunctionCb[i][0];
			pixelArray[i] = widthfunctionCb[i][1];

			pixelTotal = pixelTotal + pixelArray[i];
			timeTotalNum = timeTotalNum + timeArray[i];
		}

		area = pixelTotal * xRes * yRes;
		delta = (timeArray[widthFunctionLength - 1] - timeArray[0]) / (widthFunctionLength - 1);

		widthFunction = new double[widthFunctionLength][3];
		double cum = 0.0;
		for( int i = 0; i < widthFunctionLength; i++ ) {
			widthFunction[i][0] = timeArray[i] / pCelerity;
			widthFunction[i][1] = pixelArray[i] * xRes * yRes / delta * pCelerity;
			double tmpSum = pixelArray[i] / pixelTotal;
			cum = cum + tmpSum;
			widthFunction[i][2] = cum;
		}
	}




	/**
	 * Store of the results in hashmaps 
	 *
	 * @param actualInput is the input after the partition
	 * @param waterStorage is the water storage
	 * @param discharge is the discharge
	 * @param AET is the actual ET
	 * @throws SchemaException the schema exception
	 */

	private void storeResult_series(int ID, double actualInput,  double waterStorage,double discharge,
			double AET) throws SchemaException {
		
		outHMActualInput.put(ID, new double[]{actualInput});
		outHMStorage.put(ID, new double[]{waterStorage});
		outHMDischarge.put(ID, new double[]{discharge});
		outHMDischarge_mm.put(ID, new double[]{discharge*1000/area*3600});
		outHMET.put(ID, new double[]{AET});


	}


}