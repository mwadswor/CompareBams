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
		
		if(args.length!=3){
			System.out.println("USAGE: java -jar CompareBams.jar Input1.bam Input2.bam output.bam");
			System.out.println("This detects the reads that are in the first sorted bam file but not in the second sorted bam file and prints them out to the output.bam");
		}
		else{
			program.infile1 = new File(args[0]);
			program.infile2 = new File(args[1]);
			program.outfile = new File(args[2]);
			program.run();
		}
		
	}


	private void run() {
		Difference diff = new Difference(infile1, infile2);
		writeOutput(diff);
	}


	private void writeOutput(Difference diff) {
		diff.writeOutput(outfile);
	}
	
	
	
}
