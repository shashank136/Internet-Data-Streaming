import java.util.*;
import java.io.*;

class XBloomFilter{
	public int hashFunction(int flowId, int randomNum){
		return Integer.valueOf(flowId^randomNum).hashCode();
	}
}

class CountingBloomFilter extends XBloomFilter{
	public int noOfElements;
	public int noOfElementsToRemoved;
	public int noOfElementsToAdded;
	public int noOfCounters;
	public int noOfHashes;
	public int[] countingBloomFilter;
	public int[] kHashFunction;
	public Random rand;

	public CountingBloomFilter(int noOfElements, int noOfElementsToRemoved, int noOfElementsToAdded, int noOfCounters, int noOfHashes){
		this.noOfElements = noOfElements;
		this.noOfElementsToRemoved = noOfElementsToRemoved;
		this.noOfElementsToAdded = noOfElementsToAdded;
		this.noOfCounters = noOfCounters;
		this.noOfHashes = noOfHashes;
		this.countingBloomFilter = new int[noOfCounters];
		this.kHashFunction = new int[noOfHashes];
		rand = new Random();
		setUpkHashFunction();
	}

	public void setUpkHashFunction(){
		// set up kHashFunction with random numbers  
		for(int i=0; i<kHashFunction.length; i++){
			kHashFunction[i] = rand.nextInt(10000); // fill with a random number
		}
	}

	public void encode(int element){
		for(int randNum: kHashFunction){
			int index = hashFunction(element, randNum)%noOfCounters;
			countingBloomFilter[index]++; // increment the counter at index 
		}
	}

	public void remove(int element){
		for(int randNum: kHashFunction){
			int index = hashFunction(element, randNum)%noOfCounters;
			if(countingBloomFilter[index]>0)
				countingBloomFilter[index]--; // decrement the counter at index
		}
	}

	public int lookUp(int element){
		int count=Integer.MAX_VALUE;
		for(int randNum: kHashFunction){
			int index = hashFunction(element, randNum)%noOfCounters;
			count = Math.min(count, countingBloomFilter[index]);
		}

		return count;
	}
}

class BloomFilter extends XBloomFilter{
	public int noOfElements;
	public int noOfBits;
	public int noOfHashes;
	public int[] bloomFilter;
	public Random rand;
	private int[] kHashFunction;

	public BloomFilter(int noOfElements, int noOfBits, int noOfHashes){
		this.noOfElements = noOfElements;
		this.noOfBits = noOfBits;
		this.noOfHashes = noOfHashes;
		this.bloomFilter = new int[noOfBits];
		this.kHashFunction = new int[noOfHashes];
		rand = new Random();
		setUpkHashFunction();
	}

	public void setUpkHashFunction(){
		// set up kHashFunction with random numbers  
		for(int i=0; i<kHashFunction.length; i++){
			kHashFunction[i] = rand.nextInt(10000); // fill with a random number
		}
	}

	// encode an element into the bloom filter
	public void encode(int element){
		for(int randNum: kHashFunction){
			int index = hashFunction(element, randNum)%noOfBits;
			bloomFilter[index] = 1; // set the bit at index as 1
		}
	}

	// lookup an element in the bloom filter
	public boolean lookUp(int element){
		for(int randNum: kHashFunction){
			int index = hashFunction(element, randNum)%noOfBits;
			if(bloomFilter[index] != 1)
				return false; // return false if the bit at index is 0
		}
		return true;
	}
}

class CodedBloomFilter extends XBloomFilter{
	public int numOfSets;
	public int numOfElementsSet;
	public int numOfFilters;
	public int numOfBits;
	public int numOfHashes;
	public int[][] codedBloomFilter;
	private int[] kHashFunction;
	public Random rand;

	public CodedBloomFilter(int numOfSets, int numOfElementsSet, int numOfFilters, int numOfBits, int numOfHashes){
		this.numOfSets = numOfSets;
		this.numOfElementsSet = numOfElementsSet;
		this.numOfFilters = numOfFilters;
		this.numOfBits = numOfBits;
		this.numOfHashes = numOfHashes;
		this.codedBloomFilter = new int[numOfFilters][numOfBits];
		this.kHashFunction = new int[numOfHashes];
		rand = new Random();
		setUpkHashFunction();
	}

	public void setUpkHashFunction(){
		// set up kHashFunction with random numbers  
		for(int i=0; i<kHashFunction.length; i++){
			kHashFunction[i] = rand.nextInt(Integer.MAX_VALUE); // fill with a random number
		}
	}

	public void encode(int segment, int element){
		// get the binary String for the String
		StringBuilder binaryString = new StringBuilder();
		for (int i = numOfFilters-1; i >= 0; i--) {
			int mask = 1 << i;
		    binaryString.append((segment & mask) != 0 ? "1" : "0");
		}

		for(int i=0; i<binaryString.length(); i++){
			if(binaryString.charAt(i)=='1'){ // get the bloom filter out of log(g+1) filters
				for(int randNum: kHashFunction){ // get hash function output
					int index = hashFunction(element, randNum)%numOfBits;
					codedBloomFilter[i][index] = 1; // set the bit at index as 1
				}
			}
		}
	}

	public boolean lookUp(int segment, int element){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<numOfFilters; i++){ // get the bloom filter
			boolean flag = true;
			for(int randNum: kHashFunction){ // check if the element is encoded to the given bloom filter
				int index = hashFunction(element, randNum)%numOfBits;
				if(codedBloomFilter[i][index] != 1){
					flag = false;
					break;
				}
			}
			if(flag) sb.append('1');
			else sb.append('0');
		}

		// Build segment code from Binary String
		int expectedSegmentCode = Integer.parseInt(sb.toString(), 2);
		return expectedSegmentCode==segment;
	}
}

public class XBloomFilters{
	public static void runBloomFilter(Scanner sc) throws IOException{
		Random rand = new Random();
		Set<Integer> setA = new HashSet<>();
		Set<Integer> setB = new HashSet<>();
		FileWriter output = new FileWriter("BloomFilter.txt");

		try(BufferedWriter writer = new BufferedWriter(output)){

			System.out.println("*************** Bloom Filter **************");
			// bloom filter
			System.out.println("Enter no. of elements in set:");
			int noOfElements = sc.nextInt();
			
			System.out.println("Enter no. of bits in bloom filter:");
			int noOfBits = sc.nextInt();
			
			System.out.println("Enter no. of hashes:");
			int noOfHashes = sc.nextInt();

			// instance of x-bloom filter
			BloomFilter bloomFilter = new BloomFilter(noOfElements, noOfBits, noOfHashes);

			int i=0;
			while(i<noOfElements){
				int element = rand.nextInt(Integer.MAX_VALUE);
				if(setA.add(element))
					i++;
			}

			// encode setA elements into x-bloom-filter
			for(int element: setA){
				bloomFilter.encode(element);
			}

			// lookup elements of setA in the BloomFilter
			int count = 0;
			for(int element: setA){
				if(bloomFilter.lookUp(element)){
					count++;
				}
			}

			// System.out.println
			writer.write("Number of elements of setA found successfully during lookup: "+count);
			writer.newLine();

			i=0;
			while(i<noOfElements){
				int element = rand.nextInt(Integer.MAX_VALUE);
				if(setB.add(element))
					i++;
			}

			count = 0;
			for(int element: setB){
				if(bloomFilter.lookUp(element)){
					count++;
				}
			}

			// System.out.println
			writer.write("Number of elements of setB found successfully during lookup: "+count);
			writer.newLine();
			// System.out.println();
		}
	}
	
	public static void runCountingBloomFilter(Scanner sc) throws IOException{
		Random rand = new Random();
		Set<Integer> setA = new HashSet<>();
		Set<Integer> setB = new HashSet<>();
		FileWriter output = new FileWriter("CountingBloomFilter.txt");

		try(BufferedWriter writer = new BufferedWriter(output)){
			System.out.println("*************** Counting Bloom Filter **************");
			// Counting Bloom filter
			System.out.println("Enter no. of elements for counting bloomFilter:");
			int noOfElements_counting = sc.nextInt();
			
			System.out.println("Enter no. of bits in counting-bloom filter:");
			int noOfCounters_counting = sc.nextInt();
			
			System.out.println("Enter no. of hashes for counting:");
			int noOfHashes_counting = sc.nextInt();

			System.out.println("Enter no. of elements to add for counting bloom filter:");
			int noOfElementsToAdded = sc.nextInt();

			System.out.println("Enter no. of elements to remove for counting bloom filter:");
			int noOfElementsToRemoved = sc.nextInt();

			CountingBloomFilter countingBloomFilter = new CountingBloomFilter(noOfElements_counting, noOfElementsToRemoved, noOfElementsToAdded, noOfCounters_counting, noOfHashes_counting);

			// setA
			int i=0;
			while(i<noOfElements_counting){
				int element = rand.nextInt(Integer.MAX_VALUE);
				if(setA.add(element))
					i++;
			}

			// setB
			i=0;
			while(i<noOfElements_counting){
				int element = rand.nextInt(Integer.MAX_VALUE);
				if(setB.add(element))
					i++;
			}

			// using setA with 1000 elements for encoding
			for(int element: setA){
				countingBloomFilter.encode(element);
			}

			// remove 500 elements from setA 
			i=0;
			for(int x: setA){
				if(i%2==0){
					countingBloomFilter.remove(x);
				}
				i++;
			}

			// insert another 500 random elements. Using setB elements
			i=0;
			for(int x: setB){
				countingBloomFilter.encode(x);
				i++;
				if(i>=500) break;
			}

			// lookup elements of setA in the BloomFilter
			int count = 0;
			for(int element: setA){
				if(countingBloomFilter.lookUp(element)>0){
					count++;
				}
			}

			// System.out.println
			writer.write("Number of elements of Set A found during look up in Counting Bloom Filter: "+count);
			// System.out.println();
		}
	}

	public static void runCodedBloomFilter(Scanner sc) throws IOException{
		Random rand = new Random();
		FileWriter output = new FileWriter("CodedBloomFilter.txt");

		try(BufferedWriter writer = new BufferedWriter(output)){
			System.out.println("*************** Coded Bloom Filter **************");
			System.out.println("Enter number of Sets: ");
			int numOfSets = sc.nextInt();

			System.out.println("Enter number of elements in each set: ");
			int numOfElementsSet = sc.nextInt();

			System.out.println("Enter number of filters: ");
			int numOfFilters = sc.nextInt();

			System.out.println("Enter number of bits: ");
			int numOfBits = sc.nextInt();

			System.out.println("Enter number of hashes: ");
			int numOfHashes = sc.nextInt();

			CodedBloomFilter codedBloomFilter = new CodedBloomFilter(numOfSets, numOfElementsSet, numOfFilters, numOfBits, numOfHashes);

			int totalNumOfElements = numOfSets*numOfElementsSet;
			Set<Integer> set = new HashSet<>();

			int i=0;

			while(i<totalNumOfElements){
				int element = rand.nextInt(Integer.MAX_VALUE);
				if(set.add(element))
					i++;
			}

			int[][] sets = new int[numOfSets][numOfElementsSet];
			
			// encode all the elements from sets
			i=0;
			int segment = 0;
			for(int x: set){
				sets[segment][i++] = x;
				// encode the element on the fly
				codedBloomFilter.encode(segment, x);
				if(i==numOfElementsSet){
					segment++;
					i=0;
				}
			}

			int count=0;
			for(i=0; i<numOfSets; i++){
				for(int j=0; j<numOfElementsSet; j++){
					if(codedBloomFilter.lookUp(i, sets[i][j]))
						count++;
				}
			}

			// System.out.println
			writer.write("Number of Elements which has correct lookUp result: "+count);
		}
	}

	public static void main(String[] args) throws IOException{

		Scanner sc = new Scanner(System.in);
		
		runBloomFilter(sc);

		runCountingBloomFilter(sc);
		
		runCodedBloomFilter(sc);
	}
}