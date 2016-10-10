import java.io.File;
import java.io.IOException;
import java.util.*;
/**************************************************************************
 * 
 * 
 * Regression implementation of a RBF Neural Network.
 * 
 * 
**************************************************************************/

public class RBFNN {
	
	ArrayList<Neuron> neurons = new ArrayList<Neuron>();
	ArrayList<String[]> fileAsArray = new ArrayList<String[]>();
	ArrayList<String[]> testFile = new ArrayList<String[]>();
	ArrayList<Double> actual = new ArrayList<Double>();
	ArrayList<Double> predicted = new ArrayList<Double>();
	String[] paths;
	
	double networkOutput = 0;
	double error = Double.MAX_VALUE;
	double eta = .001; // learning rate
	
	/**************************************************************************
	Initialize Neurons with random 10% of training set.
	**************************************************************************/
	
	public void setNeurons(){
		for(int i = 0; i < fileAsArray.size()*.1; i++){
			int random = (int)Math.random()*fileAsArray.size();
			neurons.add(new Neuron(convertData(fileAsArray.get(random))));
			fileAsArray.remove(random);
		}
		initializeWeights();
	}
	
	/**************************************************************************
	Calculates all of the basis outputs for each neuron given a queryPoint
	**************************************************************************/
	
	public void calculateBasisOutputs(String[] queryPoint){
		actual.add(Double.parseDouble(queryPoint[queryPoint.length-1])); // actual value.
		for(Neuron n : neurons){
			n.RBF(convertData(queryPoint));
		}
	}

	/**************************************************************************
	Updates weights using linear function to approximate the error gradient
	**************************************************************************/
	
	public void updateWeights(String[] queryPoint){
		for(Neuron n : neurons){
			double gradError = (Double.parseDouble(queryPoint[queryPoint.length-1])-networkOutput)*n.basisOutput;
			n.weights.set(0, n.weights.get(0) + eta*gradError);
		}
	}
	
	/**************************************************************************
	Linear combination of the weights and basis function outputs.
	**************************************************************************/
	
	public void calculateNetworkOutput(){
		double value = 0;
		for(Neuron n : neurons){
			value += (n.weights.get(0)*n.basisOutput);//one weight for regression problem
		}
		networkOutput = value;
		predicted.add(networkOutput);
	}
	
	/**************************************************************************
	Calculate overall error by acquiring real values and estimated values and
	averaging the errors between them.
	**************************************************************************/
	
	public void calculateOverallError(){
		Double error = 0.0;
		for(int i = 0; i < actual.size(); i++){
			double val = Math.abs((predicted.get(i)-actual.get(i)));
			error+=val;
		}
		this.error = Math.abs(error/actual.size());
		actual.removeAll(actual);
		predicted.removeAll(predicted);
	}

	
	/**************************************************************************
	Initializes weights randomly.
	**************************************************************************/
	public void initializeWeights(){
		for(Neuron n : neurons){
			n.weights.add(Math.random());
		}
	}

	/**************************************************************************
	 convert string array to double array.
	**************************************************************************/
	
	/**************************************************************************
	 * 
	 * 
	 * Helper Methods
	 * 
	 * 
	**************************************************************************/
	
	/**************************************************************************
	 Convert string array to double array ignoring the classification value.
	**************************************************************************/
	
	public Double[] convertData(String[] array){
		Double[] vals = new Double[array.length-1]; // ignore last element in the array (class).
		for(int i = 0; i < vals.length; i++){
			vals[i] = Double.parseDouble(array[i]);
		}
		return vals;
	}
	
	/**************************************************************************
	 Fill train file. Ignores index that is going to be used for testing.
	**************************************************************************/
	
	public void fillFile(String[] filePaths, int indexToSkip) throws IOException{
		for(int i = 0; i < filePaths.length; i++){
			if(i == indexToSkip){
				continue;
			}
			Scanner fileScanner = new Scanner(new File(filePaths[i]));
			while(fileScanner.hasNextLine()){
				fileAsArray.add(fileScanner.nextLine().split(" "));
			}
			fileScanner.close();
		}
	}

	/**************************************************************************
	 Print a string array.
	**************************************************************************/
	
	public void printArray(String[] array){
		for(int i = 0; i < array.length; i++){
			System.out.print(array[i] + " ");
		}
		System.out.println();
	}
	
	/**************************************************************************
	 Fill test file with skipped index.
	**************************************************************************/
	
	public void fillTestFile(String path) throws IOException{
		Scanner fileScanner = new Scanner(new File(path));
		while(fileScanner.hasNextLine()){
			testFile.add(fileScanner.nextLine().split(" "));
		}
		fileScanner.close();
	}
	
	/**************************************************************************
	Print weights.
	**************************************************************************/
	public void printWeights(){
		for(Neuron n : neurons){
			System.out.print(n.weights.get(0) + " ");
		}
		System.out.println();
	}
	
}
