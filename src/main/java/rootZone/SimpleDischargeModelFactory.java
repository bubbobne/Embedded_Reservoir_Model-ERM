package rootZone;

public class SimpleDischargeModelFactory {

	public static UpTakeModel createModel(String type,double a, double S_i, double b){
		UpTakeModel model=null;
		if (type.equals("NonLinearReservoir")){
			model=new NonLinearReservoir(a,S_i,b);
		}else if (type.equals("Clapp-H")){
			model=new Clapp(a,S_i,b);
		}
			
		return model;
		
	}
}
