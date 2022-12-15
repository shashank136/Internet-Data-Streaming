import java.util.*;
import java.io.*;

class Hasher{
	public int hashFunction(String flowId, int randomNum){
		return (flowId.hashCode()^randomNum)&0xfffffff; // preventing the overflow of hashcode beyond the int range
	}
}

class ActiveCounter extends Hasher{
	public int countTill = 1000000;
	public int cn=0, ce=0;
	public Random rand = new Random();

	public void startActiveCounter() throws IOException{
		while(countTill-->0){
			// generate a random number in the range of 0 to (2^ce)-1. 
			// Such, that probability of each number to show up is 1/(2^ce).
			int num = rand.nextInt((int)Math.pow(2, ce));
			if(num==0){ // increment cn only if the random num is 0. 
				cn++;
			}
			String binaryString = Integer.toBinaryString(cn);
			if(binaryString.length()>16){
				ce++;
				cn >>= 1;
			}
		}

		int counterState = cn*(int)(Math.pow(2, ce));
		FileWriter output = new FileWriter("ActiveCounter.txt");
		try(BufferedWriter writer = new BufferedWriter(output)){
			writer.write(""+counterState);
			writer.newLine();
		}
	}
}

class CounterSketch extends Hasher{
	public int n; // num of flows
	public int k; // num of counter arrays
	public int w; // num of counters in each array
	public int[][] counters;
	public int[] kHashFunction;
	public Random rand;
	public HashMap<String, Integer> actualValues;

	public CounterSketch(int n, int k, int w){
		this.n=n;
		this.k=k;
		this.w=w;
		this.counters = new int[k][w];
		this.kHashFunction = new int[k];
		rand = new Random();
		setUpkHashFunction();
	}

	public void setUpkHashFunction(){
		// set up kHashFunction with random numbers  
		for(int i=0; i<kHashFunction.length; i++){
			kHashFunction[i] = rand.nextInt(Integer.MAX_VALUE); // fill with a random number
		}
	}

	public void record(String flowId, int freq){
		for(int i=0; i<k; i++){
			int hash = hashFunction(flowId, kHashFunction[i]);
			int index = hash%w;
			if(((hash>>31)&1)==1)
				counters[i][index]+=freq;
			else
				counters[i][index]-=freq;
		}
	}

	public int query(String flowId){
		int[] ar = new int[k];
		for(int i=0; i<k; i++){
			int index = hashFunction(flowId, kHashFunction[i])%w;
			ar[i] = Math.abs(counters[i][index]);
		}
		Arrays.sort(ar);
		if(ar.length%2==0){
			return (int)(ar[ar.length/2]+ar[ar.length/2 +1])/2;
		}else{
			return ar[(int)ar.length/2];
		}
	}

	public int getError(String flowId){
		int expectedValue = query(flowId);
		return Math.abs(expectedValue-actualValues.get(flowId));
	}
}

class CountMin extends Hasher{
	public int n; // num of flows
	public int k; // num of counter arrays
	public int w; // num of counters in each array
	public int[][] counters;
	public int[] kHashFunction;
	public Random rand;
	public HashMap<String, Integer> actualValues;

	public CountMin(int n, int k, int w){
		this.n=n;
		this.k=k;
		this.w=w;
		this.counters = new int[k][w];
		this.kHashFunction = new int[k];
		rand = new Random();
		setUpkHashFunction();
	}

	public void setUpkHashFunction(){
		// set up kHashFunction with random numbers  
		for(int i=0; i<kHashFunction.length; i++){
			kHashFunction[i] = rand.nextInt(Integer.MAX_VALUE); // fill with a random number
		}
	}

	public void record(String flowId, int freq){
		for(int i=0; i<k; i++){
			int index = hashFunction(flowId, kHashFunction[i])%w;
			counters[i][index]+=freq;
		}
	}

	public int query(String flowId){
		int countMin = Integer.MAX_VALUE;
		for(int i=0; i<k; i++){
			int index = hashFunction(flowId, kHashFunction[i])%w;
			countMin = Math.min(counters[i][index], countMin);
		}
		return countMin;
	}

	public int getError(String flowId){
		int expectedValue = query(flowId);
		return Math.abs(expectedValue-actualValues.get(flowId));
	}
}

public class Counters{
	public static class query{
		String flowId;
		int actual;
		int expected;

		public query(String flowId, int actual, int expected){
			this.flowId=flowId;
			this.actual=actual;
			this.expected=expected;
		}
	}
	public static void main(String[] args) throws IOException{
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Enter the file name for input: ");
		String fileName = sc.nextLine(); // file name
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String str;
		int numFlows = -1;
		HashMap<String, Integer> map = new HashMap<>();
		while((str=br.readLine())!=null){
			if(numFlows==-1)
				numFlows = Integer.parseInt(str);
			else{
				String[] parts = str.split("\\s+");
				map.put(parts[0], Integer.parseInt(parts[1]));
			}
		}
		
		System.out.println("Enter no. of counter arrays");
		int counterArrays = sc.nextInt();
		System.out.println("Enter no. of counters");
		int counters = sc.nextInt();

		// ************* Count min **************
		FileWriter output = new FileWriter("CountMinOutput.txt");
		
		try(BufferedWriter writer = new BufferedWriter(output)){
			CountMin countMin = new CountMin(numFlows, counterArrays, counters);
			countMin.actualValues = map;

			// record
			for(String flowId: countMin.actualValues.keySet()){
				countMin.record(flowId, countMin.actualValues.get(flowId));
			}

			// query
			int error=0;
			PriorityQueue<query> queryResults = new PriorityQueue<>((a,b)->b.actual-a.actual); // flowId, [actual, expected]
			for(String flowId: countMin.actualValues.keySet()){
				int ans = countMin.query(flowId);
				queryResults.offer(new query(flowId, countMin.actualValues.get(flowId), ans));
				error += Math.abs(ans-countMin.actualValues.get(flowId));
			}

			double avgError = error/queryResults.size();

			writer.write("Avg. Error: "+avgError);
			writer.newLine();
			writer.write("Flow Id   Actual-Count	Expected-Count");
			writer.newLine();
			for(int i=0; i<100; i++){
				query q = queryResults.poll();
				writer.write(q.flowId+"	"+q.actual+"	"+q.expected);
				writer.newLine();
			}
		}

		// ************* Counter Sketch **************
		FileWriter output1 = new FileWriter("CounterSketchOutput.txt");

		try(BufferedWriter writer = new BufferedWriter(output1)){
			CounterSketch counterSketch = new CounterSketch(numFlows, counterArrays, counters);
			counterSketch.actualValues = map;

			for(String flowId: counterSketch.actualValues.keySet()){
				counterSketch.record(flowId, counterSketch.actualValues.get(flowId));
			}

			// query
			int error=0;
			PriorityQueue<query> queryResults = new PriorityQueue<>((a,b)->b.actual-a.actual); // flowId, [actual, expected]
			for(String flowId: counterSketch.actualValues.keySet()){
				int ans = counterSketch.query(flowId);
				queryResults.offer(new query(flowId, counterSketch.actualValues.get(flowId), ans));
				error += Math.abs(ans-counterSketch.actualValues.get(flowId));
			}

			double avgError1 = error/queryResults.size();

			writer.write("Avg. Error: "+avgError1);
			writer.newLine();
			writer.write("Flow Id   Actual-Count	Expected-Count");
			writer.newLine();
			for(int i=0; i<100; i++){
				query q = queryResults.poll();
				writer.write(q.flowId+"	"+q.actual+"	"+q.expected);
				writer.newLine();
			}
		}

		

		// ***************** Active Counter ******************
		ActiveCounter activeCounter = new ActiveCounter();
		activeCounter.startActiveCounter();
	}
}