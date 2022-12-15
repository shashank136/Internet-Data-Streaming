import java.util.*;
import java.io.*;

abstract class XHashTable{
	// insert inserts a flowID into a xHashTable
	abstract void insert(int flowID);
	// hashFunction uses Java in-build hashCode method to generate the hashed Index for flowID XOR random number
	public int hashFunction(int flowId, int randomNum){
		return Integer.valueOf(flowId^randomNum).hashCode();
	}
	// getTableEntries prints number of entries in hash table and
	// prints value of hash table as <flowID, index> to a output file.
	public void getTableEntries(String fileName, int[][] hashTable, int numTableEntries, int numberEntries) throws IOException{
		FileWriter output = new FileWriter(fileName+".txt");
		try(BufferedWriter writer = new BufferedWriter(output)) {
	        writer.write("*************************");
	        writer.newLine();
	        writer.write(fileName+" Report");
	        writer.newLine();
	        writer.write("*************************");
	        writer.newLine();
	        writer.write("Number of flowID in "+ fileName+" : "+numberEntries);
	        writer.newLine();
	        writer.newLine();
	        writer.write("flowID   index");
	        writer.newLine();
	        for(int i=0; i<numTableEntries; i++){
				writer.write(hashTable[i][0]+"   "+i);
				writer.newLine();
			}
	    }
	}
}

class DLeftHashTable extends XHashTable{
	public int[][] dLeftHashTable; // cukooHashTable[n][2] - stores flow id and counter
	private int[] kHashFunction; // kHashFunction[k] - random number generated for k hashfunctions
	private int numSegments;
	private Random rand;
	private int numFlows;
	public int numTableEntries;
	private int base;
	public int numberEntries;

	DLeftHashTable(int numTableEntries, int numFlows, int numSegments){
		this.numFlows = numFlows;
		this.numSegments = numSegments;
		this.numTableEntries = numTableEntries;
		this.dLeftHashTable = new int[numTableEntries][2];
		this.kHashFunction = new int[numSegments];
		this.base = (int)numTableEntries/numSegments;
		rand = new Random();
		setUpkHashFunction();
	}

	public void setUpkHashFunction(){
		// set up kHashFunction with random numbers  
		for(int i=0; i<kHashFunction.length; i++){
			kHashFunction[i] = rand.nextInt(10000); // fill with a random number
		}
	}

	public void insert(int flowID){

		for(int i=0; i<numSegments; i++){
			int randNum = kHashFunction[i];
			// taking the index as per the segment size and adding segment size multiplier
			// to get index in other segments to the right.
			int index = hashFunction(flowID, randNum)%base + (i*base);
			// check if the index position is empty
			if(dLeftHashTable[index][0]==0){
				dLeftHashTable[index][0] = flowID;
				dLeftHashTable[index][1]++;
				numberEntries++;
				break;
			}else if(dLeftHashTable[index][0]==flowID){
				// flowID is already registered
				dLeftHashTable[index][1]++;
				numberEntries++;
				break;
			}
		}

		// flowID cannot be accomodated in any of the available segments.
	}
}

class CukooHashTable extends XHashTable{
	public int[][] cukooHashTable; // cukooHashTable[n][2] - stores flow id and counter
	private int[] kHashFunction; // kHashFunction[k] - random number generated for k hashfunctions
	private int numFlows;
	private Random rand;
	private int cukooSteps;
	public int numberEntries;
	public int numTableEntries;

	public CukooHashTable(int numTableEntries, int numFlows, int numHashes, int cukooSteps){
		this.numTableEntries = numTableEntries;
		this.cukooHashTable = new int[numTableEntries][2]; 
		this.kHashFunction = new int[numHashes];
		this.numFlows = numFlows;
		this.cukooSteps = cukooSteps;
		rand = new Random();
		setUpkHashFunction();
	}

	public void setUpkHashFunction(){
		// set up kHashFunction with random numbers  
		for(int i=0; i<kHashFunction.length; i++){
			kHashFunction[i] = rand.nextInt(10000); // fill with a random number
		}
	}

	public void insert(int flowID){

		// check if the flowID is already recorded
		for(int randNum: kHashFunction){
			int index = hashFunction(flowID, randNum)%numTableEntries;
			if(cukooHashTable[index][0]==flowID){
				cukooHashTable[index][1]++;
				return;
			}
		}

		// check if we can insert in an empty slot 
		for(int randNum: kHashFunction){
			int index = hashFunction(flowID, randNum)%numTableEntries;

			if(cukooHashTable[index][0]==0){
				// found a slot for current flowID
				cukooHashTable[index][0] = flowID;
				cukooHashTable[index][1]++;
				numberEntries++;
				return;
			}
		}

		// check if we can move any one of the occupied slot flowID to other positions
		for(int randNum: kHashFunction){
			int index = hashFunction(flowID, randNum)%numTableEntries;
			int curFlowID = cukooHashTable[index][0];
			if(move(index,cukooSteps)){
				// curFlowID was moved
				cukooHashTable[index][0] = flowID;
				cukooHashTable[index][1]++;
				numberEntries++;
				return;
			}
		}

		// flowID cannot be inserted into the cukoo Hash table
	}

	public boolean move(int pos, int steps){
		if(steps<=0) return false;
		int flowID = cukooHashTable[pos][0];
		int counter = cukooHashTable[pos][1];

		// check for empty position for curFLowID
		for(int randNum: kHashFunction){
			int index = hashFunction(flowID, randNum)%numTableEntries;

			if(cukooHashTable[index][0]!=flowID && cukooHashTable[index][0]==0){
				cukooHashTable[index][0] = flowID;
				cukooHashTable[index][1] = counter;
				return true;
			}
		}

		// check if the flowID can be accomodated in next cukoo step
		for(int randNum: kHashFunction){
			int index = hashFunction(flowID, randNum)%numTableEntries;

			if(cukooHashTable[index][0]!=flowID && move(index, steps-1)){
				cukooHashTable[index][0] = flowID;
				cukooHashTable[index][1] = counter;
				return true;
			}
		}

		return false;
	}
}

class MultiHashTables extends XHashTable {
	public int[][] multiHashTable; // multiHashTable[n][2] - stores flow id and counter
	private int[] kHashFunction; // kHashFunction[k] - random number generated for k hashfunctions
	private int numFlows;
	private Random rand;
	public int numberEntries;
	public int numTableEntries;

	public MultiHashTables(int numTableEntries, int numFlows, int numHashes){
		this.numTableEntries = numTableEntries;
		this.multiHashTable = new int[numTableEntries][2]; 
		this.kHashFunction = new int[numHashes];
		this.numFlows = numFlows;
		rand = new Random();
		setUpkHashFunction();
	}

	public void setUpkHashFunction(){
		// set up kHashFunction with random numbers  
		for(int i=0; i<kHashFunction.length; i++){
			kHashFunction[i] = rand.nextInt(10000); // fill with a random number
		}
	}

	public void insert(int flowID){
		for(int randNum: kHashFunction){
			int index = hashFunction(flowID, randNum)%numTableEntries;
			// check if the flowID is already present
			if(multiHashTable[index][0]==flowID){
				multiHashTable[index][1]++; // increment the counter
				break;
			}else if(multiHashTable[index][0]==0){
				// insert flowID
				multiHashTable[index][0] = flowID;
				multiHashTable[index][1]++;
				numberEntries++;
				break;
			}
		}
		// no index found where we can insert the flowID.
		// skip the insertion for such flowIDs
	}
}

public class XHashTables{
	public static void main(String[] args) throws IOException{
		// int numTableEntries = 1000, numFlows = 1000, numHashes = 3;
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Enter number of entries in Hashtable: ");
		int numTableEntries = sc.nextInt();
		
		System.out.println("Enter number of Flows: ");
		int numFlows = sc.nextInt();
		
		System.out.println("Enter number of numHashes: ");
		int numHashes = sc.nextInt();

		System.out.println("Enter number of Cukoo steps: ");
		int cukooSteps = sc.nextInt();

		System.out.println("Enter number of segments: ");
		int numSegments = sc.nextInt();
		
		MultiHashTables multiHashTable = new MultiHashTables(numTableEntries, numFlows, numHashes);
		CukooHashTable cukooHashTable = new CukooHashTable(numTableEntries, numFlows, numHashes, cukooSteps);
		DLeftHashTable dLeftHashTable = new DLeftHashTable(numTableEntries, numFlows, numSegments);
		Random rand = new Random();

		while(numFlows-->0){
			int flowId = rand.nextInt(100000);
			multiHashTable.insert(flowId);
			cukooHashTable.insert(flowId);
			dLeftHashTable.insert(flowId);
		}

		multiHashTable.getTableEntries("multiHashTable", multiHashTable.multiHashTable,
										numTableEntries, multiHashTable.numberEntries);
		cukooHashTable.getTableEntries("cukooHashTable", cukooHashTable.cukooHashTable, 
										numTableEntries, cukooHashTable.numberEntries);
		dLeftHashTable.getTableEntries("dLeftHashTable", dLeftHashTable.dLeftHashTable, 
										numTableEntries, dLeftHashTable.numberEntries);
	}
}