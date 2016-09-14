package rootZone;

public class SimpleWRCModelFactory {

	public static SwrcModel createModel(String type, double zeta, double eta, double psiB, double beta){
		SwrcModel model=null;
		if (type.equals("Brooks&Corey")){
			model=new BeC(zeta, eta, psiB, beta);
		}else if (type.equals("VanGenuchten")){
			model=new VG();
		}
			
		return model;
		
	}
}
