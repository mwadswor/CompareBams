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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import picard.PicardException;

public class Difference {

//	private List<SAMRecord> differences; 
//	private Set<String> readNames;
	private SAMFileHeader header;
	private File outfile;
	private FileWriter output;
	
	public Difference(File infile1, File infile2, File output, boolean isStat) {
//		this.differences = new ArrayList<SAMRecord>();
//		this.readNames = new HashSet<String>();
		this.header = null;
		this.outfile = output;
		
		
		IOUtil.assertFileIsReadable(infile1);
		IOUtil.assertFileIsReadable(infile2);
		if(isStat == false){
			runDifferenceCheck(infile1,infile2);
		}
		else{
			getStats(infile1, infile2);
		}

	}
	
	private void getStats(File infile1, File infile2) {
		final SamHeaderAndIterator headerAndIterator1 = openInputs(infile1);
		final SamHeaderAndIterator headerAndIterator2 = openInputs(infile2);
		
		final CloseableIterator<SAMRecord> iterator1 = headerAndIterator1.iterator;
		final CloseableIterator<SAMRecord> iterator2 = headerAndIterator2.iterator;
        
		HashMap<String, Integer> file1Set = new HashMap<String, Integer>();
		HashMap<String, Integer>  file2Set = new HashMap<String, Integer>();
		
		try {
			this.output = new FileWriter(outfile);
			output.write("#CHR\tPOS\tSAMTOOLS\tPICARD\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		SAMRecord rec1 = iterator1.next();
		SAMRecord rec2 = iterator2.next();
		
		String CHR = rec1.getReferenceName();
		
		while(iterator1.hasNext() && iterator2.hasNext()){
		
			String tempCHR = rec1.getReferenceName();
			while(tempCHR.equals(CHR)){
				placeReadInMap(file1Set, rec1);
				if(!iterator1.hasNext()){
					break;
				}
				rec1 = iterator1.next();
				tempCHR = rec1.getReferenceName();
			}
			
			String tempCHR2 = rec2.getReferenceName();
			while(tempCHR2.equals(CHR)){
				placeReadInMap(file2Set, rec2);
				if(!iterator2.hasNext())
					break;
				rec2 = iterator2.next();
				tempCHR2 = rec2.getReferenceName();
			}
			System.out.println("CHR = "+CHR);
			System.out.println(tempCHR + " = " +tempCHR2);
			
			if(tempCHR.equals(tempCHR2))
				CHR = tempCHR;
			else{
				System.out.println("Your bam files are not in the same order");
				System.exit(1);
			}
			
			System.out.println(CHR);
			
			try {
				printCHR(file1Set, file2Set);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			file1Set.clear();
			file2Set.clear();
		}
		
		try {
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void printCHR(HashMap<String, Integer> file1Set, HashMap<String, Integer> file2Set) throws IOException {
		for(HashMap.Entry<String, Integer> temp : file1Set.entrySet()){
			String Key1 = temp.getKey();
			Integer Value1 = temp.getValue();
			if(file2Set.containsKey(Key1)){
				output.write(Key1+"\t"+String.valueOf(Value1)+"\t"+String.valueOf(file2Set.get(Key1))+"\n");
				file2Set.remove(Key1);
			}
			else{
				output.write(Key1+"\t"+String.valueOf(Value1)+"\t0\n");
			}
		}
		for(HashMap.Entry<String, Integer> temp : file2Set.entrySet()){
			String Key2 = temp.getKey();
			Integer Value2 = temp.getValue();
			output.write(Key2+"\t0\t"+String.valueOf(Value2)+"\n");
		}
	}

	private void placeReadInMap(HashMap<String, Integer> fileSet, SAMRecord rec) {
		String Key1 = makeKey(rec);
		if(fileSet.containsKey(Key1)){
			//System.out.println("in the already there portion of placeReadMap");
			int value = fileSet.get(Key1);
			value++;
			fileSet.put(Key1, value);
		}
		else{
			fileSet.put(Key1, 1);
		}
		
	}

	private String makeKey(SAMRecord rec){
		StringBuilder key = new StringBuilder();
		String chr = rec.getReferenceName();
		if(chr.contains("chr")){
			chr = chr.replace("chr", "");
		}
		else if(chr.contains("CHR")){
//			chr = chr.replace("CHR", "");
		}
		if(chr.equals("M")){
			chr = "23";
		}
		else if(chr.equals("X")){
			chr = "24";
		}
		else if(chr.equals("Y")){
			chr = "25";
		}
		key.append(chr+"\t"+rec.getAlignmentStart());
		return key.toString();
	}

	private void runDifferenceCheck(File infile1, File infile2){
		
		final SamHeaderAndIterator headerAndIterator1 = openInputs(infile1);
		final SamHeaderAndIterator headerAndIterator2 = openInputs(infile2);
		
		final CloseableIterator<SAMRecord> iterator1 = headerAndIterator1.iterator;
		final CloseableIterator<SAMRecord> iterator2 = headerAndIterator2.iterator;
        
		header = headerAndIterator2.header;
//		SAMRecord prevRec = new SAMRecord(header);
		final SAMFileWriter out = new SAMFileWriterFactory().makeSAMOrBAMWriter(header, false, outfile);
		
//		THIRD ROUND********************************************************		
		
		String Chromosome = "";
		String tempCHR = "";
		SAMRecord rec1 = iterator1.next();
		SAMRecord rec2 = iterator2.next();
		
		while(iterator1.hasNext() && iterator2.hasNext()){
			
			HashSet<String> smallerFile = new HashSet<String>();
			
			while(Chromosome.equals(tempCHR)){
				
				
				if(Chromosome.equals("")){
					Chromosome = rec1.getReferenceName();
				}
				
				tempCHR = rec1.getReferenceName();
				smallerFile.add(makeStringFromRec(rec1));
				
				if(!iterator1.hasNext())
					break;
				
				rec1 = iterator1.next();
				tempCHR = rec1.getReferenceName();
			}
			
			String temp2CHR = Chromosome;
			while(Chromosome.equals(temp2CHR)){
				
				temp2CHR = rec2.getReferenceName();
				
				if(!smallerFile.contains(makeStringFromRec(rec2))){
					out.addAlignment(rec2);
				}
				if(!iterator2.hasNext())
					break;
				
				rec2 = iterator2.next();
				temp2CHR = rec2.getReferenceName();
			}
			
			System.out.println(tempCHR + " == " + temp2CHR);
			if(!tempCHR.equals(temp2CHR)){
				System.out.println("You're file is not sorted based on Chromosome and position.");
				System.exit(1);
			}
			System.out.println(Chromosome);
			Chromosome = tempCHR;
			
			
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
    	
    	System.setProperty("java.io.tmpdir", "/fslhome/mwadswor/compute/tmp");

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
