package rootZone;

public class SimpleAlphaModelFactory {

	public static AlphaModel createModel(String type,double pB, double S_max, double alpha, double Pval, double S_rz){
		AlphaModel model=null;
		if (type.equals("Hymod")){
			model=new Hymod(S_rz, Pval, S_max,pB);
		}else if (type.equals("Value")){
			model=new Value(alpha);
		}
			
		return model;
		
	}
}
