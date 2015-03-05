package edu.isi.karma.storm.strategy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


public class LocalFileDistributionStrategy extends AbstractFileDistributionStrategy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private byte[] serializedFile;
	
	public LocalFileDistributionStrategy(byte[] serializedFile, String fileName, boolean isZipped)
	{
		super(fileName, isZipped);
		this.serializedFile = serializedFile;
		
	}

	@Override
	protected InputStream getStream() throws IOException {
		return new ByteArrayInputStream(serializedFile);
	}
}
