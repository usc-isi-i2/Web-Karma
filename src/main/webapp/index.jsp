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
		      
		      
		      
		      <jsp:include page="fileImport.jsp"></jsp:include>
		      
			
		</div>
	
		<div id="commHistoryAndWorkspace">
			<div id="commandHistory" class="ui-corner-top">
				<div id="titleCommand" class="ui-corner-top">
					<span>Command History</span>
				</div>
			</div>
			<div id="tablesWorkspace"></div>
		</div>
		<div id="columnHeadingDropDownMenu" class="ui-corner-all" style="display: none">
            <button id="addColumnButton">Add Column</button>
            <button id="pyTransform">PyTransform</button>
            <button id="invokeServiceButton">Invoke Service</button>
            <button id="renameColumnButton">Rename</button>
            <button id="splitByComma">Split Column</button>
            <button id="cleanColumnButton">Transform</button>
            <button id="showChartButton">Show Chart</button>
            <button id="hideColumnButton" style="display: none">Hide</button>
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
        <div id="DatabaseImportDiv" style="display: none">
            <table>
                <tbody>
                    <tr>
                        <td> Database Type:
                            <select id="databaseTypeSelector" class="DatabaseImportOption">
                                <option>SQLServer</option>
                                <option>MySQL</option>
                                <option>Oracle</option>
                                <option>PostGIS</option>
                                <option>Sybase</option>
                            </select></td>
                        <td> Host Name:
                            <input type="textarea" id="DatabaseHostName" value="localhost" class="DatabaseImportOption" maxlength="40" size="10"/>
                        </td>
                        <td> Port Number:
                            <input type="textarea" id="DatabasePortNumber" value="" class="DatabaseImportOption" maxlength="6" size="5"/>
                        </td>
                        <td> Username:
                            <input type="textarea" id="DatabaseUsername" value="" class="DatabaseImportOption" maxlength="20" size="10"/>
                        </td>
                        <td> Password:
                            <input type="password" id="DatabasePassword" value="" class="DatabaseImportOption" maxlength="20" size="10"/>
                        </td>
                        <td id="DBNameCell"><span>Database Name:</span>
                            <input type="textarea" id="DatabaseName" value="" class="DatabaseImportOption" maxlength="20" size="10"/>
                        </td>
                        <td>
                            <button id="DatabaseImportFieldsButton" type="button">
                                OK
                            </button></td>
                    </tr>
                </tbody>
            </table>
            <br>
            <div class="ui-widget" id="DatabaseImportErrorWindow" style="display: none">
                <div class="ui-state-error ui-corner-all" style="padding: 0 .7em;">
                    <p>
                        <span class="ui-icon ui-icon-alert"
                              style="float: left; margin-right: .3em;"></span>
                        <span id="DatabaseImportErrorWindowText"></span>
                    </p>
                    <br />
                </div>
            </div>
            <!-- For showing databases and the tables -->
            <table id="DatabaseImportTableListAndPreview">
                <tr>
                    <td> Choose Table: </td>
                </tr>
                <tr>
                    <td>
                        <input type="text" id="databaseTableFilterTable" class="DatabaseImportOption dbTableColumn" size="12"/>
                    </td>
                    <td rowspan=2 id="DatabasePreviewCell"><span id="dbPreviewTableName"></span>
                        <div id="DatabaseTablePreview">
                            <table></table>
                        </div></td>
                </tr>
                <tr>
                    <td>
                        <div id="DatabaseTablesList">
                            <table></table>
                        </div></td>
                </tr>
            </table>
            <br>
        </div>

        <div id="ServiceImportDiv" style="display: none">
            <table>
                <tbody>
                    <tr>
                        <td>Web Service URL:</td>
                        <td>
                            <input type="textarea" id="serviceUrl" value="" class="smallSizedFont" maxlength="300" size="50"/>
                        </td>
                    </tr>
                    <tr>
                        <td> Worksheet Name:</td>
                        <td>
                            <input type="textarea" id="serviceWorksheetName" value="" class="smallSizedFont" maxlength="100" size="20"/>
                        </td>
                    </tr>
                    <tr>
                    	<td>Encoding:</td>
                    	<td>
                    		<select id="serviceEncoding" class="smallSizedFont">
                    			<%@include file="encoding.jsp" %>
                    		</select>
                    	</td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <input type="checkbox" id="includeInputAttributesService">
                            <label for="includeInputAttributesService">Include input attributes in the output worksheet</label>
                        </td>
                    </tr>
                    <tr id="serviceErrorRow" class="error" style="display: none">
                        <td colspan="2">
                            Please provide values for both text fields!
                        </td>
                    </tr>
                </tbody>
            </table>
            <br>
        </div>

        <div id="ScrollHeadersPlaceholder"></div>
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
        <div id="OntologyAlternativeLinksPanel" style="display: none">
            <span class="smallSizedFont">Choose parent relationship:</span>
            <br />
            <input type="textarea" id="alternativeParentsTableFilter" class="DatabaseImportOption dbTableColumn" size="30"/>
            <br />
            <table id="AlternativeParentLinksTable"></table>
        </div>
        <div id="SplitByCommaColumnListPanel" style="display: none">
            <span class="smallSizedFont">Specify character delimiter:</span>
            <input class="smallSizedFont" type="text" id="columnSplitDelimiter" value="," maxlength="5" size="5"/>
            </br>
            <span class="smallSizedFont">Enter "space" to use single space</span></br>
            <span class="smallSizedFont">Enter "tab" to use tab</span>
        </div>
        <div id="tableCellMenuButtonDiv" style="display: none"></div>
        <div id="columnHeadingMenuButtonDiv" style="display: none"></div>
        <div id="confirmPublishRDFDialogBox" style="display: none">
            <span class="smallSizedFont"></span>
        </div>
        <div id="PublishRDFDialogBox" style="display: none">
            <span class="smallSizedFont">
                <table>
                    <tr>
                        <td>RDF namespace</td>
                        <td>
                            <input type="text" id="rdfNamespace" value="http://localhost:8080/source/" maxlength="100" style="width:100%"/>
                        </td>
                    </tr>
                    <tr>
                        <td>RDF prefix</td>
                        <td>
                            <input type="text" id="rdfPrefix" value="s" maxlength="100" style="width:100%"/>
                        </td>
                    </tr>
                    <tr>
                        <td><label for="rdfSPAQRLEndPoint">SPARQL End Point</label></td>
                        <td><input type="url" id="rdfSPAQRLEndPoint" required value="" style="width:100%" /> 
                        </td>
                    </tr>
                    <tr> <td colspan="2"> &nbsp;</td> </tr>
                    <tr>
                        <td><label for="modelGraphList">Use existing context</label></td>
                        <td> <span class="smallSizedFont">
                                <select id="modelGraphList" style="width:100%"></select>
                            </span>
                        </td>
                    </tr>
                    <tr>
                        <td> <label id="labelFor_rdfSPAQRLGraph" for="rdfSPAQRLGraph">Create new context</label> </td>
                        <td> <input type="url" id="rdfSPAQRLGraph" value="" style="width:100%"/> </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <span class="smallSizedFont">
                                <form id="graphReplaceOptions">
                                    <input type="radio" name="group1" id="graphReplace_1"
                                           value="replace" /> <label for="graphReplace_1">Replace
                                        existing data in context</label> <br /> <input type="radio"
                                                                                   name="group1" id="graphReplace_2" checked="checked" value="append" /> <label
                                                                                   for="graphReplace_2">Append to context</label> <br />
                                </form>
                            </span>
                        </td>
                    </tr>
                    <tr> <td colspan="2"> &nbsp;</td> </tr>
                </table> 
            </span>
            <input type="checkbox" id="addInverseProperties" unchecked />
            <label for="addInverseProperties" class="smallSizedFont">Add Inverse Properties</label>
            <br>
            <br>
            <!-- <input type="checkbox" id="saveToRDFStore" onChange = "showHideRdfInfo()"/>
            <label for="saveToRDFStore" class="smallSizedFont">Save to Jena RDF Store</label>
            <br /> -->
            <br />
            <div class="smallSizedFont" id="rdfStoreInfo">
                <table>
                    <tr>
                        <td>Host Name:Port</td>
                        <td>Database Name</td>
                    </tr>
                    <tr>
                        <td>
                            <input type="text" id="hostName" value="localhost:3306" maxlength="100" size="20"/>
                        </td>
                        <td>
                            <input type="text" id="dbName" value= "jenadb" maxlength="100" size="20"/>
                        </td>
                    </tr>
                    <tr>
                        <td>User Name</td>
                        <td>Password</td>
                    </tr>
                    <tr>
                        <td>
                            <input type="text" id="userName" value="root" maxlength="100" size="20"/>
                        </td>
                        <td>
                            <input type="password" id="password" value="" maxlength="100" size="20"/>
                        </td>
                    </tr>
                    <tr>
                        <td>Model Name</td>
                        <td></td>
                    </tr>
                    <tr>
                        <td>
                            <input type="text" id="modelName" value="karma" maxlength="100" size="20"/>
                        </td>
                        <td></td>
                    </tr>
                </table>
            </div>
            <br />
        </div>
        <div id="PublishR2RMLModelDialogBox" style="display: none">
            <span class="smallSizedFont">
                <table>
                    <tr><td>SPARQL End Point</td></tr>
                    <tr><td><input type="url" id="txtR2RML_URL" value="" size="65" required/></td></tr>
                </table> 
            </span>
            <br />
        </div>
        <div id="FetchR2RMLModelDialogBox" style="display: none">
            <span class="smallSizedFont">
                <table>
                    <tr><td>SPARQL End Point</td></tr>
                    <tr><td><input type="text" id="txtR2RML_URL_fetch" value="" size="65"/></td></tr>
                </table> 
            </span>
            <br />
        </div>

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
        <div id="PublishDatabaseDialogBox" style="display: none">
            <div class="smallSizedFont" id="databaseInfo">
                DB Type:
                <select id="dbType">
                    <option selected>MySQL</option>
                    <option>SQLServer</option>
                    <option>PostGIS</option>
                </select>
                <br>
                <br>
                <table>
                    <tr>
                        <td>Host Name</td>
                        <td>Port</td>
                    </tr>
                    <tr>
                        <td>
                            <input type="text" id="hostName1" value="localhost" maxlength="100" size="20"/>
                        </td>
                        <td>
                            <input type="text" id="port" value="" maxlength="100" size="20"/>
                        </td>
                    </tr>
                    <tr>
                        <td>Database Name</td>
                        <td>Table Name</td>
                    </tr>
                    <tr>
                        <td>
                            <input type="text" id="dbName1" value= "" maxlength="100" size="20"/>
                        </td>
                        <td>
                            <input type="text" id="tableName" value="" maxlength="100" size="20"/>
                        </td>
                    </tr>
                    <tr>
                        <td>User Name</td>
                        <td>Password</td>
                    </tr>
                    <tr>
                        <td>
                            <input type="text" id="userName1" value="root" maxlength="100" size="20"/>
                        </td>
                        <td>
                            <input type="password" id="password1" value="" maxlength="100" size="20"/>
                        </td>
                    </tr>
                </table>
            </div>
            <br>
            <input type="checkbox" id="overwriteTable"  onClick="overwriteTable();"/>
            <label for="overwriteTable" class="smallSizedFont">Overwrite Table</label>
            <input type="checkbox" id="insertTable" onClick="insertTable();"/>
            <label for="insertTable" class="smallSizedFont">Insert in Table</label>
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
        <div id="ColumnCleaningPanel" style="display: none">
            <table >
                <tbody>
                    <tr>
                        <td  valign="top" class="infotable">
                            <table id="infoPanel" class="transform">
                                <tbody>
                                    <tr>
                                        <td class="heading">Examples You Entered: </td>
                                    </tr>
                                    <tr class="infotable">
                                        <td class="infotable">
                                            <table id="examples" title="Previous Examples:" class="transform">

                                            </table></td>
                                    </tr>
                                    <tr>
                                        <td class="heading">Recommended for Examining:</td>
                                    </tr>
                                    <tr class="infotable">
                                        <td class="infotable">
                                            <table id="recmd" title="Recommended Examples:" class="transform">

                                            </table></td>
                                    </tr>						
                                </tbody>
                            </table></td>
                    </tr>
                    <tr><td class="heading">All Records: </td></tr>
                    <tr>
                        <td>
                            <table id="cleaningExamplesTable" class="transform">

                            </table>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
        
        <div id="resetDialogDiv" style="display: none">
            <table>
                <tr class="smallSizedFont" >
                    <td>
                        <input type="checkbox" id="forgetSemanticTypes"/>
                    </td>
                    <td><label for="forgetSemanticTypes">Semantic Types</label></td>
                </tr>
                <tr class="smallSizedFont">
                    <td>
                        <input type="checkbox" id="forgetModels"/>
                    </td>
                    <td><label for="forgetModels">Model Histories</label></td>
                </tr>
            </table>
        </div>
        <div id="renameColumnDiv" style="display: none">
            <span class="smallSizedFont">Enter new column name:</span><br>
            <input type="text" class="smallSizedFont" id="renameColumnNameInput" /><br/>
            <span class="error smallSizedFont" style="display: none">Please enter a valid name!</span>
        </div>
        <div id="pyTransformDialog" style="display: none">
        	<input type="radio" name="pyTransformType" value="edit">
        		<label class="smallSizedFont">Change existing column:</label>
        		<span class="smallSizedFont" id="pythonTransformEditColumnName"></span>
        	<br>
        	<input type="radio" name="pyTransformType" value="new" checked="checked">
            	<label class="smallSizedFont">Name of new column:</label>
            	<input type="text" class="smallSizedFont" id="pythonTransformNewColumnName">
            <br>
            <span class="error smallSizedFont" style="display: none" id="pyTransformColumnNameError">Please provide a new unique column name!</span>
            <br><br>
            <div id="transformCodeEditor"></div>
            <span class="smallSizedFont">On error:</span>
            <input type="text" class="smallSizedFont" id="pythonTransformErrorDefaultValue">
            <button id="pyTransformViewErrorButton"  style="margin-left: 20px;">View Errors</button>
            <button id="previewPyTransformButton"  style="margin-left: 60px">Preview Results for Top 5 Rows</button>
            <br>
            <table id="pythonPreviewResultsTable" class="smallSizedFont" style="display: none"></table>
            <div id="pyTransformErrorWindow" style="display: none"></div>
        </div>
        <div id="addNewColumnDialogDiv" style="display: none" class="smallSizedFont">
            <table>
                <tr>
                    <td><span>Enter new column name:</span></td>
                    <td><input type="text" id="addNewColumnNameInput" /></td>
                </tr>
                <tr>
                    <td><span>Default Value:</span></td>
                    <td><input type="text" id="addNewColumnDefaultValueInput" /></td>
                </tr>
                <tr>
                    <td class="SRIDLink"><a href="SRID.html" target='_blank'>Click to see SRID List</a></td>
                </tr>
                <tr>
                    <td colspan="2"><span class="error" style="display: none">Please enter a unique column name!</span></td>
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
        <div id="setPropertiesDialog" style="display: none" class="smallSizedFont">
            <div>
                <br><label>Model Identifier:</label><input type="text" maxlength="100" id="graphNameInput"><br><br><br>
            </div>

            <input type="checkbox" id="serviceOptions"> <label for="serviceOptions">Set Service Options</label><br><br>

            <div id="worksheetServiceOptions" style="display: none" class="smallSizedFont">
                <label>Service URL:</label><input type="text" maxlength="80" id="serviceUrlInput"><br><br>
                <label>Request method: </label>
                <select class="smallSizedFont" id="serviceRequestMethod">
                    <option value="GET">GET</option>
                    <option value="POST">POST</option>
                </select><br><br>

                <div style="display: none" id="servicePostOptions">
                    <input type="radio" id="invokePerRow" value="invokePerRow" name="serviceDataPostMethod" checked>
                    <label for="invokePerRow">Invoke service one row at a time</label><br>
                    <input type="radio" id="invokeWholeWorksheet" value="invokeWithWholeWorksheet" name="serviceDataPostMethod">
                    <label for="invokeWholeWorksheet">Post whole worksheet</label>
                </div>
            </div>
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
        <script type="text/javascript" src="js/importFromService.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/pager.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/geospatial.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/databaseImportDialog.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/alignment.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/fileImport.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/cleaning.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/services.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/d3-alignment-vis.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/reset-options.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/pytransform.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/cleaning-charts.js?<jsp:include page='version.jsp' />"></script>
        <script type="text/javascript" src="js/showModel.js?<jsp:include page='version.jsp' />"></script>
        
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
            })
        </script>
    </body>
</html>
