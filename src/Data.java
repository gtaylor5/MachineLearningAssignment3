
/**************************************************************************
This class is a helper class used to help with sorting the data based on 
location. Primarily used in k-NN.
**************************************************************************/

public class Data implements Comparable<Data>{
	
	String[] data;
	Double[] dataAsDouble;
	Double distance = 0.0;
	Double actualVal = 0.0;
	
	public Data(String[] data, Double val){
		this.data = data;
		this.actualVal = val;
	}

	@Override
	public int compareTo(Data arg0) {
		return Double.compare(this.distance, arg0.distance);
	}

}
