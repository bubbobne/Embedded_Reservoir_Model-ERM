package rungekutta;

import static org.hortonmachine.gears.libs.modules.HMConstants.isNovalue;

/**
 * Extension for root zone.
 * 
 * 
 * @author Giuseppe Formetta, Daniele Andreis
 *
 */
public class RootZoneRungeKutta extends RungeKutta {
	double sRootZoneMax;
	double pBSoil;
	double rain;
	double coeff;
	double exponent;

	public RootZoneRungeKutta(double coeff, double exponent, double sRootZoneMax, double pBSoil) {
		this.sRootZoneMax = sRootZoneMax;
		this.pBSoil = pBSoil;
		this.coeff = coeff;
		this.exponent = exponent;
	}

	@Override
	protected int getOutDimension() {
		// TODO Auto-generated method stub
		return 6;
	}

	public double[] run(double storageStart, double in, double out, double dt) {
		this.rain = in;
		return super.run(storageStart, out, dt);
	}

	// compute dS/dt
	public double[] computeFunction(double Sn, double etpnet) {
		if (Sn < 0) {
			Sn = 0;
		}
		double alpha = alpha(Sn, rain);
		double[] o = actualInputs(Sn, alpha);
		double actualInputs = o[0];
		double quick = o[1];
		double aet = computeAET(Sn, actualInputs, etpnet);
		double recharge = computeR(Sn, actualInputs, aet);
		double fun = actualInputs - aet - recharge;
		return new double[] { fun, actualInputs, recharge, aet, alpha, quick };
	}

	// compute alpha according to Hymod
	private double alpha(double Sn, double Pval) {
		double pCmax = sRootZoneMax * (pBSoil + 1);
		double coeff1 = 1.0 - ((pBSoil + 1.0) * (Sn) / pCmax);
		double exp = 1.0 / (pBSoil + 1.0);
		double ct_prev = pCmax * (1.0 - Math.pow(coeff1, exp));
		double UT1 = Math.max((Pval - pCmax + ct_prev), 0.0);
		double dummy = Math.min(((ct_prev + Pval - UT1) / pCmax), 1.0);
		double coeff2 = (1.0 - dummy);
		double exp2 = (pBSoil + 1.0);
		double xn = (pCmax / (pBSoil + 1.0)) * (1.0 - (Math.pow(coeff2, exp2)));
		double UT2 = Math.max(Pval - UT1 - (xn - Sn), 0);
		double alpha = (UT1 + UT2) / Pval;
		if (isNovalue(alpha) || alpha > 1)
			alpha = 1;
		return alpha;
	}

	// compute actual inputspublic
	private double[] actualInputs(double Sn, double alfa) {
		return new double[] { (1 - alfa) * rain, alfa * rain };
	}

	// compute groundwater recharge
	private double computeR(double Sn, double in, double et) {
		double out = coeff * Math.pow(Math.min(1, Sn / sRootZoneMax), exponent);
		out = Math.min(Sn + in - et, out + Math.max(0, Sn - sRootZoneMax + in - et - out));
		return out;

	}

	// compute AET
	private double computeAET(double Sn, double in, double etpnet) {
		return Math.min(Sn + in, etpnet * Math.min(1, 1.33 * Math.min(1, Sn / sRootZoneMax)));
	}

}
