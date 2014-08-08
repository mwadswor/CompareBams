package diff;

import htsjdk.samtools.MergingSamRecordIterator;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileHeader.SortOrder;
import htsjdk.samtools.SAMFileReader;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SamFileHeaderMerger;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReader.PrimitiveSamReader;
import htsjdk.samtools.SamReader.PrimitiveSamReaderToSamReaderAdapter;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.ValidationStringency;
import htsjdk.samtools.util.CloseableIterator;
import htsjdk.samtools.util.IOUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import picard.PicardException;

public class Difference {

	private List<SAMRecord> differences; 
	private Set<String> readNames;
	private Integer one_of_pairs_removed;
	
	public Difference(File infile1, File infile2) {
		this.differences = new ArrayList<SAMRecord>();
		this.readNames = new HashSet<String>();
		
		IOUtil.assertFileIsReadable(infile1);
		IOUtil.assertFileIsReadable(infile2);
		
		runDifferenceCheck(infile1,infile2);

	}
	
	private void runDifferenceCheck(File infile1, File infile2){
		final SamHeaderAndIterator headerAndIterator1 = openInputs(infile1);
		final SamHeaderAndIterator headerAndIterator2 = openInputs(infile2);
		
		final CloseableIterator<SAMRecord> iterator1 = headerAndIterator1.iterator;
		final CloseableIterator<SAMRecord> iterator2 = headerAndIterator2.iterator;
        
		while (iterator1.hasNext() &&  iterator2.hasNext()) {
        
			SAMRecord rec1 = iterator1.next();
			SAMRecord rec2 = iterator2.next();
			
            System.out.println("I'm in the while loop and rec1 = "+rec1.toString());
            
            if (!rec1.getReadName().equals(rec2.getReadName())){
            	differences.add(rec1);
            	if(readNames.contains(rec1)){
            		
            	}
            	if(iterator1.hasNext()){
           			rec1 = iterator1.next();
            		while(!rec1.getReadName().equals(rec2.getReadName())){
            			differences.add(rec1);
            			rec1 = iterator1.next();
            		}
            	}
            	else{
            		break;
            	}
            }
        }
		
		
	}
	
	private void checkSetForRead(SAMRecord rec){
		if(readNames.contains(rec.getReadName())){
			readNames.remove(rec.getReadName());
		}
		else{
			readNames.add(rec.getReadName());
		}
	}
	
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


}
