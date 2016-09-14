package rootZone;

import org.apache.commons.math3.analysis.UnivariateFunction;

public class equation implements  UnivariateFunction {
	double zeta;
	double eta;
	double psiB;
	double beta;
	double zeta_rz;
	double J;
	double ET;
	double dt;
	double S_max;

    public equation(double zeta, double zeta_rz,double eta, double psiB, double beta,double J, double dt,
    		double ET) {
    	this.zeta=zeta;
    	this.zeta_rz=zeta_rz;
		this.eta=eta;
		this.psiB=psiB;
		this.beta=beta;
		this.dt=dt;
		this.J=J;
		this.ET=ET;
    }



	@Override
	public double value(double x) {
		// TODO Auto-generated method stub
		return Math.pow(psiB/(zeta-x),beta)+x-Math.pow(psiB/(zeta-eta),beta)
				-eta-dt*(J-ET);
	}
}
