package rootZone;

public class Hymod implements AlphaModel{
	
	double pB;
	double S_max;
	double Pval;
	double S_rz;

	
	public Hymod(double S_rz, double Pval, double S_max,double pB){
		this.pB=pB;
		this.S_max=S_max;
		this.Pval=Pval;
		this.S_rz=S_rz;
	}

	@Override
	public double alphaValues() {
 		double pCmax=S_max *(pB+1);
 		double coeff1 = ((1.0 - ((pB + 1.0) * (S_rz) / pCmax)));
 		double exp = 1.0 / (pB + 1.0);
 		double ct_prev = pCmax * (1.0 - Math.pow(coeff1, exp));
 		double UT1 = Math.max((Pval - pCmax + ct_prev), 0.0);     
 		return UT1/Pval;
	}



}
