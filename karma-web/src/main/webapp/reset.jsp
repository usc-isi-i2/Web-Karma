<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

</head>
<body>

<div class="modal fade" id="resetDialog" tabindex="-1">
  <div class="modal-dialog">
		<div class="modal-content">
		     <div class="modal-header">
			      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
			       <h4 class="modal-title">Reset Options</h4>
			  </div>
			  <div class="modal-body">
			  	<div class="checkbox">
				  <label>
				    <input type="checkbox" id="forgetSemanticTypes">
				    Semantic Types
				  </label>
				</div>
				<div class="checkbox">
				  <label>
				    <input type="checkbox" id="forgetModels">
				    Auto-Saved Models
				  </label>
				</div>
				<div class="checkbox">
				  <label>
				    <input type="checkbox" id="forgetAlignment">
				    Learned Alignment
				  </label>
				</div>
				
				<div class="error" style="display: none" id="resetErrMsg">Please select an option to reset</div>
			  </div>
			  <div class="modal-footer">
			        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
			        <button type="submit" class="btn btn-primary" id="btnSave">Submit</button>
			  </div>
		</div><!-- /.modal-content -->
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->
        
</body>
</html>

