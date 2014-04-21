function TableColumnOptions(wsId, wsColumnId, wsColumnTitle) {
	 
	var worksheetId = wsId;
	var columnTitle = wsColumnTitle;
	var columnId = wsColumnId;
	
	var options = [
	        // Title, function to call, needs file upload
	        [ "Set Semantic Type", setSemanticType],
	        [ "divider", null],

	        [	"Add Column" , addColumn ],
			[ "Rename", renameColumn ],
			[ "Split Column", splitColumn ],
			[ "Add Row", addRow],
			[ "divider" , null ],
			
			["Extract Entities",extractEntities],
			[ "PyTransform" , pyTransform ],
			[ "Transform", transform],
			[ "divider" , null ],
			
			[ "Invoke Service" , invokeService ],
			[ "Show Chart", showChart],

			[ "divider" , null ],
			["Group By", GroupBy], 
			["Unfold", Unfold] 
	];
	
	function hideDropdown() {
		$('.dropdown.open .dropdown-toggle').dropdown('toggle');
	}
	
	function setSemanticType() {
		SetSemanticTypeDialog.getInstance().show(worksheetId, columnId, columnTitle);
	}
	
	function addRow() {
		var info = new Object();
	    info["worksheetId"] = worksheetId;
	    info["workspaceId"] = $.workspaceGlobalInformation.id;
	    info["hNodeId"] = columnId;
	    info["hTableId"] = "";
	    info["command"] = "AddRowCommand";

	    var newInfo = [];   // Used for commands that take JSONArray as input
	    newInfo.push(getParamObject("hNodeId", columnId,"hNodeId"));
	    newInfo.push(getParamObject("hTableId", "","other"));
	    newInfo.push(getParamObject("worksheetId",worksheetId,"worksheetId"));
	    
	    info["newInfo"] = JSON.stringify(newInfo);

	    // console.log(info["worksheetId"]);
	    showLoading(info["worksheetId"]);

	    var returned = $.ajax({
	        url: "RequestController",
	        type: "POST",
	        data : info,
	        dataType : "json",
	        complete :
	            function (xhr, textStatus) {
	                // alert(xhr.responseText);
	                var json = $.parseJSON(xhr.responseText);
	                parse(json);
	                hideLoading(info["worksheetId"]);
	            },
	        error :
	            function (xhr, textStatus) {
	                alert("Error occured while removing semantic types!" + textStatus);
	                hideLoading(info["worksheetId"]);
	            }
	    });
	}
	
	function addColumn() {
		hideDropdown();
		AddColumnDialog.getInstance().show(worksheetId, columnId);
	    return false;
	}
	
	
	function extractEntities() {
		hideDropdown();
		ExtractEntitiesDialog.getInstance().show(worksheetId, columnId);
        return false;
	}
	
	function pyTransform() {
		hideDropdown();
		PyTransformDialog.getInstance().show(worksheetId, columnId, columnTitle);
	    return false;
	}
	
	function invokeService() {
		var info = new Object();
        info["workspaceId"] = $.workspaceGlobalInformation.id;
        info["worksheetId"] = worksheetId;
        info["hNodeId"] = columnId;
        info["command"] = "InvokeServiceCommand";
        
        showWaitingSignOnScreen();
        var returned = $.ajax({
            url: "RequestController", 
            type: "POST",
            data : info,
            dataType : "json",
            complete : 
                function (xhr, textStatus) {
                    var json = $.parseJSON(xhr.responseText);
                    parse(json);
                    hideWaitingSignOnScreen();
                },
            error :
                function (xhr, textStatus) {
                    $.sticky("Error invoking services!");
                }          
        });
	}
	
	function renameColumn() {
		hideDropdown();
		RenameColumnDialog.getInstance().show(worksheetId, columnId);
		return false;
	}
	
	function splitColumn() {
		hideDropdown();
		SplitColumnDialog.getInstance().show(worksheetId, columnId);
        return false;
	}
	
	function transform() {
		hideDropdown();
		TransformColumnDialog.getInstance().show(worksheetId, columnId);
		return false;
	}
	
	function showChart() {
		showChartForColumn(worksheetId, columnId);
	}

	function GroupBy () {
		//console.log("Group By: " + worksheetTitle);
		hideDropdown();
		GroupByDialog.getInstance().show(worksheetId, columnId);
  }

  function Unfold () {
		//console.log("Group By: " + worksheetTitle);
		hideDropdown();
		UnfoldDialog.getInstance().show(worksheetId, columnId);
  }
	
	this.generateJS = function() {
		var dropdownId = "columnOptionsButton" + worksheetId + "_" + columnId;
		var div = 
			$("<div>")
				.attr("id", "TableOptionsDiv")
				.data("worksheetId", worksheetId)
				.addClass("tableDropdown")
				.addClass("dropdown")
				.append($("<a>")
						.attr("href", "#")
						.addClass("dropdown-toggle")
						.addClass("ColumnTitle")
						.attr("id", dropdownId)
						.data("worksheetId", worksheetId)
						.attr("data-toggle", "dropdown")
						.text(columnTitle)
						.append($("<span>").addClass("caret")
						)
				);
                

		var ul = $("<ul>").addClass("dropdown-menu");
		ul.attr("role", "menu")
			.attr("aria-labelledby", dropdownId);
		// console.log("There are " + options.length + " menu items");
		for(var i=0; i<options.length; i++) {
			var option = options[i];
			var needFile = false;
			if(option.length > 2 && option[2] == true)
				needFile = true;
			var li = $("<li>");
			// console.log("Got option" + option);
			var title = option[0];
			if(title == "divider")
				li.addClass("divider");
			else {
				var func = option[1];
				var a = $("<a>")
						.attr("href", "#");
				if(needFile) {
					// <form id="fileupload" action="ImportFileCommand"
					// method="POST" enctype="multipart/form-data">From
					// File<input type="file" name="files[]" multiple></form>
					a.addClass("fileinput-button");
					var form = $("<form>")
								.attr("id", option[3])
								.attr("action", "ImportFileCommand")
								.attr("method", "POST")
								.attr("enctype", "multipart/form-data")
								.text(title);
					var input = $("<input>")
								.attr("type", "file")
								.attr("name", "files[]");
					form.append(input);
					a.append(form);
					window.setTimeout(func, 1000);
				} else {
					a.text(title);
					a.click(func);
				}
				li.append(a);
			}
			ul.append(li);
		};
		div.append(ul);
		return div;
	};
};



var AddColumnDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
    	var dialog = $("#addColumnDialog");
    	var worksheetId, columnId;
    	
    	function init() {
    		// Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function (e) {
				hideError();
                $("input", dialog).val("");
                $("#columnName", dialog).focus();
			});
			
			// Initialize handler for Save button
			// var me = this;
			$('#btnSave', dialog).on('click', function (e) {
				e.preventDefault();
				saveDialog(e);
			});
    	}
    	
		function hideError() {
			$("div.error", dialog).hide();
		}
		
		function showError() {
			$("div.error", dialog).show();
		}
        
        function saveDialog(e) {
        	console.log("Save clicked");
	
		    var newColumnValue = $.trim($("#columnName", dialog).val());
		    var defaultValue = $.trim($("#defaultValue", dialog).val());
		    
		    var validationResult = true;
		    if (!newColumnValue)
		        validationResult = false;
		    // Check if the column name already exists
		    var columnNames = getColumnHeadings(worksheetId);
		    $.each(columnNames, function(index, columnName) {
		        if (columnName == newColumnValue) {
		            validationResult = false;
		        }
		    });
		    if (!validationResult) {
		    	showError();
		        $("#columnName", dialog).focus();
		        return false;
		    }

		    dialog.modal('hide');

		    var info = new Object();
		    info["worksheetId"] = worksheetId;
		    info["workspaceId"] = $.workspaceGlobalInformation.id;
		    info["hNodeId"] = columnId;
		    info["hTableId"] = "";
		    info["newColumnName"] = "new_column";
		    info["command"] = "AddColumnCommand";

		    var newInfo = [];	// Used for commands that take JSONArray as
								// input
		    newInfo.push(getParamObject("hNodeId", columnId,"hNodeId"));
		    newInfo.push(getParamObject("hTableId", "","other"));
		    newInfo.push(getParamObject("worksheetId", worksheetId,"worksheetId"));
		    newInfo.push(getParamObject("newColumnName", newColumnValue,"other"));
		    newInfo.push(getParamObject("defaultValue", defaultValue,"other"));
		    info["newInfo"] = JSON.stringify(newInfo);

		    // console.log(info["worksheetId"]);
		    showLoading(info["worksheetId"]);

		    var returned = $.ajax({
		        url: "RequestController",
		        type: "POST",
		        data : info,
		        dataType : "json",
		        complete :
		            function (xhr, textStatus) {
		                // alert(xhr.responseText);
		                var json = $.parseJSON(xhr.responseText);
		                parse(json);
		                hideLoading(info["worksheetId"]);
		            },
		        error :
		            function (xhr, textStatus) {
		                alert("Error occured while removing semantic types!" + textStatus);
		                hideLoading(info["worksheetId"]);
		            }
		    });
        };
        
        function show(wsId, colId) {
        	worksheetId = wsId;
        	columnId = colId;
            dialog.modal({keyboard:true, show:true, backdrop:'static'});
        };
        
        
        return {	// Return back the public methods
        	show : show,
        	init : init
        };
    };

    function getInstance() {
    	if( ! instance ) {
    		instance = new PrivateConstructor();
    		instance.init();
    	}
    	return instance;
    }
   
    return {
    	getInstance : getInstance
    };
    	
    
})();





var RenameColumnDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
    	var dialog = $("#renameColumnDialog");
    	var worksheetId, columnId;
    	
    	function init() {
    		// Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function (e) {
				hideError();
                $("input", dialog).val("");
                $("#columnName", dialog).focus();
			});
			
			// Initialize handler for Save button
			// var me = this;
			$('#btnSave', dialog).on('click', function (e) {
				e.preventDefault();
				saveDialog(e);
			});
    	}
    	
		function hideError() {
			$("div.error", dialog).hide();
		}
		
		function showError() {
			$("div.error", dialog).show();
		}
        
        function saveDialog(e) {
        	console.log("Save clicked");
	
		    var newColumnValue = $.trim($("#columnName", dialog).val());
		   
		    var validationResult = true;
		    if (!newColumnValue)
		        validationResult = false;
		    // Check if the column name already exists
		    var columnNames = getColumnHeadings(worksheetId);
		    $.each(columnNames, function(index, columnName) {
		        if (columnName == newColumnValue) {
		            validationResult = false;
		        }
		    });
		    if (!validationResult) {
		    	showError();
		        $("#columnName", dialog).focus();
		        return false;
		    }

		    dialog.modal('hide');

		    var info = new Object();
		    var newInfo = [];   // for input parameters
		    newInfo.push(getParamObject("worksheetId", worksheetId ,"worksheetId"));
		    newInfo.push(getParamObject("hNodeId", columnId,"hNodeId"));
		    newInfo.push(getParamObject("newColumnName", newColumnValue, "other"));
		    newInfo.push(getParamObject("getAlignmentUpdate", ($("#svgDiv_" + worksheetId).length >0), "other"));
		    info["newInfo"] = JSON.stringify(newInfo);
		    info["workspaceId"] = $.workspaceGlobalInformation.id;
		    info["command"] = "RenameColumnCommand";

		    var returned = $.ajax({
		        url: "RequestController",
		        type: "POST",
		        data : info,
		        dataType : "json",
		        complete :
		            function (xhr, textStatus) {
		                var json = $.parseJSON(xhr.responseText);
		                parse(json);
		            },
		        error :
		            function (xhr, textStatus) {
		                $.sticky("Error occured while renaming column!");
		            }
		    });
        };
        
        function show(wsId, colId) {
        	worksheetId = wsId;
        	columnId = colId;
            dialog.modal({keyboard:true, show:true, backdrop:'static'});
        };
        
        
        return {	// Return back the public methods
        	show : show,
        	init : init
        };
    };

    function getInstance() {
    	if( ! instance ) {
    		instance = new PrivateConstructor();
    		instance.init();
    	}
    	return instance;
    }
   
    return {
    	getInstance : getInstance
    };
    	
    
})();


var SplitColumnDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
    	var dialog = $("#splitColumnDialog");
    	var worksheetId, columnId;
    	
    	function init() {
    		// Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function (e) {
				hideError();
                $("input", dialog).val("");
                $("#columnSplitDelimiter", dialog).focus();
			});
			
			// Initialize handler for Save button
			// var me = this;
			$('#btnSave', dialog).on('click', function (e) {
				e.preventDefault();
				saveDialog(e);
			});
    	}
    	
		function hideError() {
			$("div.error", dialog).hide();
		}
		
		function showError() {
			$("div.error", dialog).show();
		}
        
        function saveDialog(e) {
        	console.log("Save clicked");
	
		    var delimiter = $.trim($("#columnSplitDelimiter", dialog).val());
		   
		    var validationResult = true;
		    if (!delimiter)
		        validationResult = false;
		    else if(delimiter != "space" && delimiter != "tab" && delimiter.length != 1)
		    	validationResult = false;
		    if (!validationResult) {
		    	showError();
		        $("#columnSplitDelimiter", dialog).focus();
		        return false;
		    }

		    dialog.modal('hide');

		    var info = new Object();
		    info["worksheetId"] = worksheetId;
		    info["workspaceId"] = $.workspaceGlobalInformation.id;
		    info["hNodeId"] = columnId;
		    info["delimiter"] = delimiter;
		    info["command"] = "SplitByCommaCommand";

		    var newInfo = [];
		    newInfo.push(getParamObject("worksheetId", worksheetId, "worksheetId"));
		    newInfo.push(getParamObject("hNodeId", columnId,"hNodeId"));
		    newInfo.push(getParamObject("delimiter", delimiter, "other"));
		    info["newInfo"] = JSON.stringify(newInfo);

		    showLoading(info["worksheetId"]);
		    var returned = $.ajax({
		        url: "RequestController",
		        type: "POST",
		        data : info,
		        dataType : "json",
		        complete :
		            function (xhr, textStatus) {
		                // alert(xhr.responseText);
		                var json = $.parseJSON(xhr.responseText);
		                parse(json);
		                hideLoading(info["worksheetId"]);
		            },
		        error :
		            function (xhr, textStatus) {
		                alert("Error occured while splitting a column by comma! " + textStatus);
		                hideLoading(info["worksheetId"]);
		            }
		    });
        };
        
        function show(wsId, colId) {
        	worksheetId = wsId;
        	columnId = colId;
            dialog.modal({keyboard:true, show:true, backdrop:'static'});
        };
        
        
        return {	// Return back the public methods
        	show : show,
        	init : init
        };
    };

    function getInstance() {
    	if( ! instance ) {
    		instance = new PrivateConstructor();
    		instance.init();
    	}
    	return instance;
    }
   
    return {
    	getInstance : getInstance
    };
    	
    
})();




var PyTransformDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
    	var dialog = $("#pyTransformDialog");
    	var worksheetId, columnId, columnName;
    	var editor;
    	
    	function init() {
    		  editor = ace.edit("transformCodeEditor");
    		  editor.setTheme("ace/theme/dreamweaver");
    		  editor.getSession().setMode("ace/mode/python");
    		  editor.getSession().setUseWrapMode(true);
    		  editor.getSession().setValue("return getValue(\"state\")");
    		    
    		    
    		// Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function (e) {
				hideError();
				var hNode = $("td#" + columnId);
               
                if(hNode.data("pythonTransformation"))
                	editor.getSession().setValue(hNode.data("pythonTransformation"));
                else
                	editor.getSession().setValue("return getValue(\"" + columnName + "\")");
               
                $("#pythonTransformEditColumnName").html(columnName);
                $("#pythonTransformNewColumnName").val("");
                // $("#pythonTransformNewColumnName").attr('disabled','disabled');
                
                $("#btnError", dialog).button('disable');
                $("input").removeAttr('disabled');
                $("#pythonPreviewResultsTable").hide();
			});
			
			// Initialize handler for Save button
			// var me = this;
			$('#btnSave', dialog).on('click', function (e) {
				e.preventDefault();
				saveDialog(e);
			});
			
			$('#btnError', dialog).on('click', function(event) {
			     $("#pyTransformErrorWindow").show();
			});
			
			$('#btnPreview', dialog).on('click', function(e) {
				previewTransform();
			});
    	}
    	
		function hideError() {
			$("div.error", dialog).hide();
		}
		
		function showError() {
			$("div.error", dialog).show();
		}
        
        function saveDialog(e) {
        	console.log("Save clicked");
        	var hNode = $("td#" + columnId);
    	    
        	var transformType = $('input:radio[name=pyTransformType]:checked').val();
        	console.log("Got transform type: " + transformType);
        	if(transformType == "edit") {
        		if(hNode.data("columnDerivedFrom"))
        			submitEditPythonTransform();
        		else {
        			// alert("We need to handle this extension of python
					// transform");
        			submitAddPythonTransform(true);
        		}
        	} else {
        		submitAddPythonTransform(false);
        	}
        };
        
        function previewTransform() {
        	var info = {};
		    info["hNodeId"] = columnId;
		    info["workspaceId"] = $.workspaceGlobalInformation.id;
		    info["worksheetId"] = worksheetId;
		    info["transformationCode"] = editor.getValue();
		    info["errorDefaultValue"] = $("#pythonTransformErrorDefaultValue").val();
		    info["command"] = "PreviewPythonTransformationResultsCommand";

		    // Send the request
		    $.ajax({
		        url: "RequestController",
		        type: "POST",
		        data : info,
		        dataType : "json",
		        complete :
		            function (xhr, textStatus) {
		                var json = $.parseJSON(xhr.responseText);
		                var previewTable = $("table#pythonPreviewResultsTable");
		                $("tr",previewTable).remove();
		                $.each(json["elements"], function(index, element) {
		                    if(element["updateType"] == "PythonPreviewResultsUpdate") {
		                        var result = element["result"];
		                        $.each(result, function(index2, resVal){
		                            previewTable.append($("<tr>").append($("<td>").text(resVal.value)));
		                        });
		                        $("div.pythonError", errorWindow).remove();
		                        var errors = element["errors"];
		                        if (errors.length > 0) {
		                            $("#pyTransformViewErrorButton").button('enable');
		                            var errorWindow = $("#pyTransformErrorWindow");
		                            $.each(errors, function(index3, error){
		                                var errorHtml = $("<div>").addClass("pythonError")
		                                    .append($("<span>").addClass("pythonErrorRowNumber").text("Row: " + error.row)).append($("<br>"))
		                                    .append($("<span>").addClass("pythonErrorText").text("Error: " + error["error"])).append($("<br>")).append($("<br>"));
		                                errorWindow.append(errorHtml);
		                            })
		                        } else {
		                            $("#pyTransformViewErrorButton").button('disable');
		                        }
		                    } else if(element["updateType"] == "KarmaError") {
		                        $.sticky(element["Error"]);
		                    }
		                });
		                previewTable.show();
		            },
		        error :
		            function (xhr, textStatus) {
		                alert("Error occured with fetching new rows! " + textStatus);
		            }
		    });
        }
        
        function submitEditPythonTransform() {
       	    var hNode = $("td#" + columnId);
       	    var columnName = $("#pythonTransformEditColumnName").val();
       	    
       	    hide();
       	    
       	    var prevTransCode = hNode.data("pythonTransformation");
       	    var newTransCode = 	editor.getValue();
       	    
       	    if(prevTransCode.trim() == newTransCode.trim()) {
       	    	console.log("Code has not changed, we do not need to perform an edit");
       	    	return;
       	    }
       	    
       	
           // prepare the JSON Object to be sent to the server
           var info = {};
           info["workspaceId"] = $.workspaceGlobalInformation.id;
           info["command"] = "SubmitEditPythonTransformationCommand";

           var newInfo = [];
           newInfo.push(getParamObject("newColumnName",columnName, "other"));
           newInfo.push(getParamObject("transformationCode", newTransCode, "other"));
           newInfo.push(getParamObject("worksheetId", worksheetId, "worksheetId"));
           newInfo.push(getParamObject("hNodeId", hNode.data("columnDerivedFrom"), "hNodeId"));
           
           newInfo.push(getParamObject("previousCommandId", hNode.data("previousCommandId"), "other"));
           newInfo.push(getParamObject("errorDefaultValue", $("#pythonTransformErrorDefaultValue").val(), "other"));
           newInfo.push(getParamObject("targetHNodeId", columnId, "hNodeId"));
           info["newInfo"] = JSON.stringify(newInfo);

           showLoading(worksheetId);
           sendRequest(info, worksheetId);
       }
       
        function submitAddPythonTransform(useExistingColumnName) {
            var hNodeId = columnId;
 
            var columnName = (useExistingColumnName == true)? $("#pythonTransformEditColumnName").html() : $("#pythonTransformNewColumnName").val();
            // Validate new column name
            var validationResult = true;
            if (!columnName)
                validationResult = false;
            // Check if the column name already exists
            if(!useExistingColumnName) {
        	    var columnNames = getColumnHeadings(worksheetId);
        	    $.each(columnNames, function(index, element) {
        	        if ($.trim(element) == columnName) {
        	            validationResult = false;
        	        }
        	    });
            }
            if (!validationResult) {
                showError();
                $("#pythonTransformNewColumnName").focus();
                return false;
            }

            hide();
            
            // prepare the JSON Object to be sent to the server
            var info = {};
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["command"] = "SubmitPythonTransformationCommand";

            var newInfo = [];
            newInfo.push(getParamObject("newColumnName",columnName, "other"));
            newInfo.push(getParamObject("transformationCode", editor.getValue(), "other"));
            newInfo.push(getParamObject("worksheetId", worksheetId, "worksheetId"));
            newInfo.push(getParamObject("hNodeId", hNodeId, "hNodeId"));
            newInfo.push(getParamObject("errorDefaultValue", $("#pythonTransformErrorDefaultValue").val(), "other"));
           // newInfo.push(getParamObject("useExistingColumnName",
			// useExistingColumnName, "useExistingColumnName"));
            info["newInfo"] = JSON.stringify(newInfo);

            showLoading(worksheetId)
            sendRequest(info, worksheetId);
        }
        
        function sendRequest(info, worksheetId) {
        	 // Send the request
            $.ajax({
                url: "RequestController",
                type: "POST",
                data : info,
                dataType : "json",
                complete :
                    function (xhr, textStatus) {
                        var json = $.parseJSON(xhr.responseText);
                        parse(json);
                        hideLoading(worksheetId);
                    },
                error :
                    function (xhr, textStatus) {
                        alert("Error occured with fetching new rows! " + textStatus);
                        hideLoading(worksheetId);
                    }
            });
        }
        
        function hide() {
        	dialog.modal('hide');
        }
        
        function show(wsId, colId, colName) {
        	worksheetId = wsId;
        	columnId = colId;
        	columnName = colName;
            dialog.modal({keyboard:true, show:true, backdrop:'static'});
        };
        
        
        return {	// Return back the public methods
        	show : show,
        	init : init
        };
    };

    function getInstance() {
    	if( ! instance ) {
    		instance = new PrivateConstructor();
    		instance.init();
    	}
    	return instance;
    }
   
    return {
    	getInstance : getInstance
    };
    	
    
})();

var ExtractEntitiesDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
    	var dialog = $("#extractEntitiesDialog");
    	var worksheetId, columnId;
    
    	function init() {
    		// Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function (e) {
				console.log("dialog displayed");
				hideError();
                $('#extractionService_URL').val("");
			});
			
			// Initialize handler for Save button
			// var me = this;
			$('#btnSave', dialog).on('click', function (e) {
				e.preventDefault();
				saveDialog(e);
				console.log("dialog hidden after save");
			});
    	}
				
		function hideError() {
			$("div.error", dialog).hide();
		}
		
		function showError() {
			$("div.error", dialog).show();
		}
        
        function saveDialog(e) {
        	console.log("Save clicked");
    		var info = new Object();
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["worksheetId"] = worksheetId;
    		info["hNodeId"] = columnId;
    	    info["hTableId"] = "";
            info["command"] = "ExtractEntitiesCommand";
            info["extractionURL"] = $('#extractionService_URL').val();
            
		    dialog.modal('hide');

		    // console.log(info["worksheetId"]);
		    showLoading(info["worksheetId"]);

		    var returned = $.ajax({
			   	url: "RequestController",
			   	type: "POST",
			   	data : info,
			   	dataType : "json",
			   	complete :
			   		function (xhr, textStatus) {
			    		var json = $.parseJSON(xhr.responseText);
			    		parse(json);
			    		hideLoading(info["worksheetId"]);
				   	},
				error :
					function (xhr, textStatus) {
			   			alert("Error occured while extracting entities!" + textStatus);
			   			hideLoading(info["worksheetId"]);
				   	}
			});	
        };
        
        function show(wsId, colId) {
        	worksheetId = wsId;
        	columnId = colId;
            dialog.modal({keyboard:true, show:true, backdrop:'static'});
        };
        
        
        return {	// Return back the public methods
        	show : show,
        	init : init
        };
    };

    function getInstance() {
    	if( ! instance ) {
    		instance = new PrivateConstructor();
    		instance.init();
    	}
    	return instance;
    }
   
    return {
    	getInstance : getInstance
    };
    	
    
})();


var GroupByDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
        var dialog = $("#groupByDialog");
        var worksheetId, columnId;
        function init() {
            
            //Initialize handler for Save button
            //var me = this;
            $('#btnSave', dialog).on('click', function (e) {
                e.preventDefault();
                saveDialog(e);
            });    
        }
        
        function hideError() {
            $("div.error", dialog).hide();
        }
        
        function showError() {
            $("div.error", dialog).show();
        }
        
        function saveDialog(e) {
            console.log("Save clicked");
            
            var checkboxes = dialog.find(":checked");
            var checked = [];
            for (var i = 0; i < checkboxes.length; i++) {
                var checkbox = checkboxes[i];
                checked.push(getParamObject("checked", checkbox['value'], "other"));    
            }
            if (checked.length == 0) {
            	hide();
            	return;
            }
            //console.log(checked);
            var info = new Object();
            info["worksheetId"] = worksheetId;
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["command"] = "GroupByCommand";

            var newInfo = [];
            newInfo.push(getParamObject("worksheetId", worksheetId, "worksheetId"));
		    		newInfo.push(getParamObject("hNodeId", columnId, "hNodeId"));
            newInfo.push(getParamObject("values", JSON.stringify(checked), "other"));
            info["newInfo"] = JSON.stringify(newInfo);

            showLoading(info["worksheetId"]);
            var returned = $.ajax({
                url: "RequestController",
                type: "POST",
                data : info,
                dataType : "json",
                complete :
                    function (xhr, textStatus) {
                        //alert(xhr.responseText);
                        var json = $.parseJSON(xhr.responseText);
                        console.log(json);
                        parse(json);
                        hideLoading(info["worksheetId"]);
                    },
                error :
                    function (xhr, textStatus) {
                        alert("Error occured while generating the automatic model!" + textStatus);
                        hideLoading(info["worksheetId"]);
                    }
            });
            
            hide();
        };
        
        function hide() {
            dialog.modal('hide');
        }
        function getHeaders() {
        	var info = new Object();
	        info["worksheetId"] = worksheetId;
	        info["workspaceId"] = $.workspaceGlobalInformation.id;
	        info["hNodeId"] = columnId;
	        info["command"] = "GetHeadersCommand";
	        var headers;
	        var returned = $.ajax({
	            url: "RequestController",
	            type: "POST",
	            data : info,
	            dataType : "json",
	            async : false,
	            complete :
	                function (xhr, textStatus) {
		            	var json = $.parseJSON(xhr.responseText);
		            	headers = json.elements[0];
	                },
	            error :
	                function (xhr, textStatus) {
	                    alert("Error occured while getting worksheet headers!" + textStatus);
	                    hideLoading(info["worksheetId"]);
	                }
	        });
	        return headers;
        }
        function show(wsId, cId) {
            worksheetId = wsId;
            columnId = cId;
            dialog.on('show.bs.modal', function (e) {
                hideError();
                var dialogContent = $("#groupByDialogColumns", dialog);
                dialogContent.empty();
                var headers = getHeaders();
                //console.log(headers);
                for (var i = 0; i < headers.length; i++) {

                	var columnName = headers[i].ColumnName;
                	var id = headers[i].HNodeId;
                	//console.log(columnName);
                	//console.log(id);
                	var row = $("<div>").addClass("checkbox");
                  var label = $("<label>").text(columnName);
                  var input = $("<input>")
                      					.attr("type", "checkbox")
                                .attr("id", "selectcolumns")
                                .attr("value", id);
                  label.append(input);
                  row.append(label);
                  dialogContent.append(row);
                }
            });
            dialog.modal({keyboard:true, show:true, backdrop:'static'});
        };
        
        return {    //Return back the public methods
            show : show,
            init : init
        };
    };

    function getInstance() {
        if( ! instance ) {
            instance = new PrivateConstructor();
            instance.init();
        }
        return instance;
    }
   
    return {
        getInstance : getInstance
    };
})();

var UnfoldDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
        var dialog = $("#unfoldDialog");
        var worksheetId, columnId;
        function init() {
            
            //Initialize handler for Save button
            //var me = this;
            $('#btnSave', dialog).on('click', function (e) {
                e.preventDefault();
                saveDialog(e);
            });    
        }
        
        function hideError() {
            $("div.error", dialog).hide();
        }
        
        function showError() {
            $("div.error", dialog).show();
        }
        
        function saveDialog(e) {
            console.log("Save clicked");
            
            var checkboxes = dialog.find(":checked");
            if (checkboxes.length == 0) {
            	hide();
            	return;
            }
            var checked = checkboxes[0];
            
            //console.log(checked);
            var info = new Object();
            info["worksheetId"] = worksheetId;
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["command"] = "UnfoldCommand";
            var newInfo = [];
            newInfo.push(getParamObject("keyhNodeId", columnId, "other"));
            newInfo.push(getParamObject("valuehNodeId", checked['value'], "other"));
            newInfo.push(getParamObject("worksheetId", worksheetId, "worksheetId"));
            info["newInfo"] = JSON.stringify(newInfo);
            showLoading(info["worksheetId"]);
            var returned = $.ajax({
                url: "RequestController",
                type: "POST",
                data : info,
                dataType : "json",
                complete :
                    function (xhr, textStatus) {
                        //alert(xhr.responseText);
                        var json = $.parseJSON(xhr.responseText);
                        console.log(json);
                        parse(json);
                        hideLoading(info["worksheetId"]);
                    },
                error :
                    function (xhr, textStatus) {
                        alert("Error occured while generating the automatic model!" + textStatus);
                        hideLoading(info["worksheetId"]);
                    }
            });
            
            hide();
        };
        
        function hide() {
            dialog.modal('hide');
        }
        function getHeaders() {
        	var info = new Object();
	        info["worksheetId"] = worksheetId;
	        info["workspaceId"] = $.workspaceGlobalInformation.id;
	        info["hNodeId"] = columnId;
	        info["command"] = "GetHeadersCommand";
	        var headers;
	        var returned = $.ajax({
	            url: "RequestController",
	            type: "POST",
	            data : info,
	            dataType : "json",
	            async : false,
	            complete :
	                function (xhr, textStatus) {
		            	var json = $.parseJSON(xhr.responseText);
		            	headers = json.elements[0];
	                },
	            error :
	                function (xhr, textStatus) {
	                    alert("Error occured while getting worksheet headers!" + textStatus);
	                    hideLoading(info["worksheetId"]);
	                }
	        });
	        return headers;
        }
        function show(wsId, cId) {
            worksheetId = wsId;
            columnId = cId;
            dialog.on('show.bs.modal', function (e) {
                hideError();
                var dialogContent = $("#unfoldDialogColumns", dialog);
                dialogContent.empty();
                var headers = getHeaders();
                //console.log(headers);
                for (var i = 0; i < headers.length; i++) {

                	var columnName = headers[i].ColumnName;
                	var id = headers[i].HNodeId;
                	//console.log(columnName);
                	//console.log(id);
                	var row = $("<div>").addClass("checkbox");
                  var label = $("<label>").text(columnName);
                  var input = $("<input>")
                      					.attr("type", "radio")
                                .attr("id", "selectcolumns")
                                .attr("value", id)
                                .attr("name", "unfoldColumn");
                  label.append(input);
                  row.append(label);
                  dialogContent.append(row);
                }
            });
            dialog.modal({keyboard:true, show:true, backdrop:'static'});
        };
        
        return {    //Return back the public methods
            show : show,
            init : init
        };
    };

    function getInstance() {
        if( ! instance ) {
            instance = new PrivateConstructor();
            instance.init();
        }
        return instance;
    }
   
    return {
        getInstance : getInstance
    };
})();

