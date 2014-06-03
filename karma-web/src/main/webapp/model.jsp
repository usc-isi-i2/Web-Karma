<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<style>

#csv_columns li:hover {
	cursor: pointer;
	color: red;
}
a.icon-remove:hover {
	cursor: pointer;
	font-weight:  bold;
}

</style>
</head>
<body>
<div class="modal fade" id="saveModelDialog" tabindex="-1">
  <div class="modal-dialog">
  		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
			     <div class="modal-header">
				      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				       <h4 class="modal-title">Save Model</h4>
				  </div>
				  <div class="modal-body">
					<div class="form-group">
						<input class="form-control" type="radio" id="buttonCollection_Save" value="Collection" name="buttonCollection_Save">Collection
						<input class="form-control" type="radio" id="buttonCollection_Save" value="URL" name="buttonCollection_Save" checked="checked">URL
					</div>
					<div class="form-group">
						<label for="txtR2RML_URL">Model URL</label>
						<input class="form-control" type="text" id="txtModel_URL_Save" required>
					</div>
					<div class="form-group">
						<label for="txtR2RML_URL">Graph Name</label>
						<input class="form-control" type="text" id="txtGraph_URL_Save" required>
					</div>

					<div class="form-group">
						<label for="txtR2RML_URL">SPARQL End Point</label>
						<input class="form-control" type="text" id="txtR2RML_URL_Save" required>
					</div>
					
					<div class="error" style="display: none">Please enter an end point</div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
				        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
				        <button type="submit" class="btn btn-primary" id="btnSave">Save</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<div class="modal fade" id="clearModelDialog" tabindex="-1">
  <div class="modal-dialog">
  		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
			     <div class="modal-header">
				      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				       <h4 class="modal-title">Clear Model</h4>
				  </div>
				  <div class="modal-body">

					<div class="form-group">
						<label for="txtR2RML_URL">Graph Name</label>
						<input class="form-control" type="text" id="txtGraph_URL_Clear" required>
					</div>

					<div class="form-group">
						<label for="txtR2RML_URL">SPARQL End Point</label>
						<input class="form-control" type="text" id="txtR2RML_URL_Clear" required>
					</div>
					
					<div class="error" style="display: none">Please enter an end point</div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
				        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
				        <button type="submit" class="btn btn-primary" id="btnSave">Clear</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->

</body>
</html>