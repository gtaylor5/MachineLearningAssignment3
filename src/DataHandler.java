import java.util.ArrayList;
import java.util.HashMap;
import java.util.*;
import java.io.*;

/************************************************
 * 
 * @author Gerard Taylor 10/6/2016
 * 
 * This class is used to pre-process the data sets.
 * For this particular assignment, we are 
 * implementing 5 - Fold Cross Validation
 * 
 * The 4 data sets that are used are below:
 * 
 * ecoli: https://archive.ics.uci.edu/ml/machine-learning-databases/ecoli/
 * 
 * image segmentation: https://archive.ics.uci.edu/ml/machine-learning-databases/image/
 * 
 * cpu performance: https://archive.ics.uci.edu/ml/machine-learning-databases/cpu-performance/
 * 
 * forest fires: https://archive.ics.uci.edu/ml/machine-learning-databases/forest-fires/
 *
 ***********************************************/

public class DataHandler {
	
	String[] unProcessedFiles = {"ecoli.data.txt", "machine.data.txt", "segmentation.data.txt", "forestfires.csv"};
	String[] dataSets = {"ecoli","machine","segmentation", "forest"};
	ArrayList<String[]> fileAsArray = new ArrayList<String[]>();
	HashMap<String, Integer> classCounts = new HashMap<String, Integer>();
	String dataSetName = "";
	String filePath = "";
	int lineLength = 0;
	int classIndex = 0;
	
	/**************************************************************
	 * Constructor
	 *************************************************************/
	
	public DataHandler(String name, String filePath) {
		this.dataSetName = name;
		this.filePath = filePath;
		File file = new File(filePath);
		Scanner fileScanner;
		try {
			fileScanner = new Scanner(file);
			if(fileScanner.hasNextLine()){
				String[] line = fileScanner.nextLine().split(",");
				this.lineLength = line.length;
			}
			fileScanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		switch(this.dataSetName){
			case "ecoli":
			case "forest":
			case "segmentation":
				classIndex = lineLength-1;
				break;
			case "machine":
				classIndex = lineLength-2;
		}
	}
	
	/**************************************************************
	 * Process Data Task
	 *************************************************************/
	public static void main(String[] args) {
		DataHandler handler = new DataHandler("machine", "Data/machine.data.txt");
		handler.processData();
	}
	public void processData(){
		try {
			storeFileAsArray();
			countClasses();
			splitData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**************************************************************
	 * Parse file and store in arraylist as string array.
	 *************************************************************/
	public void storeFileAsArray() throws FileNotFoundException{
		File file = new File(this.filePath);
		Scanner fileScanner = new Scanner(file);
		int j = 0;
		while(fileScanner.hasNextLine()){
			if(dataSetName.equalsIgnoreCase("ecoli")){ // space separated file
				String[] line = fileScanner.nextLine().split("\\s+");
				String[] modified = new String[line.length-1];
				this.classIndex = modified.length-1;
				for(int i = 1; i < line.length; i++){
					modified[i-1] = line[i];
				}
				fileAsArray.add(modified);
			}else{
				String[] line = fileScanner.nextLine().split(",");
				if(dataSetName.equalsIgnoreCase("segmentation")){
					line = swap(0,line);
					fileAsArray.add(line);
				}else if(dataSetName.equalsIgnoreCase("machine")){
					Double value = Double.parseDouble(line[classIndex]);
					if(value <= 20){
						line[classIndex] = "1";
					}else if(value <= 100){
						line[classIndex] = "2";
					}else if(value <= 200){
						line[classIndex] = "3";
					}else if(value <= 300){
						line[classIndex] = "4";
					}else if(value <= 400){
						line[classIndex] = "5";
					}else if(value <= 500){
						line[classIndex] = "6";
					}else if(value <= 600){
						line[classIndex] = "7";
					}else{
						line[classIndex] = "8";
					}
					line = swap(classIndex, line);
					String[] modified = new String[line.length-2];
					for(int i = 2; i < line.length; i++){
						modified[i-2] = line[i];
					}
					fileAsArray.add(modified);
				}else{
					line = encodeDate(line);
					fileAsArray.add(line);
				}
			}
			if(j == 0){
				lineLength = fileAsArray.get(0).length;
				if(dataSetName.equalsIgnoreCase("forest")){
					fileAsArray.remove(0);
				}
				j++;
			}
		}
		if(dataSetName.equalsIgnoreCase("machine")){
			classIndex = lineLength-1;
		}
		fileScanner.close();
		fileAsArray.trimToSize();
		//printArrayList(fileAsArray);
	}
	
	/**************************************************************
	 * Count frequency of classes.
	 * -machine.data has the class at index 8 and the regression 
	 * guess is at index 9.
	 * -segmentation.data has the classification at index 0;
	 * -ecoli.data and forestfires.csv have classes at the last 
	 * index.
	 *************************************************************/
	
	public void countClasses(){
		//handle special cases.
		for(int i = 0; i < fileAsArray.size(); i++){
			if(classCounts.containsKey(fileAsArray.get(i)[classIndex])){ // hashmap contains class?
				int currentVal = classCounts.get(fileAsArray.get(i)[classIndex]); // get current count
				currentVal++; // increment count by 1
				classCounts.put(fileAsArray.get(i)[classIndex], currentVal); //update map.
			}else{
				classCounts.put(fileAsArray.get(i)[classIndex], 1); // add class to map
			}
		}
		printClassCounts(); // prints class counts.
	}
	
	/**************************************************************
	 * Split data into 5 different sets.
	 * Keep class representation percentages.
	 * @throws IOException 
	 *************************************************************/
	
	public void splitData() throws IOException{
		for(int i = 0; i < 5; i++){ // number of files
			ArrayList<String[]> temp = new ArrayList<String[]>(); // file as Array
			Iterator<Map.Entry<String, Integer>> it = classCounts.entrySet().iterator();
			while(it.hasNext()){
				Map.Entry<String, Integer> pair = it.next();
				int classCount = pair.getValue(); // overall class count
				int j = 0;
				int dataPointsNeeded = 0;
				if(i == 4){ // fill last set with rest of data.
					dataPointsNeeded = classCount/5 + classCount%5; // get left over values
					while(j < fileAsArray.size()){
						temp.add(fileAsArray.get(j));
						j++;
					}
					fileAsArray.removeAll(fileAsArray);
					break;
				}
				if(classCount >= 5){
					dataPointsNeeded = classCount/5;
					while(j < fileAsArray.size() && dataPointsNeeded > 0){
						if(pair.getKey().equalsIgnoreCase(fileAsArray.get(j)[classIndex])){
							temp.add(fileAsArray.get(j));
							fileAsArray.remove(j);
							dataPointsNeeded--;
						}
						j++;
					}
				}else if(classCount < 5){//distribute as much as possible.
					dataPointsNeeded = 1;
					while(j < fileAsArray.size() && dataPointsNeeded > 0){
						if(pair.getKey().equalsIgnoreCase(fileAsArray.get(j)[classIndex])){
							temp.add(fileAsArray.get(j));
							fileAsArray.remove(j);
							dataPointsNeeded--;
						}
						j++;
					}
				}
			}
		writeToFile(i, temp); //write set to file. see below.
		}
	}
	
	/**************************************************************
	 * Helper method to print the number of occurrences of all the
	 * classes.
	 *************************************************************/
	
	public void printClassCounts(){ 
		Iterator<Map.Entry<String, Integer>> it = classCounts.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Integer> pair = (Map.Entry<String,Integer>) it.next();
			System.out.println(pair.getKey() + " = " + pair.getValue());
		}
	}
	
	
	/**************************************************************
	 * Writes data to file.
	 *************************************************************/
	
	public void writeToFile(int fileNum, ArrayList<String[]> array) throws IOException{
		String path = "Data/"+dataSetName+"/Set"+(fileNum+1)+".txt";
		File file = new File(path);
		file.getParentFile().mkdirs();
		file.createNewFile();
		PrintWriter writer = new PrintWriter(new FileWriter(file, true));
		for(int i = 0; i < array.size(); i++){
			for(int j = 0; j < array.get(i).length; j++){
				writer.print(array.get(i)[j]+ " ");
			}
			writer.println();
		}
		writer.close();
	}
	
	/**************************************************************
	 * Helper method to print the data sets. Use for test purposes.
	 *************************************************************/
	
	public void printArrayList(ArrayList<String[]> array){
		for(int i = 0; i < array.size(); i++){
			for(int j = 0; j < array.get(i).length; j++){
				System.out.print(array.get(i)[j] + " ");
			}
			System.out.println();
		}
	}
	
	/**************************************************************
	 * Helper method to print the contents of a string array.
	 *************************************************************/
	
	public void printArray(String[] array){
		for(String val : array){
			System.out.print(val + " ");
		}
		System.out.println();
	}
	
	public String[] encodeDate(String[] array){
		String[] mon = {"jan","feb", "mar", "apr", "may", "jun","jul","aug","sep","oct","nov", "dec"};
		String [] day = {"mon", "tue","wed","thu","fri", "sat", "sun"};
		for(int i = 0; i < mon.length; i++){
			if(array[2].equalsIgnoreCase(mon[i])){
				array[2] = Integer.toString(i);
			}
		}
		for(int i = 0; i < day.length; i++){
			if(array[3].equalsIgnoreCase(day[i])){
				array[3] = Integer.toString(i);
			}
		}
		return array;
	}
	
	/**************************************************************
	 * Method used to relocate class value to end of array.
	 *************************************************************/
	
	public String[] swap(int start, String[] array){
		for(int j = start; j < array.length-1; j++){
			String temp = array[j];
			array[j] = array[j+1];
			array[j+1] = temp;
		}
		return array;
	}
}
