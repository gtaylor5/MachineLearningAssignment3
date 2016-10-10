import java.util.ArrayList;

/**************************************************************************
 * 
 * This class serves as a framework for the Neurons (hidden layer) of the
 * RBF Neural Network.
 * 
 * 
**************************************************************************/

public class Neuron {
	
	ArrayList<Double> weights = new ArrayList<Double>();
	Double[] center;
	double sigma = 1.5;
	double basisOutput;
	
	public Neuron(Double[] center){
		this.center = center;
	}
	
	/**************************************************************************
	 * Calculate basis function given a query point.
	**************************************************************************/

	public void RBF(Double[] queryPoint){
		double dotProd = euclidNorm(queryPoint);
		double sigmaSquared = sigma*sigma;
		double fractionTerm = (-1.0)/(2.0*sigmaSquared);
		double dotProdXFractionTerm = fractionTerm*dotProd;
		double exponentiate = Math.exp(dotProdXFractionTerm);
		basisOutput = exponentiate;
	}
	
	/**************************************************************************
	 * 
	 * 
	 * Helper Methods.
	 * 
	 * 
	**************************************************************************/
	
	public Double euclidNorm(Double[] x){
		Double dist = 0.0;
		for(int i = 0; i < x.length; i++){
			dist += Math.pow((x[i]-center[i]),2);
		}
		return dist;
	}
	
	public Double dot(Double[] queryPoint){
		double sum = 0;
		for(int i = 0; i < queryPoint.length; i++){
			sum+= (queryPoint[i]*center[i]);
		}
		return sum;
	}

}
