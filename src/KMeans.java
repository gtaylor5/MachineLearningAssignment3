import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

//Number of clusters & means are equal to the number of classes

/**************************************************************************
The Kmeans class is a class that executes a supervised version of the
KMeans clustering algorithm.
**************************************************************************/

public class KMeans {

	/**************************************************************************
	The DataConfig class is used to hold the configurations of the dataSets for
	easy iteration through the algorithms. See implementation in Main.class
	**************************************************************************/
	
	ArrayList<Double[]> means; // Average values of each cluster.
	ArrayList<Double[]> newMeans; // means that are calculated after each iteration of the respective algorithms.
	HashMap<String,ArrayList<String[]>> clusters; // set of clusters. Set to the amount of classes in each data set.
	HashMap<String, Double[]> centroids;
	String[] files;
	int dataLength = 0; // i.e number of features.
	int k = 0; // number of classes
	int val = 0;
	
	/**************************************************************************
	KMeans constructor. Sets attribute values above.
	**************************************************************************/
	
	public KMeans(String[] filePaths, int k) throws FileNotFoundException{
		files = filePaths;
		Scanner fileScanner = new Scanner(new File(this.files[0]));
		if(fileScanner.hasNextLine()){
			String[] line = fileScanner.nextLine().split(" ");
			this.dataLength = line.length;
		}
		means = new ArrayList<Double[]>(k);
		newMeans = new ArrayList<Double[]>(k);
		clusters = new HashMap<String,ArrayList<String[]>>(k); // clusters matches # classes.
		centroids = new HashMap<String, Double[]>(k);
		this.k = k; // set k.
		fileScanner.close(); // close file scanner.
	}
	
	/**************************************************************
	 * Takes test data and classifies it based on relative location
	 * to a cluster.
	 *************************************************************/
	
	public String classify(Double[] xQ){
		Iterator<Entry<String, Double[]>> it = centroids.entrySet().iterator();
		double minDistance = Double.MAX_VALUE;
		String classification = "";
		while(it.hasNext()){
			Entry<String, Double[]> pair = it.next();
			double distance = euclidDistance(xQ, pair.getValue());
			if(distance < minDistance){
				minDistance = distance;
				classification = pair.getKey();
			}
		}
		return classification; // return classification of closest cluster.
	}
	
	/**************************************************************************
	 generateMeans generates the means for each cluster until the means don't 
	 change.
	**************************************************************************/
	
	public void generateCentroids(int indexToSkip) throws FileNotFoundException{
		assignClusters(indexToSkip); //skips index to facilitate 5-fold validation.
		setCentroids();
	}
	
	/**************************************************************************
	 Iterate through each cluster and each double array and each element in 
	 the double array and generate averages for each attribute in each cluster.
	 Only one attribute is averaged at a time. (i.e top down).
	**************************************************************************/
	
	public void setCentroids(){
		Iterator<Map.Entry<String, ArrayList<String[]>>> it = clusters.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, ArrayList<String[]>> pair = it.next();
			ArrayList<String[]> temp = pair.getValue();
			Double[] centroid = new Double[dataLength-1];
			Arrays.fill(centroid, 0.0);
			for(int i = 0; i < temp.size();i++){
				for(int j = 0; j < temp.get(i).length-1; j++){
					Double[] tempDouble = convertData(temp.get(i));
					centroid[j] += tempDouble[j];
				}
			}
			for(int i = 0; i < centroid.length; i++){
				centroid[i] /= temp.size();
			}
			centroids.replace(pair.getKey(), centroid);
		}
	}
	
	/**************************************************************************
	 Assigns a data instance to the proper cluster based on the classification.
	 * @throws FileNotFoundException 
	**************************************************************************/
	
	public void assignClusters(int indexToSkip) throws FileNotFoundException{
		for(int i = 0; i < files.length; i++){
			if(i == indexToSkip){
				continue;
			}
			Scanner fileScanner = new Scanner(new File(files[i]));
			while(fileScanner.hasNextLine()){
				String[] line = fileScanner.nextLine().split(" ");
				ArrayList<String[]> temp = clusters.get(line[line.length-1]);
				temp.add(line);
				clusters.replace(line[line.length-1], temp);
			}
			fileScanner.close();
		}
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
	 Initialize means. pick a data instance from each class as the cluster mean.
	**************************************************************************/
	
	public void initializeClusters() throws FileNotFoundException{
		Scanner fileScanner = new Scanner(new File(files[0]));
		String currentClass = "";
		if(fileScanner.hasNextLine()){
			currentClass = fileScanner.nextLine().split(" ")[dataLength-1];
			clusters.put(currentClass, new ArrayList<String[]>());
			centroids.put(currentClass, new Double[dataLength]);
		}
		while(fileScanner.hasNextLine()){
			String val = fileScanner.nextLine().split(" ")[dataLength-1];
			if(!clusters.containsKey(val)){
				clusters.put(val, new ArrayList<String[]>());
				centroids.put(val, new Double[dataLength]);
			}
		}
		fileScanner.close();
	}
	
	/**************************************************************************
	 Prints a string array as space separated values.
	**************************************************************************/
	
	public void printArray(String[] array){
		for(int i = 0; i < array.length; i++){
			System.out.print(array[i] + " ");
		}
		System.out.println();
	}
	
	/**************************************************************************
	 Prints a double array as space separated values.
	**************************************************************************/
	
	public void printArray(Double[] array){
		for(int i = 0; i < array.length; i++){
			System.out.print(array[i] + " ");
		}
		System.out.println();
	}
	
	/**************************************************************************
	 Prints all data instances in a cluster.
	**************************************************************************/
	
	public void printClusters(){
		for(int i = 0; i < clusters.size(); i++){
			System.out.println();
			System.out.println("Cluster: " + (i+1));
			System.out.println();
			for(int j = 0; j < clusters.get(i).size(); j++){
				for(int f = 0; f < clusters.get(i).get(j).length; f++){
					System.out.print(clusters.get(i).get(j)[f]+ " ");
				}
				System.out.println();
			}
		}
	}
	
	/**************************************************************************
	Prints cluster sizes.
	**************************************************************************/
	
	public void printClusters2(){
		for(int i = 0; i < clusters.size(); i++){
			System.out.println();
			System.out.println("Cluster: " + (i+1));
			System.out.println();
			System.out.println(clusters.get(i).size());
		}
	}
	
	/**************************************************************************
	 returns average value of a double array.
	**************************************************************************/
	
	public Double getAverage(Double[] array){
		double sum = 0;
		for(int i = 0; i < array.length; i++){
			sum+=array[i];
		}
		return sum/array.length;
	}
	
}
