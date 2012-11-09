package edu.isi.karma.er.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.isi.karma.er.helper.ontology.MatchResultOntology;
import edu.isi.karma.er.web.service.ResultService;

public class ListMatchResultHistoryServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1532725411716988169L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String srcUri = request.getParameter("srcUri");
		String dstUri = request.getParameter("dstUri");
		
		String repositoryName = (String)request.getSession().getAttribute("repositoryName");
		
		ResultService ser = new ResultService(repositoryName);
		List<MatchResultOntology> list = ser.listHistory(srcUri, dstUri);
		
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		int i = 0;
		for (MatchResultOntology onto : list) {
			if (i++ > 0) {
				sb.append(",");
			}
			sb.append("{");
			sb.append("\"updated\":\"").append(onto.getUpdated()).append("\"");
			sb.append(",\"comment\":\"").append(onto.getComment()).append("\"");
			sb.append(",\"creator\":\"").append(onto.getCreator()).append("\"");
			sb.append(",\"matched\":\"").append(toResult(onto.getMatched())).append("\"");
			sb.append("}");
		}
		sb.append("]");
		
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.print(sb.toString());
		out.flush();
		out.close();
	}
	
	private String toResult(String matched) {
		String result = null;
		if ("M".equals(matched)) {
			result = "Exact Match";
		} else if ("N".equals(matched)) {
			result = "Not Match";
		} else if ("U".equals(matched)){
			result = "Unsure";
		} else {
			result = "";
		}
		return result;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
}
