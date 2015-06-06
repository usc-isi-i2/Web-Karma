/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.karma.webserver;


import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

//@RunWith(PowerMockRunner.class)
@PrepareForTest({ KarmaServlet.class, URL.class, HttpURLConnection.class, WorkspaceManager.class, Workspace.class})

public class KarmaServletTest {
	
    /**
     * Our output.
     */
    private HttpServletResponse response;

    /**
     * Our input.
     */
    private HttpServletRequest request;

    /**
     * Instance under tests.
     */
    private KarmaServlet instance;
    
    /**
     * Test workspaceId.
     */
    private String workspaceId = "WSP3";
    
    private PrintWriter writer;

    @Before
    public void setUp() throws Exception
    {
        this.request = PowerMockito.mock(HttpServletRequest.class);
        
        String contextPath = KarmaServlet.class.getResource("").getPath();
        int idx = contextPath.indexOf("/karma-web");
        if(idx != -1)
        	contextPath = contextPath.substring(0, idx) + "/karma-web/src/main/webapp";
    	System.out.println("Got base path:" + contextPath);
    	ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getDefault();
		contextParameters.setParameterValue(ContextParameter.WEBAPP_PATH, contextPath);
		
        Mockito.when(this.request.getHeader("Accept-Encoding")).thenReturn("gzip,deflate,sdch");
        Mockito.when(this.request.getHeader("Accept-Language")).thenReturn("en-US,en;q=0.8");
        Mockito.when(this.request.getHeader("Cache-Control")).thenReturn("max-age=0");
        Mockito.when(this.request.getHeader("Connection")).thenReturn("keep-alive");
        Mockito.when(this.request.getHeader("Host")).thenReturn("localhost:8080");
        Mockito.when(this.request.getHeader("Referer")).thenReturn("http://localhost:8080");
        this.response = PowerMockito.mock(HttpServletResponse.class);
        this.writer = PowerMockito.mock(PrintWriter.class);
        Mockito.when(response.getWriter()).thenReturn(writer);
        this.instance = new KarmaServlet();
        
        
        
    }
	
    @Test
    public void testDoGetWithoutCookies() throws Exception
    {
    	
    	Mockito.when(request.getParameter("hasPreferenceId")).thenReturn("false");
    	
    	
		
    	this.instance.doGet(request, response);
    	Mockito.verify(response).setContentType("application/json");
    	Mockito.verify(response).setStatus(HttpServletResponse.SC_OK);
    	//Mockito.verify(writer).println("{\n  \"workspaceId\" : \"WSP2\" , \n  \"elements\" : [\n    {\n      \"updateType\" : \"WorksheetListUpdate\" , \n      \"worksheets\" : [\n      ]\n    }\n  ]\n}\n");
    	
    }
    @Test
    public void testDoGetWithCookies() throws Exception
    {
    	
    	Mockito.when(request.getParameter("hasPreferenceId")).thenReturn("true");
    	Mockito.when(request.getParameter("workspacePreferencesId")).thenReturn(workspaceId);
    	this.instance.doGet(request, response);
    	Mockito.verify(response).setContentType("application/json");
    	Mockito.verify(response).setStatus(HttpServletResponse.SC_OK);
    	//Mockito.verify(writer).println("{\n  \"workspaceId\" : \"WSP1\" , \n  \"elements\" : [\n    {\n      \"updateType\" : \"WorksheetListUpdate\" , \n      \"worksheets\" : [\n      ]\n    }\n  ]\n}\n");
    }
}

