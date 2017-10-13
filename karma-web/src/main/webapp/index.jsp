
<%@page import="edu.isi.karma.config.ModelingConfiguration"%>
<%@page import="edu.isi.karma.config.ModelingConfigurationRegistry"%>
<%@page import="edu.isi.karma.webserver.ContextParametersRegistry"%>
<%@page import="edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter"%>
<%@page import="edu.isi.karma.webserver.ServletContextParameterMap"%>
<%@page import="edu.isi.karma.config.UIConfiguration"%>
<%@page import="edu.isi.karma.config.UIConfigurationRegistry"%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<!--

Copyright 2012 University of Southern California

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

This code was developed by the Information Integration Group as part
of the Karma project at the Information Sciences Institute of the
University of Southern California.  For more information, publications,
and related projects, please see: http://www.isi.edu/integration

-->
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">

        <meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta http-equiv="cache-control" content="max-age=0" />
		<meta http-equiv="cache-control" content="no-cache" />
		<meta http-equiv="expires" content="0" />
		<meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT" />
		<meta http-equiv="pragma" content="no-cache" />
        
        <title>Karma Data Integration</title>
       
        <link rel="stylesheet" type="text/css" href="./uiLibs/twitterBootstrap/css/bootstrap.min.css" media="screen">
        <link rel="stylesheet" type="text/css" href="./uiLibs/jquery/css/jquery-ui-themes/smoothness/jquery-ui.min.css" />
        <link rel="stylesheet" type="text/css" href="./uiLibs/bootstrap3-editable/css/bootstrap-editable.css" />
        <link rel="stylesheet" type="text/css" href="./uiLibs/jquery/css/jquery.fileupload.css" />
        <link rel="stylesheet" type="text/css" href="./uiLibs/jquery/css/jquery.fileupload-ui.css" />
        <link rel="stylesheet" type="text/css" href="./uiLibs/jquery/css/jquery.qtip.min.css" />
        <link rel="stylesheet" type="text/css" href="./uiLibs/jquery/css/nestable.css" />
        <link rel="stylesheet" type="text/css" href="./uiLibs/sticky/css/sticky.min.css" />
        <link rel="stylesheet/less" type="text/css" href="./uiLibs/less/css/styles-03d-fixed-pixel.less" />
        <link rel="stylesheet" type="text/css" href="./uiLibs/jquery/css/jstree-themes/default/style.css" />
        <link href="./css/sticky-footer-navbar.css?<jsp:include page='version.jsp'/>" rel="stylesheet">
        
        <link rel="stylesheet" type="text/css" href="./css/TopMenuBar.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/dropdown.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/command_History.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/tables_workspace.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/fileImport.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/DatabaseImport.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/ServiceImport.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/headerParsingExample.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/AlignmentHeaders.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/alignment.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/d3.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/main.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/pyTransform.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/cleaningChart.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/model.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/augmentdata.css?<jsp:include page='version.jsp' />" />
        <%

        if(UIConfigurationRegistry.getInstance().getUIConfiguration(ContextParametersRegistry.getInstance().getDefault().getId()).isForceModelLayoutEnabled()) {

        %>
        <link rel="stylesheet" href="css/d3-model-layout.css?<jsp:include page='version.jsp' />" />
		<%
        } else {
		%>
        <link rel="stylesheet" href="css/d3-wide-model-layout.css?<jsp:include page='version.jsp' />" />
      	<%
        }
      	%>
		<style type="text/css">
			div.sticky {
    		/*	color: #555555; */
    			font-size: 14px;
			}
			
			
			.container {
			    width: auto;
			}
			
			a:hover, a:focus {
				text-decoration: none;
				color: black;
			}
			
			.table-header-container a {
				color: #CCCCCC;
			}
			
			.table-header-container a:hover,  .table-header-container a:focus{
				color: #AAAAAA;
			}
			
			
			.collapse.in {
			    height: auto;  /* this style already exists in bootstrap.css */
			    overflow: visible;  /* this one doesn't. Add it! */
			}
			
			.contextMenu {
			  position: absolute;
			  display:none;
			}
			
			/* Override for removing a dashed line below editable items */
			.editable-click, a.editable-click, a.editable-click:hover {
				border-bottom: 0px;
			}
			.editableform .form-control {
			    /* width: auto; */
			    width: 300px;
			}
			
			.editable-empty, .editable-empty:hover, .editable-empty:focus {
				color: #cccccc;
			}

			.wk-row-selected {
    		background-color: #EEEEEE;
			}

			.table-no-border td {
			    border-top: 0 none;
			}

			.table-no-border > thead > tr > th, .table > tbody > tr > th, .table-no-border > tfoot > tr > th, .table > thead > tr > td, .table-no-border > tbody > tr > td, .table-no-border > tfoot > tr > td {
			    border-top: 0px;
			}

		</style>
	
	</head>

	<body>
	
		<div class="container">
		
			<div id="karmaHeader" class="navbar navbar-default navbar-inverse" role="navigation">
		        <div class="navbar-header">
		          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
		            <span class="sr-only">Toggle navigation</span>
		            <span class="icon-bar"></span>
		            <span class="icon-bar"></span>
		            <span class="icon-bar"></span>
		          </button>
		          <a class="navbar-brand" href="#">Karma 
		              	<span id="karma-version"><jsp:include page="version.jsp" /></a>
		        </div>
		        <div class="navbar-collapse collapse">
		          <ul class="nav navbar-nav col-sm-5">
		            <li class="dropdown">
		              <a href="#" class="dropdown-toggle" data-toggle="dropdown">Import <b class="caret"></b></a>
		              <ul class="dropdown-menu multi-level">
		                  <li><a href="#" id="importDatabaseTableButton">Database Table</a></li>
		                  <li><a href="#" id="importSQLButton">Using SQL</a></li>
		                  <li><a href="#" id="importFromServiceButton" >From Service</a></li>
		                  <li><a href="#" class="fileinput-button"><form id="fileupload" action="ImportFileCommand" method="POST" enctype="multipart/form-data">From File<input type="file" name="files[]" multiple></form></a></li>
		              </ul>
		            </li>
		            
		            <li><a href="#" id="modelManagerButton" data-html='true' data-toggle='tooltip' data-placement='bottom'>Manage Models</a></li>

		            <li class="dropdown">
		              <a href="#" class="dropdown-toggle" data-toggle="dropdown">Settings <b class="caret"></b></a>
		              <ul class="dropdown-menu multi-level">
		              	  <li><a href="#" id="displayGithubSettings">Github</a></li>
		                  <li class="dropdown-submenu"><a href="#" id="settingDisplayRDFSLabel">Display rdfs:label</a>
		                  	<ul class="dropdown-menu">
		                  		<li><a href="#" id="displayRDFSLabel_labelFirst"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span>&nbsp;rdfs:label First</a></li>
								<li><a href="#" id="displayRDFSLabel_idFirst"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span>&nbsp;Name or ID First</a></li>
		                  	</ul>
		                  </li>
		                  <li><a href="#" id="r2rmlExportSuperclass"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span>&nbsp;Export Superclass</a>
		                  </li>
		              </ul>
		            </li>

					<li><a href="#" id="resetButton" data-html='true' title='Delete all saved files,<br/>use with care!' data-toggle='tooltip' data-placement='bottom'>Reset ...</a></li>
					
		            <li>
		            		<div class="span5 fileupload-progress fade">
								<!-- The global progress bar -->
								<div class="progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100">
									<div class="bar" style="width:0%;"></div>
								</div>
								<!-- The extended global progress information -->
								<div class="progress-extended">
									&nbsp;
								</div>
								
							</div>
		            	
		            </li>
		          </ul>
		          <ul class="nav navbar-nav navbar-middle col-sm-2">
		            <li id="modeHeader" style="display:none;"><a href="#" style="cursor: none"></a></li>
		          </ul>
		          
		          <ul class="nav navbar-nav navbar-right">
		            <li><a target="_blank" href="https://github.com/InformationIntegrationGroup/Web-Karma/wiki" title='View user guide in GitHub' data-toggle='tooltip' data-placement='bottom'>User Guide</a></li>
			        <li><a target="_blank" href="http://isi.edu/integration/karma" title='Open the Karma home page in a new window' data-toggle='tooltip' data-placement='bottom'>Karma Home</a></li>
			        <li><a target="_blank" href="" id="sparql_end_point_link" title='Open RDF workbench in a new window' data-toggle='tooltip' data-placement='bottom'>OpenRDF</a></li>
		          </ul>
		        </div>
		      </div> <!--  end of navbar -->

		      <div class="row">
		      	<div class="col-sm-12">
			      	<div id="tablesWorkspace"></div>
			     </div>
		      </div>
		      
		      <jsp:include page="fileImport.jsp"></jsp:include>
		      <jsp:include page="serviceImport.jsp"></jsp:include>
			  <jsp:include page="databaseImport.jsp"></jsp:include>
			  <jsp:include page="reset.jsp"></jsp:include>
			  <jsp:include page="tableColumnDialogs.jsp"></jsp:include>
			  <jsp:include page="selection.jsp"></jsp:include>
			  <jsp:include page="tableOptionsDialogs.jsp"></jsp:include>
			  <jsp:include page="semanticTypes.jsp"></jsp:include>
			  <jsp:include page="showModel.jsp"></jsp:include>
			  <jsp:include page="model.jsp"></jsp:include>
			  <jsp:include page="settings.jsp"></jsp:include>
			  
			  <div class="modal fade" id="karmaErrorWindow" tabindex="-1">
			  	<div class="modal-dialog">
			  		<form class="bs-example bs-example-form" role="form">
						<div class="modal-content">
						     <div class="modal-header">
							      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
							       <h4 class="modal-title">Error Report</h4>
							  </div>
							  <div class="modal-body">
								<div id="errrorText">&nbsp;</div>
							  </div> <!-- /.modal-body -->
						</div><!-- /.modal-content -->
					</form>
				</div><!-- /.modal-dialog -->
			</div><!-- /.modal -->

		</div>
 		
 		<div id="footer">
	      <div class="container">
	        <p class="text-muted">
	        	<div class="row">
	        		<div class="col-sm-4">
	        			<div class="form-group">
	        				<label>Karma Home: </label>
	        				<span id="karmaHome"><%=ContextParametersRegistry.getInstance().getDefault().getParameterValue(ContextParameter.USER_DIRECTORY_PATH) %></span>
	        			</div>
	        		</div>
	        		<div class="col-sm-4">
	        			<div class="form-group">
							<label for="txtModel_URL">Model Endpoint: </label>
							<span class="edit" id="txtModel_URL"></span>
						</div>
	        		</div>
	        		<div class="col-sm-4">
	        			<div class="form-group">
							<label for="txtData_URL">Data Endpoint: </label>
							<span class="edit" id="txtData_URL"></span>
						</div>
	        		</div>
	        	</div>
	        </p>
	      </div>
	    </div>
    
        <div id="ScrollHeadersPlaceholder"></div>
       
        
        <div id="drawBigChartId" style="display: none">
            <div id="bigChartTitle" ></div>
        </div>

        <div id="modelListDiv" style="display: none">
            <span class="smallSizedFont">
                <form id="modelListRadioBtnGrp"></form>
            </span>
        </div>
        <div id="invokeDMServiceDiv" style="display: none">
            <span class="smallSizedFont" id="invokeDMServiceSpan">
            </span>
        </div>
        
        <div id="ColumnSelection" style="display: none">
            <table id="allcolumns">
                <tr>
                    <td class="cleaningTableHeading noBorder">Selected</td>
                    <td class="examplesDivider noBorder"></td>
                    <td class="cleaningTableHeading noBorder" colspan=3>Columns</td>
                </tr>
            </table>
        </div>

        <div id="helpDiv" style="display:none; position:absolute;left:0px; top:0px; width:100%">
        </div>

		<div id="WaitingDiv" style="display:none; position:absolute;left:0px; top:0px;width:100%; height:100%" class="waitingDiv">
			<div style="width:10px;height:50px">&nbsp;</div>
			<div>Loading Workspace..</div>
            <img src="images/ajax-loader.gif" />
		</div>
		
        <!--  Load all scripts last for faster page load -->
        
        <!-- Third Party JavaScript files		 -->
        <script type="text/javascript" src="uiLibs/jquery/js/jquery-1.10.2.min.js"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/jquery-ui-1.10.3.custom.min.js"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/nestable.js"></script>
		<script type="text/javascript" src="uiLibs/twitterBootstrap/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="uiLibs/twitterBootstrap/js/bootstrap3-typeahead.js"></script>
		<script type="text/javascript" src="uiLibs/bootstrap3-editable/js/bootstrap-editable.min.js"></script>
		
        <script type="text/javascript" src="uiLibs/jquery/js/jquery.tmpl.min.js"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/jquery.hoverIntent.js"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/jquery.jstree.js"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/jquery.qtip.min.js"></script>
        
        <%
        if(UIConfigurationRegistry.getInstance().getUIConfiguration(ContextParametersRegistry.getInstance().getDefault().getId()).isGoogleEarthEnabled()) {
        %>
        <script type="text/javascript" src="https://www.google.com/jsapi?key=AIzaSyDEvzzmlVOhVWTy13y5r6OPt5BRNR5QKsg&sensor=false"></script>
        <%
        }
        %>
        <script type="text/javascript" src="uiLibs/sticky/js/sticky.min.js"></script>

        <script type="text/javascript" src="uiLibs/json/js/json2.js"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/jquery.cookie.js"></script>
        <script type="text/javascript" src="uiLibs/d3/js/d3.v3.5.16.min.js"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/jquery.iframe-transport.js"></script>
        <script type="text/javascript" src="uiLibs/ace/js/ace.js" charset="utf-8"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/jquery.fileupload.js"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/jquery.fileupload-ui.js"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/jquery.fileupload-jquery-ui.js"></script>
        <script type="text/javascript" src="uiLibs/locale/js/locale.js"></script>
        <script type="text/javascript" src="uiLibs/less/js/less-1.4.1.min.js"></script>
        <script type="text/javascript" src="uiLibs/modernizr/js/modernizr.custom.59953.js"></script>
		
        <!-- Home grown JavaScript files -->
        <script type="text/javascript" src="js/ServerResponseObjectParsing.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/initWS.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/commandHistory.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/publishRDF.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/serviceImport.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/pager.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/geospatial.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/databaseImportDialog.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/d3-model-manager.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/fileImport.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/reset-options.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/cleaning-charts.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/showModel.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/worksheetOptions.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/tableColumnOptions.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/tableOptions.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/util.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/semanticTypes.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/classUI.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/propertyUI.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/classDialog.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/classTabs.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/classFunctions.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/anchorDropdown.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/propertyDialog.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/propertyTabs.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/model-layout.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/UnconnectedNodesLayout.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/model.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/saveSvgAsPng.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/historyOptions.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/settings.js?<jsp:include page='version.jsp' />"></script>
        
        <%
        if(UIConfigurationRegistry.getInstance().getUIConfiguration(ContextParametersRegistry.getInstance().getDefault().getId()).isForceModelLayoutEnabled()) {
        %>
		<script type="text/javascript" src="js/d3-model-layout.js?<jsp:include page='version.jsp' />"></script>
		<%
        } else {
        %>
        <script type="text/javascript" src="js/d3-wide-model-layout.js?<jsp:include page='version.jsp' />"></script>
        <%
        }
        %>
        <script>
        	var googleEarthEnabled = <%=UIConfigurationRegistry.getInstance().getUIConfiguration(ContextParametersRegistry.getInstance().getDefault().getId()).isGoogleEarthEnabled()%>;
        	var ontologyAligment = <%=ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ContextParametersRegistry.getInstance().getDefault().getId()).getOntologyAlignment()%>;
        	var knownModelsAlignment = <%=ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ContextParametersRegistry.getInstance().getDefault().getId()).getKnownModelsAlignment()%>;
        	var forceLayoutEnabled = <%=UIConfigurationRegistry.getInstance().getUIConfiguration(ContextParametersRegistry.getInstance().getDefault().getId()).isForceModelLayoutEnabled()%>;
        	var DEFAULT_PROPERTY_URI = "<%=ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ContextParametersRegistry.getInstance().getDefault().getId()).getDefaultProperty()%>";
        	var showRDFSLabel_LabelFirst = <%=UIConfigurationRegistry.getInstance().getUIConfiguration(ContextParametersRegistry.getInstance().getDefault().getId()).showRDFSLabelWithLabelFirst()%>;
			var showRDFSLabel_IDFirst = <%=UIConfigurationRegistry.getInstance().getUIConfiguration(ContextParametersRegistry.getInstance().getDefault().getId()).showRDFSLabelWithIDFirst()%>;
			var r2rml_export_superclass = <%=ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ContextParametersRegistry.getInstance().getDefault().getId()).getR2rmlExportSuperClass()%>;

            $(function() {
                // Clear the workspace when closing the window
                $(window).bind("beforeunload", function() {
                    var info = new Object();
                    info["workspaceId"] = $.workspaceGlobalInformation.id;
                    info["command"] = "CloseWorkspaceCommand";

                    var returned = $.ajax({
                        url : "RequestController",
                        type : "POST",
                        data : info,
                        dataType : "json",
                        complete : function(xhr, textStatus) {
                            // do nothing
                        },
                        error : function(xhr, textStatus) {
                            // alert("Error while removing the workspace from server memory! " + textStatus);
                        }
                    });
                  });
                  $(window).bind("unload", function() {
                    var info = new Object();
                    info["workspaceId"] = $.workspaceGlobalInformation.id;
                    info["command"] = "CloseWorkspaceCommand";

                    var returned = $.ajax({
                        url : "RequestController",
                        type : "POST",
                        data : info,
                        dataType : "json",
                        complete : function(xhr, textStatus) {
                            // do nothing
                        },
                        error : function(xhr, textStatus) {
                            // alert("Error while removing the workspace from server memory! " + textStatus);
                        }
                    });
                	});

                // Prevent the backspace key from navigating back.
                $(document).unbind('keydown').bind('keydown', function(event) {
                    var doPrevent = false;
                    if(event.keyCode === 8) {
                        var d = event.srcElement || event.target;
                        if((d.tagName.toUpperCase() === 'INPUT' && (d.type.toUpperCase() === 'TEXT' || d.type.toUpperCase() === 'PASSWORD' || d.type.toUpperCase() === 'URL')) || d.tagName.toUpperCase() === 'TEXTAREA') {
                            doPrevent = d.readOnly || d.disabled;
                        } else {
                            doPrevent = true;
                        }
						if(doPrevent) {
							var ans = confirm("Are you sure you want to go back? You will lose all the progress.");
							if(ans) {
								// Go with normal behavior
							} else {
								event.preventDefault();
							}
						}
					}
				});
                $('#sparql_end_point_link').attr('href', 'http://'+window.location.host + '/openrdf-workbench/repositories/');
                
                $('body').delegate('.smallChart', 'click', function() {
                    var pid = $(this).parent().attr('id');
                    //console.log(pid);
                    var chartTitle = drawBigChart(pid);
                    var dialogBox = $("div#drawBigChartId");
                    dialogBox.dialog({width: 550, title: chartTitle
                        , buttons: { "Close": function() { $(this).dialog("close"); } }})
                });
                
                $("#karmaHome").editable({
		       			 type: 'text',
		       			 success: function(response, newValue) {
		       				 var old = this.textContent;
		       				 console.log("Set Karma Home:" + newValue);
		       				 var success = changeKarmaHome(newValue);
		       				 if(!success) {
		       					 return old;
		       				 }
		       			 },
		       			 showbuttons: 'bottom',
		       			 mode: 'popup',
		       			 inputclass: 'worksheetInputEdit'	 
		            });
                
                
            	positionFooter();
            	$(window)
                    .scroll(startPositionFooter)
                    .resize(startPositionFooter);
            	
            	if(ontologyAligment || knownModelsAlignment)
            		showModeHeader("Automatic Mode");

            	loadPropertiesForCache();
            	Settings.getInstance().setDisplayRDFSLabel(showRDFSLabel_LabelFirst, showRDFSLabel_IDFirst);
                Settings.getInstance().setDisplaySuperclass(r2rml_export_superclass);
			});
            
            var footerPositionTimer = null;
            function startPositionFooter() {
            	if(footerPositionTimer != null) {
            		window.clearTimeout(footerPositionTimer);
            	}
            	footerPositionTimer = window.setTimeout(function() {
            		footerPositionTimer = null;
            		positionFooter();
            	}, 100);
            }
            
            function positionFooter() {
            	var footerHeight = 0,
                footerTop = 0,
                $footer = $("#footer");
            	
            	 $footer.css({
                     position: "relative"
            	 });
            	 
                footerHeight = $footer.height();
                footerTop = ($(window).scrollTop()+$(window).height()-footerHeight)+"px";
       
                //console.log("Height:" + footerHeight + ",top=" + footerTop + ":scrollTop:" + $(window).scrollTop() + ":winHeight:" + $(window).height());
                
               if ( ($(document.body).height()+footerHeight) < $(window).height()) {
                   $footer.css({
                        position: "absolute"
                   }).animate({
                        top: footerTop
                   });
               } else {
                   $footer.css({
                        position: "static"
                   });
               }
               
       		}
            
            function showModeHeader(headerTxt) {
            	$("#modeHeader a").html(headerTxt);
            	$("#modeHeader").show();
            }

            function loadPropertiesForCache() {
            	$("div#WaitingDiv").show();
				window.setTimeout(function() {
					PropertyDialog.getInstance();
					ClassDialog.getInstance();
					$("div#WaitingDiv").hide();
				}, 10);
            }
		</script>
		<script type="text/javascript">
			// if(googleEarthEnabled) {
			// 	google.load("earth", "1", {
			// 		"callback" : earthCallback
			// 	});
			// }
			
			// function earthCallback() {
			// 	// alert("Earth namespace loaded!");
			// }
		</script>

        <script type="text/javascript">
            $(document).ready(function(){
                $("[data-toggle='tooltip']").tooltip();
            });
            
        </script>
    </body>
</html>
