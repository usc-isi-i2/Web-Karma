package edu.isi.karma.webserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.isi.karma.er.helper.TripleStoreUtil;

public class R2RMLMappingTripleStoreServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4964901214061089214L;
	private String serverAddress = "http://localhost:8080/openrdf-sesame/repositories/karma_models";
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String baseURL = request.getRequestURL().substring(0, request.getRequestURL().lastIndexOf("R2RMLMapping/local/repository/"));
		serverAddress = baseURL + "openrdf-sesame/repositories/karma_models";
		String[] test = request.getParameterValues("modelRepository");
		if (test.length > 0)
			serverAddress = test[0];
		String path = request.getPathInfo();
		String context = "";
		String url = "";
		if (request.getPathInfo() != null) {
			if (path.lastIndexOf("://") != path.indexOf("://")) {
				int pos = path.lastIndexOf("://");
				int length = pos - path.substring(0, pos).lastIndexOf("/");
				context = path.substring(1, pos - length);
				url = path.substring(pos - length + 1);
			}
			else {
				url = path.substring(1);
			}
		}
		TripleStoreUtil util = new TripleStoreUtil();
		//response.getWriter().println(TripleStoreUtil.defaultModelsRepoUrl);
		try {
			response.getWriter().println(util.getMappingFromTripleStore(serverAddress, context, url));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
