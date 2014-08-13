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
#tblDefinedServices tr td {
	vertical-align: middle;
}

</style>
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
					
					<div id="worksheetServiceOptions" style="margin-left:30px;">
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
		                	<div class="radio">
			                	<label>
						    		<input type="radio" id="invokePerRow" name="serviceDataPostMethod" checked value="invokePerRow">
						    		Invoke service one row at a time
					  			</label>
					  		</div>
					  		<div class="radio">
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




<div class="modal fade" id="exportCSVDialog" tabindex="-1">
  <div class="modal-dialog">
  		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
			     <div class="modal-header">
				      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				       <h4 class="modal-title" id="exportCSV_ModelTitle">Export CSV</h4>
				  </div>
				  <div class="modal-body">
				  <div id="csvDialogColumnList">
				  
				  </div>
				  <!-- <div id="csvDialogContent">
						<div class="form-group">
							<label for="csvSPAQRLEndPoint">SPARQL End Point</label>
							<input class="form-control" type="text" id="csvSPAQRLEndPoint" required url value="" maxlength="1000" />
						</div>
						<div class="form-group">
								<label for="csvModelGraphList">Graph</label>
								<select id="csvModelGraphList">
			                	</select>
						</div>
					</div> -->
					<div id="csvDataDialogContent">
						<div class="form-group" id="formDivSparql">
							<label for="csvDataEndPoint">SPARQL End Point</label>
							<input class="form-control" type="text" id="csvDataEndPoint" required url value="" maxlength="1000" />
						</div>
						<div class="form-group" id="formDivGraphs">
								<label for="csvDataGraphList">Graph</label>
								<select id="csvDataGraphList">
			                	</select>
						</div>
					</div>
					<div class="form-group formDivDMUrl" style="display: none">
						<label for="dataMiningUrl">Data Mining URL</label>
							<input class="form-control" type="url" id="dataMiningUrl" required url value="" maxlength="1000" />
						</div>
						<div id="DMresults" style="display: none"></div>
					<div class="error" style="display: none">Please enter all valid url</div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
				        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
				        <!-- <button type="submit" class="btn btn-primary" id="btnSave">Fetch Columns</button> -->
				        <button type="button" class="btn btn-primary" id="btnExportCSV">Export</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->



<div class="modal fade" id="publishRDFDialog" tabindex="-1">
  <div class="modal-dialog">
  		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
			     <div class="modal-header">
				      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				       <h4 class="modal-title">Publish RDF</h4>
				  </div>
				  <div class="modal-body">
					<div class="form-group">
							<label for="modelGraphList">RDF Graphs</label>
							<select id="modelGraphList">
		                	</select>
					</div>
					<div class="form-group">
						<label id="labelFor_rdfSPAQRLGraph" for="rdfSPAQRLGraph">Create New Graph</label>
						<input class="form-control" type="text" id="rdfSPAQRLGraph" required value="" maxlength="100">
					</div>	
					<div class="radio">
						<label>
				    		<input type="radio" id="graphReplace_1" name="group1"  value="replace">
				    		Replace Existing Data
			  			</label>
			  		</div>
			  		<div class="radio">
			  			<label>
				    		<input type="radio" id="graphReplace_2" name="group1" checked value="append">
				    		Append
			  			</label>
					</div>	
					
					<div class="checkbox">
						<label>
				    		<input type="checkbox" id="addInverseProperties" unchecked></input>
				    		Add Inverse Properties
			  			</label>
					</div>	
					<div class="checkbox">
						<label>
				    		<input type="checkbox" id="generateBloomFilters" unchecked></input>
				    		Index Data to Support Joins
			  			</label>
					</div>
					
					<div id="rdfStoreInfo">
						<div class="form-group">
							<div class="col-sm-6">
								<label>
					    			<input class="form-control" type="text" id="hostName" value="localhost:3306" maxlength="100" size="20"/>
					    			Host Name:Port
				  				</label>
				  			</div>
				  			<div class="col-sm-6">
								<label>
					    			<input class="form-control" type="text" id="dbName" value="jenadb" maxlength="100" size="20"/>
					    			Database Name
				  				</label>
				  			</div>
						</div>
						<div class="form-group">
							<div class="col-sm-6">
								<label>
					    			<input class="form-control" type="text" id="username" value="root" maxlength="100" size="20"/>
					    			User Name
				  				</label>
				  			</div>
				  			<div class="col-sm-6">
								<label>
					    			<input class="form-control" type="password" id="password" value="" maxlength="100" size="20"/>
					    			Password
				  				</label>
				  			</div>
						</div>
						<div class="form-group">
							<div class="col-sm-6">
								<label>
					    			<input class="form-control" type="text" id="modelName" value="karma" maxlength="100" size="20"/>
					    			Model Name
				  				</label>
				  			</div>
				  			<div class="col-sm-6">
								
				  			</div>
						</div>
        			</div>
        			
					<div class="error" style="display: none">Please enter all values</div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
				        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
				        <button type="submit" class="btn btn-primary" id="btnSave">Publish</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<div class="modal fade" id="fetchModelDialog" tabindex="-1">
  <div class="modal-dialog">
  		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
			     <div class="modal-header">
				      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				       <h4 class="modal-title">Invoke Service</h4>
				  </div>
				  <div class="modal-body">
					<div class="form-group">
						<label for="txtR2RML_URL_fetch">SPARQL End Point</label>
						<input class="form-control" type="text" id="txtR2RML_URL_fetch" required>
					</div>
					
					<div class="error" style="display: none">Please enter an end point</div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
				        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
				        <button type="submit" class="btn btn-primary" id="btnSave">Fetch</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->
      
<div class="modal fade" id="publishDatabaseDialog" tabindex="-1">
  <div class="modal-dialog">
  		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
			     <div class="modal-header">
				      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				       <h4 class="modal-title">Export to Database</h4>
				  </div>
				  <div class="modal-body">
						<div class="row">
			        		<div class="col-sm-12">
								<div class="form-group">
									<label for="dbType1">Database Type</label>
									<select id="dbType1" class="form-control">
		                                <option>SQLServer</option>
		                                <option>MySQL</option>
		                                <option>Oracle</option>
		                                <option>PostGIS</option>
		                                <option>Sybase</option>
		                            </select>
								</div>
							</div>
						</div>
						<div class="row">
							<div class="col-sm-6">
								<div class="form-group">
									<label for="hostName1">Hostname</label>
									<input type="text" id="hostName1" value="localhost" class="form-control" maxlength="40" size="10" placeholder='localhost' required/></div>
							</div>
			        		<div class="col-sm-6">
			        			<div class="form-group">
									<label for="port1">Port</label>
									<input type="number" id="port1" value="" class="form-control" maxlength="6" size="5" required/>
								</div>
			        		</div>
			        	</div>
			        	<div class="row">
				        		<div class="col-sm-6">
				        			<div class="form-group">
										<label for="userName1">Username</label>
										<input type="text" id="userName1" value="root" class="form-control" maxlength="20" size="10" required/>
									</div>
				        		</div>
				        		<div class="col-sm-6">
				        			<div class="form-group">
										<label for="password1">Password</label>
										<input type="password" id="password1" value="" class="form-control" maxlength="20" size="10" required/>
									</div>
				        		</div>
				        </div>
				        <div class="row">
				        		<div class="col-sm-6">
				        			<div class="form-group">
										<label for="dbName1">Database</label>
	                            		<input type="text" id="dbName1" value="" class="form-control" maxlength="20" size="10" required/>
	                        
									</div>
				        		</div>
				        		<div class="col-sm-6">
				        			<div class="form-group">
										<label for="tableName1">Tablename</label>
	                            		<input type="text" id="tableName1" value="" class="form-control" maxlength="20" size="10" required/>
	                        
									</div>
				        		</div>
			        		
			        	</div>	<!--  end of row -->
						
						
							<div class="radio">
								<label>
						    		<input type="radio" name="overwriteInstruction" value="overwrite">
						    		Overwrite Table
					  			</label>
							</div>	
							<div class="radio">
								<label>
						    		<input type="radio" name="overwriteInstruction" value="insert" checked>
						    		Insert in Table
					  			</label>
							</div>
						
					<div class="error" style="display: none">Please enter all values</div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
				        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
				        <button type="submit" class="btn btn-primary" id="btnSave">Export</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->


<div class="modal fade" id="foldDialog" tabindex="-1">
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

<div class="modal fade" id="glueDialog2" tabindex="-1">
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
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
				        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
				        <button type="submit" class="btn btn-primary" id="btnSave">Submit</button>
				   </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->   

<div class="modal fade" id="groupByDialog2" tabindex="-1">
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

<div class="modal fade" id="organizeColumnsDialog" tabindex="-1">
  <div class="modal-dialog">
  		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
			     <div class="modal-header">
				      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				       <h4 class="modal-title">Organize Columns</h4>
				  </div>
				  <div class="modal-body">
					<div id="organizeColumns_body" class="cf nestable-lists">
						<div class="dd" id="nestable">
						</div>
					</div>
					<div class="error" style="display: none">Error</div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
				        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
				        <button type="submit" class="btn btn-primary" id="btnSave">Save</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->
             
<div class="modal fade" id="publishJSONDialog" tabindex="-1">
  <div class="modal-dialog">
  		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
			     <div class="modal-header">
				      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				       <h4 class="modal-title">Save as JSON</h4>
				  </div>
				  <div class="modal-body">
					Would you like to also import the JSON as a new Worksheet?
					<div class="error" style="display: none">Error</div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
				        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
				        <button type="submit" class="btn btn-default" id="btnNo">No, just Export as JSON</button>
				        <button type="submit" class="btn btn-primary" id="btnYes">Yes, Import as Worksheet</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->

<div class="modal fade" id="applyModelDialog" tabindex="-1">
  <div class="modal-dialog modal-wide">
  		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
			     <div class="modal-header">
				      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				       <h4 class="modal-title">Apply Model From Repository</h4>
				  </div>
				  <div class="modal-body">
				  	<h4>Select Model:</h4>
				  	<div id="applyModelDialogHeaders"></div>
						<div id="applyModelDialogColumns" style="max-height: 300px; overflow: auto;"></div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
				        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
				        <button type="submit" class="btn btn-primary" id="btnSave">Apply</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->


<div class="modal fade" id="invokeServicesDialog" >
  <div class="modal-dialog modal-wide">
  		<form class="bs-example bs-example-form" role="form">
			<div class="modal-content">
			     <div class="modal-header">
				      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
				       <h4 class="modal-title" id="modal_title">Invoke Services</h4>
				  </div>
				  <div class="modal-body">
				  <div id="service_list">
				  <table class="table table-condensed table-hover" id="tblDefinedServices">
				  	<thead>
				  		<tr>
					  		<th>Label</th>
					  		<th>Service Url</th>
					  		<th>Method</th>
				  		</tr>
				  	</thead>
				  </table>
				  </div>
				  <div id="csvDataDialogContent">
						<div class="form-group" id="formDivSparql">
							<label for="spaqrlEndPoint">SPARQL End Point</label>
							<input class="form-control" type="text" id="spaqrlEndPoint" required url value="" maxlength="1000" />
						</div>
						<div class="form-group" id="formDivGraphs">
								<label for="graphList">Graph</label>
								<select id="graphList">
			                	</select>
						</div>
					</div>
				  </div> <!-- /.modal-body -->
				  <div class="modal-footer">
				        <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
				        <button type="button" class="btn btn-primary" id="btnInvokeServices">Invoke</button>
				  </div> <!-- /.modal-footer -->
			</div><!-- /.modal-content -->
		</form>
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->

</body>
</html>