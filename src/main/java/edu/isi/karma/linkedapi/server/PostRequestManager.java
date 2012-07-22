package edu.isi.karma.linkedapi.server;

import javax.servlet.http.HttpServletResponse;


public class PostRequestManager extends LinkedApiRequestManager {

	private String input; 
	
	public PostRequestManager(String serviceId, 
			String input,
			ResourceType resourceType, 
			String returnType,
			HttpServletResponse response) {
		super(serviceId, resourceType, returnType, response);
		this.input = input;
	}
	
	/**
	 * checks whether the input has correct RDf syntax or not
	 * @return
	 */
	private boolean validateInputSyntax() {
		return true;
	}
	
	/**
	 * checks if the input data matches with the service input graph
	 * @return
	 */
	private boolean validateInputSemantic() {
		return false;
	}
	
	public void HandleRequest() {
		if (!validateInputSyntax()) {
			
		}
		
		if (!validateInputSemantic()) {
			
		}
		
		// check input validi
	}
	
	
}
