<div class="modal fade" id="pyTransformSelectionDialog" tabindex="-1">
	<div class="modal-dialog">
  		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
			     <div class="modal-header">
				      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				       <h4 class="modal-title">PyTransform Selection</h4>
				  </div>
				  <div class="modal-body">
					<br/>
	        <div id="transformCodeEditorSelection"></div>
					<br/>
					<div class="checkbox">
						<label>
				    		<input type="checkbox" id="onErrorSelection" unchecked></input>
				    		Select On Error
			  			</label>
					</div>	
					<button type="button" class="btn btn-default" id="btnErrorsSelection">View Errors</button>
					<button type="button" class="btn btn-default" id="btnPreviewSelection">Preview results for top 5 rows</button>
					<br>
					<div id="pyTransformErrorWindowSelection" style="display:none"></div>
            		<table id="pythonPreviewResultsTableSelection" class="table table-striped table-condensed" style="display: none"></table>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
				        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
				        <button type="submit" class="btn btn-primary" id="btnSaveSelection">Save</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->     