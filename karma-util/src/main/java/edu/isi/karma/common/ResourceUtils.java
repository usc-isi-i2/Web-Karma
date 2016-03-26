package edu.isi.karma.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

/**
 * Class ResourceUtils
 *
 * @since 03/12/2014
 */
public class ResourceUtils {

	private ResourceUtils() {
	}

	public static List<String> getListOfFiles(String classpathPath) throws IOException {
		InputStream resourceAsStream = ResourceUtils.class.getClassLoader().getResourceAsStream("csv/");
		return IOUtils.readLines(resourceAsStream);
	}
}
