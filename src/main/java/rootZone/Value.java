package rootZone;

public class Value implements AlphaModel{
	
	double alpha;

	
	public Value(double alpha){
		this.alpha=alpha;
		
	}


	@Override
	public double alphaValues() {
		// TODO Auto-generated method stub
		return alpha;
	}

}
