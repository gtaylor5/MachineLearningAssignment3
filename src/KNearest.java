
import java.util.*;
import java.io.*;

public class KNearest {
	
	String[] dataSets = {"Data/ecoli","machine","segmentation", "forest"};
	ArrayList<String[]> fileAsArray = new ArrayList<String[]>();
	String regOrClass = "";
	int bestK = 0;
	
	/**************************************************************
	 * Constructor
	 *************************************************************/
	
	public KNearest(String dataType){
		this.regOrClass = dataType;
	}
	
	
	/**************************************************************************
	 * 
	 * 
	 * K-NEAREST NEIGHBOR (REGRESSION)
	 * 
	 * 
	**************************************************************************/
	
	/**************************************************************************
	Finds the best k value based on lowest MSE.
	**************************************************************************/
	public void chooseBestKRegression(String path) throws IOException{
		int k = 1;
		double minError = Double.MAX_VALUE;
		while(true){
			Double currentError = knearestRegression(path,k);
			if(currentError < minError){
				minError = currentError;
				k++;
			}else{
				k--;
				break;
			}
		}
		this.bestK = k;
	}
	
	/**************************************************************************
	K-nearest regression algorithm. Finds the estimated value and compares it
	to the actual value. Then the MSE is returned.
	**************************************************************************/
	
	public double knearestRegression(String path, int k) throws IOException{
		Scanner fileScanner = new Scanner(new File(path));
		ArrayList<Double> estimatedValue = new ArrayList<Double>();
		ArrayList<Double> actualValue = new ArrayList<Double>();
		while(fileScanner.hasNextLine()){
			String[] line = fileScanner.nextLine().split(" ");
			Data data = new Data(line, Double.parseDouble(line[line.length-1])); // Special data type for ease of use. See Data.java
			data.dataAsDouble = convertData(data.data); //converts string data to double data.
			estimatedValue.add(findKNearest(k, data)); //returns estimated value based on average of nearest neighbor values.
			actualValue.add(data.actualVal);
		}
		fileScanner.close();
		return MSE(estimatedValue, actualValue);
	}
	
	/**************************************************************************
	findKNearest finds the k closest points to a querypoint. This function 
	returns the average of all of the values of the neighbors.
	**************************************************************************/
	
	public double findKNearest(int k, Data queryPoint){
		ArrayList<Data> data = new ArrayList<Data>(); // arraylist of data points.
		//fill data arraylist.
		for(int i = 0; i < fileAsArray.size(); i++){
			data.add(new Data(fileAsArray.get(i), Double.parseDouble(fileAsArray.get(i)[fileAsArray.get(i).length-1])));
			data.get(i).dataAsDouble = convertData(data.get(i).data);
			data.get(i).distance = euclidDistance(data.get(i).dataAsDouble, queryPoint.dataAsDouble);//set distances.
		}
		Collections.sort(data);//sort data based on distances.
		double sum = 0.0;
		for(int i = 0; i < k; i++){
			sum+=data.get(i).actualVal;
		}
		sum /= k; //average value.
		return sum;
	}
	
	/**************************************************************************
	MSE finds the mean squared error between the predicited and observed values
	of the test file.
	**************************************************************************/
	
	public Double MSE(ArrayList<Double> prediction, ArrayList<Double> observed){
		Double value = 0.0;
		int i = 0;
		while(i < prediction.size()){
			value = value + Math.pow(prediction.get(i)-observed.get(i),2);
			i++;
		}
		return value/i;
	}

	/**************************************************************************
	 * 
	 * 
	 * K-NEAREST NEIGHBOR (CLASSIFICATION)
	 * 
	 * 
	**************************************************************************/

	/**************************************************************************
	 Test k-nearest classification. This method returns the performance based
	 on training the learner on 80% of the data and testing to 20% of the data.
	 This can be used in conjunction with 5-fold cross validation.
	**************************************************************************/
	
	public double testPerformanceForClassification(String path, int k) throws IOException{
		Scanner fileScanner = new Scanner(new File(path)); // file scanner.
		double performance = 0; // initialize performance
		int fileSize = 0; //file size counter for calculating average.
		while(fileScanner.hasNextLine()){
			fileSize++;
			String[] line = fileScanner.nextLine().split(" ");
			String currentClass = line[line.length-1]; // get the current class value.
			Double[] data = convertData(line); // convert string data to double data.
			HashMap<String, Integer> results = getKNearestForClassification(k, data); // get classification counts for nearest neighbors (See below)
			Iterator<Map.Entry<String, Integer>> it = results.entrySet().iterator(); // iterate through hashmap
			int max = Integer.MIN_VALUE; //initialize max value
			String classification = "";
			/*This loop returns the class that is most common among the nearest neighbors. */
			while(it.hasNext()){
				Map.Entry<String, Integer> pair = it.next();
				if(pair.getValue() > max){
					max = pair.getValue();
					classification = pair.getKey();
				}
			}
			
			if(classification.equalsIgnoreCase(currentClass)){
				performance++;//if they match increcment performance.
			}
		}
		fileScanner.close(); //close scanner.
		return (performance/fileSize); //return classification performance.
	}
	
	/**************************************************************************
	 This version of k-nearest neighbor removes the entries that are classified
	 incorrectly.
	**************************************************************************/
	
	public double condensedKNearestNeighbor(String path, int k, int i) throws IOException{
		Scanner fileScanner = new Scanner(new File(path)); // file scanner.
		ArrayList<String[]> array = new ArrayList<String[]>();
		double performance = 0; // initialize performance
		int fileSize = 0; //file size counter for calculating average.
		while(fileScanner.hasNextLine()){
			fileSize++;
			String[] line = fileScanner.nextLine().split(" ");
			String currentClass = line[line.length-1]; // get the current class value.
			Double[] data = convertData(line); // convert string data to double data.
			HashMap<String, Integer> results = getKNearestForClassification(k, data); // get classification counts for nearest neighbors (See below)
			Iterator<Map.Entry<String, Integer>> it = results.entrySet().iterator(); // iterate through hashmap
			int max = Integer.MIN_VALUE; //initialize max value
			String classification = "";
			/*This loop returns the class that is most common among the nearest neighbors. */
			while(it.hasNext()){
				Map.Entry<String, Integer> pair = it.next();
				if(pair.getValue() > max){
					max = pair.getValue();
					classification = pair.getKey();
				}
			}
			
			if(classification.equalsIgnoreCase(currentClass)){
				performance++;//if they match increcment performance.
				array.add(line);
			}
		}
		fileScanner.close(); //close scanner.
		writeFile(array, path, i);
		return (performance/fileSize); //return classification performance.
	}
	

	/**************************************************************************
	 getKNearestForClassification finds the k closest neighbors for a query 
	 point. It returns a hashmap of the class value and the class count.
	**************************************************************************/
	
	public HashMap<String, Integer> getKNearestForClassification(int k, Double[] queryPoint){
		HashSet<Integer> indexesVisited = new HashSet<Integer>();//keep track of indexes visited.
		HashMap<String, Integer> classCounts = new HashMap<String, Integer>(); //class values and occurrences.
		int j = 0; //iterator value.
		/*This loop finds the k closest points and adds them to the hashmap accordingly*/
		while(j < k){
			Double minDistance = Double.MAX_VALUE;
			int minIndex = 0;
			for(int i = 0; i < fileAsArray.size(); i++){
				if(indexesVisited.contains(i)){
					continue;
				}
				Double distance = 0.0;
				Double[] data = convertData(fileAsArray.get(i)); // convert string data to double data
				distance = euclidDistance(queryPoint,data); //calculate euclidean distance.
				if(distance < minDistance){
					minDistance = distance;
					minIndex = i;
				}
			}
			j++;
			if(classCounts.containsKey(fileAsArray.get(minIndex)[fileAsArray.get(minIndex).length-1])){ // hashmap contains class?
				int currentVal = classCounts.get(fileAsArray.get(minIndex)[fileAsArray.get(minIndex).length-1]); // get current count
				currentVal++; // increment count by 1
				classCounts.put(fileAsArray.get(minIndex)[fileAsArray.get(minIndex).length-1], currentVal); //update map.
			}else{
				classCounts.put(fileAsArray.get(minIndex)[fileAsArray.get(minIndex).length-1], 1); // add class to map
			}
			indexesVisited.add(minIndex);//skip index if visited.
		}
		return classCounts;
	}
	
	/**************************************************************************
	Finds the best k value based on highest performance.
	**************************************************************************/
	
	public void chooseBestKClassification(String path) throws IOException{
		int k = 1;
		double maxPerformance = Double.MIN_VALUE;
		while(true){
			Double currentPerformance = testPerformanceForClassification(path,k);
			if(currentPerformance > maxPerformance){
				maxPerformance = currentPerformance;
				k++;
			}else{
				k--;
				break;
			}
		}
		this.bestK = k;
	}
	
	/**************************************************************************
	 * 
	 * 
	 * Helper Methods.
	 * 
	 * 
	**************************************************************************/
	
	/**************************************************************************
	 Fills fileAsArray with 80% of training data. Ignores one index which is used 
	 for test data.
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
	 convert string array to double array.
	**************************************************************************/
	
	public Double[] convertData(String[] array){
		Double[] vals = new Double[array.length-1]; // ignore last element in the array (class).
		for(int i = 0; i < vals.length; i++){
			vals[i] = Double.parseDouble(array[i]);
		}
		return vals;
	}
	
	/**************************************************************************
	 calculate euclidean distance between two vectors.
	**************************************************************************/
	
	public Double euclidDistance(Double[] x, Double[] m){
		Double dist = 0.0;
		for(int i = 0; i < x.length; i++){
			dist += Math.pow((x[i]-m[i]),2);
		}
		return Math.sqrt(dist);
	}
	
	/**************************************************************************
	 Array Printing method.
	**************************************************************************/
	
	public void printArray(String[] array){
		for(int i = 0; i<array.length; i++){
			System.out.print(array[i] + " ");
		}
	}
	
	/**************************************************************************
	Writes condensed modified files to separate directory.
	**************************************************************************/
	
	public void writeFile(ArrayList<String[]> array, String path,int index) throws IOException{
		if(path.split("/")[1].equalsIgnoreCase("ecoli")){
			path = "Data/ecoliCondensed/CondensedSet"+(index+1)+".txt";
		}else{
			path = "Data/segmentationCondensed/CondensedSet"+(index+1)+".txt";
		}
		File file = new File(path);
		file.getParentFile().mkdirs();
		file.createNewFile();
		PrintWriter writer = new PrintWriter(new FileWriter(path,false));
		for(int i = 0; i < array.size(); i++){
			for(int j = 0; j < array.get(i).length; j++){
				writer.print(array.get(i)[j] + " ");
			}
			writer.println();
		}
		writer.close();
	}
	
}
