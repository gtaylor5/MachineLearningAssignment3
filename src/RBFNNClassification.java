import java.util.*;
import java.io.*;

/**************************************************************************
 * 
 * The implementation of a classification version of an RBFNN is almost
 * identical to the regression version. However, the only difference is
 * that there needs to be an output for each class. Additionally, each 
 * output has their own weights otherwise this will not work. Also, the 
 * classes need to be one-hot encoded in order for this to work as well.
 * 
**************************************************************************/

public class RBFNNClassification{
	
	ArrayList<Neuron> neurons = new ArrayList<Neuron>();
	ArrayList<String[]> fileAsArray = new ArrayList<String[]>();
	ArrayList<String[]> testFile = new ArrayList<String[]>();
	ArrayList<Double> actual = new ArrayList<Double>();
	ArrayList<Double> predicted = new ArrayList<Double>();
	ArrayList<String> classes = new ArrayList<String>();
	Double[] outputVector;
	Double[] logisticOutput;
	Double[] u;
	String[] paths;
	String dataSetName = "";
	

	double error = Double.MAX_VALUE;
	double eta = .1; // learning rate
	double performance = 0;
	
	/**************************************************************************
	Constructor
	**************************************************************************/
	
	public RBFNNClassification(String path, String name) throws FileNotFoundException{
		this.dataSetName = name;
		Scanner fileScanner = new Scanner(new File(path));
		while(fileScanner.hasNextLine()){
			String[] line = fileScanner.nextLine().split(" " );
			if(!classes.contains(line[line.length-1])){
				classes.add(line[line.length-1]);
			}
		}
		outputVector = new Double[classes.size()];
		logisticOutput = new Double[classes.size()];
		u = new Double[classes.size()];
		fileScanner.close();
	}
	
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
		for(Neuron n : neurons){
			n.RBF(convertData(queryPoint));
		}
	}
	
	/**************************************************************************
	Variation of updating weights for multi-class.
	**************************************************************************/
	
	public void updateWeights(String[] queryPoint){
		int j = 0;
		//get index where classification is located.
		while(j < classes.size()){
			if(!classes.get(j).equalsIgnoreCase(queryPoint[queryPoint.length-1])){
				j++;
			}
			break;
		}
		for(int i = 0; i < classes.size(); i++){
			for(Neuron n : neurons){
				double gradError = (j-u[i])*n.basisOutput*u[i]*(1-u[i]); // calculate error with classification encoding.
				n.weights.set(i,n.weights.get(i) + eta*gradError); // update weights.
			}
		}
	}
	
	/**************************************************************************
	Puts outputVector through the activation function to get a logistic output.
	Also, performance is incremented conditionally here.
	**************************************************************************/
	
	public void activate(String[] queryPoint){
		for(int i = 0; i < classes.size(); i++){
			u[i] = 1/(1+Math.exp((-1.0)*outputVector[i])); // generate logistic output for each class.
		}
		Double max = Double.MIN_VALUE;
		int index = 0;
		//find index where the highest networkOutput value occurs.
		for(int i = 0; i < u.length; i++){
			if(outputVector[i] > max){
				max = outputVector[i];
				index = i;
			}
		}
		if(!classes.get(index).equalsIgnoreCase(queryPoint[queryPoint.length-1])){ //if this is equal to the query points expected value increment performance
			performance++;
		}
	}
	
	/**************************************************************************
	Same as before but for all classes.
	**************************************************************************/
	
	public void calculateNetworkOutput(String[] queryPoint){
		for(int i = 0; i < classes.size(); i++){
			double val = 0;
			for(Neuron n : neurons){
				val += n.weights.get(i)*n.basisOutput;
			}
			outputVector[i] = val;	
		}
	}
	
	/**************************************************************************
	Same as before but for all classes.
	**************************************************************************/
	
	
	public void initializeWeights(){
		for(int i = 0; i < classes.size(); i++){
			for(Neuron n : neurons){
				n.weights.add(Math.random()/2);
			}
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
	
	
	public Double[] convertData(String[] array){
		Double[] vals = new Double[array.length-1]; // ignore last element in the array (class).
		for(int i = 0; i < vals.length; i++){
			vals[i] = Double.parseDouble(array[i]);
		}
		return vals;
	}
	
	
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
	

	public void printArray(String[] array){
		for(int i = 0; i < array.length; i++){
			System.out.print(array[i] + " ");
		}
		System.out.println();
	}
	
	
	public void fillTestFile(String path) throws IOException{
		Scanner fileScanner = new Scanner(new File(path));
		while(fileScanner.hasNextLine()){
			testFile.add(fileScanner.nextLine().split(" "));
		}
		fileScanner.close();
	}
	

	public void printWeights(){
		for(int i = 0; i < classes.size(); i++){
			for(Neuron n : neurons){
				System.out.print(n.weights.get(i));
			}
			System.out.println();
		}
	}
	
}
