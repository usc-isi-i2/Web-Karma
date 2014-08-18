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
				                <div class="col-sm-6"><button type="button" id="addType" class="btn btn-default pull-right">Add Row</button></div>
				            </div>
					  		<div id="semanticTypesTableDiv">
						  		<table id="semanticTypesTable" class="table table-striped table-condensed">
						  			<tr><th /><th /><th>Primary</th><th/><th/></tr>
					  			</table>
					  		</div>
				  		</div>
			  		</div>
			  		 
                	<div class="row">
            			<div class="col-sm-6">
  			  				<div class="checkbox">
						  		<label>
						    		<input type="checkbox" id="chooseClassKey">
						    		Mark as key for the class
						  		</label>
							</div>
            			</div>
            			<div class="col-sm-6">
							<label>
								Literal Type:
								<input type="text" class="form-control" id="literalTypeSelect" autocomplete="off">
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

</body>