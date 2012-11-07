package edu.isi.karma.er.web.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
import edu.isi.karma.er.helper.entity.Paginator;
import edu.isi.karma.er.helper.ontology.MatchResultOntology;
import edu.isi.karma.er.web.service.ResultService;

/**
 * Servlet implementation class ShowMatchResultServlet
 */
public class ShowMatchResultServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
 
    /**
     * Default constructor. 
     */
    public ShowMatchResultServlet() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
     
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String sortBy = request.getParameter("sort_by");
		String page = request.getParameter("page");
		int curPage = 1;
		try {
			curPage = Integer.parseInt(page);
		} catch (NumberFormatException e) {
			
		}
		Paginator pager = new Paginator();
		pager.setCurPage(curPage);
		
		ResultService serv = new ResultService();
		List<MatchResultOntology> resultList = serv.getResultList(pager, sortBy);
		//String filename = serv.getFilename();
		// String datetime = filename.substring(6, filename.lastIndexOf('.'));
		request.setAttribute("pager", pager);
		request.setAttribute("resultList", resultList);
		request.setAttribute("sortBy", sortBy);
		
		request.getRequestDispatcher("show_result.jsp").forward(request, response);
	}

}
