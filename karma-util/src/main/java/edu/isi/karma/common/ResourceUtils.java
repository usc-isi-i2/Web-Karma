package edu.isi.karma.common;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Class ResourceUtils
 *
 * @since 03/12/2014
 */
public class ResourceUtils {

	public static List<String> getListOfFiles(String classpathPath) throws IOException {
		InputStream resourceAsStream = ResourceUtils.class.getClassLoader().getResourceAsStream("csv/");
		return IOUtils.readLines(resourceAsStream);
	}
}
