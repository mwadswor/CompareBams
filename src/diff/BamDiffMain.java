package diff;

import java.io.File;

import htsjdk.samtools.SamReaderFactory;


public class BamDiffMain {

	private File infile1;
	private File infile2;
	private File outfile;
	
	
	
	public BamDiffMain(){
		return;
	}
	
	
	public static void main(String[] args){
		
		BamDiffMain program = new BamDiffMain();
		
		if(args.length!=4){
			System.out.println("USAGE: java -jar CompareBams.jar -c or -s Input1.bam Input2.bam output.bam");
			System.out.println("This detects the reads that are in the first sorted bam file but not in the second sorted bam file and prints them out to the output.bam");
		}
		else if(args[0].equals("-s")){
				program.infile1 = new File(args[1]);
				program.infile2 = new File(args[2]);
				program.outfile = new File(args[3]);
				program.runStats();
		}
		else if(args[0].equals("-c")){
			program.infile1 = new File(args[1]);
			program.infile2 = new File(args[2]);
			program.outfile = new File(args[3]);
			program.run();
		}
		else{
			System.out.println(args[0]);
		}
	}


	private void runStats() {
		Difference diff = new Difference(infile1, infile2, outfile, true);
	}


	private void run() {
		Difference diff = new Difference(infile1, infile2, outfile, false);
//		writeOutput(diff);
	}


//	private void writeOutput(Difference diff) {
//		diff.writeOutput(outfile);
//	}
//	
	
	
}
