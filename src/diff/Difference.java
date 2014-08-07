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
import java.util.List;

import picard.PicardException;

public class Difference {

	private List<SAMRecord> differences = new ArrayList<SAMRecord>();
	
	public Difference(File infile1, File infile2) {
		IOUtil.assertFileIsReadable(infile1);
		IOUtil.assertFileIsReadable(infile2);
		
		final SamHeaderAndIterator headerAndIterator1 = openInputs(infile1);
		final SamHeaderAndIterator headerAndIterator2 = openInputs(infile2);
		
		final CloseableIterator<SAMRecord> iterator1 = headerAndIterator1.iterator;
		final CloseableIterator<SAMRecord> iterator2 = headerAndIterator2.iterator;
        while (iterator1.hasNext() &&  iterator2.hasNext()) {
            final SAMRecord rec1 = iterator1.next();
            final SAMRecord rec2 = iterator2.next();
            if (rec1 != rec2){
              
            }
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
