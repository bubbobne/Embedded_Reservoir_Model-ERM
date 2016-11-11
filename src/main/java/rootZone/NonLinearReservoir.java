package rootZone;

public class NonLinearReservoir implements UpTakeModel{
	
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
