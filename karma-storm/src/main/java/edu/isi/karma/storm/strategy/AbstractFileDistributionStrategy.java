package edu.isi.karma.storm.strategy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;

public abstract class AbstractFileDistributionStrategy implements
		FileDistributionStrategy {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger LOG = LoggerFactory.getLogger(AbstractFileDistributionStrategy.class);
	private String fileName;
	private boolean isZipped;
	private Path materializedFilePath;
	private Path tempDirPath;
	
	public AbstractFileDistributionStrategy(String fileName, boolean isZipped)
	{
		this.fileName = fileName;
		this.isZipped = isZipped;
	}
	@Override
	public void prepare(@SuppressWarnings("rawtypes") Map globalConfig) {
		try
		{
			tempDirPath = Files.createTempDirectory("karma_home", new FileAttribute[0]);
			File tempDir = tempDirPath.toFile();
			tempDir.deleteOnExit();
			InputStream serializedFileStream = getStream();
			if(isZipped)
			{
				ZipUtil.unpack(serializedFileStream, tempDir);
				materializedFilePath = FileSystems.getDefault().getPath(tempDir.getAbsolutePath(), "karma");
			}
			else
			{
				Path filePath = FileSystems.getDefault().getPath(tempDir.getAbsolutePath(), fileName);
				Files.copy(serializedFileStream,filePath, new CopyOption[0]);
				materializedFilePath = filePath;
			}
			LOG.info("Materialized file at: " + materializedFilePath.toAbsolutePath());
		}
		catch (Exception e)
		{
			LOG.error("Unable to materialize file: " + fileName, e);
		}
		
	}
	
	protected abstract InputStream getStream() throws IOException;

	@Override
	public String getPath() {
		return materializedFilePath.toString();
	}
	
	public void cleanup()
	{
		if(tempDirPath != null)
		{
			try {
				FileUtils.deleteDirectory(tempDirPath.toFile());
			} catch (IOException e) {
				LOG.error("Unable to delete directory: " + tempDirPath.toAbsolutePath());
			}
		}
	}
}
