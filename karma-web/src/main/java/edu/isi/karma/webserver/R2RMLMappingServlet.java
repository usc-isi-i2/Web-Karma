package edu.isi.karma.webserver;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.json.*;

import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;


public class R2RMLMappingServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = -979319404654953710L;
	private String serverAddress = "";
	private final String R2RML_PUBLISH_DIR = ServletContextParameterMap.getParameterValue(ContextParameter.WEBAPP_PATH) + "/publish/R2RML";
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		serverAddress = "http://" + request.getServerName() + ":" + request.getServerPort();
		if (request.getPathInfo() == null) {
			File directory = new File(R2RML_PUBLISH_DIR);
			File[] contents = directory.listFiles();
			JSONArray array = new JSONArray();
			for (int i = 0; i < contents.length; i++) {
				if (FilenameUtils.getExtension(contents[i].getName()).compareTo("ttl") == 0) {
					JSONObject obj = new JSONObject();
					obj.put("url", serverAddress + "/R2RMLMapping/local/" + contents[i].getName());
					array.put(obj);
				}
			}
			response.setContentType("application/json");
			response.getWriter().println(array);
		}
		else {
			String filename = request.getPathInfo();
			File file = new File(R2RML_PUBLISH_DIR + filename);
			response.setContentType("text/plain");
			if (file.exists() && !file.isDirectory()) {
				Scanner in = new Scanner(file);
				while (in.hasNextLine()) {
					response.getWriter().println(in.nextLine());
				}
				in.close();
			}
			else {
				response.setStatus(404);
			}
		}
	}

}
