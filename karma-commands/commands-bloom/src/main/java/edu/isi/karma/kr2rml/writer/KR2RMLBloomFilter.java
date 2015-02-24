package edu.isi.karma.kr2rml.writer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.BitSet;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.util.bloom.BloomFilter;
//import org.apache.hadoop.util.bloom.BloomFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KR2RMLBloomFilter extends BloomFilter {

	private static final Logger LOG = LoggerFactory.getLogger(KR2RMLBloomFilter.class);
	public static final int defaultVectorSize = 1000000;
	public static final int defaultnbHash = 8;
	static Field bitsField;
	static
	{
		try {
			bitsField = BloomFilter.class.getDeclaredField("bits");
			bitsField.setAccessible(true);
		} catch (SecurityException | NoSuchFieldException e) {
			LOG.error("Unable to set up KR2RMLBloomFilter: " + e.getMessage());
		}
	}
	public KR2RMLBloomFilter(int vectorSize, int nbHash, int hashType) {
		super(vectorSize,nbHash, hashType);
	}
	public KR2RMLBloomFilter() {
		super();
	}
	public int estimateNumberOfHashedValues()
	{
		int num = 0;
		try {
			BitSet bits = (BitSet)bitsField.get(this);

			int setBits = bits.cardinality();
			double N = this.getVectorSize();
			int k = this.nbHash;
			double tmp = -(N * Math.log(1 - (setBits / N))) / (double)k;
			num = (int) Math.round(tmp);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			LOG.error("Unable to estimate number of hashed values: " + e.getMessage());
		}
		return num;
		
	}
	
	public String compressAndBase64Encode() throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream(getVectorSize() + 1000);
		String base64EncodedCompressedSerializedBloomFilter = null;
		ObjectOutputStream dout = new ObjectOutputStream(new DeflaterOutputStream(baos));
		write(dout);
		dout.flush();
		dout.close();
		base64EncodedCompressedSerializedBloomFilter = Base64.encodeBase64String(baos.toByteArray());
		return base64EncodedCompressedSerializedBloomFilter;
	}
	
	public void populateFromCompressedAndBase64EncodedString(String base64EncodedBloomFilter) throws IOException
	{
		byte[] serializedBloomFilter = Base64.decodeBase64(base64EncodedBloomFilter);
		readFields(new ObjectInputStream(new InflaterInputStream(new ByteArrayInputStream(serializedBloomFilter))));
	}
}
