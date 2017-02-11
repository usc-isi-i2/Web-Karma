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
			  		<div class="row">
				  		<div class="col-sm-12">
					  		<div class="row">
				                <div class="col-sm-6"><b>Semantic Types:</b></div>
				                <div class="col-sm-6"><button type="button" id="addType" class="btn btn-default pull-right" style="padding: 0px 12px">Add Row</button></div>
				            </div>
					  		<div id="semanticTypesTableDiv">
						  		<table id="semanticTypesTable" class="table table-striped table-condensed">
						  			<tr><th /><th width="60%"></th><th>Primary</th><th class="sem-provenance">Provenance</th><th/><th/></tr>
					  			</table>
					  		</div>
				  		</div>
			  		</div>
			  		 
                	<div class="row">
            			<div class="col-sm-6">
							<label>
								Literal Type:
								<input type="text" class="form-control" id="literalTypeSelect" autocomplete="off">
							</label>
            			</div>
            			<div class="col-sm-6">
							<label>
								Language:
								<input type="text" class="form-control" id="languageSelect" autocomplete="off">
							</label>
        				</div>
  					</div>
						
					<div class="row">
						<div class="col-sm-11">
							<button type="button" id="semanticTypesAdvancedOptions" class="btn btn-default">Advanced Options</button>
						</div>
					</div>
					
					<div class="row">
						<div id="semanticTypesAdvacedOptionsDiv" style="display: none; padding-top:10px" class="col-sm-12">
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


<div class="modal fade" id="incomingOutgoingLinksDialog" tabindex="-1">
  <div class="modal-dialog">
		<div class="modal-content">
			<form class="bs-example bs-example-form" role="form">
		     <div class="modal-header">
			      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
			       <h4 class="modal-title"><span id="incomingOutgoingLinksDialog_title"></span></h4>
			  </div>
			  <div class="modal-body">
			  	
			  		<div class="main"></div>
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

 <div class="modal fade" id="manageIncomingOutgoingLinksDialog" tabindex="-1">
  <div class="modal-dialog modal-medium">
		<div class="modal-content">
			<form class="bs-example bs-example-form" role="form">
		     <div class="modal-header">
			      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
			       <h4 class="modal-title">Incoming-Outgoing Links for: <span id="columnName"></span></h4>
			  </div>
			  <div class="modal-body">
			  	
			  		<div class="form-group">
			  			<B>Incoming Links:</B>
			  			<div id="incomingLinksDiv">
					  		<table id="incomingLinksTable" class="table table-striped table-condensed">
				  			</table>
				  		</div>
				  	</div>
			  			
			  		<div class="form-group">
			  			<B>Outgoing Links:</B>
			  			<div id="outgoingLinksDiv">
					  		<table id="outgoingLinksTable" class="table table-striped table-condensed">
				  			</table>
				  		</div>
				  	</div>
				  	
				  	<div class="error" style="display: none">Please select the file format!</div>
				  	
			  </div>
			  <div class="modal-footer">
			  		<button type="button" class="btn btn-default" id="btnAddIncomingLink">Add Incoming Link</button>
				  	<button type="button" class="btn btn-default" id="btnAddOutgoingLink">Add Outgoing Link</button>
			        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
			        <button type="submit" class="btn btn-primary" id="btnSave">Save</button>
			  </div>
			 </form>
		</div><!-- /.modal-content -->
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<div class="modal fade" id="augmentDataDialog" tabindex="-1">
  <div class="modal-dialog modal-wide">
  		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
			     <div class="modal-header">
				      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				       <h4 class="modal-title" id="augmentHeader">Augment Data</h4>
				  </div>
				  <div class="modal-body">
				  	<div id="augmentDataDialogHeaders"></div>
						<div id="augmentDataDialogColumns" style = "max-height: 300px; overflow: auto;"></div>
						<div class="form-group">
							<label for="altPredicate">"Same As" property</label>
							<input class="form-control" type="text" value="owl:sameAs" id="altPredicate" style = "width: 150px;"required>
						</div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
				        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
				        <button type="submit" class="btn btn-primary" id="btnSave">Submit</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<div class="modal fade" id="exportJSONDialog" tabindex="-1">
  <div class="modal-dialog modal-wide">
  		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
			     <div class="modal-header">
				      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				       <h4 class="modal-title" id="augmentHeader">Export JSON</h4>
				  </div>
				  <div class="modal-body">
						<div class="form-group">
							<div class="checkbox">
							  	<label>
								    <input type="checkbox" id="useContext"required>
								    Use @Context
							  	</label>
							  	
								<div id="useContextControl">
									<div class="radio">
									  	<label>
									    	<input type="radio" id="useContextFromModel" name="context">
									    	Use @Context From Model
									  	</label>
									</div>
									<div class="radio">
									  	<label>
									    	<input type="radio" id="useContextFromURL" name="context">
									    	Use @Context From URL
									  	</label>
									  	<input type="text" id="useContextFromURLText" class="form-control">
									</div>
									<div class="radio">
									  	<label>
										    <input type="radio" id="useContextFromFile" name="context">
										    Use Uploaded @Context
									  	</label>
									  	<br/>
									  	<div class="btn btn-primary fileinput-button">
							  				<span>Upload Context</span>
							  				<input type="file" name="files[]" id="contextupload" multiple>
					  					</div>
					  			
									</div>
								</div>
							</div>
						</div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
				        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
				        <button type="submit" class="btn btn-primary" id="btnSave">Submit</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->
       
<div class="modal fade" id="classDialog" tabindex="-1">
  <div class="modal-dialog modal-wide">
		<div class="modal-content">
			<form class="bs-example bs-example-form" role="form">
		     <div class="modal-header">
			      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
			       <h4 class="modal-title"><span id="classDialog_title"></span></h4>
			  </div>
			  <div class="modal-body">
			  		<div class="main">
			  			<div class="row">
			  				<div class="col-sm-2" id="classDialogFunctions"></div>
			  				<div class="col-sm-10" id="classDialogRight"></div>			  				
			  			</div>
						<div class="error" style="display: none"></div>
					</div>
			  </div>
			 </form>
		</div><!-- /.modal-content -->
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<div id="classTabs" style="display:none">
	<ul class="nav nav-tabs" role="tablist" id="class_tabs">
	    <li role="presentation" class="active"><a href="#class_recommended" aria-controls="class_recommended" role="tab" data-toggle="tab">Recommended</a></li>
	    <li role="presentation"><a href="#class_compatible" aria-controls="class_compatible" role="tab" data-toggle="tab">Compatible</a></li>
	    <li role="presentation"><a href="#class_all" aria-controls="class_all" role="tab" data-toggle="tab">All</a></li>
  	</ul>

	<!-- Tab panes -->
	<div class="tab-content">
    	<div role="tabpanel" class="tab-pane active" id="class_recommended">
    		<ul class="list-unstyled row"></ul>
    	</div>
    	<div role="tabpanel" class="tab-pane" id="class_compatible">
    		<ul class="list-unstyled row"></ul>
    	</div>
    	<div role="tabpanel" class="tab-pane" id="class_all">
    		<ul class="list-unstyled row"></ul>
    		<div class="input-group col-sm-offset-6">
		  		<input type="text" class="form-control" aria-describedby="basic-input_classDialog-addOn" id="input_classDialog">
				<span class="input-group-addon glyphicon glyphicon-search" id="basic-input_classDialog-addOn" style="top:0px"></span>
			</div>
		</div>
  	</div>
</div>

<div class="modal fade" id="propertyDialog" tabindex="-1">
  <div class="modal-dialog modal-wide">
		<div class="modal-content">
			<form class="bs-example bs-example-form" role="form">
		     <div class="modal-header">
			      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
			       <h4 class="modal-title"><span id="propertyDialog_title"></span></h4>
			  </div>
			  <div class="modal-body">
			  		<div class="main">
			  			<div class="row">
			  				<div class="col-sm-2" id="propertyDialogFunctions"></div>
			  				<div class="col-sm-10" id="propertyDialogRight"></div>
			  			</div>
			  		</div>
					<div class="error" style="display: none"></div>
			  </div>
			 </form>
		</div><!-- /.modal-content -->
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->


	<div id="propertyTabs" style="display:none">
		<ul class="nav nav-tabs" role="tablist" id="property_tabs">
    		<li role="presentation" class="active"><a href="#property_recommended" aria-controls="property_recommended" role="tab" data-toggle="tab">Recommended</a></li>
    		<li role="presentation"><a href="#property_compatible" aria-controls="property_compatible" role="tab" data-toggle="tab">Compatible</a></li>
    		<li role="presentation"><a href="#property_all" aria-controls="property_all" role="tab" data-toggle="tab">All</a></li>
  		</ul>

	  	<!-- Tab panes -->
	  	<div class="tab-content">
    		<div role="tabpanel" class="tab-pane active" id="property_recommended">
    			<ul class="list-unstyled row"></ul>
    		</div>
	    	<div role="tabpanel" class="tab-pane" id="property_compatible">
	    		<ul class="list-unstyled row"></ul>
	    	</div>
	    	<div role="tabpanel" class="tab-pane" id="property_all">
	    		<ul class="list-unstyled row"></ul>
	    		<div class="input-group col-sm-offset-6">
			  		<input type="text" class="form-control" aria-describedby="basic-input_propertyDialog-addOn" id="input_propertyDialog">
					<span class="input-group-addon glyphicon glyphicon-search" id="basic-input_propertyDialog-addOn" style="top:0px"></span>
				</div>

	    	</div>
  		</div>
	</div>

	<div id="propertyAdvanceOptions" style="display:none">
		<div class="row" id="advOptionsLiteralTypeRow">
			<div class="form-group col-sm-6">
				<label>
					Literal Type:
					<input type="text" class="form-control" id="propertyLiteralType" autocomplete="off">
				</label>
			</div>
			<div class="form-group col-sm-6">
				<label>
					Language:
					<input type="text" class="form-control" id="propertyLanguage" autocomplete="off">
				</label>
			</div>
		</div>
		<div class="row" id="advOptionsClassRow">
			<div class="form-group">
				<div class="checkbox">
				  	<label>
					    <input type="checkbox" id="propertyIsSubclass" name='subclass'>
					    specifies class for node
				  	</label>
				</div>
			</div>
		</div>			  	
		<div class="row">
			<div class="form-group">
				<div class="checkbox">
				  	<label>
					    <input type="checkbox" id="propertyIsProvenance" name='provenance'>
					    specifies provenance
				  	</label>
				</div>
			</div>
		</div>	
		<div class="row">
			<button type="submit" class="btn btn-primary" id="btnSaveAdvanceOptions">Save</button>
		</div>
	</div>			

<div class="modal fade" id="addNodeDialog" tabindex="-1">
  <div class="modal-dialog">
		<div class="modal-content">
			<form class="bs-example bs-example-form" role="form">
		     <div class="modal-header">
			      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
			       <h4 class="modal-title">Add Node</h4>
			  </div>
			  <div class="modal-body">
			  	
			  		<div class="main"></div>
					<div class="error" style="display: none"></div>
				
			  </div>
			  <div class="modal-footer">
			        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
			        <button type="submit" class="btn btn-primary" id="btnSave">Add</button>
			  </div>
			 </form>
		</div><!-- /.modal-content -->
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<div class="modal fade" id="addLiteralNodeDialog" tabindex="-1">
  <div class="modal-dialog">
		<div class="modal-content">
			<form class="bs-example bs-example-form" role="form">
		     <div class="modal-header">
			      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
			       <h4 class="modal-title">Add Literal Node</h4>
			  </div>
			  <div class="modal-body">
			  	<div class="row">
			  		<div class="col-sm-6" id="col-literal">
				  		<div class="form-group">
							<label for="literal">Literal:</label>
							<input type="text" id="literal" class="form-control">
						</div>
						<div class="row" id="literalTypeRow">
							<div class="form-group col-sm-6">
							    <label>
							    	Type:
							    	<input type="text" id="literalType" class="form-control">
							    </label>
							    
							</div>
							<div class="form-group col-sm-6">
								<label>
									Language:
									<input type="text" class="form-control" id="literalLanguage" autocomplete="off">
								</label>
							</div>
						</div>
						<div class="checkbox" id="isUriRow">
						  <label>
						    <input type="checkbox" name="isUri" id="isUri">
						   Is URI?
						  </label>
						</div>
					</div>
					<div class="col-sm-6" id="col-property">
					
					</div>
				</div>
				
				<div class="error" style="display: none"></div>
				
			  </div>
			  <div class="modal-footer">
			        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
			        <button type="submit" class="btn btn-primary" id="btnSave">Add</button>
			  </div>
			 </form>
		</div><!-- /.modal-content -->
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->


</body>