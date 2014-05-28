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
	private final String serverAddress = "http://localhost:8080/openrdf-sesame/repositories/karma_models";
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		//response.getWriter().println(request.getPathInfo());
		String path = request.getPathInfo();
		String context = "";
		String url = "";
		if (request.getPathInfo() != null) {
			if (path.lastIndexOf("http") != path.indexOf("http")) {
				int pos = path.lastIndexOf("http");
				context = path.substring(1, pos - 1);
				url = path.substring(pos);
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
