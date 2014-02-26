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
        <link rel="stylesheet" type="text/css" href="./uiLibs/jquery/css/jquery.fileupload.css" />
        <link rel="stylesheet" type="text/css" href="./uiLibs/jquery/css/jquery.fileupload-ui.css" />
        <link rel="stylesheet" type="text/css" href="./uiLibs/jquery/css/jquery.qtip.min.css" />
        <link rel="stylesheet" type="text/css" href="./uiLibs/sticky/css/sticky.min.css" />
        <link rel="stylesheet/less" type="text/css" href="./uiLibs/less/css/styles-03d-fixed-pixel.less" />
        
        <link rel="stylesheet" type="text/css" href="./css/TopMenuBar.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/command_History.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/tables_workspace.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/fileImport.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/DatabaseImport.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/ServiceImport.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/headerParsingExample.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/AlignmentHeaders.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/cleaning.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/alignment.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/d3.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/main.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/pyTransform.css?<jsp:include page='version.jsp' />" />
        <link rel="stylesheet" type="text/css" href="./css/cleaningChart.css?<jsp:include page='version.jsp' />" />
      
		<style type="text/css">
			div.sticky {
    		/*	color: #555555; */
    			font-size: 14px;
			}
			
			@media (min-width: 1600px) {
			  .container {
			    width: 1570px;
			  }
			}

			@media (min-width: 2048px) {
			  .container {
			    width: 2018px;
			  }
			}
			
			@media (min-width: 2560px) {
			  .container {
			    width: 2530px;
			  }
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
		</style>
	</head>

	<body>
	
		<div class="container">
		
			<div class="navbar navbar-default" role="navigation">
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
		          <ul class="nav navbar-nav">
		            <li class="dropdown">
		              <a href="#" class="dropdown-toggle" data-toggle="dropdown">Import <b class="caret"></b></a>
		              <ul class="dropdown-menu">
		                <li><a href="#" id="importDatabaseTableButton">Database Table</a></li>
		                  <li><a href="#" id="importFromServiceButton" >From Service</a></li>
		                  <li><a href="#" class="fileinput-button"><form id="fileupload" action="ImportFileCommand" method="POST" enctype="multipart/form-data">From File<input type="file" name="files[]" multiple></form></a></li>
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
		          <ul class="nav navbar-nav navbar-right">
		            <li><a target="_blank" href="https://github.com/InformationIntegrationGroup/Web-Karma/wiki" title='View user guide in GitHub' data-toggle='tooltip' data-placement='bottom'>User Guide</a></li>
			        <li><a target="_blank" href="http://isi.edu/integration/karma" title='Open the Karma home page in a new window' data-toggle='tooltip' data-placement='bottom'>Karma Home</a></li>
			        <li><a target="_blank" href="" id="sparql_end_point_link" title='Open RDF workbench in a new window' data-toggle='tooltip' data-placement='bottom'>OpenRDF</a></li>
		          </ul>
		        </div>
		      </div> <!--  end of navbar -->
		      
		      <div class="row">
			      <div id="commHistoryAndWorkspace">
			      	<div class="col-sm-2">
				      	<div class="ui-corner-top" id="commandHistory">
				      		<div id="titleCommand" class="ui-corner-top"><span>Command History</span></div>
				      	</div>
			      	</div>
			      	<div class="col-sm-10">
			      		<div id="tablesWorkspace"></div>
			      	 </div>
			      </div>
		      </div>
		      
		      <jsp:include page="fileImport.jsp"></jsp:include>
		      <jsp:include page="serviceImport.jsp"></jsp:include>
			  <jsp:include page="databaseImport.jsp"></jsp:include>
			  <jsp:include page="reset.jsp"></jsp:include>
			  <jsp:include page="tableColumnDialogs.jsp"></jsp:include>
			  <jsp:include page="tableOptionsDialogs.jsp"></jsp:include>
			  <jsp:include page="semanticTypes.jsp"></jsp:include>
		</div>

        <div id="tableCellToolBarMenu" class="ui-corner-all" style="display: none">
            <button id="editCellButton">
                Edit
            </button>
            <button id="expandValueButton">
                Expand
            </button>
        </div>
        <div id="ExpandCellValueDialog" title="Cell Value" style="display: none">
            <div id="cellExpandedValueDiv">
                <span class="mediumSizedFont">Cell Value:</span>
                <br />
                <div id="cellExpandedValue"></div>
            </div>
            <br />
            <div id="RDFValueDiv">
                <span class="mediumSizedFont">RDF triples:</span>
                <br />
                <div id="rdfValue"></div>
            </div>
        </div>

        
        <div id="tableCellEditDiv" style="display: none"></div>
        <div id="r2rmlModelNameDiv" style="display: none"> <textarea id="txtR2RMLModelName" style="width: 100%"> </textarea> </div>
        
        <div id="ScrollHeadersPlaceholder"></div>
       <div id="OntologyAlternativeLinksPanel" style="display: none">
            <span class="smallSizedFont">Choose parent relationship:</span>
            <br />
            <input type="textarea" id="alternativeParentsTableFilter" class="DatabaseImportOption dbTableColumn" size="30"/>
            <br />
            <table id="AlternativeParentLinksTable"></table>
        </div>
        
        <div id="tableCellMenuButtonDiv" style="display: none"></div>
        <div id="columnHeadingMenuButtonDiv" style="display: none"></div>
      

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
         
        <div id="chooseNodeDialog" style="display: none">
            <div id="entitiesFacetPanel" class="smallSizedFont">
                Show
                <input type="radio" name="chooseNodeGroup" value="existingNodes" id="chooseExistingNodes" checked>
                <label for="chooseExistingNodes">Nodes in model</label>
                <!--<input type="radio" name="chooseNodeGroup" value="domains" id="chooseDomain" disabled>-->
                <!--<label for="chooseDomain">Domains of property</label><br>-->
                <input type="radio" name="chooseNodeGroup" value="allNodes" id="chooseAllNodes">
                <label for="chooseAllNodes">All nodes</label><br><br>

                <span class="error" style="display: none">Please select a value!</span>
            </div>
            <label class="smallSizedFont">Search: </label><input type="text" id="nodesTableFilter" class="smallSizedFont" size="25"/><br>
            <div id="entitiesListPanel" class="chooseNodeLinkPanel smallSizedFont">
                <table id="nodesList">

                </table>
            </div>
        </div>
        <div id="chooseLinkDialog" style="display: none">
            <div id="linksFacetPanel" class="smallSizedFont">
                <input type="radio" name="chooseLinkGroup" value="existingLinks" id="chooseExistingLinks" disabled>
                <label for="chooseExistingLinks">Compatible links</label>
                <!--<input type="radio" name="chooseLinkGroup" value="domains" id="choosePropertyWithDomainAndRange" disabled>-->
                <!--<label for="choosePropertyWithDomainAndRange">Property that links</label><br>-->
                <input type="radio" name="chooseLinkGroup" value="allLinks" id="chooseAllLinks" checked>
                <label for="chooseAllLinks">All possible links</label><br><br>

                <span class="error" style="display: none">Please select a value!</span>
            </div>
            <label class="smallSizedFont">Search: </label><input type="text" id="linksTableFilter" class="smallSizedFont" size="25"/><br>
            <div id="linksListPanel" class="chooseNodeLinkPanel smallSizedFont">
                <table id="linksList">

                </table>
            </div>
        </div>

        <div id="rdfGenerationErrorWindow" style="display: none" class="smallSizedFont"></div>
        <div id="alternativeLinkDialog" style="display: none" class="smallSizedFont">
            <input type="radio" name="chooseLinkGroup" value="compatibleLinks" id="showCompatibleLinks">
            <label for="showCompatibleLinks">Compatible links</label>
            <input type="radio" name="chooseLinkGroup" value="allObjectProperties" id="showAllAlternativeLinks" checked>
            <label for="showAllAlternativeLinks">All links</label><br><br>
            <label class="smallSizedFont">Search: </label><input type="text" id="alternateLinksTableFilter" class="smallSizedFont" size="25"/><br>
            <span class="error" style="display: none">Please select a value!</span><br>
            <table id="alternativeLinksList">

            </table>
        </div>
        
        <div id="showExistingModelDialog" style="display: none" class="smallSizedFont">
            <input type="radio" name="chooseModelGroup" value="matchingModels" id="chooseMatchingModels">
            <label for="chooseMatchingModels">Matching source name</label>
            <!--<input type="radio" name="chooseLinkGroup" value="domains" id="choosePropertyWithDomainAndRange" disabled>-->
            <!--<label for="choosePropertyWithDomainAndRange">Property that links</label><br>-->
            <input type="radio" name="chooseModelGroup" value="allModels" id="chooseAllModels" checked>
            <label for="chooseAllModels">All models</label><br><br>

            <div class="error" style="display: none">
                <span class="error">Please select a model!</span>
            </div>
            <div class="italic noItems" style="display: none">
                <span>none</span>
            </div>

            <div id="modelsListPanel" class="smallSizedFont">
                <table id="modelsList">

                </table>
            </div>
        </div>
       
        
        <!--  Load all scripts last for faster page load -->
        
        <!-- Third Party JavaScript files		 -->
        <script type="text/javascript" src="uiLibs/jquery/js/jquery-1.10.2.min.js"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/jquery-ui-1.10.3.custom.min.js"></script>
		<script type="text/javascript" src="uiLibs/twitterBootstrap/js/bootstrap.min.js"></script>
		<script type="text/javascript" src="uiLibs/twitterBootstrap/js/bootstrap3-typeahead.min.js"></script>
		
        <script type="text/javascript" src="uiLibs/jquery/js/jquery.tmpl.min.js"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/jquery.hoverIntent.js"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/jquery.jstree.js"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/jquery.qtip.min.js"></script>
        <!-- 		<script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?key=AIzaSyDEvzzmlVOhVWTy13y5r6OPt5BRNR5QKsg&sensor=false"></script> -->
        <script type="text/javascript" src="https://www.google.com/jsapi?key=AIzaSyDEvzzmlVOhVWTy13y5r6OPt5BRNR5QKsg&sensor=false"></script>
        <script type="text/javascript" src="uiLibs/sticky/js/sticky.min.js"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/jquery.jeditable.js"></script>
        <script type="text/javascript" src="uiLibs/json/js/json2.js"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/jquery.cookie.js"></script>
        <script type="text/javascript" src="uiLibs/d3/js/d3.v2.min.js"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/jquery.iframe-transport.js"></script>
        <script type="text/javascript" src="uiLibs/ace/js/ace.js" charset="utf-8"></script>

        <script type="text/javascript" src="uiLibs/jquery/js/jquery.fileupload.js"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/jquery.fileupload-ui.js"></script>
        <script type="text/javascript" src="uiLibs/jquery/js/jquery.fileupload-jquery-ui.js"></script>
        <script type="text/javascript" src="uiLibs/locale/js/locale.js"></script>
        <script type="text/javascript" src="uiLibs/less/js/less-1.4.1.min.js"></script>
        <script type="text/javascript" src="uiLibs/modernizr/js/modernizr.custom.59953.js"></script>
		
        <!-- Home grown JavaScript files -->
        <script type="text/javascript" src="js/initWS.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/table_manipulation.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/ServerResponseObjectParsing.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/commandHistory.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/tableWorkspace.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/publishRDF.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/publishDatabase.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/serviceImport.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/pager.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/geospatial.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/databaseImportDialog.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/d3-alignment-vis.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/fileImport.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/cleaning.js?<jsp:include page='version.jsp' />"></script>
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
        <script type="text/javascript" src="js/classDropdown.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/propertyDropdown.js?<jsp:include page='version.jsp' />"></script>
         
        <script>
            $(function() {
				
                // Assign style and handlers to table cell menu
                styleAndAssignHandlersToTableCellMenu();
               
               // Assign style and handler to the merge button
                styleAndAssignHandlersToMergeButton();

                // Assign style and handler to select model for worksheet dialog (in showModel.js)
                styleAndAssignHandlersToApplyModelDialog();

               
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
			});
		</script>
		<script type="text/javascript">
			google.load("earth", "1", {
				"callback" : earthCallback
			});
			function earthCallback() {
				// alert("Earth namespace loaded!");
			}
		</script>

        <script type="text/javascript">
            $(document).ready(function(){
                $("[data-toggle='tooltip']").tooltip();
            });
            
        </script>
    </body>
</html>
