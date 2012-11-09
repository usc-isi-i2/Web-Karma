package edu.isi.karma.er.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.isi.karma.er.helper.ontology.MatchResultOntology;
import edu.isi.karma.er.web.service.ResultService;

public class UpdateMatchResultServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Default constructor. 
     */
    public UpdateMatchResultServlet() {
        
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String srcUri = request.getParameter("srcUri");
		String dstUri = request.getParameter("dstUri");
		String comment = request.getParameter("comment");
		String matched = request.getParameter("matched");
		
		String repositoryName = (String)request.getSession().getAttribute("repositoryName");
		
		ResultService serv = new ResultService(repositoryName);
		MatchResultOntology onto = serv.updateRecord(srcUri, dstUri, matched, comment);
		System.out.println("get" + srcUri + dstUri + comment + matched);
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.write("{\"result\":\"ok\", \"updated\":\"" + onto.getUpdated() + "\"}");
		out.flush();
		out.close();
	}
     
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}
