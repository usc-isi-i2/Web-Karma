function TableColumnOptions(wsId, wsColumnId, wsColumnTitle) {
	 
	var worksheetId = wsId;
	var columnTitle = wsColumnTitle;
	var columnId = wsColumnId;
	
	var options = [
	        //Title, function to call, needs file upload       
			[	"Add Column" , addColumn ],
			[ "Rename", renameColumn ],
			[ "Split Column", splitColumn ],
			[ "divider" , null ],
			
			[ "PyTransform" , pyTransform ],
			[ "Transform", transform],
			[ "divider" , null ],
			
			[ "Invoke Service" , invokeService ],
			[ "Show Chart", showChart],
			//[ "Apply R2RML Model" , applyR2RMLModel, true, "applyWorksheetHistory" ],
			//[ "divider" , null ],
			
	];
	
	function hideDropdown() {
		$('.dropdown.open .dropdown-toggle').dropdown('toggle');
	}
	
	function addColumn() {
		hideDropdown();
		
		AddColumnDialog.getInstance().show(worksheetId, columnId);
		
	    return false;
	}
	
	function pyTransform() {
		
	}
	
	function invokeService() {
		
	}
	
	function renameColumn() {
		RenameColumnDialog.getInstance().show(worksheetId, columnId);
	}
	
	function splitColumn() {
		hideDropdown();
		var splitPanel = $("div#SplitByCommaColumnListPanel");
        splitPanel.dialog({width: 300, height: 200
            , buttons: { "Cancel": function() { $(this).dialog("close"); }, "Submit": splitColumnByComma }});
        return false;
	}
	
	function transform() {
		
	}
	
	function showChart() {
		
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
		//console.log("There are " + options.length + " menu items");
		for(var i=0; i<options.length; i++) {
			var option = options[i];
			var needFile = false;
			if(option.length > 2 && option[2] == true)
				needFile = true;
			var li = $("<li>");
			//console.log("Got option" +  option);
			var title = option[0];
			if(title == "divider")
				li.addClass("divider");
			else {
				var func = option[1];
				var a = $("<a>")
						.attr("href", "#");
				if(needFile) {
					//<form id="fileupload" action="ImportFileCommand" method="POST" enctype="multipart/form-data">From File<input type="file" name="files[]" multiple></form>
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
    		//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function (e) {
				hideError();
                $("input", dialog).val("");
                $("#columnName", dialog).focus();
			});
			
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

		    var newInfo = [];	// Used for commands that take JSONArray as input
		    newInfo.push(getParamObject("hNodeId", columnId,"hNodeId"));
		    newInfo.push(getParamObject("hTableId", "","other"));
		    newInfo.push(getParamObject("worksheetId", worksheetId,"worksheetId"));
		    newInfo.push(getParamObject("newColumnName", newColumnValue,"other"));
		    newInfo.push(getParamObject("defaultValue", defaultValue,"other"));
		    info["newInfo"] = JSON.stringify(newInfo);

		    //console.log(info["worksheetId"]);
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
            dialog.modal({keyboard:true, show:true});
        };
        
        
        return {	//Return back the public methods
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
    		//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function (e) {
				hideError();
                $("input", dialog).val("");
                $("#columnName", dialog).focus();
			});
			
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
            dialog.modal({keyboard:true, show:true});
        };
        
        
        return {	//Return back the public methods
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