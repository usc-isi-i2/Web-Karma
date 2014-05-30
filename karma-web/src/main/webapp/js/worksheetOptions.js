function WorksheetOptions(wsId, wsTitle) {
	 
	var worksheetId = wsId;
	var worksheetTitle = wsTitle;
	var worksheetOptionsDiv;
	
	var options = [
	        {name:"View model using straight lines", func:viewStraightLineModel, showCheckbox:true, defaultChecked:true, initFunc:initStrightLineModel},
	        {name:"Organize Columns", func:organizeColumns},
	        {name:"divider"},
	        {name:"Show Model" , func:showModel},
			{name:"Set Properties", func:setProperties},
			{name:"Show Auto Model", func:showAutoModel},
			{name:"Apply R2RML Model" , func:applyR2RMLModel, useFileUpload:true, uploadDiv:"applyWorksheetHistory"},
			{name:"divider"},
			{name:"Publish RDF" , func:publishRDF},
			{name:"Publish Model" , func:publishModel},
			{name:"Save Model" , func:saveModel},
			{name:"Clear Model" , func:clearModel},
			{name:"Fetch Model" , func:fetchModel},
			{name:"Publish Service Model", func:publishServiceModel},
			{name:"Publish Report", func:publishReport},
			{name:"Save as JSON", func:saveAsJson},
			{name:"divider"},
			{name:"Populate Source", func:populateSource},
			{name:"Invoke Service", func:invokeService},
			{name:"divider"},
			{name:"Export to CSV", func:exportToCSV},
			{name:"Export to Database", func:exportToDatabase},
			{name:"Export to MDB", func:exportToMDB},
			{name:"Export to SpatialData", func:exportToSpatial},
			{name:"divider"},
			{name:"Fold" , func:Fold},
			{name:"GroupBy" , func:GroupBy}, 
			{name:"Glue" , func:Glue}, 
			{name:"Delete", func:deleteWorksheet},
	];
	
	function hideDropdown() {
		$('.dropdown.open .dropdown-toggle').dropdown('toggle');
	}
	
	function getCheckboxState(event) {
		var target = event.target;
		var checkbox;
		if(target.localName == "input") {
			checkbox = target;
		} else {
			checkbox = $("input[type='checkbox']", target)[0];
			$(checkbox).prop('checked', !checkbox.checked);
		}
		return checkbox.checked;
	}
	
	function organizeColumns() {
		hideDropdown();
		OrganizeColumnsDialog.getInstance().show(worksheetId);
		return false;
	}
	
	function publishReport() {
		hideDropdown();
		var info = new Object();
        info["worksheetId"] = worksheetId;
        info["workspaceId"] = $.workspaceGlobalInformation.id;
        info["command"] = "PublishReportCommand";

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
                    alert("Error publishing report" + textStatus);
                    hideLoading(info["worksheetId"]);
                }
        });
        return false;
	}
	function deleteWorksheet() {
		if(confirm("Are you sure you wish to delete the worksheet? \nYou cannot undo this operation")) {
			hideDropdown();
			var info = new Object();
	        info["worksheetId"] = worksheetId;
	        info["workspaceId"] = $.workspaceGlobalInformation.id;
	        info["command"] = "DeleteWorksheetCommand";

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
	                    alert("Error deleting worksheet" + textStatus);
	                    hideLoading(info["worksheetId"]);
	                }
	        });
		}
		return false;
	}
	
	function viewStraightLineModel(event) {
		var isChecked = getCheckboxState(event);
		console.log("viewStraightLineModel: " + isChecked);
		worksheetOptionsDiv.data("viewStraightLineModel", isChecked);
		
		hideDropdown();
		refreshAlignmentTree(worksheetId);
		return false;
	}
	
	function initStrightLineModel() {
		if(worksheetOptionsDiv)
			worksheetOptionsDiv.data("viewStraightLineModel", true);
		else
			window.setTimeout(initStrightLineModel, 100);
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
		console.log("Fold: " + worksheetTitle);
		hideDropdown();
		FoldDialog.getInstance().show(worksheetId);
    }

    function GroupBy () {
		console.log("GroupBy: " + worksheetTitle);
		hideDropdown();
		GroupByDialog2.getInstance().show(worksheetId);
    }
    function Glue () {
		console.log("Glue: " + worksheetTitle);
		hideDropdown();
		GlueDialog2.getInstance().show(worksheetId);
    }
  function saveRowID () {
		console.log("saveRowID: " + worksheetTitle);
		hideDropdown();
		var checked = [];
		$(".selectRowID").each( function(index, checkbox) {
			if (checkbox.checked) {
				checked.push(getParamObject("selectedRowID", checkbox['value'], "other"));
				console.log(checkbox['value']);
			}
		});
		var info = new Object();
	  info["worksheetId"] = worksheetId;
	  info["workspaceId"] = $.workspaceGlobalInformation.id;
	  info["command"] = "SaveRowIDCommand";

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
	        //parse(json);
	        hideLoading(info["worksheetId"]);
	    },
	    error :
	      function (xhr, textStatus) {
	        alert("Error occured while generating the automatic model!" + textStatus);
	        hideLoading(info["worksheetId"]);
	      }
	  });
		//console.log(checked);
		//FoldDialog.getInstance().show(worksheetId);
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
		$("#applyWorksheetHistory_" + worksheetId).fileupload({
	        add : function (e, data) {
	            $("#applyWorksheetHistory_" + worksheetId).fileupload({
	                url: "RequestController?workspaceId=" + $.workspaceGlobalInformation.id +
	                    "&command=ApplyHistoryFromR2RMLModelCommand&worksheetId=" + worksheetId
	            });
	            hideDropdown();
	            showLoading(worksheetId);
	            data.submit();
	        },
	        done: function(e, data) {
	            $("div.fileupload-progress").hide();
	            console.log(data);
	            parse(data.result);
	            hideLoading(worksheetId);
	        },
	        fail: function(e, data) {
	            $.sticky("History file upload failed!");
	            hideLoading(worksheetId);
	        },
	        dropZone: null
	    });
		$('#applyWorksheetHistory_' + worksheetId).fileupload('option', 'redirect', window.location.href.replace(/\/[^\/]*$/, '/cors/result.html?%s'));
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

	function saveModel(event) {
		console.log("Save Model: " + worksheetTitle);
		hideDropdown();
		saveModelDialog.getInstance().show(worksheetId);
		return false;
	}

	function clearModel(event) {
		console.log("Clear Model: " + worksheetTitle);
		hideDropdown();
		clearModelDialog.getInstance().show(worksheetId);
		return false;
	}

	function fetchModel(event) {
		console.log("Fetch Model: " + worksheetTitle);
		hideDropdown();
		fetchModelListDialog.getInstance().show(worksheetId);
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
	
	function saveAsJson() {
		console.log("Save as json");
		hideDropdown();
		PublishJSONDialog.getInstance().show(worksheetId);
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
			var needFile = option.useFileUpload;
			
			var li = $("<li>");
			//console.log("Got option" +  option);
			var title = option.name;
			if(title == "divider")
				li.addClass("divider");
			else {
				var func = option.func;
				var a = $("<a>")
						.attr("href", "#");
				if(needFile) {
					//<form id="fileupload" action="ImportFileCommand" method="POST" enctype="multipart/form-data">From File<input type="file" name="files[]" multiple></form>
					a.addClass("fileinput-button");
					var form = $("<form>")
								.attr("id", option.uploadDiv + "_" + worksheetId)
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
					if(option.showCheckbox) {
						var checkbox = $("<input>").attr("type", "checkbox");
						if(option.defaultChecked)
							checkbox.attr("checked","checked");
						var label = $("<span>").append(checkbox).append("&nbsp;").append(title);
						a.append(label);
						a.click(func);
					} else {
						a.text(title);
						a.click(func);
					}
					
				}
				li.append(a);
			}
			if(option.initFunc)
				option.initFunc();
			ul.append(li);
		};
		div.append(ul);
		worksheetOptionsDiv = div;
		return div;
	}
	
	
}