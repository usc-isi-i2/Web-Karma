package edu.isi.karma.kr2rml;

import java.lang.reflect.Field;
import java.util.BitSet;

import org.apache.hadoop.util.bloom.BloomFilter;

public class KR2RMLBloomFilter extends BloomFilter {

	static Field bitsField;
	static
	{
		try {
			bitsField = BloomFilter.class.getDeclaredField("bits");
			bitsField.setAccessible(true);
		} catch (SecurityException | NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		int setBits = 0;
		try {
			BitSet bits = (BitSet)bitsField.get(this);

			for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i+1)) {
				setBits++;
			 }
			double N = this.getVectorSize();
			int k = this.nbHash;
			num = (int)(-(N * Math.log(1 - (setBits / N))) / (double)k);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return num;
		
	}
}
