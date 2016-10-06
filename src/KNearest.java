
import java.util.*;
import java.io.*;

public class KNearest {
	
	String[] dataSets = {"Data/ecoli","machine","segmentation", "forest"};
	ArrayList<String[]> fileAsArray = new ArrayList<String[]>();
	String regOrClass = "";
	
	public static void main(String[] args) throws IOException {
		String[] setPaths = new String[5];
		for(int i = 0; i < setPaths.length; i++){
			setPaths[i] = "Data/machine/Set"+(i+1)+".txt";
		}
		for(int k = 1; k < 11; k++){
			Double performance = 0.0;
			for(int i = 0; i < 5; i++){
				KNearest test = new KNearest("regression");
				test.resetFile();
				test.fillFile(setPaths, i);
				double tempPerformance = test.testPerformance(setPaths[i], k);
				System.out.println("Testing Set " + (i+1) + " : " + tempPerformance);
				performance+=tempPerformance;
			}
			System.out.println("K = " + k +" : "+100*(performance/5));
		}
	}
	
	public KNearest(String dataType){
		this.regOrClass = dataType;
	}
	
	public double testPerformance(String path, int k) throws IOException{
		Scanner fileScanner = new Scanner(new File(path));
		double performance = 0;
		int fileSize = 0;
		while(fileScanner.hasNextLine()){
			fileSize++;
			String[] line = fileScanner.nextLine().split(" ");
			String currentClass = line[line.length-1];
			Double[] data = convertData(line);
			HashMap<String, Integer> results = getKNearest(k, data);
			Iterator<Map.Entry<String, Integer>> it = results.entrySet().iterator();
			int max = Integer.MIN_VALUE;
			String classification = "";
			double sum = 0;
			if(regOrClass.equals("regression")){
				while(it.hasNext()){
					Map.Entry<String, Integer> pair = it.next();
					int val = Integer.parseInt(pair.getKey());
					val*=pair.getValue();
					sum+=val;
				}
				sum/=results.size();
				System.out.println(sum);
				classification = Integer.toString((int)Math.round(sum));
				System.out.println(classification + " : " + currentClass);
				if(classification.equalsIgnoreCase(currentClass)){
					performance++;
				}
				continue;
			}
			while(it.hasNext()){
				Map.Entry<String, Integer> pair = it.next();
				if(pair.getValue() > max){
					classification = pair.getKey();
				}
			}
			if(classification.equalsIgnoreCase(currentClass)){
				performance++;
			}
		}
		fileScanner.close();
		return (performance/(double)fileSize);
	}
	
	public HashMap<String, Integer> getKNearest(int k, Double[] queryPoint){
		HashSet<Integer> indexesVisited = new HashSet<Integer>();
		HashMap<String, Integer> classCounts = new HashMap<String, Integer>();
		int j = 0;
		while(j < k){
			Double minDistance = Double.MAX_VALUE;
			int minIndex = 0;
			for(int i = 0; i < fileAsArray.size(); i++){
				if(indexesVisited.contains(i)){
					continue;
				}
				Double distance = 0.0;
				Double[] data = convertData(fileAsArray.get(i));
				distance = euclidDistance(queryPoint,data);
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
			indexesVisited.add(minIndex);
		}
		return classCounts;
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
	
	public void resetFile(){
		fileAsArray.removeAll(fileAsArray);
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
	
}
