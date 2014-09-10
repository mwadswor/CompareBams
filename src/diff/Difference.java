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
	
	public Difference(File infile1, File infile2, File output) {
//		this.differences = new ArrayList<SAMRecord>();
//		this.readNames = new HashSet<String>();
		this.header = null;
		this.outfile = output;
		
		
		IOUtil.assertFileIsReadable(infile1);
		IOUtil.assertFileIsReadable(infile2);
		
		runDifferenceCheck(infile1,infile2);

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
		line.append(rec.getReadName()+"\t"+rec.getReadString());
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
