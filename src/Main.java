import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

/**************************************************************************
Runner class.
**************************************************************************/


public class Main {
	
	static File results = new File("Results.txt");
	String[] dataSets = {"forest", "machine", "segmentation","ecoli"};
	public static void main(String[] args) throws IOException {
		Main main = new Main();
		for(int i = 0; i < main.dataSets.length; i++){
			String[] setPaths = new String[5];
			String[] condensedPaths = new String[5];
			for(int j = 0; j < setPaths.length; j++){
				setPaths[j] = "Data/"+main.dataSets[i]+"/Set"+(j+1)+".txt";
				if(i==2 || i==3){
					condensedPaths[j] = "Data/"+main.dataSets[i]+"Condensed/CondensedSet"+(j+1)+".txt";
				}
			}
			if(i==2 || i==3){
				main.runCondensedKNN(main.dataSets[i], setPaths, condensedPaths);
				main.runKnearestClassification(main.dataSets[i], setPaths);
				if(i == 2){
					main.runKMeansClassification(main.dataSets[i], setPaths, 7);
					main.runRBFNNClassification(main.dataSets[i], setPaths);
				}else{
					main.runKMeansClassification(main.dataSets[i], setPaths, 5);
					main.runRBFNNClassification(main.dataSets[i], setPaths);
				}
			}else{
				main.runKnearestRegression(main.dataSets[i], setPaths);
				main.runKMeansRegression(main.dataSets[i], setPaths);
				main.runRBFNNKMeansRegression(main.dataSets[i],setPaths);
			}
		}
	}
	
	/***************************************************************************************************************
	These methods are adapted from main methods that existed in their respective classes. They all print out the 
	performance of all of the algorithms.
	***************************************************************************************************************/

	
	public void runCondensedKNN(String dataSet, String[] setPaths, String[] condensedPaths) throws IOException{
		PrintWriter writer = new PrintWriter(new FileWriter(Main.results,true));
		writer.println("Testing Condensed K-NN: " + dataSet);
		int k = 0;
		double performance = 0;
		double currentPerformance = 1.0;
		while(currentPerformance > performance){
			performance = currentPerformance;
			currentPerformance = 0;
			k++;
			for(int a = 0; a < 5; a++){
				KNearest test = new KNearest(" ");
				test.fillFile(setPaths, a);
				double val = 0;
				if(k == 1){
					val = test.condensedKNearestNeighbor(setPaths[a], k, a);
				}else{
					val = test.condensedKNearestNeighbor(condensedPaths[a], k, a);
				}
				currentPerformance += val;
			}
			currentPerformance /= 5;
			currentPerformance*=100;
		}
		for(int a = 0; a < 5; a++){
			KNearest test = new KNearest(" ");
			test.fillFile(setPaths, a);
			double val = test.testPerformanceForClassification(condensedPaths[a], k);
			writer.println("Cross Validation Performance : "+ val*100);
		}
		writer.println();
		writer.close();
	}

	public void runKnearestClassification(String dataSet, String[] setPaths) throws IOException{
		PrintWriter writer = new PrintWriter(new FileWriter(Main.results,true));
		writer.println("Testing K-NN for Classification: " + dataSet);
		int k = 0;
		double performance = 0;
		double currentPerformance = 1.0;
		while(currentPerformance > performance){
			performance = currentPerformance;
			currentPerformance = 0;
			k++;
			for(int a = 0; a < 5; a++){
				KNearest test = new KNearest(" ");
				test.fillFile(setPaths, a);
				double val = 0;
				val = test.testPerformanceForClassification(setPaths[a], k);
				currentPerformance += val;
			}
			currentPerformance /= 5;
			currentPerformance*=100;
		}
		for(int a = 0; a < 5; a++){
			KNearest test = new KNearest(" ");
			test.fillFile(setPaths, a);
			double val = test.testPerformanceForClassification(setPaths[a], k);
			writer.println("Cross Validation Performance : "+ val*100);
		}
		writer.println();
		writer.close();
	}
	
	public void runKnearestRegression(String dataSet, String[] setPaths) throws IOException{
		PrintWriter writer = new PrintWriter(new FileWriter(Main.results,true));
		writer.println("Testing K-NN Regression: " + dataSet);
		double performance = 0;
		double currentPerformance = 1.0;
		while(currentPerformance > performance){
			performance = currentPerformance;
			currentPerformance = 0;
			for(int a = 0; a < 5; a++){
				KNearest test = new KNearest(" ");
				test.fillFile(setPaths, a);
				test.chooseBestKRegression(setPaths[a]);
				double val = 0;
				val = test.knearestRegression(setPaths[a], test.bestK);
				currentPerformance += val;
			}
			currentPerformance /= 5;
		}
		for(int a = 0; a < 5; a++){
			KNearest test = new KNearest(" ");
			test.fillFile(setPaths, a);
			test.chooseBestKRegression(setPaths[a]);
			double val = test.knearestRegression(setPaths[a], test.bestK);
			writer.println("Cross Validation Performance (MSE) : "+ val);
		}
		writer.println();
		writer.close();
	}

	public void runKMeansClassification(String dataSet, String[] setPaths, int k) throws IOException{
		PrintWriter writer = new PrintWriter(new FileWriter(Main.results,true));
		writer.println("KMeans K-NN classification: " + dataSet);
		for(int i = 0; i < 5; i++){
			KMeans test = new KMeans(setPaths,k);
			test.initializeClusters();
			test.generateCentroids(i);
			Scanner fileScanner = new Scanner(new File(setPaths[i]));
			int fileLength = 0;
			double performance = 0;
			while(fileScanner.hasNextLine()){
				fileLength++;
				String[] line = fileScanner.nextLine().split(" ");
				Double[] data = test.convertData(line);
				String classification = test.classify(data);
				if(classification.equalsIgnoreCase(line[line.length-1])){
					performance++;
				}
			}
			fileScanner.close();
			writer.println("Cross Validation Performance : " + (performance/fileLength)*100);
		}
		writer.println();
		writer.close();
	}
	
	public void runKMeansRegression(String dataSet, String[] setPaths) throws IOException{
		PrintWriter writer = new PrintWriter(new FileWriter(Main.results,true));
		writer.println("KMeans K-NN Regression: " + dataSet);
		for(int i = 0; i < 5; i++){
			ArrayList<Double> estimatedValue = new ArrayList<Double>();
			ArrayList<Double> actualValue = new ArrayList<Double>();
			KMeansRegression test = new KMeansRegression(setPaths, 50);
			test.initializeClusters(i);
			test.generateCentroids();
			Scanner fileScanner = new Scanner(new File(setPaths[i]));
			while(fileScanner.hasNextLine()){
				String[] line = fileScanner.nextLine().split(" ");
				actualValue.add(Double.parseDouble(line[line.length-1]));
				Double[] data = test.convertData(line);
				Double value = test.regress(data);
				estimatedValue.add(value);
			}
			fileScanner.close();
			writer.println("Cross Validation Performance (MSE): " + MSE(estimatedValue, actualValue));
		}
		writer.println();
		writer.close();
	}
	
	public void runRBFNNRegression(String dataSet, String[] setPaths) throws IOException{
		PrintWriter writer = new PrintWriter(new FileWriter(Main.results,true));
		writer.println("RBFNN Regression: " + dataSet);
		for(int i = 0; i < 5; i++){
			RBFNN network = new RBFNN();
			network.paths = setPaths;
			network.fillFile(setPaths, i);
			network.setNeurons();
			while(network.error > .025){
				for(int j = 0; j < network.fileAsArray.size(); j++){
					String[] line = network.fileAsArray.get(j);
					network.calculateBasisOutputs(line);
					network.calculateNetworkOutput();
					network.updateWeights(line);
				}
				network.calculateOverallError();
			}
			network.fillTestFile(setPaths[i]);
			for(String[] line : network.testFile){
				network.calculateBasisOutputs(line);
				network.calculateNetworkOutput();
			}
			network.calculateOverallError();
			writer.println("Cross Validation Performance : "+ (1.0-network.error)*100);
		}
		writer.println();
		writer.close();
	}
	
	public void runRBFNNClassification(String dataSet, String[] setPaths) throws IOException{
		PrintWriter writer = new PrintWriter(new FileWriter(Main.results,true));
		writer.println("Testing RBFNN Classification: " + dataSet);
		for(int i = 0; i < 5; i++){
			RBFNNClassification network = new RBFNNClassification(setPaths[i],"dataSet");
			network.paths = setPaths;
			network.fillFile(setPaths, i);
			network.setNeurons();
			while(network.performance <.75){
				network.performance = 0;
				for(int j = 0; j < network.fileAsArray.size(); j++){
					String[] line = network.fileAsArray.get(j);
					network.calculateBasisOutputs(line);
					network.calculateNetworkOutput(line);
					network.activate(line);
					network.updateWeights(line);
				}
				network.performance = network.performance/network.fileAsArray.size();
			}
			network.performance = 0;
			network.fillTestFile(setPaths[i]);
			for(String[] line : network.testFile){
				network.calculateBasisOutputs(line);
				network.calculateNetworkOutput(line);
				network.activate(line);
			}
			writer.println("Cross Validation Performance : "+network.performance/network.testFile.size()*100);
		}
		writer.println();
		writer.close();
	}
	
	public Double MSE(ArrayList<Double> prediction, ArrayList<Double> observed){

		Double value = 0.0;
		int i = 0;
		while(i < prediction.size()){
			value = value + Math.pow(prediction.get(i)-observed.get(i),2);
			i++;
		}
		return value/i;
	}
	
	public void runRBFNNKMeansRegression(String dataSet, String[] setPaths) throws IOException{
		PrintWriter writer = new PrintWriter(new FileWriter(Main.results,true));
		writer.println("Testing K-Means RBFNN Regression: " + dataSet);
		for(int i = 0; i < 5; i++){
			KMeansRegression test = new KMeansRegression(setPaths, 50);
			test.initializeClusters(i);
			test.generateCentroids();
			RBFNN network = new RBFNN();
			network.paths = setPaths;
			network.fillFile(setPaths, i);
			Iterator<Map.Entry<String, Double[]>> it = test.centroids.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String, Double[]> pair = it.next();
				network.neurons.add(new Neuron(pair.getValue()));
			}
			network.initializeWeights();
			while(network.error > .05){
				for(int j = 0; j < network.fileAsArray.size(); j++){
					String[] line = network.fileAsArray.get(j);
					network.calculateBasisOutputs(line);
					network.calculateNetworkOutput();
					network.updateWeights(line);
				}
				network.calculateOverallError();
			}
			network.fillTestFile(setPaths[i]);
			for(String[] line : network.testFile){
				network.calculateBasisOutputs(line);
				network.calculateNetworkOutput();
			}
			network.calculateOverallError();
			writer.println("Cross Validation Performance : "+ (1.0-network.error)*100);
		}
		writer.println();
		writer.close();
	}
	
	public void runRBFNNKMeansClassification(String dataSet, String[] setPaths, int k) throws IOException{
		PrintWriter writer = new PrintWriter(new FileWriter(Main.results,true));
		writer.println("KMeans K-NN classification: " + dataSet);
		for(int i = 0; i < 5; i++){
			RBFNNClassification network = new RBFNNClassification(setPaths[i],"dataSet");
			network.paths = setPaths;
			network.fillFile(setPaths, i);
			KMeans test = new KMeans(setPaths,k);
			test.initializeClusters();
			test.generateCentroids(i);
			Iterator<Map.Entry<String, Double[]>> it = test.centroids.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String, Double[]> pair = it.next();
				network.neurons.add(new Neuron(pair.getValue()));
			}
			network.initializeWeights();
			while(network.performance <.75){
				network.performance = 0;
				for(int j = 0; j < network.fileAsArray.size(); j++){
					String[] line = network.fileAsArray.get(j);
					network.calculateBasisOutputs(line);
					network.calculateNetworkOutput(line);
					network.activate(line);
					network.updateWeights(line);
				}
				network.performance = network.performance/network.fileAsArray.size();
			}
			network.performance = 0;
			network.fillTestFile(setPaths[i]);
			for(String[] line : network.testFile){
				network.calculateBasisOutputs(line);
				network.calculateNetworkOutput(line);
				network.activate(line);
			}
			writer.println("Cross Validation Performance : "+network.performance/network.testFile.size()*100);
		}
		writer.println();
		writer.close();
	}
	
}
