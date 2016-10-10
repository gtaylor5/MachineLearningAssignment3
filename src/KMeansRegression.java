import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/************************************************
 * 
 * @author Gerard Taylor 10/9/2016
 * 
 *This class uses K-Means clustering to determine
 *real values given a feature set. Once the 
 *centroids are created, which ever centroid is 
 *closest to the current data point will be chosen
 *as the classifier. Then, the real value index
 *usually line[line.length-1] will be averaged
 *across the cluster and that average is the 
 *expected value. See the "regress" method below.
 *
 *This class inherits directly from KMeans. 
 *There are only a few differences. 
 *
 ***********************************************/

public class KMeansRegression extends KMeans{
	
	ArrayList<String[]> trainFile = new ArrayList<String[]>();

	public KMeansRegression(String[] filePaths, int k) throws FileNotFoundException {
		super(filePaths, k);
	}
	
	/**************************************************************************
	Copied from K-Means
	**************************************************************************/
	
	public void generateCentroids(){
		assignClusters();
	}
	
	/**************************************************************************
	 Similar to K-Means. 
	**************************************************************************/
	
	public void assignClusters(){
		for(String[] file : trainFile){
			Double[] fileAsDouble = convertData(file);
			Iterator<Map.Entry<String, Double[]>> it = this.centroids.entrySet().iterator();
			Double minDistance = Double.MAX_VALUE;
			String cluster = "";
			while(it.hasNext()){
				Map.Entry<String, Double[]> pair = it.next();
				if(this.euclidDistance(fileAsDouble, pair.getValue()) < minDistance){
					minDistance = this.euclidDistance(fileAsDouble, pair.getValue());
					cluster = pair.getKey();
				}
			}
			try{
				ArrayList<String[]> temp = clusters.get(cluster);
				temp.add(file);
				clusters.replace(cluster, temp);
			}catch(Exception e){
				
			}
			setCentroids();
		}
		setCentroids();
	}
	
	/**************************************************************************
	 Get's anticipated value by regressing and using KNN.
	**************************************************************************/
	
	public Double regress(Double[] xQ){
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
		
		ArrayList<String[]> cluster = clusters.get(classification);
		double sum = 0;
		for(String[] array : cluster){
			sum+=Double.parseDouble(array[array.length-1]);
		}
		
		sum /= cluster.size();
		
		return sum;
	}
	
	/**************************************************************************
	 Similar to K-Means. 
	**************************************************************************/
	
	public void setCentroids(){
		Iterator<Map.Entry<String, ArrayList<String[]>>> it = this.clusters.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, ArrayList<String[]>> pair = it.next();
			ArrayList<String[]> array = pair.getValue();
			Double[] meanVals = new Double[array.get(0).length-1];
			Arrays.fill(meanVals, 0.0);
			for(String[] arr : array){
				for(int i = 0; i < arr.length-1; i++){
					meanVals[i] += Double.parseDouble(arr[i]);
				}
			}
			for(Double val : meanVals){
				val/=array.size();
			}
			centroids.put(pair.getKey(), meanVals);
		}
	}
	
	/**************************************************************************
	 Similar to K-Means. 
	**************************************************************************/
	
	public void initializeClusters(int indexToSkip) throws IOException{
		for(int i = 0; i < this.files.length; i++){
			if(i == indexToSkip) continue;
				Scanner fileScanner = new Scanner(new File(this.files[i]));
				while(fileScanner.hasNextLine()){
					trainFile.add(fileScanner.nextLine().split(" "));
				}
				fileScanner.close();
				for(int j = 0; j < trainFile.size()*.1; j++){
					int random = (int)Math.random()*trainFile.size();
					ArrayList<String[]> cluster = new ArrayList<String[]>();
					cluster.add(trainFile.get(random));
					this.clusters.put(Integer.toString(j), cluster);
					trainFile.remove(random);
				}
				setCentroids();
		}
	}
	
	/**************************************************************************
	 * 
	 * 
	 * Helper Methods.
	 * 
	 * 
	**************************************************************************/
	
	public void printClusters(){
		Iterator<Map.Entry<String, ArrayList<String[]>>> it = this.clusters.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, ArrayList<String[]>> pair = it.next();
			ArrayList<String[]> array = pair.getValue();
			System.out.println("Cluster : " + pair.getKey());
			for(String[] arr : array){
				for(int i = 0; i < arr.length-1; i++){
					System.out.print(arr[i] +  " ");
				}
				System.out.println();
			}
		}
	}
	
	public Double euclidDistance(Double[] x, Double[] m){
		Double dist = 0.0;
		for(int i = 0; i < x.length; i++){
			dist += Math.pow((x[i]-m[i]),2);
		}
		return Math.sqrt(dist);
	}

}
