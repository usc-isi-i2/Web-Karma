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
<<<<<<< HEAD
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta http-equiv="cache-control" content="max-age=0" />
=======
        <meta http-equiv="cache-control" content="max-age=0" />
>>>>>>> development
		<meta http-equiv="cache-control" content="no-cache" />
		<meta http-equiv="expires" content="0" />
		<meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT" />
		<meta http-equiv="pragma" content="no-cache" />
<<<<<<< HEAD
        
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
        
=======


        
        <title>Karma Data Integration</title>
        <!-- Date: 2011-08-01 -->

        <!-- CSS -->
        <link rel="stylesheet" type="text/css" href="./css/jquery-ui-1.8.16.custom.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet" type="text/css" href="./css/TopMenuBar.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet" type="text/css" href="./css/command_History.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet" type="text/css" href="./css/tables_workspace.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet" type="text/css" href="./css/jquery.fileupload-ui.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet" type="text/css" href="./css/CSVImport.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet" type="text/css" href="./css/fileImport.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet" type="text/css" href="./css/DatabaseImport.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet" type="text/css" href="./css/ServiceImport.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet" type="text/css" href="./css/headerParsingExample.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet" type="text/css" href="./css/AlignmentHeaders.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet" type="text/css" href="./css/jquery.qtip.min.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet" type="text/css" href="./css/sticky.min.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet" type="text/css" href="./css/cleaning.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet" type="text/css" href="./css/alignment.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet" type="text/css" href="./css/d3.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet" type="text/css" href="./css/main.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet" type="text/css" href="./css/pyTransform.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet" type="text/css" href="./css/cleaningChart.css?<jsp:include page='version.jsp'></jsp:include>" />
        <link rel="stylesheet/less" type="text/css" href="./css/styles-03d-fixed-pixel.less?<jsp:include page='version.jsp'></jsp:include>" />
>>>>>>> development
		<style type="text/css">
			div.sticky {
    		/*	color: #555555; */
    			font-size: 14px;
			}
<<<<<<< HEAD
			
			@media (min-width: 1600px) {
			  .container {
			    width: 1570px;
			  }
=======
		</style>
        <!-- Third Party JavaScript files		 -->
        <script type="text/javascript" src="js/jquery-1.6.2.min.js"></script>
        <script type="text/javascript" src="js/jquery-ui-1.8.16.custom.min.js"></script>

        <script type="text/javascript" src="js/jquery.tmpl.min.js"></script>
        <script type="text/javascript" src="js/jquery.hoverIntent.js"></script>
        <script type="text/javascript" src="js/jquery.jstree.js"></script>
        <script type="text/javascript" src="js/jquery.qtip.min.js"></script>
        <!-- 		<script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?key=AIzaSyDEvzzmlVOhVWTy13y5r6OPt5BRNR5QKsg&sensor=false"></script> -->
        <script type="text/javascript" src="https://www.google.com/jsapi?key=AIzaSyDEvzzmlVOhVWTy13y5r6OPt5BRNR5QKsg&sensor=false"></script>
        <script type="text/javascript" src="js/sticky.min.js"></script>
        <script type="text/javascript" src="js/jquery.jeditable.js"></script>
        <script type="text/javascript" src="js/json2.js"></script>
        <script type="text/javascript" src="js/jquery.cookie.js"></script>
        <script type="text/javascript" src="js/d3.v2.min.js"></script>
        <script src="js/jquery.iframe-transport.js"></script>
        <script src="js/ace.js" type="text/javascript" charset="utf-8"></script>

        <script src="js/jquery.fileupload.js"></script>
        <script src="js/jquery.fileupload-ui.js"></script>
        <script src="js/jquery.fileupload-jui.js"></script>
        <script src="js/locale.js"></script>
        <script type="text/javascript" src="js/less-1.4.1.min.js"></script>
        <script type="text/javascript" src="js/modernizr.custom.59953.js"></script>

        <!-- Home grown JavaScript files -->
        <script type="text/javascript" src="js/table_manipulation.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/ServerResponseObjectParsing.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/commandHistory.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/tableWorkspace.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/publishRDF.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/publishDatabase.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/importFromService.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/pager.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/geospatial.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/databaseImportDialog.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/alignment.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/csvImport.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/fileImport.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/cleaning.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/services.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/d3-alignment-vis.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/reset-options.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/pytransform.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/cleaning-charts.js?<jsp:include page='version.jsp'></jsp:include>"></script>
        <script type="text/javascript" src="js/showModel.js?<jsp:include page='version.jsp'></jsp:include>"></script>


        <script type="text/javascript">
            /* Load and parse initial JSON */
            // Get the preferences Id from cookies if present
            var bootupURL = "KarmaServlet?rand=" + (new Date()).valueOf();
            if($.cookie("workspacePreferencesId") != null) {
                bootupURL += "&hasPreferenceId=" + true + "&workspacePreferencesId=" + $.cookie("workspacePreferencesId");
            } else {
                bootupURL += "&hasPreferenceId=" + false;
            }

            $.getJSON(bootupURL, function(data) {
                parse(data);

                // Set the preferences workspace cookie if null
                if($.cookie("workspacePreferencesId") == null)
                    $.cookie("workspacePreferencesId", $.workspaceGlobalInformation.id, {
                        expires : 7000
                    });
                
                //MVS: disable or enable the revision checkbox + dropdown
                function disableRevision (disabled){
                    $('#revisedWorksheetSelector').prop('disabled', disabled);
                    $("input:checkbox[name='RevisionCheck']").prop('disabled', disabled);
                }

                // Initialize the jQuery File Upload widget:
                $('#fileupload').fileupload({
                    url : "RequestController?workspaceId=" + $.workspaceGlobalInformation.id + "&command=ImportCSVFileCommand",
                    add : function(e, data) {
                        var fileName = data.files[0].name;
                        $("span#fileFormatError").hide();
                        $("input:radio[name=FileFormatSelection]").attr("checked", false);
                        
                        /*
                         * code for disabling revision functionality for ontologies
                      
                         $(":radio[name=FileFormatSelection]").change(function(){
                            var disabled = $(":radio[name=FileFormatSelection]").val() == "Ontology";
  
                            disableRevision(disabled);
                        });*/

                        if(fileName.match(".csv$") || fileName.match(".tsv$") || fileName.match(".txt$") || fileName.match(".log$")) {
                            $(":radio[name=FileFormatSelection][value=CSV]").attr("checked", true);
                        } else if(fileName.match(".xml$")) {
                            $(":radio[name=FileFormatSelection][value=XMLFile]").attr("checked", true);
                        } else if(fileName.match(".xls$") || fileName.match(".xlsx$")) {
                            $(":radio[name=FileFormatSelection][value=ExcelFile]").attr("checked", true);
                        } else if(fileName.match(".owl$") || fileName.match(".rdf$")) {
                            $(":radio[name=FileFormatSelection][value=Ontology]").attr("checked", true);
                        } else if(fileName.match(".json$")) {
                            $(":radio[name=FileFormatSelection][value=JSONFile]").attr("checked", true);
                        }

                        $("div#fileFormatSelectionDialog").dialog({
                            width : 200,
                            height : 250,
                            title : "Confirm File Format",
                            modal : true,
                            open : function(event, ui) {
                                $('.ui-dialog-buttonset  button:last').filter(":last").focus();
                                
                                var worksheets = $('.Worksheet');
                                if (worksheets.size() > 0){
                                    disableRevision(false);
                  
                                    worksheets.each(function(){
                                        var item = $('<option />');
                                        item.val($(this).attr('id'));
                                        item.text($(this).find('.tableTitleTextDiv').text());

                                        $('#revisedWorksheetSelector').append(item);
                                    });
                                } else {
                                    disableRevision(true);
                                }
                            },
                            buttons : {
                                "Cancel" : function() {
                                    $(this).dialog("close");
                                },
                                "Submit" : function() {
                                    var selectedFormat = $("input:radio[name='FileFormatSelection']:checked").val();
                                    if(selectedFormat == null || selectedFormat == "") {
                                        $("span#fileFormatError").show();
                                        return false;
                                    }

                                    var urlString = "RequestController?workspaceId=" + $.workspaceGlobalInformation.id;
                                    
                                    //MVS: add the id of the revised worksheet in the request
                                    if ($("input:checkbox[name='RevisionCheck']").prop('checked')) {
                                        urlString += "&revisedWorksheet=" + $('#revisedWorksheetSelector').val();
                                    }
                  
                                    urlString += "&command=";

                                    // Change the command according to the selected format
                                    if(selectedFormat == "CSV") {
                                        $('#fileupload').fileupload({
                                            url : urlString + "ImportCSVFileCommand",
                                            done : function(e, data) {
                                                resetCSVDialogOptions();
                                                showCSVImportOptions(data.result);
                                            }
                                        });
                                    } else if(selectedFormat == "JSONFile" || selectedFormat == "XMLFile" || selectedFormat== "ExcelFile" || selectedFormat == "Ontology") {
                                    	$('#fileupload').fileupload({
	                                    	url : urlString + "Import" + selectedFormat + "Command",
	                                        done : function(e, data) {
	                                            resetFileDialogOptions();
	                                            showFileImportOptions(data.result, selectedFormat);
	                                        }
                                    	});
                                    } 

                                    data.submit();
                                    $(this).dialog("close");
                                }
                            }
                        });
                    },
                    done : function(e, data) {
                        parse(data.result);
                    },
                    fail : function(e, data) {
                        $.sticky("File upload failed!");
                    },
                    dropZone : $(document)
                });

                // Enable iframe cross-domain access via redirect option:
                $('#fileupload').fileupload('option', 'redirect', window.location.href.replace(/\/[^\/]*$/, '/cors/result.html?%s'));
            }).error(function() {
                alert("Trouble connecting to server!");
            });

        </script>
        <script>
            $(function() {
                // Database Import div options
                styleAndAssignHandlerstoDatabaseImportObjects();

                // Service Import div options
                styleAndAssignHandlerstoServiceImportObjects();

                // Stylize the worksheet options div panel
                styleAndAssignHandlersToWorksheetOptionButtons();

                // Attach handlers to the ontology options table
                attachOntologyOptionsRadioButtonHandlers();

                // Assign style and handlers to table cell menu
                styleAndAssignHandlersToTableCellMenu();

                // Assign style and handlers to column heading menu
                styleAndAssignHandlersToColumnHeadingMenu();

                // Assign handler and style to the Reset options (in reset-options.js)
                styleAndAssignHandlerToResetButton();

                // Assign style and handler to the merge button
                styleAndAssignHandlersToMergeButton();

                // Assign style and handler to Python Transform windows elements (in pytransform.js)
                styleAndAssignHandlersToPyTransformElements();

                // Assign style and handler to select model for worksheet dialog (in showModel.js)
                styleAndAssignHandlersToApplyModelDialog();

                // Assign style and handler to the modeling class bubble options
                styleAndAssignHandlersToModelingVizElements();

                // When the user changes header line index
                $('.CSVImportOption').change(function() {
                    CSVImportOptionsChanged("preview");
                });
                $('.FileImportOption').change(function() {
                    FileImportOptionsChanged("preview");
                });
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
>>>>>>> development
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
			  
			  <div id="classDropdownMenu" class="dropdown clearfix contextMenu">
				    <ul class="dropdown-menu" role="menu" style="display:block;position:static;margin-bottom:5px;">
				      <li><a tabindex="-1" href="#">Add Incoming Link</a></li>
				      <li><a tabindex="-1" href="#">Add Outgoing Link</a></li>
				      <li class="divider"></li>
				      <li><a tabindex="-1" href="#">Invoke Reconciliation Service</a></li>
				      <li><a tabindex="-1" href="#">Invoke M/L Service</a></li>
				    </ul>
			</div>
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
<<<<<<< HEAD
=======
        <div id="CSVImportDiv" style="display: none">
            <table id="CSVImportOptionsTable">
                <tbody>
                    <tr>
                        <td id="importOptionsCell" colspan="5"><span id="CSVSourceName"></span></td>
                    </tr>
                    <tr>
                        <td><span id="importOptionsHeader">Import Options</span></td>
                        <td>
                        <table>
                        	<tr>
                        		 <td> Column Delimiter:
		                            <select id="delimiterSelector" class="CSVImportOption">
		                                <option>comma</option>
		                                <option>tab</option>
		                                <option>space</option>
		                                <option>pipe</option>
		                            </select></td>
		                        <td> Header Line Index:
		                            <input type="textarea" value="1" id="CSVHeaderLineIndex" class="CSVImportOption" maxlength="3" size="2"/>
		                        </td>
		                        <td> Data Start Row Index:
		                            <input type="textarea" id="startRowIndex" class="CSVImportOption" maxlength="3" size="2" value="2"/>
		                        </td>
		                        <td> Text Qualifier:
		                            <input type="textarea" id="textQualifier" class="CSVImportOption" maxlength="3" size="2" value='"'/>
		                        </td>
                        	</tr>
                        	<tr>
                        		<td> Encoding:
                        			<select id="encoding" class="CSVImportOption">
                        				<%@include file="encoding.jsp" %>
                        			</select>
                        		</td>
                        		<td colspan='2'>Number of Lines to import:
                        			<input type="textarea" id="maxNumLines" class="CSVImportOption" maxlength="6" size="6" value="100"/>
                        	</tr>
                        </table>
                       
                     </tr>
                </tbody>
            </table>
            <div id="previewTableDiv">
                <span id="previewTableHeader">Preview (Only 5 rows shown)</span>
                <br>
                <table id="CSVPreviewTable"></table>
            </div>
        </div>
        
        <div id="FileImportDiv" style="display: none">
            <table id="FileImportOptionsTable">
                <tbody>
                    <tr>
                        <td id="importOptionsCell" colspan="5"><span id="FileSourceName"></span></td>
                    </tr>
                    <tr>
                        <td><span id="importOptionsHeader">Import Options</span></td>
                      	 <td>
                        <table>
                        	<tr>  
                        		<td> Encoding:
                        			<select id="fileEncoding" class="FileImportOption">
                        				<%@include file="encoding.jsp" %>
                        			</select>
                        		</td>
                        		<td  id="colMaxNumLines">Number of <span id="fileMaxName">Lines</span> to import:
                        			<input type="textarea" id="fileMaxNumLines" class="FileImportOption" maxlength="6" size="6" value="100"/>
                        		</td>
                    		</tr>
                    	</table>
                    </tr>
                </tbody>
            </table>
            <div id="filePreviewTableDiv">
                <span id="previewTableHeader">Preview (Only 5 rows shown)</span>
                <br>
                <table id="FilePreviewTable"></table>
            </div>
        </div>
>>>>>>> development
        
        <div id="tableCellEditDiv" style="display: none"></div>
        <div id="r2rmlModelNameDiv" style="display: none"> <textarea id="txtR2RMLModelName" style="width: 100%"> </textarea> </div>
        
        <div id="ScrollHeadersPlaceholder"></div>
<<<<<<< HEAD
     
=======
        <div id="WorksheetOptionsDiv" style="display: none">
            <button href="#" id="showModel">
                Show Model
            </button>
            <button href="#" id="setWorksheetProperties">
                Set properties
            </button>
            <button href="#" id="showAutoModel">
                Show AutoModel
            </button>
            <button href="#" id="resetModel" style="display: none">
                Reset Model
            </button>
            <button href="#" id="publishRDF">
                Publish RDF
            </button>
            <button href="#" id="publishR2RML">
                Publish Model
            </button>
            <!-- <button href="#" id="saveR2RMLToTripleStore">
                Publish Model
            </button> -->
            <button href="#" id="populateSource">
                Populate Source
            </button>
            <button href="#" id="csvExport">
                Export to CSV
            </button>
            <button href="#" id="publishDatabase">
                Export to Database
            </button>
            <button href="#" id="mdbExport">
                Export to MDB
            </button>
            <button href="#" id="spatialdataExport">
                Export to SpatialData
            </button>
            <button href="#" id="publishServiceModel">
                Publish Service Model
            </button>
            <button href="#" id="showR2RMLFromTripleStore">
                Invoke Service
            </button>
            <!--<button href="#" id="transformcolumns">-->
            <!--Transform Columns-->
            <!--</button>-->
            <!--<button href="#" id="publishWorksheetHistory">-->
            <!--Publish Worksheet History-->
            <!--</button>-->
            <div class="container">
                <!-- <input type="file" name="file1" id="uploadFile_JSON"> -->
                <form id="applyWorksheetHistory" action="ImportFileCommand" method="POST" enctype="multipart/form-data">
                    <!-- The fileupload-buttonbar contains buttons to add/delete files and start/cancel the upload -->
                    <div class="row fileupload-buttonbar">
                        <div class="span7">
                            <!-- The fileinput-button span is used to style the file input field as button -->
                            <span class="btn btn-success fileinput-button importButton" id="applyHistoryButton"> <!-- <i class="icon-plus icon-white"></i> --> <span>Apply R2RML Model</span>
                                <input type="file" name="files[]" multiple>
                            </span>
                        </div>
                        <!-- The global progress information -->
                        <div class="span5 fileupload-progress fade">
                            <!-- The global progress bar -->
                            <div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100">
                                <div class="bar" style="width:0%;"></div>
                            </div>
                            <!-- The extended global progress information -->
                            <div class="progress-extended">
                                &nbsp;
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <div id="ChangeSemanticTypesDialogBox" style="display: none">
            <div id="CurrentSemanticTypesBox" class="ui-corner-all">
                <span class="mediumSizedFont">Semantic types:</span>
                <table id="currentSemanticTypesTable">
                    <tr><td /><td /><td>Primary</td><td/>
                    </tr>
                </table>
                <br />
                <button id="addSemanticType">
                    Add synonym semantic type
                </button>
                <br />
                <input class="smallSizedFont" type="checkbox" id="chooseClassKey" />
                <label for="chooseClassKey" class="smallSizedFont">Mark as key for the class.</label>
                <br />
                <div class="ui-widget" id="rdfTypeSelectDiv">
                    <label class="smallSizedFont">Literal type: </label>
                    <select class="smallSizedFont" id="rdfTypeSelect">
                        <option>xsd:string</option>
                        <option>xsd:boolean</option>
                        <option>xsd:decimal</option>
                        <option>xsd:integer</option>
                        <option>xsd:double</option>
                        <option>xsd:float</option>
                         <option>xsd:date</option>
                        <option>xsd:time</option>
                        <option>xsd:dateTime</option>
                        <option>xsd:dateTimeStamp</option>
                        <option>xsd:gYear</option>
                        <option>xsd:gMonth</option>
                        <option>xsd:gDay</option>
                        <option>xsd:gYearMonth</option>
                        <option>xsd:gMonthDay</option>
                        <option>xsd:duration</option>
                        <option>xsd:yearMonthDuration</option>
                        <option>xsd:dayTimeDuration</option>
                        <option>xsd:byte</option>
                        <option>xsd:short</option>
                        <option>xsd:int</option>
                        <option>xsd:long</option>
                        <option>xsd:unsignedByte</option>
                        <option>xsd:unsignedShort</option>
                        <option>xsd:unsignedInt</option>
                        <option>xsd:unsignedLong</option>
                        <option>xsd:positiveInteger</option>
                        <option>xsd:nonNegativeInteger</option>
                        <option>xsd:negativeInteger</option>
                        <option>xsd:nonPositiveInteger</option>
                        <option>xsd:hexBinary</option>
                        <option>xsd:base64Binary</option>
                        <option>xsd:anyURI</option>
                        <option>xsd:language</option>
                        <option>xsd:normalizedString</option>
                        <option>xsd:token</option>
                        <option>xsd:NMTOKEN</option>
                        <option>xsd:Name</option>
                        <option>xsd:NCName</option>
                    </select>
                </div>
                <button id="semanticTypingAdvancedOptions">
                    Advanced Options
                </button>
                <br />
                <div id="semanticTypingAdvacedOptionsDiv" style="display: none">
                    <table>
                        <tr class="smallSizedFont">
                            <td>
                                <input type="checkbox" id="isUriOfClass">
                                </input></td>
                            <td style="text-align: right;">contains URI for node:</td>
                            <td>
                                <input type="text" id="isUriOfClassTextBox"/>
                            </td>
                        </tr>
                        <tr class="smallSizedFont">
                            <td>
                                <input type="checkbox" id="isSubclassOfClass">
                                </input></td>
                            <td style="text-align: right;">specifies class for node:</td>
                            <td>
                                <input type="text" id="isSubclassOfClassTextBox"/>
                            </td>
                        </tr>
                        <tr class="smallSizedFont">
                            <td>
                                <input type="checkbox" id="isSpecializationForEdge">
                                </input></td>
                            <td style="text-align: right;">specifies specialization for edge:</td>
                            <td>
                                <input type="text" id="isSpecializationForEdgeTextBox"/>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
            <div class="ui-widget" id="SemanticTypeErrorWindow" style="display: none">
                <br />
                <div class="ui-state-error ui-corner-all" style="padding: 0 .7em;">
                    <p>
                        <span class="ui-icon ui-icon-alert"
                              style="float: left; margin-right: .3em;"></span>
                        <span id="SemanticTypeErrorWindowText"></span>
                    </p>
                    <br />
                </div>
                <br />
            </div>
        </div>
        <div id="propertyOntologyBox" style="display: none">
            <input id="filterPropertyByDomain" type="checkbox"/>
            <label for="filterPropertyByDomain">Show properties with domain</label><span id="className" class="bold"></span>
            <br />
            <br />
            <input type="text" id="propertyKeyword" maxlength="100" size="20"/>
            <button id="propertySearch">
                Search
            </button>
            <br />
            <br />
            <div id="propertyTree"></div>
        </div>
        <div id="classOntologyBox" style="display: none">
            <input id="filterClassByDomain" type="checkbox"/>
            <label for="filterClassByDomain">Show domains for property</label><span id="propertyName" class="italic"></span>
            <br />
            <br />
            <input type="text" id="classKeyword" maxlength="100" size="20"/>
            <button id="classSearch">
                Search
            </button>
            <br />
            <br />
            <div id="classTree"></div>
        </div>
>>>>>>> development
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
        
        

		<div class="smallSizedFont" id="incomingOutgoingLinksDialog" style="display:none">
			<div id="incomingOutgoingLinksBox" class="ui-corner-all">
				<table width="100%">
					<tr>
						<td width="50%"><span id="incomingOutgoingLinksDirection">Direction</span> Class:</td>
						<td>Property:</td>
					</tr>
					<tr>
						<td valign="top">
							<input type="text" id="incomingOutgoingClassKeyword" maxlength="100" size="20"/>
				            <button id="incomingOutgoingClassSearch">
				                Search
				            </button>
						</td>
						
						<td valign="top">
							<input type="text" id="incomingOutgoingPropertyKeyword" maxlength="100" size="20"/>
				            <button id="incomingOutgoingPropertySearch">
				                Search
				            </button>
						</td>
					</tr>
					<tr>
						<td valign="top">
							<div id="incomingOutgoingLinksClassData" style="display:none">
	                		</div>
							<div id="incomingOutgoingLinksClassDiv1">
	                		</div>
	                		<div class="incomingOutgoingLinksSeparator">
	                		</div>
	                		<div id="incomingOutgoingLinksClassDiv2">
	                		</div>
						</td>
						<td valign="top">
							<div id="incomingOutgoingLinksPropertyData" style="display:none">
	                		</div>
							<div id="incomingOutgoingLinksPropertyDiv1">
	                		</div>
	                		<div class="incomingOutgoingLinksSeparator">
	                		</div>
	                		<div id="incomingOutgoingLinksPropertyDiv2">
	                		</div>
						</td>
					</tr>
				</table>
            </div>
            <div class="ui-widget" id="incomingOutgoingLinksErrorWindowBox" style="display: none">
                <br>
                <div class="ui-state-error ui-corner-all" style="padding: 0 .7em;">
                    <p id="incomingOutgoingLinksErrorWindow">
                        <span class="ui-icon ui-icon-alert"
                              style="float: left; margin-right: .3em;"></span>
                        <span id="incomingOutgoingLinksWindowText"></span>
                    </p>
                    <br />
                </div>
                <br />
            </div>
		</div>
		
        <div class="smallSizedFont" id="currentLinksInternalNodeDialog" style="display: none">
            <div id="CurrentLinksBox" class="ui-corner-all">
                <h3>Incoming Links:</h3>
                <table id="currentIncomingLinksTable">
                </table>
                <br>
                <button id="addIncomingInternalNodeLink">
                    Add Incoming link
                </button>
                <br><br>
                <h3>Outgoing Links:</h3>
                <table id="currentOutgoingLinksTable">
                </table>
                <br>
                <button id="addOutgoingInternalNodeLink">
                    Add Outgoing link
                </button><br>
            </div>
            <div class="ui-widget" id="currentLinksErrorWindowBox" style="display: none">
                <br>
                <div class="ui-state-error ui-corner-all" style="padding: 0 .7em;">
                    <p id="currentLinksErrorWindow">
                        <span class="ui-icon ui-icon-alert"
                              style="float: left; margin-right: .3em;"></span>
                        <span id="currentLinksWindowText"></span>
                    </p>
                    <br />
                </div>
                <br />
            </div>
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
       
        <div id="modelingClassDropDownMenu" class="ui-corner-all" style="display:none">
        	<button id="addNodeIncomingLink">Add Incoming Link</button>
        	<button id="addNodeOutgoingLink">Add Outgoing Link</button>
        	<button id="invokeRubenReconciliationService">Invoke Reconciliation Service</button>
            <button id="invokeMachineLearningService">Invoke M/L Service</button>
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
        <script type="text/javascript" src="js/classPropertyUI.js?<jsp:include page='version.jsp' />"></script>
         <script type="text/javascript" src="js/classDropdown.js?<jsp:include page='version.jsp' />"></script>
         
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
            
            $(function() {
            	  
            	  var $contextMenu = $("#contextMenu");
            	  var $rowClicked;
            	  
            	  $("body").on("click", "div svg text.LinkLabel", function(e) {
            		  $rowClicked = $(this);  
            	    $contextMenu.css({
            	      display: "block",
            	      left: e.pageX,
            	      top: e.pageY
            	    });
            	    return false;
            	  });
            	  
            	  $("body").on("click", "div svg g.InternalNode", function(e) {
            		  $rowClicked = $(this);  
            	    $contextMenu.css({
            	      display: "block",
            	      left: e.pageX,
            	      top: e.pageY
            	    });
            	    return false;
            	  });
            	  
            	  $contextMenu.on("click", "a", function () {
            		    var message = "You clicked on the row '" + 
            		        $rowClicked.children("*")[1].innerHTML + "'\n"
            		    message += "And selected the menu item '" + $(this).text() + "'"
            		    alert(message);
            		    $contextMenu.hide();
            		});
            	  
            	  $(document).click(function () {
            		    $contextMenu.hide();
            		});
            	  
            	});
        </script>
    </body>
</html>
