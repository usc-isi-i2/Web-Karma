package edu.isi.karma.storm.strategy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class URLFileDistributionStrategy extends AbstractFileDistributionStrategy {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private URL fileURL;
	
	public URLFileDistributionStrategy(URL fileURL, String fileName, boolean isZipped)
	{
		super(fileName, isZipped);
		this.fileURL = fileURL;
	}
	
	@Override
	protected InputStream getStream() throws IOException {
		InputStream serializedFileStream = fileURL.openStream();
		return serializedFileStream;
	}
}
