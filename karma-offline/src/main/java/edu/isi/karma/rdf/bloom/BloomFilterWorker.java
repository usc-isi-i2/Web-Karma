package edu.isi.karma.rdf.bloom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.hadoop.util.hash.Hash;

import edu.isi.karma.kr2rml.writer.KR2RMLBloomFilter;
public class BloomFilterWorker implements Runnable{
	private KR2RMLBloomFilter bf = new KR2RMLBloomFilter(KR2RMLBloomFilter.defaultVectorSize, KR2RMLBloomFilter.defaultnbHash, Hash.JENKINS_HASH);
	private List<String> bloomfilters = Collections.synchronizedList(new ArrayList<String>());
	private boolean isFinished = false;
	private boolean isDone = false;

	public void addBloomfilters(String bloomfilter) {
		synchronized (bloomfilters) { 
			bloomfilters.add(bloomfilter);
			bloomfilters.notify();
		}
	}

	public KR2RMLBloomFilter getKR2RMLBloomFilter() {
		return bf;
	}

	public boolean isFinished() {
		return isFinished;
	}

	private String getBloomfilter() {
		synchronized (bloomfilters) {
			if (bloomfilters.isEmpty())
				try {
					bloomfilters.wait();;
				} catch (InterruptedException e) {

				}
			if (!bloomfilters.isEmpty()) {
				return bloomfilters.remove(0);
			}
		}
		return null;
	}

	public void setDone() {
		isDone = true;
		synchronized (bloomfilters) {
			bloomfilters.notify();
		}
	}

	@Override
	public void run() {

		while(!isDone || !bloomfilters.isEmpty()) {
			KR2RMLBloomFilter bf = new KR2RMLBloomFilter(KR2RMLBloomFilter.defaultVectorSize, KR2RMLBloomFilter.defaultnbHash, Hash.JENKINS_HASH);
			String tmp = getBloomfilter();
			try {
				if (tmp != null)
				{
					bf.populateFromCompressedAndBase64EncodedString(tmp);
				}
					
			} catch (IOException e) {
				
			}
			this.bf.or(bf);
		}
		isFinished = true;

	}	


}
