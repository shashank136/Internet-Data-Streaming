import java.util.*;
import java.io.*;
import java.math.*;

class HyperLoglog{
	// Since each hash is 32 bits, it cannot have more then 31 leadling zeroes
	private int m;
	private double alpha;
	private BitSet[] B; // array of registers. 

	public HyperLoglog(int m){
		this.m = m;
		this.B = new BitSet[m]; // initialize m registers
		for(int i=0; i<m; i++){
			B[i] = new BitSet(5);
		}
		this.alpha = 0.7213/(1+1.079/m); // alpha is a function of m
	}

	public int hashFunction(int element){
		return Integer.valueOf(element).hashCode();
	}

	public int getNumLeadingZeroes(int element){
		// generate hash for the element
		int hash = hashFunction(element);
		// count leading zeroes from the hash of the element
		// I have first calculated the position of leftmost non-zero bit in the binary 
		// representation and used it to calculate number of leading zeroes before the 
		// leftmost non-zero bit.
		int k = (int)(Math.log(hash) / Math.log(2));
		return 31-k;
	}

	// insert registers an element
	public void insert(int element){
		// generate the hash of the element
		int hash = hashFunction(element);
		int register = hash%m;
		// get number of leading zeroes for the hash
		int Ge1 = getNumLeadingZeroes(hash);
		
		if(B[register].length()==0)
			B[register] = BitSet.valueOf(BigInteger.valueOf(Ge1).toByteArray());
		else{
			if(new BigInteger(B[register].toByteArray()).intValue()<Ge1){
				B[register] = BitSet.valueOf(BigInteger.valueOf(Ge1).toByteArray());
			}
		}
	}

	// getSpread return the estimated flow spread
	public double getSpread(){
		double val = 0;
		for(int i=0; i<m; i++){
			// System.out.println(binaryToInt(getBinaryStringFromBitSet(B[i])));
			val += (1.0/Math.pow(2, (B[i].length()==0) ? 0 : new BigInteger(B[i].toByteArray()).intValue()));
		}
		return alpha*Math.pow(m,2)*(1.0/val);
	}

	// reset
	public void reset(){
		this.B = new BitSet[m];
		for(int i=0; i<m; i++)
			B[i] = new BitSet(5);
	}
}

class ProbabilisticBitMap{
	private int m;
	private int[] bitmap;
	private int zeroes;
	private double probability;
	private int maxValue = Integer.MAX_VALUE;

	public ProbabilisticBitMap(int m, double probability){
		this.m = m;
		this.bitmap = new int[m];
		this.zeroes = m;
		this.probability = probability;
	}

	public int hashFunction(int element){
		return Integer.valueOf(element).hashCode();
	}

	public int uniformHashser(int element){
		return Integer.valueOf(element^53).hashCode();
	}

	// insert registers an element
	public void insert(int element){
		int Hf = uniformHashser(element);
		if(Hf<(maxValue*probability)){
			int index = hashFunction(element)%m;
			if(this.bitmap[index]==0){
				this.bitmap[index]=1; 
				this.zeroes--;
			}
		}
	}

	// getSpread return the estimated flow spread
	public double getSpread(){
		return -(this.m/this.probability)*Math.log(new Double(this.zeroes)/new Double(this.m));
	}

	public void reset(){
		Arrays.fill(this.bitmap, 0);
		this.zeroes = this.m;
	}
}

class BitMap{
	private int m;
	private int[] bitmap;
	private int zeroes;

	public BitMap(int m){
		this.m = m;
		this.bitmap = new int[m];
		this.zeroes = m;
	}

	public int hashFunction(int element){
		return Integer.valueOf(element).hashCode();
	}

	// insert registers an element
	public void insert(int element){
		int index = hashFunction(element)%m;
		if(this.bitmap[index]==0){
			this.bitmap[index]=1;
			this.zeroes--;
		}
	}

	// getSpread return the estimated flow spread
	public double getSpread(){
		return -this.m*Math.log(new Double(this.zeroes)/new Double(this.m));
	}

	public void reset(){
		Arrays.fill(this.bitmap, 0);
		this.zeroes = this.m;
	}
}

public class SingleFlowSpreadSketches{
	public static void main(String[] args) throws IOException{
		Scanner sc = new Scanner(System.in);
		Random rand = new Random();
		
		System.out.println("Enter value for bitmap size: ");
		int m = sc.nextInt();

		System.out.println("Enter value for propability for Probabilistic BitMap: ");
		double p = sc.nextDouble();

		System.out.println("Enter number of registers for HyperLoglog");
		int regs = sc.nextInt();


		int spread = 100;
		
		BitMap bitMap = new BitMap(m);
		ProbabilisticBitMap probabilisticBitMap = new ProbabilisticBitMap(m, p);
		HyperLoglog hyperLoglog = new HyperLoglog(regs);
 

		// generate spread for 5 flows
		try(FileWriter output = new FileWriter("bitmap.txt");
			FileWriter output1 = new FileWriter("probabilisticBitMap.txt");
			FileWriter output2 = new FileWriter("hyperLoglog.txt");
			BufferedWriter writer1 = new BufferedWriter(output);
			BufferedWriter writer2 = new BufferedWriter(output1);
			BufferedWriter writer3 = new BufferedWriter(output2)){
			
			writer1.write("actual \t estimated");
			writer1.newLine();
			
			writer2.write("actual \t estimated");
			writer2.newLine();

			writer3.write("actual \t estimated");
			writer3.newLine();

			for(int flow=0; flow<5; flow++){
				for(int i=0; i<spread; i++){
					int randomNumber = rand.nextInt(Integer.MAX_VALUE);
					bitMap.insert(randomNumber);
					probabilisticBitMap.insert(randomNumber);
					if(flow>=1)
						hyperLoglog.insert(randomNumber);
				}
				// get the estimated flow-spread
				double estimatedSpread1 = bitMap.getSpread();
				double estimatedSpread2 = probabilisticBitMap.getSpread();
				double estimatedSpread3 = 0;
				if(flow>=1)
					estimatedSpread3 = hyperLoglog.getSpread();
				

				// increment the spread by multiple of 10
				writer1.write(spread+"\t"+estimatedSpread1);
				writer1.newLine();

				writer2.write(spread+"\t"+estimatedSpread2);
				writer2.newLine();

				if(flow>=1){
					writer3.write(spread+"\t"+estimatedSpread3);
					writer3.newLine();
				}

				spread*=10;

				// clear the bitmap - reset
				bitMap.reset();
				probabilisticBitMap.reset();
				hyperLoglog.reset();
			}
		}
	}
}