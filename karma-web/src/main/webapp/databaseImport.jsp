<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

</head>
<body>

<div class="modal fade" id="databaseImportDialog" tabindex="-1">
  <div class="modal-dialog modal-wide">
		<div class="modal-content">
		     <div class="modal-header">
			      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
			       <h4 class="modal-title">Import Database Table</h4>
			  </div>
			  <div class="modal-body">
			  		<form class="bs-example bs-example-form" role="form">
			  			
			        	<div class="row">
			        		<div class="col-sm-2">
								<div class="form-group">
									<label for="databaseTypeSelector">Database Type</label>
									<select id="databaseTypeSelector" class="form-control DatabaseImportOption">
		                                <option>SQLServer</option>
		                                <option>MySQL</option>
		                                <option>Oracle</option>
		                                <option>PostGIS</option>
		                                <option>Sybase</option>
		                                <option>Ingres</option>
		                            </select>
								</div>
							</div>
							<div class="col-sm-10">
								<div class="col-sm-2">
									<div class="form-group">
										<label for="DatabaseHostName">Hostname</label>
										<input type="text" id="DatabaseHostName" value="localhost" class="form-control DatabaseImportOption" maxlength="40" size="10" placeholder='localhost' required/></div>
								</div>
				        		<div class="col-sm-2">
				        			<div class="form-group">
										<label for="DatabasePortNumber">Port</label>
										<input type="text" id="DatabasePortNumber" value="" class="form-control DatabaseImportOption" maxlength="6" size="5" required/>
									</div>
				        		</div>
				        		<div class="col-sm-2">
				        			<div class="form-group">
										<label for="DatabaseUsername">Username</label>
										<input type="text" id="DatabaseUsername" value="" class="form-control DatabaseImportOption" maxlength="20" size="10" required/>
									</div>
				        		</div>
				        		<div class="col-sm-2">
				        			<div class="form-group">
										<label for="DatabasePassword">Password</label>
										<input type="password" id="DatabasePassword" value="" class="form-control DatabaseImportOption" maxlength="20" size="10" required/>
									</div>
				        		</div>
				        		<div class="col-sm-2">
				        			<div class="form-group">
										<label for="DatabaseName" id="lblDatabase">Database</label>
	                            		<input type="text" id="DatabaseName" value="" class="form-control DatabaseImportOption" maxlength="20" size="10" required/>
	                        
									</div>
				        		</div>
				        		<div class="col-sm-1">
				        			<div class="form-group">
										<label for="btnConnect">&nbsp;</label>
	                            		<button type="submit" class="btn btn-primary form-control" id="btnConnect">OK</button>
	                        
									</div>
				        			
				        		</div>
			        		</div>
			        	</div>	<!--  end of row -->
					</form>

					<div id="databaseImportError" class="error"></div>
					
					<div class="row">
						<hr />
					</div>
					
					<div class="row" id="listAndPreview" display="none">
						<div class="col-sm-4" id="tablesDiv">
							<div class="form-group">
								<label for="databaseTableFilterTable">Choose Table</label>
	                            <input type="text" id="databaseTableFilterTable" value="" class="form-control DatabaseImportOption" style="width:90%"/>
	                        </div>
		                    <table class='table table-hover' style="width:90%"></table>
						</div>
						
						<div class="col-sm-7 col-sm-offset-1" id="previewDiv">
							<h5><span id="dbPreviewTableName"></span></h5>
		                   <table class='table table-hover' style="width:90%">
		                   </table>
						</div>
					</div>
			  </div>
			  <div class="modal-footer">
			        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
			  </div>
		</div><!-- /.modal-content -->
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->
        
<div class="modal fade" id="sqlImportDialog" tabindex="-1">
  <div class="modal-dialog modal-wide">
		<div class="modal-content">
		     <div class="modal-header">
			      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
			       <h4 class="modal-title">Import Using SQL</h4>
			  </div>
			  <div class="modal-body">
			  		<form class="bs-example bs-example-form" role="form">
			  			
			        	<div class="row">
			        		<div class="col-sm-2">
								<div class="form-group">
									<label for="databaseTypeSelector">Database Type</label>
									<select id="databaseTypeSelector" class="form-control DatabaseImportOption">
		                                <option>SQLServer</option>
		                                <option>MySQL</option>
		                                <option>Oracle</option>
		                                <option>PostGIS</option>
		                                <option>Sybase</option>
		                                <option>Ingres</option>
		                            </select>
								</div>
							</div>
							<div class="col-sm-10">
								<div class="col-sm-2">
									<div class="form-group">
										<label for="DatabaseHostName">Hostname</label>
										<input type="text" id="DatabaseHostName" value="localhost" class="form-control DatabaseImportOption" maxlength="40" size="10" placeholder='localhost' required/></div>
								</div>
				        		<div class="col-sm-2">
				        			<div class="form-group">
										<label for="DatabasePortNumber">Port</label>
										<input type="number" id="DatabasePortNumber" value="" class="form-control DatabaseImportOption" maxlength="6" size="5" required/>
									</div>
				        		</div>
				        		<div class="col-sm-2">
				        			<div class="form-group">
										<label for="DatabaseUsername">Username</label>
										<input type="text" id="DatabaseUsername" value="" class="form-control DatabaseImportOption" maxlength="20" size="10" required/>
									</div>
				        		</div>
				        		<div class="col-sm-2">
				        			<div class="form-group">
										<label for="DatabasePassword">Password</label>
										<input type="password" id="DatabasePassword" value="" class="form-control DatabaseImportOption" maxlength="20" size="10" required/>
									</div>
				        		</div>
				        		<div class="col-sm-2">
				        			<div class="form-group">
										<label for="DatabaseName" id="lblDatabase">Database</label>
	                            		<input type="text" id="DatabaseName" value="" class="form-control DatabaseImportOption" maxlength="20" size="10" required/>
	                        
									</div>
				        		</div>
			        		</div>
			        	</div>	<!--  end of row -->
			        	<div id="row">
			        		<div class="form-group">
			        			<label for="DatabaseQuery">SQL Query</label>
			        			<textarea rows="4" style="width:100%" id="DatabaseQuery" class="form-control DatabaseImportOption" required>
			        			</textarea>
			        		</div>
			        	</div>
					</form>

					<div id="databaseImportError" class="error"></div>
			
			  </div>
			  <div class="modal-footer">
			        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
			        <button type="submit" class="btn btn-primary" id="btnImport">Import</button>
			  </div>
		</div><!-- /.modal-content -->
	</div><!-- /.modal-dialog -->
</div><!-- /.modal -->
</body>
</html>