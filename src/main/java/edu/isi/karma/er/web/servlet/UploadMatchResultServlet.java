package edu.isi.karma.er.web.servlet;

import java.io.IOException;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jspsmart.upload.File;
import com.jspsmart.upload.SmartUpload;
import com.jspsmart.upload.SmartUploadException;

import edu.isi.karma.er.web.service.ResultService;

public class UploadMatchResultServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		throw new IllegalArgumentException("This page can not be accessed by url");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean result = false;
		int count = 0;
		String repoName = null;
		SmartUpload su = new SmartUpload();
		su.initialize(this.getServletConfig(), request, response);
		
		su.setAllowedFilesList("csv");
		try {
			su.upload();
			//int count = su.save("/upload");
			File file = su.getFiles().getFile(0);
			
			String ext= file.getFileExt(); 
			Calendar calendar = Calendar.getInstance();  
			String filename = String.valueOf(calendar.getTimeInMillis());   
			String saveurl= this.getServletContext().getRealPath("/") + "upload/";  
			saveurl += filename+"."+ext; 
			
			file.saveAs(saveurl);
			
			repoName = su.getRequest().getParameter("inputName");
			
			ResultService serv = new ResultService(repoName);
			count = serv.createOrUpdateRespository(saveurl);
			System.out.println("count updated:" + count);
			if (count > 0) {
				result = true;
				boolean exists = false;
				for (String name : ResultService.getRepositoryList()) {
					if (name.equals(repoName)) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					serv.addRepositoryToList(repoName);
				}
			}
			
		} catch (SmartUploadException e) {
			e.printStackTrace();
		}
		
		if (result == true) {
			response.sendRedirect("ShowMatchResultServlet?repositoryName=" + repoName);
		} else {
			request.getRequestDispatcher("upload_result.jsp").forward(request, response);
		}
		
	}
}
