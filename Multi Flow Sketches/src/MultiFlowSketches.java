import java.util.*;
import java.io.*;
import java.math.*;
import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;

class VirtualBitMap{
	private int[] bitmap;
	private int[] randNums;
	private int m;
	private int l;
	private int zeroes;
	private Random rand;

	public VirtualBitMap(int m, int l){
		this.m = m;
		this.l = l;
		this.zeroes = m;
		this.bitmap = new int[m];
		this.randNums = new int[l];
		this.rand = new Random();
		for(int i=0; i<l; i++){
			this.randNums[i] = rand.nextInt();
		}
	}

	// hashfunction
	public int hashFunction(int element){
		return Integer.valueOf(element).hashCode();
	}

	// record element for given flowID
	public void record(String flowId, int element){
		int r = this.randNums[Math.abs(hashFunction(element))%l];
		int index = Math.abs(hashFunction(flowId.hashCode()^r))%m;
		if(bitmap[index]==0){
			bitmap[index]=1;
			zeroes--;
		}
	}

	// query returns the estimated spread for given flowId
	public double query(String flowId){
		double vF = l;
		for(int x: randNums){
			if(bitmap[Math.abs(hashFunction(flowId.hashCode()^x))%m]==1)
				vF--;
		}
		vF /= l;
		double vB = zeroes/(double)m;

		return l*Math.log(vB) - l*Math.log(vF);
	}

}

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
	public void record(int element){
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
	public double query(){
		double val = 0;
		for(int i=0; i<m; i++){
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

class bSkt{
	private HyperLoglog[] hyperLoglogs;
	private int[] randNums;
	private int l;
	private int m;
	private Random rand;

	public bSkt(int m, int l, int k){
		this.m = m;
		this.l = l;
		this.hyperLoglogs = new HyperLoglog[m];
		this.randNums = new int[k];
		rand = new Random();

		for(int i=0; i<m; i++){
			hyperLoglogs[i] = new HyperLoglog(l);
		}

		for(int i=0; i<k; i++){
			this.randNums[i] = rand.nextInt();
		}
	}

	// record the element for given flowId
	public void record(String flowId, int element){
		for(int x: randNums){
			int hash = Math.abs(flowId.hashCode()^x)%m;
			hyperLoglogs[hash].record(element);
		}
	}

	// query returns the estimated spread for given flowId
	public double query(String flowId){
		double spread = Double.MAX_VALUE;
		for(int x: randNums){
			int hash = Math.abs(flowId.hashCode()^x)%m;
			// double temp = hyperLoglogs[hash].query();
			spread = Math.min(spread, hyperLoglogs[hash].query()); //(spread<=temp) ? spread : temp;
		}

		return spread;
	}

}

class Node{
	String flowId;
	int actualSpread;

	public Node(String flowId, int actualSpread){
		this.flowId = flowId;
		this.actualSpread = actualSpread;
	}
}

// Graph class to draw the grpah from the points
class Graph extends JPanel{
    private int mar=50;
    private Graphics2D g1;
    private ArrayList<double[]> points;
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);

    public Graph(ArrayList<double[]> points){
    	this.points = points;
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        g1=(Graphics2D)g;
        g1.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        int width=550;
        int height=550;
        
        g1.setStroke(GRAPH_STROKE);

        g1.draw(new Line2D.Double(mar,mar,mar,height-mar));
        
        Font valueFont = new Font("Digital-7", Font.PLAIN, 20);
        g1.setFont(valueFont);
    	g1.setColor(Color.black);
        g1.drawString("Actual spread", 250, 520);
        
        g1.draw(new Line2D.Double(mar,height-mar,width-mar,height-mar));


        double x=(double)(width-2*mar)/(points.size()-1);
        double scale=(double)(height-2*mar)/getMax();
        g1.setPaint(Color.BLUE);
        
        // plotting points
        for(int i=0;i<points.size();i++){
            double x1= mar+points.get(i)[0]-1;
            double y1= height-mar-(points.get(i)[1]<0 ? 0 : points.get(i)[1])-1;
            // plotting points within the range of x => [0,500]
            if(x1<=500 && y1<=500)
            	g1.fill(new Ellipse2D.Double(x1,y1,3,3));
        }
        
        valueFont = new Font("Digital-7", Font.PLAIN, 20);
        g1.setFont(valueFont);
    	g1.setColor(Color.black);
        g1.rotate(1.571, 30, 220);
        g1.drawString("Estimated spread", 30, 220);
        
    }
    private int getMax(){
        int max=-Integer.MAX_VALUE;
        for(int i=0;i<points.size();i++){
            if(points.get(i)[0]>max)
                max=(int)points.get(i)[0];
           
        }return max;
    }

}

public class MultiFlowSketches{
	public static void main(String[] args) throws IOException{
		Scanner sc = new Scanner(System.in);

		System.out.println("Enter value for m:");
		int m = sc.nextInt();
		
		System.out.println("Enter value for l:");
		int l = sc.nextInt();

		Random rand = new Random();
		
		File file = new File("project5input.txt");
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		// VirtualBitMap
		VirtualBitMap virtualBitMap = new VirtualBitMap(m,l);
		Graph G=null;

		// bSkt
		System.out.println("Enter no of flows for bSkt(m):");
		int m1 = sc.nextInt();

		System.out.println("Enter no of estimators for bSkt(l):");
		int l1 = sc.nextInt();

		System.out.println("Enter no of estimators each flow is hashed to for bSkt(k):");
		int k = sc.nextInt();

		bSkt bskt = new bSkt(m1, l1 , k);

		String str;
		int n = -1;
		HashMap<String, Integer> map = new HashMap<>();
		PriorityQueue<Node> q = new PriorityQueue<>((a,b)->b.actualSpread-a.actualSpread);

		while((str=br.readLine())!=null){
			if(n==-1)
				n = Integer.parseInt(str);
			else{
				String[] parts = str.split("\\s+");
				map.put(parts[0], Integer.parseInt(parts[1]));
				q.offer(new Node(parts[0], Integer.parseInt(parts[1])));

				// genrate random flow for current flowID
				for(int i=0; i<map.get(parts[0]); i++){
					int element = rand.nextInt(Integer.MAX_VALUE);
					virtualBitMap.record(parts[0], element);
					bskt.record(parts[0], element);
				}
			}
		}

		ArrayList<double[]> points = new ArrayList<>();
		for(String flowId: map.keySet()){
			points.add(new double[]{Double.valueOf(map.get(flowId)), virtualBitMap.query(flowId)});
		}

		G = new Graph(points);

		FileWriter output = new FileWriter("output.txt");
		try(BufferedWriter writer = new BufferedWriter(output)){
			for(int i=0; i<25; i++){
				Node node = q.poll();
				writer.write(node.flowId+"   "+bskt.query(node.flowId));
				writer.newLine();
			}
		}

		JFrame frame =new JFrame();
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.add(G);
	    frame.setSize(600,600);
	    frame.setLocation(200,200);
	    frame.setVisible(true);
	}
}