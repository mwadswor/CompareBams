package diff;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileHeader.SortOrder;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.IOUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import picard.PicardException;

public class Difference {

//	private List<SAMRecord> differences; 
//	private Set<String> readNames;
	private SAMFileHeader header;
	private File outfile;
	private HashMap<String, Integer> chromLengths;
	
	public Difference(File infile1, File infile2, File output) {
//		this.differences = new ArrayList<SAMRecord>();
//		this.readNames = new HashSet<String>();
		this.header = null;
		this.outfile = output;
		
		
		IOUtil.assertFileIsReadable(infile1);
		IOUtil.assertFileIsReadable(infile2);
		
		chromLengths = new HashMap<String, Integer>();
		chromLengths.put("chrM", 16571);
		chromLengths.put("chr1", 249250621);
		chromLengths.put("chr2", 243199373);
		chromLengths.put("chr3", 198022430);
		chromLengths.put("chr4", 191154276);
		chromLengths.put("chr5", 180915260);
		chromLengths.put("chr6", 171115067);
		chromLengths.put("chr7", 159138663);
		chromLengths.put("chr8", 146364022);
		chromLengths.put("chr9", 141213431);
		chromLengths.put("chr10", 135534747);
		chromLengths.put("chr11", 135006516);
		chromLengths.put("chr12", 133851895);
		chromLengths.put("chr13", 115169878);
		chromLengths.put("chr14", 107349540);
		chromLengths.put("chr15", 102531392);
		chromLengths.put("chr16", 90354753);
		chromLengths.put("chr17", 81195210);
		chromLengths.put("chr18", 78077248);
		chromLengths.put("chr19", 59128983);
		chromLengths.put("chr20", 63025520);
		chromLengths.put("chr21", 48129895);
		chromLengths.put("chr22", 51304566);
		chromLengths.put("chrX", 155270560);
		chromLengths.put("chrY", 59373566);
		chromLengths.put("*", 0);

		runDifferenceCheck(infile1,infile2);
	}
	
	private void runDifferenceCheck(File infile1, File infile2){
		
		final SamHeaderAndIterator headerAndIterator1 = openInputs(infile1);
		final SamHeaderAndIterator headerAndIterator2 = openInputs(infile2);
		
		final CloseableIterator<SAMRecord> iterator1 = headerAndIterator1.iterator;
		final CloseableIterator<SAMRecord> iterator2 = headerAndIterator2.iterator;
        
		header = headerAndIterator2.header;
//		SAMRecord prevRec = new SAMRecord(header);
		final SAMFileWriter out = new SAMFileWriterFactory().makeSAMOrBAMWriter(header, true, outfile);
		
//		THIRD ROUND********************************************************		
		
		String Chromosome = "";
		String tempCHR = "", temp2CHR;
		int chromEnd = -1;
		int binEnd = -1;
		int binSize = 100000;
		int currPos = -1, currPos2 = -1;
		SAMRecord rec1 = iterator1.next();
		SAMRecord rec2 = iterator2.next();
		int iterCount = 0;
		
		Chromosome = rec1.getReferenceName();
		tempCHR = rec1.getReferenceName();
		chromEnd = this.chromLengths.get(Chromosome);
		binEnd = binSize;
		currPos = rec1.getAlignmentStart();
		while(iterator1.hasNext() && iterator2.hasNext()){
			
			HashSet<String> smallerFile = new HashSet<String>();
			
			if(iterCount % 100 == 0){
				reportMemoryStats("Iteration " + iterCount + ": ");
				System.out.println("Chrom: " + Chromosome + ", CurrPos: " + currPos + ", chromEnd: " + chromEnd + "\n");
			}
			
			while(Chromosome.equals(tempCHR) && currPos <= binEnd){
				
				/* Stop when we hit chr '*' */
				if(!tempCHR.equals("*")){
					smallerFile.add(makeStringFromRec(rec1));
				}

				
				if(!iterator1.hasNext())
					break;
				
				rec1 = iterator1.next();
				tempCHR = rec1.getReferenceName();
				currPos = rec1.getAlignmentStart();
			}
			
			//String temp2CHR = Chromosome;
			temp2CHR = rec2.getReferenceName();
			currPos2 = rec2.getAlignmentStart();
			while(Chromosome.equals(temp2CHR) && currPos2 <= binEnd){
				
				if(!temp2CHR.equals("*")
						&& !smallerFile.contains(makeStringFromRec(rec2))){
					out.addAlignment(rec2);
				}
				if(!iterator2.hasNext())
					break;
				
				rec2 = iterator2.next();
				temp2CHR = rec2.getReferenceName();
				currPos2 = rec2.getAlignmentStart();
			}
			
//			System.out.println(tempCHR + " == " + temp2CHR);
//			System.out.println("CurrPos: " + currPos + ", binEnd: " + binEnd + ", chromEnd: " + chromEnd);
			if(!tempCHR.equals(temp2CHR)){
				System.out.println("Your file is not sorted based on Chromosome and position.");
				System.exit(1);
			}
//			System.out.println(Chromosome);
			
			/* If tempCHR has moved to the next chr, reset
			 * the currPos, chromEnd, and binEnd
			 */
			if(!Chromosome.equals(tempCHR)){
				System.out.println("Starting chrom: '" + tempCHR + "'");
				Chromosome = tempCHR;
				chromEnd = this.chromLengths.get(Chromosome);
				binEnd = binSize;
			}
			/* Otherwise, if the remaining portion of the chr is
			 * less than the bin size, just set binEnd to the chromEnd
			 * so we don't go over.
			 */
			else if(chromEnd - binSize < binEnd){
				binEnd = chromEnd;
			}
			/* Otherwise, just add binSize to binEnd */
			else{
				binEnd += binSize;
			}
			
			iterCount++;
		}
		
		
		out.close();
		
		
		
		
		
		
		
		
//		SECOND ROUND*******************************************************
		
		
//		HashSet<String> dupsRemoved = new HashSet<String>();
//		
//		while (iterator1.hasNext()){
//			
//			SAMRecord rec1 = iterator1.next();
//			dupsRemoved.add(makeStringFromRec(rec1));
//			
//		}
//		
//		while (iterator2.hasNext()){
//			
//			SAMRecord rec2 = iterator2.next();
//			if(!dupsRemoved.contains(makeStringFromRec(rec2))){
//				differences.add(rec2);
//			}
//			
//		}
		
		
//		FIRST ROUND*******************************************************		
		
		
//		while (iterator1.hasNext() &&  iterator2.hasNext()) {
//        
//			SAMRecord rec1 = iterator1.next();
//			SAMRecord rec2 = iterator2.next();
//			
//            System.out.println("I'm in the while loop and rec1 = "+rec1.getReadName());
            
			
			
			
            
//            if(rec1.getReadName().equals(rec2.getReadName())){
//            	if(!rec1.getReadString().equals(rec2.getReadString()) || !rec1.getBaseQualityString().equals(rec2.getBaseQualityString()) || rec1.getFlags() != rec2.getFlags()){
//           			checkNextRead(iterator1, iterator2, rec1, rec2);
//            	}
//            }
//            else{
//            	differences.add(rec1);
////            	checkSetForRead(rec1);
//            	if(iterator1.hasNext()){
//            		rec1 = iterator1.next();
//            		while(!rec1.getReadName().equals(rec2.getReadName())){
//            			differences.add(rec1);
////            			checkSetForRead(rec1);
//            			rec1 = iterator1.next();
//            		}
//            		if(!rec1.getReadString().equals(rec2.getReadString()) || !rec1.getBaseQualityString().equals(rec2.getBaseQualityString()) || rec1.getFlags() != rec2.getFlags()){
//            			checkNextRead(iterator1, iterator2, rec1, rec2);
//            		}
//            	}
//            }
            
            
//        }
		
		
	}
	
	private String makeStringFromRec(SAMRecord rec){
		StringBuilder line = new StringBuilder();
		line.append(rec.getReadName()+"\t"+rec.getFlags()+"\t"+rec.getReadString());
		return line.toString();
	}
	
    /** Print out some quick JVM memory stats. */
    private void reportMemoryStats(final String stage) {
        System.gc();
        final Runtime runtime = Runtime.getRuntime();
        System.out.println(stage + " freeMemory: " + runtime.freeMemory() + "; totalMemory: " + runtime.totalMemory() +
                "; maxMemory: " + runtime.maxMemory());
    }
    
	
//	private void checkNextRead(CloseableIterator<SAMRecord> iterator1, CloseableIterator<SAMRecord> iterator2, SAMRecord rec1, SAMRecord rec2) {
//		SAMRecord prevRec1 = rec1;
//		rec1 = iterator1.next();
//		if(rec1.getReadString().equals(rec2.getReadString()) && rec1.getBaseQualityString().equals(rec2.getBaseQualityString()) && rec1.getFlags() == rec2.getFlags()){
//			rec2 = iterator2.next();
//			if(!prevRec1.getReadString().equals(rec2.getReadString()) || !prevRec1.getBaseQualityString().equals(rec1.getBaseQualityString()) || prevRec1.getFlags() != rec2.getFlags()){
//				differences.add(prevRec1);
//			}
//		}
//		else{
//			System.out.println("Something is very wrong!!!\nRec1 = "+rec1.toString()+"and Rec2 = "+rec2.toString());
//			System.exit(1);
//		}
//		
//	}

//	private boolean comparePairedReads(SAMRecord rec1, SAMRecord rec2, SAMRecord prevRec2, CloseableIterator<SAMRecord> iterator1){
//		
//		SAMRecord prevRec1 = rec1;
//		rec1 = iterator1.next();
//		
//		if(!rec1.equals(prevRec2) && !prevRec1.equals(rec2)){
//			differences.add(prevRec1);
//			differences.add(rec1);
//			checkSetForRead(prevRec1);
//			checkSetForRead(rec1);
//			return false;
//		}
//		return true;
//		
//		
//		
//		
//	}
	
	
//	private void checkSetForRead(SAMRecord rec){
//		if(readNames.contains(rec.getReadName())){
//			readNames.remove(rec.getReadName());
//		}
//		else{
//			readNames.add(rec.getReadName());
//		}
//	}
	
    private static final class SamHeaderAndIterator {
    	
        final SAMFileHeader header;
        final CloseableIterator<SAMRecord> iterator;

        private SamHeaderAndIterator(final SAMFileHeader header, final CloseableIterator<SAMRecord> iterator) {
        	
            this.header = header;
            this.iterator = iterator;
            
        }   
    }   

    private SamHeaderAndIterator openInputs(final File f) {

		final SamReaderFactory factory =
		          SamReaderFactory.makeDefault()
		              .enable(SamReaderFactory.Option.INCLUDE_SOURCE_IN_RECORDS, SamReaderFactory.Option.VALIDATE_CRC_CHECKSUMS)
		              .validationStringency(ValidationStringency.SILENT);

        final SamReader reader = factory.open(f);
        
        final SAMFileHeader header = reader.getFileHeader();

        if (header.getSortOrder() != SortOrder.coordinate) {
        	throw new PicardException("Input file " + f.getAbsolutePath() + " is not coordinate sorted.");
        }   

        return new SamHeaderAndIterator(header, reader.iterator());
    }

//	public void writeOutput(File outfile) {
//		
//		  final SAMFileWriter out = new SAMFileWriterFactory().makeSAMOrBAMWriter(header, false, outfile);
//		  
//		  for(SAMRecord rec : differences){
//			  out.addAlignment(rec);
//		  }
//		  
//		  out.close();
		  
//		  String name = outfile.getName();
//		  String filename = name.substring(0, name.lastIndexOf("."));
//		  StringBuilder sb = new StringBuilder(filename);
//		  sb.append(".txt");
//		  filename = sb.toString();
		  
//		  System.out.println("The number of reads that only one of the two reads was removed as a duplicate is " + readNames.size());
		  
//	}   


}
