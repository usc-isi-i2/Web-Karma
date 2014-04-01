<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

</head>
<body>

<div class="modal fade" id="showExistingModelDialog" tabindex="-1">
  <div class="modal-dialog">
  		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
			     <div class="modal-header">
				      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				       <h4 class="modal-title">Apply Existing Model</h4>
				  </div>
				  <div class="modal-body">
					<div class="radio">
						  <label>
						    <input type="radio" name="chooseModelGroup" value="matchingModels" id="chooseMatchingModels">
						    Matching source name
					  	</label>
					</div>
					<div class="radio">
						  <label>
						    <input type="radio" name="chooseModelGroup" value="allModels" id="chooseAllModels" checked>
						    All models
					  	</label>
					</div>
					
					<div id="modelsListPanel">
	                	<table id="modelsList" class="table">
	
	                	</table>
            		</div>
            
					<div class="error" id="addColumnError" style="display: none">Please select a model!</div>
					<div class="italic noItems" style="display: none"><span>none</span></div>
					
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
				        <button type="submit" class="btn btn-primary" id="btnCreateNew">Create New Model</button>
				        <button type="button" class="btn btn-default" id="btnApplySelected">Apply Selected Model</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->


</body>
</html>
