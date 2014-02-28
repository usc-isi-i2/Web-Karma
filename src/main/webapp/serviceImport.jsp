<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

</head>
<body>

<div class="modal fade" id="importFromServiceDialog" tabindex="-1">
  <div class="modal-dialog">
		<div class="modal-content">
			<form class="bs-example bs-example-form" role="form">
		     <div class="modal-header">
			      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
			       <h4 class="modal-title">Import From Service</h4>
			  </div>
			  <div class="modal-body">
						<div class="form-group">
							<label for="serviceUrl">Web Service URL</label>
							<input class="form-control" type="url" id="serviceUrl" pattern="https?://.+" required>
						</div>
						<div class="form-group">
							<label for="serviceWorksheetName">Worksheet Name</label>
							<input class="form-control" type="text" id="serviceWorksheetName" maxlength="100" required>
						</div>
						<div class="form-group">
							<label for="serviceEncoding">Encoding</label>
							<select class="form-control" id="serviceEncoding">
								<%@include file="encoding.jsp" %>
							</select>
						</div>
						<label>
							<input type="checkbox" name="includeInputAttributesService" id="includeInputAttributesService">
							Include input attributes in the output worksheet
						</label>
						
						<div class="error" style="display: none" id="serviceErrorRow">Please provide values for both text fields!</div>
					
			  </div>
			  <div class="modal-footer">
			        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
			        <button type="submit" class="btn btn-primary" id="btnSave">Import</button>
			  </div>
			 </form>
		</div><!-- /.modal-content -->
		
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->

</body>
</html>