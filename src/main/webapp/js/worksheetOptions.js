function WorksheetOptions(wsId, wsTitle) {
	 
	var worksheetId = wsId;
	var worksheetTitle = wsTitle;
	
	var options = [
	        //Title, function to call, needs file upload       
			[	"Show Model" , showModel ],
			[ "Set Properties" , setProperties ],
			[ "Show Auto Model" , showAutoModel ],
			//[ "Reset Model" , resetModel ],
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
		handlePublishModelToStoreButton(event);
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
		var dialog = $('#FetchR2RMLModelDialogBox');
        $('#txtR2RML_URL_fetch').val('http://'+window.location.host + '/openrdf-sesame/repositories/karma_models');
        dialog.dialog(
            { title: 'SPARQL End point',
                buttons: { "Cancel": function() { $(this).dialog("close"); },
                    "Fetch": renderR2RMLModels }, width: 400, height: 170});
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
		 getDatabasePreferences();
	        var dbDialogBox = $("div#PublishDatabaseDialogBox");
	        dbDialogBox.data("worksheetId", worksheetId);
	        // Show the dialog box
	        dbDialogBox.dialog({width: 300
	            , buttons: { "Cancel": function() { $(this).dialog("close"); }, "Submit": publishDatabaseFunction }});
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