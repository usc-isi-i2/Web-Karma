<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

</head>
<body>

<!--
----------------------------------------
	Dialog to select File Format: CSV/XML/..
----------------------------------------- 
 -->
<div class="modal fade" id="fileFormatSelectionDialog">
  <div class="modal-dialog">
		<div class="modal-content">
		     <div class="modal-header">
			      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
			       <h4 class="modal-title">Confirm File Format</h4>
			  </div>
			  <div class="modal-body">
			        
					<div class="radio">
					  <label>
					    <input type="radio" name="FileFormatSelection" id="CSVFileFormat" value="CSVFile">
					    CSV Text File
					  </label>
					</div>
					<div class="radio">
					  <label>
					    <input type="radio" name="FileFormatSelection" id="JSONFileFormat" value="JSONFile" checked>
					    JSON
					  </label>
					</div>
					<div class="radio">
					  <label>
					    <input type="radio" name="FileFormatSelection" id="XMLFileFormat" value="XMLFile">
					    XML
					  </label>
					</div>
					<div class="radio">
					  <label>
					    <input type="radio" name="FileFormatSelection" id="XLSFileFormat" value="ExcelFile">
					    Excel Spreadsheet
					  </label>
					</div>
					<div class="radio">
					  <label>
					    <input type="radio" name="FileFormatSelection" id="OWLFileFormat" value="Ontology">
					    OWL Ontology
					  </label>
					</div>
					<div>
						<hr />
					</div>
					<div class="checkbox">
					  <label>
					    <input type="checkbox" name="RevisionCheck">
					    Revision of worksheet <select id="revisedWorksheetSelector"></select>
					  </label>
					</div>
					<div class="error" style="display: none" id="fileFormatError">Please select the file format!</div>
			  </div>
			  <div class="modal-footer">
			        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
			        <button type="button" class="btn btn-primary" id="btnSave">Save changes</button>
			  </div>
		</div><!-- /.modal-content -->
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->
			
<!--
----------------------------------------
	Dialog to File OPtions: Encoding, NUmber of lines to import etc
----------------------------------------- 
 -->			

<div class="modal fade" id="fileOptionsDialog">
  <div class="modal-dialog">
		<div class="modal-content">
		     <div class="modal-header">
			      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
			       <h4 class="modal-title">File Import Options: <span id="filename"></span></h4>
			  </div>
			  <div class="modal-body">
			  		<form class="bs-example bs-example-form" role="form">
			  			
			        	<div class="row" id="optionsLine1">
			        		<div class="col-lg-6">
								<div class="form-group" id="colDelimiterSelector">
									<label for="delimiterSelector">Column Delimiter</label>
									<select id="delimiterSelector" class="form-control ImportOption">
		                                <option>comma</option>
		                                <option>tab</option>
		                                <option>space</option>
		                            </select>
								</div>
							</div>
							<div class="col-lg-6">
								<div class="form-group" id="colTextQualifier">
									<label for="textQualifier">Text Qualifier</label>
									<input class="form-control ImportOption" type="text" placeholder='"' id="textQualifier">
								</div>
							</div>
			        	</div>
			        	<div class="row" id="optionsLine2">
			        		<div class="col-lg-6" id="colHeaderStartIndex">
			        			<div class="form-group">
									<label for="headerStartIndex">Header Start Index</label>
									<input class="form-control ImportOption" type="text" placeholder="1" id="headerStartIndex">
								</div>
			        		</div>
			        		<div class="col-lg-6" id="colStartRowIndex">
			        			<div class="form-group">
									<label for="startRowIndex">Data Start Row Index</label>
									<input class="form-control ImportOption" type="text" placeholder="2" id="startRowIndex">
								</div>
			        		</div>
			        	</div>
			        	<div class="row" id="optionsLine3">
			        		<div class="col-lg-6" id="colEncoding">
			        			<div class="form-group">
									<label for="encoding">Encoding</label>
									<select id="encoding" class="form-control ImportOption">
                        				<%@include file="encoding.jsp" %>
                        			</select>
								</div>
			        		</div>
			        		<div class="col-lg-6" id="colMaxNumLines">
			        			<div class="form-group">
									<label for="maxNumLines">Number of lines to import</label>
									<input class="form-control ImportOption" type="text" placeholder="1" id="maxNumLines">
								</div>
			        		</div>
			        	</div>
					</form>
					<div id="previewTableDiv" style="overflow:auto">
						<hr />
		                <span class="label label-default">Preview (Top 5 Rows)</span>
		                <br>
		                <table id="previewTable" class="table table-striped table-condensed"></table>
		            </div>
			  </div>
			  <div class="modal-footer">
			        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
			        <button type="button" class="btn btn-primary" id="btnSave">Save changes</button>
			  </div>
		</div><!-- /.modal-content -->
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->
			
        
</body>
</html>