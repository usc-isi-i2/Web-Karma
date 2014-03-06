function WorksheetOptions(wsId, wsTitle) {
	 
	var worksheetId = wsId;
	var worksheetTitle = wsTitle;
	
	var options = [
	        //Title, function to call, needs file upload       
			[	"Show Model" , showModel ],
			[ "Set Properties" , setProperties ],
			[ "Show Auto Model" , showAutoModel ],
			[ "Fold" , Fold ],
			[ "Apply R2RML Model" , applyR2RMLModel, true, "applyWorksheetHistory" ],
			[ "divider" , null ],
			[ "Publish RDF" , publishRDF ],
			[ "Publish Model" , publishModel ],
			["Publish Service Model", publishServiceModel],
			[ "divider" , null ],
			["Populate Source", populateSource],
			["Invoke Service", invokeService],
			[ "divider" , null ],
			["Export to CSV", exportToCSV],
			["Export to Database", exportToDatabase],
			["Export to MDB", exportToMDB],
			["Export to SpatialData", exportToSpatial],
	];
	
	function hideDropdown() {
		$('.dropdown.open .dropdown-toggle').dropdown('toggle');
	}
	function showModel() {
		console.log("SHow Model: " + worksheetTitle);
		hideDropdown();
		var info = new Object();
        info["worksheetId"] = worksheetId;
        info["workspaceId"] = $.workspaceGlobalInformation.id;
        info["command"] = "ShowModelCommand";

        var newInfo = [];
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
                    var json = $.parseJSON(xhr.responseText);
                    parse(json);
                    hideLoading(info["worksheetId"]);
                },
            error :
                function (xhr, textStatus) {
                    alert("Error occured while generating semantic types!" + textStatus);
                    hideLoading(info["worksheetId"]);
                }
        });
		return false;
	}
	
	function setProperties() {
		console.log("Set Properties: " + worksheetTitle);
		hideDropdown();
		SetPropertiesDialog.getInstance().show(worksheetId);
		return false;
	}
	
	function showAutoModel() {
		console.log("SHow Auto Model: " + worksheetTitle);
		hideDropdown();
		var info = new Object();
        info["worksheetId"] = worksheetId;
        info["workspaceId"] = $.workspaceGlobalInformation.id;
        info["command"] = "ShowAutoModelCommand";

        var newInfo = [];
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
                    parse(json);
                    hideLoading(info["worksheetId"]);
                },
            error :
                function (xhr, textStatus) {
                    alert("Error occured while generating the automatic model!" + textStatus);
                    hideLoading(info["worksheetId"]);
                }
        });
		return false;
	}

	function Fold () {
        var worksheetPanel = $("div.Worksheet#" + worksheetId);
        var tableContainer = $("div.table-container", worksheetPanel);
        var tableHeaderContainer = $("div.table-header-container", worksheetPanel);
        var headersTable = $("table.wk-table", tableHeaderContainer);
        var headersRow = $("tr.wk-row-odd", headersTable);
        //console.log(worksheetId);
        //console.log($('div#foldDialog').html());
        $('div#foldDialog').empty();
        headersRow.each(function (index, element) {
            var a = element.children;
            
            for (var i = 0; i < a.length; i++) {
                var cell = $("div.tableDropdown", a[i]);
                var name = $("a.dropdown-toggle", cell)
                console.log(name.text());
                if (name.html().indexOf("td") == -1) {
                    $('<label />', {text: name.text()}).appendTo($('div#foldDialog'));
                    $('<input />', { type: 'checkbox', id: 'selectcolumns', value: element.cells[i].id }).appendTo($('div#foldDialog'));
                    $("</br>").appendTo($('div#foldDialog'));
                }
            }
        });
         $('div#foldDialog').dialog({width: 540, height: 460, title:"Fold", resizable:true
            , buttons: {
            "Cancel": function() { $(this).dialog("close"); },
            "Submit": submitFold}
        });
        //console.log(headersRow.html());
        //var headersTable = $("tbody.tr.wk-table htable-odd", tableHeaderContainer);
    }

	function submitFold() {
        var dialog = $('div#foldDialog');
        var checkboxes = dialog.find(":checked");
        var checked = [];
        for (var i = 0; i < checkboxes.length; i++) {
            var checkbox = checkboxes[i];
            checked.push(getParamObject("checked", checkbox['value'], "other"));    
        }
        $('div#foldDialog').dialog("close");
        //console.log(checked);
        var info = new Object();
        info["worksheetId"] = worksheetId;
        info["workspaceId"] = $.workspaceGlobalInformation.id;
        info["command"] = "FoldCommand";

        var newInfo = [];
        newInfo.push(getParamObject("worksheetId", worksheetId, "worksheetId"));
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
    }
	
	function resetModel() {
		console.log("Reset Model: " + worksheetTitle);
		hideDropdown();
		var info = new Object();
        info["worksheetId"] = worksheetId;
        info["workspaceId"] = $.workspaceGlobalInformation.id;
        info["command"] = "ResetModelCommand";

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
		return false;
	}
	
	function applyR2RMLModel() {
		console.log("Apply R2RMl Model: " + worksheetTitle);
		//hideDropdown();
		$("#applyWorksheetHistory").fileupload({
	        add : function (e, data) {
	            $("#applyWorksheetHistory").fileupload({
	                url: "RequestController?workspaceId=" + $.workspaceGlobalInformation.id +
	                    "&command=ApplyHistoryFromR2RMLModelCommand&worksheetId=" + worksheetId
	            });
	            hideDropdown();
	            showLoading(worksheetId);
	            data.submit();
	        },
	        done: function(e, data) {
	            $("div.fileupload-progress").hide();
	            parse(data.result);
	            hideLoading(worksheetId);
	        },
	        fail: function(e, data) {
	            $.sticky("History file upload failed!");
	            hideLoading(worksheetId);
	        },
	        dropZone: null
	    });
		$('#applyWorksheetHistory').fileupload('option', 'redirect', window.location.href.replace(/\/[^\/]*$/, '/cors/result.html?%s'));
		return false;
	}
	
	function publishRDF() {
		console.log("Publish RDF: " + worksheetTitle);
		hideDropdown();
		
		PublishRDFDialog.getInstance().show(worksheetId);
		
		return false;
	}
	
	function publishModel(event) {
		console.log("Publish Model: " + worksheetTitle);
		hideDropdown();
		PublishModelDialog.getInstance().show(worksheetId);
		return false;
	}
	
	function publishServiceModel() {
		console.log("Publish Service Model: " + worksheetTitle);
		hideDropdown();
		 var info = new Object();
	        info["worksheetId"] = worksheetId;
	        info["workspaceId"] = $.workspaceGlobalInformation.id;
	        info["command"] = "PublishModelCommand";

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
	                    alert("Error occured while publishing service model!" + textStatus);
	                    hideLoading(info["worksheetId"]);
	                }
	        });
		return false;
	}
	
	function populateSource() {
		console.log("Populate Source: " + worksheetTitle);
		hideDropdown();
		var info = new Object();
      info["worksheetId"] = worksheetId;
      info["workspaceId"] = $.workspaceGlobalInformation.id;
      info["command"] = "PopulateCommand";

      var newInfo = [];	// Used for commands that take JSONArray as input
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
                  parse(json);
                  hideLoading(info["worksheetId"]);
              },
          error :
              function (xhr, textStatus) {
                  alert("Error occured while populating source!" + textStatus);
                  hideLoading(info["worksheetId"]);
              }
      });
		return false;
	}
	
	function invokeService() {
		console.log("Invoke Service " + worksheetTitle);
		hideDropdown();
		FetchModelDialog.getInstance().show(worksheetId);
		return false;
	}
	
	function exportToCSV() {
		console.log("Export to CSV: " + worksheetTitle);
		hideDropdown();
		
		 var info = new Object();
	        info["worksheetId"] = worksheetId;
	        info["workspaceId"] = $.workspaceGlobalInformation.id;
	        info["command"] = "PublishCSVCommand";
	
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
	                    alert("Error occured while exporting CSV!" + textStatus);
	                    hideLoading(info["worksheetId"]);
	                }
	        });
		
		return false;
	}
	
	function exportToDatabase() {
		hideDropdown();
		PublishDatabaseDialog.getInstance().show(worksheetId);
		return false;
	}
	
	function exportToMDB() {
		console.log("Export To MDB: " + worksheetTitle);
		hideDropdown();
		
		var info = new Object();
        info["worksheetId"] = worksheetId;
        info["workspaceId"] = $.workspaceGlobalInformation.id;
        info["command"] = "PublishMDBCommand";

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
                    alert("Error occured while exporting MDB!" + textStatus);
                    hideLoading(info["worksheetId"]);
                }
        });
		return false;
	}
	
	function exportToSpatial() {
		console.log("Export to Spatial: " + worksheetTitle);
		hideDropdown();
		
		var info = new Object();
        info["worksheetId"] = worksheetId;
        info["workspaceId"] = $.workspaceGlobalInformation.id;
        info["command"] = "PublishSpatialDataCommand";

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
                    alert("Error occured while exporting spatial data!" + textStatus);
                    hideLoading(info["worksheetId"]);
                }
        });
		return false;
	}
	
	this.generateJS = function() {
		var div = 
			$("<div>")
				.attr("id", "WorksheetOptionsDiv")
				.data("worksheetId", worksheetId)
				.addClass("worksheetDropdown")
				.addClass("dropdown")
				.append($("<a>")
						.attr("href", "#")
						.addClass("dropdown-toggle")
						.addClass("WorksheetTitle")
						//.addClass("btn").addClass("dropdown-toggle").addClass("sr-only")
						.attr("id", "optionsButton" + worksheetId)
						.data("worksheetId", worksheetId)
						.attr("data-toggle", "dropdown")
						//.attr("type", "button")
						.text(worksheetTitle)
						.append($("<span>").addClass("caret")
						)
				);
                

		var ul = $("<ul>").addClass("dropdown-menu");
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
	}
	
	
}