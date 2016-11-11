package runoff;

public class SimpleETModelFactory {

	public static ETModel createModel(String type,double S_i, double s_max){
		ETModel model=null;
		if (type.equals("AET")){
			model=new AETmodel(S_i,s_max);
		}
			
		return model;
		
	}
}
