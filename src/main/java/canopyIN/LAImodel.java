package canopyIN;

public class LAImodel implements ETModel{
	
	public static double s_max;
	public static double S_i;
	public static double AETcoefficient;
	public static double LAI;
	public static double k;
	

	
	public LAImodel(double S_i, double s_max,double k, double LAI){
		this.s_max=s_max;
		this.S_i=S_i;
		this.k=k;
		this.LAI=LAI;
	}

	public double ETcoefficient() {
		double SCF=1-Math.exp(-k*LAI);
		
		AETcoefficient=S_i/s_max*(1-SCF);
		return AETcoefficient;
	}




}
