package canopy;

public class AETmodel implements ETModel{
	
	public static double s_max;
	public static double S_i;
	public static double AETcoefficient;

	
	public AETmodel(double S_i, double s_max){
		this.s_max=s_max;
		this.S_i=S_i;
	}

	public double ETcoefficient() {
		AETcoefficient=S_i/s_max;
		return AETcoefficient;
	}



}
