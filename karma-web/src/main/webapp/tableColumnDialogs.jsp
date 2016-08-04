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

<div class="modal fade" id="renameColumnDialog" tabindex="-1">
  <div class="modal-dialog">
		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
				 <div class="modal-header">
					  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					   <h4 class="modal-title">Rename Column</h4>
				  </div>
				  <div class="modal-body">
					<div class="form-group">
						<label for="columnName">Enter new Column Name</label>
						<input class="form-control" type="text" id="columnName" required>
					</div>
				
					
					<div class="error" style="display: none">Please enter a valid column name!</div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
						<button type="submit" class="btn btn-primary" id="btnSave">Save</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<div id="SplitByCommaColumnListPanel" style="display: none">
			<span class="smallSizedFont">Specify character delimiter:</span>
			<input class="smallSizedFont" type="text" id="columnSplitDelimiter" value="," maxlength="5" size="5"/>
			</br>
			<span class="smallSizedFont">Enter "space" to use single space</span></br>
			<span class="smallSizedFont">Enter "tab" to use tab</span>
		</div>
		
		
<div class="modal fade" id="splitColumnDialog" tabindex="-1">
	<div class="modal-dialog">
		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
				 <div class="modal-header">
					  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					   <h4 class="modal-title">Split Column</h4>
				  </div>
				  <div class="modal-body">
					<div class="form-group">
						<label for="columnSplitDelimiter">Specify character delimiter</label>
						<input class="form-control" type="text" id="columnSplitDelimiter" required>
						<span class="help-block">Enter "space" to use single space</span>
						<span class="help-block">Enter "tab" to use tab</span>
					</div>
				
					
					<div class="error" style="display: none">Length of the delimter should be 1</div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
						<button type="submit" class="btn btn-primary" id="btnSave">Save</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<div class="modal fade" id="splitValuesDialog" tabindex="-1">
	<div class="modal-dialog">
		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
				 <div class="modal-header">
					  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					   <h4 class="modal-title">Split Values</h4>
				  </div>
				  <div class="modal-body">
					<div class="form-group">
						<div class="form-group">
							<div class="radio">
								<div class="col-sm-5">
								  <label>
									<input type="radio" value="new" name="splitValuesType">
									Name of new column:
									</label>
								</div>
								<div class="col-sm-6"><input class="form-control" type="text" id="valueSplitNewColName"></div>
							 </div>	
							 <div class="radio">
								<div class="col-sm-5">
								  <label>
									<input type="radio" value="edit" name="splitValuesType" id="splitValuesTypeEdit">
									Update existing column:
								</label>
								</div>
								<div class="col-sm-6"><select id="splitValuesUpdateColumns"></select></div>
							</div>
						</div>
						<div class="row">&nbsp;</div>
						<label for="valueSplitDelimiter">Specify character delimiter</label>
						<input class="form-control" type="text" id="valueSplitDelimiter" required>
						<span class="help-block">Enter "space" to use single space</span>
						<span class="help-block">Enter "tab" to use tab</span>
						<span class="help-block">Enter "character" to split by every character</span>
						<span class="help-block">Enter "regex:" followed by a Regular Expression to split using java's String.split method</span>
					</div>
				
					
					<div class="error" style="display: none" id="splitValuesError">Length of the delimter should be 1</div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
						<button type="submit" class="btn btn-primary" id="btnSave">Save</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->

		
<div class="modal fade" id="pyTransformDialog" tabindex="-1">
	<div class="modal-dialog">
		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
				 <div class="modal-header">
					  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					   <h4 class="modal-title">PyTransform Column</h4>
				  </div>
				  <div class="modal-body">
					<div class="form-group">
						<div class="radio">
							<div class="col-sm-5">
							  <label>
								<input type="radio" name="pyTransformType" value="edit">
								Change existing column:
							</label>
							</div>
							<div class="col-sm-6"><span id="pythonTransformEditColumnName"></span></div>
						</div>
						<div class="radio">
							<div class="col-sm-5">
							  <label>
								<input type="radio" name="pyTransformType" value="new">
								Name of new column:
								</label>
							</div>
							<div class="col-sm-6"><input class="form-control" type="text" id="pythonTransformNewColumnName" /></div>
						</div>
					</div>
					<div class="error" style="display: none">Please provide a new unique column name!</div>
					<br/>
					<div class="row">
						<div class="col-sm-1"><button type="button" class="btn btn-default" id="btnPrevCache">&lt;</button></div>
						<div class="col-sm-9"><div id="transformCodeEditor"></div></div>
						<div class="col-sm-1"><button type="button" class="btn btn-default" id="btnNextCache">&gt;</button></div>
					</div>
					<br/>
					<div class="form-group">
					  <label for="pythonTransformErrorDefaultValue" class="col-sm-4">On Error</label>
					  <div class="col-sm-8">
					    <input type="text" class="form-control" id="pythonTransformErrorDefaultValue">
					  </div>
					</div>
					<div class="form-group">
					  <label for="pythonTransformUseJSONOutput" class="col-sm-4">Use JSON Output:</label>
					  <div class="col-sm-8">
					    <input type="checkbox" id="pythonTransformUseJSONOutput">
					  </div>
					</div>
					<br/><br/><br/>
					<button type="button" class="btn btn-default" id="btnErrors">View Errors</button>
					<button type="button" class="btn btn-default" id="btnPreview">Preview results for top 5 rows</button>
					<br>
					<div id="pyTransformErrorWindow" style="display:none"></div>
					<table id="pythonPreviewResultsTable" class="table table-striped table-condensed" style="display: none"></table>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
						<button type="submit" class="btn btn-primary" id="btnSave">Save</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->     
 
 
 <div class="modal fade" id="transformColumnDialog" tabindex="-1">
	<div class="modal-dialog">
		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
				 <div class="modal-header">
					  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					   <h4 class="modal-title">Transform Column</h4>
				  </div>
				  <div class="modal-body">
					<B>Examples you entered:</B>
					<div class="cleaningOverflowDiv">
						<table id="examples" class="table table-striped table-condensed">
						</table>
					</div>	
					<B>Recommended Examples:</B>
					<div class="cleaningOverflowDiv">
						<table id="recmd" class="table table-striped table-condensed">
						</table>
					</div>	
					<B>All Records:</B>
					<div class="cleaningOverflowDivLarge">
						<table id="cleaningExamplesTable" class="table table-striped table-condensed" style="max-height:100px; overflow:auto">
						</table>
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


<div class="modal fade" id="groupByDialog" tabindex="-1">
	 <div class="modal-dialog">
		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
				 <div class="modal-header">
					  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					  <h4 class="modal-title">Group By</h4>
				  </div>
				  <div class="modal-body">
						<h4>Select Columns:</h4>
						<div id="groupByDialogColumns"></div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
						<button type="submit" class="btn btn-primary" id="btnSave">Submit</button>
				   </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->   

<div class="modal fade" id="glueDialog" tabindex="-1">
	 <div class="modal-dialog">
		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
				 <div class="modal-header">
					  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					  <h4 class="modal-title">Glue</h4>
				  </div>
				  <div class="modal-body">
						<h4>Select Columns:</h4>
						<div id="glueDialogColumns"></div>
						<div>
							<select id="glueDialogImplWays">
								<option value="Longest">Longest</option>
								<option value="Shortest">Shortest</option>
								<option value="CrossProduct">Cross Product</option>
							</select>
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

<div class="modal fade" id="unfoldDialog" tabindex="-1">
  <div class="modal-dialog">
		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
				 <div class="modal-header">
					  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					   <h4 class="modal-title">Unfold</h4>
				  </div>
				  <div class="modal-body">
						<h4>Select Column:</h4>
						<div id="unfoldDialogColumns"></div>
						<div id="unfoldOtherColumns">
							<input type="checkbox">&nbsp;Use Other Columns</input>
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

<div class="modal fade" id="foldDialog2" tabindex="-1">
  <div class="modal-dialog">
		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
				 <div class="modal-header">
					  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					   <h4 class="modal-title">Fold</h4>
				  </div>
				  <div class="modal-body">
						<h4>Select Columns:</h4>
						<div id="foldDialogColumns"></div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
						<button type="submit" class="btn btn-primary" id="btnSave">Submit</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->     
		  
<div class="modal fade" id="extractEntitiesDialog" tabindex="-1">
  <div class="modal-dialog">
		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
				 <div class="modal-header">
					  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					   <h4 class="modal-title">Extract Entities</h4>
				  </div>
				  <div class="modal-body">
					<div class="form-group">
						<label for="extractionService_URL">URL for Extraction Service</label>
						<input class="form-control" type="text" id="extractionService_URL" required>
					</div>
					
					<div class="error" style="display: none">Please enter a URL</div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
						<button type="submit" class="btn btn-primary" id="btnSave">OK</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->  

<div class="modal fade" id="extractionCapabilitiesDialog" tabindex="-1">
  <div class="modal-dialog">
		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
				 <div class="modal-header">
					  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					   <h4 class="modal-title">Select Entities for Extraction</h4>
				  </div>
				  <div class="modal-body">
					<div id="userSelection"></div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
						<button type="submit" class="btn btn-primary" id="btnSave">OK</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<div class="modal fade" id="aggregationTransformDialog" tabindex="-1">
  <div class="modal-dialog">
			<div class="modal-content">
				 <div class="modal-header">
					  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
					   <h4 class="modal-title">Aggregation</h4>
				  </div>
				  <div class="modal-body">
				  	<form class="form-horizontal">
					  <div class="form-group">
					    <label for="aggregationNewColumnName" class="col-sm-4">Name of new Column</label>
					    <div class="col-sm-8">
					      <input type="text" class="form-control" id="aggregationNewColumnName" placeholder="New Column Name">
					    </div>
					  </div>
					  <div class="form-group">
					    <label for="aggregationConstructor" class="col-sm-4">Function</label>
					    <div class="col-sm-8">
					      <input type="text" class="form-control" id="aggregationConstructor" placeholder="method(columnName)">
					    </div>
					  </div>
				 	</br>
				 	<div class="form-group">
					    <label for="transformCodeEditorAggregation" class="col-sm-12">Code for Custom Functions</label>
					    <div class="col-sm-12">
					      <div id="transformCodeEditorAggregation"></div>
					    </div>
					  </div>
					<div class="error" id="aggregationError" style="display: none"></div>
					</form>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
						<button type="submit" class="btn btn-primary" id="btnSave">OK</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->
	 
</body>
</html>
