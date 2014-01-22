<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

</head>
<body>

<div class="modal fade" id="setPropertiesDialog" tabindex="-1">
  <div class="modal-dialog">
  		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
			     <div class="modal-header">
				      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				       <h4 class="modal-title">Set Properties</h4>
				  </div>
				  <div class="modal-body">
					<div class="form-group">
						<label for="graphNameInput">Model Identifier</label>
						<input class="form-control" type="text" id="graphNameInput" required>
					</div>
				
					<label>
					    <input type="checkbox" id="serviceOptions">
					    Set Service Options
				  	</label>
					<br />
					
					<div id="worksheetServiceOptions" style="display: none; margin-left:30px;">
						<div class="form-group">
							<label for="serviceUrlInput">Service URL</label>
							<input class="form-control" type="text" id="serviceUrlInput" required>
						</div>
						<div class="form-group">
							<label for="serviceRequestMethod">Request Method</label>
							<select id="serviceRequestMethod">
			                    <option value="GET">GET</option>
			                    <option value="POST">POST</option>
		                	</select>
						</div>
		                
		                <div style="display: none; margin-left: 30px;" id="servicePostOptions">
		                	<div class="form-group">
			                	<label>
						    		<input type="radio" id="invokePerRow" name="serviceDataPostMethod" checked value="invokePerRow">
						    		Invoke service one row at a time
					  			</label>
					  		</div>
					  		<div class="form-group">
					  			<label>
						    		<input type="radio" id="invokeWholeWorksheet" name="serviceDataPostMethod" value="invokeWithWholeWorksheet">
						    		Post whole worksheet
					  			</label>
					  		</div>
		                </div>
		            </div>
				
					
					<div class="error" style="display: none">Please enter a model identifier</div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
				        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
				        <button type="submit" class="btn btn-primary" id="btnSave">Submit</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->

        
</body>
</html>