package canopy;

public class SimpleETModelFactory {

	public static ETModel createModel(String type,double S_i, double s_max, double k, double LAI){
		ETModel model=null;
		if (type.equals("AET")){
			model=new AETmodel(S_i,s_max);
		}else if (type.equals("LAI")){
			model=new LAImodel(S_i,s_max,k,LAI);
		}
			
		return model;
		
	}
}
