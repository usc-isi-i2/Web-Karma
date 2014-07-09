package edu.isi.karma.web.services.rdf;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/hello")
public class RDFGeneratorServlet{

	/**
	 * 
	 
	private static final long serialVersionUID = -979319404654953710L;
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
				response.getWriter().println("Hello world Germany!");
	
	}*/
	
	
	@GET
	@Path("/world")
	public Response getMsg ()
	{
		String output = "Jersey says Hello World";
		return Response.status(200).entity(output).build();
	}

}
