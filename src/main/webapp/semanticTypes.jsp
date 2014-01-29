<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

</head>
<body>


<div class="modal fade" id="setSemanticTypeDialog" tabindex="-1">
  <div class="modal-dialog">
		<div class="modal-content">
			<form class="bs-example bs-example-form" role="form">
		     <div class="modal-header">
			      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
			       <h4 class="modal-title">Set Semantic Type for: <span id="semanticType_columnName"></span></h4>
			  </div>
			  <div class="modal-body">
			  	
			  		<div class="form-group">
				  		<B>Semantic Types:</B>
				  		<table id="semanticTypesTable" class="table table-striped table-condensed">
				  			<tr><th /><th /><th>Primary</th><th/></tr>
			  			</table>
			  		</div>
			  		
			  		
			  		<button type="button" id="addType" class="btn btn-default">Add synonym Semantic Type</button>
			  		
			        
                	<div class="checkbox">
					  <label>
					    <input type="checkbox" id="chooseClassKey">
					    Mark as key for the class
					  </label>
					</div>
					
					<label>
						Literal Type:
							<select class="form-control" id="literalTypeSelect">
								<option></option>
								<option>xsd:string</option>
		                        <option>xsd:boolean</option>
		                        <option>xsd:decimal</option>
		                        <option>xsd:integer</option>
		                        <option>xsd:double</option>
		                        <option>xsd:float</option>
		                        <option>xsd:time</option>
		                        <option>xsd:dateTime</option>
		                        <option>xsd:dateTimeStamp</option>
		                        <option>xsd:gYear</option>
		                        <option>xsd:gMonth</option>
		                        <option>xsd:gDay</option>
		                        <option>xsd:gYearMonth</option>
		                        <option>xsd:gMonthDay</option>
		                        <option>xsd:duration</option>
		                        <option>xsd:yearMonthDuration</option>
		                        <option>xsd:dayTimeDuration</option>
		                        <option>xsd:byte</option>
		                        <option>xsd:short</option>
		                        <option>xsd:int</option>
		                        <option>xsd:long</option>
		                        <option>xsd:unsignedByte</option>
		                        <option>xsd:unsignedShort</option>
		                        <option>xsd:unsignedInt</option>
		                        <option>xsd:unsignedLong</option>
		                        <option>xsd:positiveInteger</option>
		                        <option>xsd:nonNegativeInteger</option>
		                        <option>xsd:negativeInteger</option>
		                        <option>xsd:nonPositiveInteger</option>
		                        <option>xsd:hexBinary</option>
		                        <option>xsd:base64Binary</option>
		                        <option>xsd:anyURI</option>
		                        <option>xsd:language</option>
		                        <option>xsd:normalizedString</option>
		                        <option>xsd:token</option>
		                        <option>xsd:NMTOKEN</option>
		                        <option>xsd:Name</option>
		                        <option>xsd:NCName</option>
							</select>
					</label>
						
					<div class="row" style="padding-top:10px">
						<div class="col-sm-11">
						<button type="button" id="semanticTypesAdvancedOptions" class="btn btn-default">Advanced Options</button>
						</div>
					</div>
					<div class="row">
						<div id="semanticTypesAdvacedOptionsDiv" style="display: none; padding-top:10px" class="col-sm-12">
							<div class="input-group form-group">
						       <span class="input-group-addon">
						          <input type="checkbox" id="isUriOfClass">
						       </span>
							   <span class="input-group-addon">contains URI for node</span>
						      <input type="text" class="form-control" id="isUriOfClassTextBox" autocomplete="off">
						    </div><!-- /input-group -->
	    
	    					<div class="input-group form-group">
						       <span class="input-group-addon">
						          <input type="checkbox" id="isSubclassOfClass">
						       </span>
							   <span class="input-group-addon">specifies class for node</span>
						      <input type="text" class="form-control" id="isSubclassOfClassTextBox" autocomplete="off">
						    </div><!-- /input-group -->
						    
						    <div class="input-group form-group">
						       <span class="input-group-addon">
						          <input type="checkbox" id="isSpecializationForEdge">
						       </span>
							   <span class="input-group-addon">specifies specialization for edge</span>
						      <input type="text" class="form-control" id="isSpecializationForEdgeTextBox" autocomplete="off">
						    </div><!-- /input-group -->
	                	</div>
                	</div>
                	
					<div class="error" style="display: none">Please select the file format!</div>
				
			  </div>
			  <div class="modal-footer">
			        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
			        <button type="submit" class="btn btn-primary" id="btnSave">Save</button>
			  </div>
			 </form>
		</div><!-- /.modal-content -->
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->


        
</body>