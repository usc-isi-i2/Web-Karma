package edu.isi.karma.webserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LinkedApiServiceHandler extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uid = request.getParameter("uid");
		if(uid != null)
			response.getWriter().write("Request ID: " + uid);
		else
			response.getWriter().write("No Service Request ID Found!");
		response.flushBuffer(); 
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uid = request.getParameter("uid");
		if(uid != null)
			response.getWriter().write("Request ID: " + uid);
		else
			response.getWriter().write("No Service Request ID Found!");
		response.flushBuffer(); 
	}
}
