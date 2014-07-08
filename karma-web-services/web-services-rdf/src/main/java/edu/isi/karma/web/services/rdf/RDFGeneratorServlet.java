package edu.isi.karma.web.services.rdf;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RDFGeneratorServlet extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = -979319404654953710L;
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
				response.getWriter().println("Hello world Germany!");
	
	}

}
