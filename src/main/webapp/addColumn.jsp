<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

</head>
<body>

<div class="modal fade" id="addColumnDialog" tabindex="-1">
  <div class="modal-dialog">
  		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
			     <div class="modal-header">
				      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				       <h4 class="modal-title">Add New Column</h4>
				  </div>
				  <div class="modal-body">
					<div class="form-group">
						<label for="columnName">Enter new Column Name</label>
						<input class="form-control" type="text" id="columnName" required>
					</div>
				
					<div class="form-group">
						<label for="defaultValue">Default Value</label>
						<input class="form-control" type="text" id="defaultValue" required>
					</div>
				
					<div class="form-group">
						<a href="SRID.html" target='_blank'>Click to see SRID List</a>
					</div>
				
					
					<div class="error" id="addColumnError" style="display: none">Please enter a unique column name!</div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
				        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
				        <button type="submit" class="btn btn-primary" id="btnSave">Add</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->

</body>
</html>