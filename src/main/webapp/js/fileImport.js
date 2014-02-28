
$('#fileupload').fileupload({
    url : "RequestController?command=ImportFileCommand",
    add : function(e, data) {
    	console.log("add");
    	FileFormatSelectionDialog.getInstance().show(data);
    },
    done : function(e, data) {
    	console.log("done");
        parse(data.result);
    },
    fail : function(e, data) {
        $.sticky("File upload failed!");
    },
    dropZone : $(document)
});
//Enable iframe cross-domain access via redirect option:
$('#fileupload').fileupload('option', 'redirect', window.location.href.replace(/\/[^\/]*$/, '/cors/result.html?%s'));


var FileFormatSelectionDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
    	var dialog = $("#fileFormatSelectionDialog");
    	
    	function init() {
    		//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function (e) {
				$("span#fileFormatError").hide();
                $("input:radio[name=FileFormatSelection]").attr("checked", false);
                
                var fileName = $("#fileFormatSelectionDialog").data("fileName");
                console.log("show.bs.modal::Select filename:" + fileName);
                if(fileName.match(".csv$") || fileName.match(".tsv$") || fileName.match(".txt$") || fileName.match(".log$")) {
                    $(":radio[name=FileFormatSelection][value=CSVFile]").prop("checked", true);
                } else if(fileName.match(".xml$")) {
                    $(":radio[name=FileFormatSelection][value=XMLFile]").prop("checked", true);
                } else if(fileName.match(".xls$") || fileName.match(".xlsx$")) {
                    $(":radio[name=FileFormatSelection][value=ExcelFile]").prop("checked", true);
                } else if(fileName.match(".owl$") || fileName.match(".rdf$")) {
                    $(":radio[name=FileFormatSelection][value=Ontology]").prop("checked", true);
                } else if(fileName.match(".json$")) {
                    $(":radio[name=FileFormatSelection][value=JSONFile]").prop("checked", true);
                }
                
				var worksheets = $('.Worksheet');
                if (worksheets.size() > 0){
                    disableRevision(false);
  
                    worksheets.each(function(){
                        var item = $('<option />');
                        item.val($(this).attr('id'));
                        item.text($(this).find('.WorksheetTitle').text());

                        $('#revisedWorksheetSelector').append(item);
                    });
                } else {
                    disableRevision(true);
                }
			});
			
			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function (e) {
				e.preventDefault();
				saveDialog(e);
                dialog.modal('hide');
			});
    	}
    	
		
		function disableRevision (disabled){
            $('#revisedWorksheetSelector').prop('disabled', disabled);
            $("input:checkbox[name='RevisionCheck']").prop('disabled', disabled);
        };
        
        function saveDialog(e) {
        	console.log("Save clicked");
			var data = dialog.data("formData");
			var selectedFormat = $("input:radio[name='FileFormatSelection']:checked").val();
			console.log("Selected format:" + selectedFormat);
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

            $("#fileupload").fileupload({
            	url : urlString + "Import" + selectedFormat + "Command",
            	done : function(e, data) {
            		FileOptionsDialog.getInstance().show(data.result, selectedFormat);
            	}
            });

            data.submit();
        };
        
        function show(data) {
        	var fileName = data.files[0].name;
            dialog.data("fileName", fileName);
            dialog.data("formData", data);
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


var FileOptionsDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
    	var dialog = $("#fileOptionsDialog");
    	var optionSettings = {
    			"JSONFile": ["colEncoding", "colMaxNumLines"],
    			"CSVFile" : ["colDelimiterSelector", "colTextQualifier", "colHeaderStartIndex", "colStartRowIndex", "colEncoding", "colMaxNumLines"],
    			"XMLFile" : ["colEncoding", "colMaxNumLines"],
    			"ExcelFile" : ["colEncoding", "colMaxNumLines"],
    			"Ontology" : ["colEncoding", "colMaxNumLines"]
    	};
    	
    	function init() {
	    	//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function (e) {
				
			});
			
			//Initialize handler for Save button
			$('#btnSave', dialog).on('click', function (e) {
				e.preventDefault();
				saveDialog(e);
                dialog.modal('hide');
			});
			
			
			$('.ImportOption').change(function() {
                reloadOptions(false);
            });
    	}
    	
    	function saveDialog(e) {
    		console.log("Save clicked");
			reloadOptions(true);
    	}
    	
    	function reset() {
			 $("#delimiterSelector :nth-child(1)", dialog).attr('selected', 'selected');
			  $("#headerStartIndex", dialog).val("1");
			  $("#startRowIndex", dialog).val("2");
			  $("#textQualifier", dialog).val("\"");
			  $("#encoding", dialog).val("\"");
			  $("#maxNumLines", dialog).val("1000");
		};
		
		function showOptions(responseJSON) {
			console.log("ShowOptions: " + responseJSON);
			dialog.data("formData", responseJSON);
			var format = dialog.data("format");
			console.log("Got format: " + format);
			
			//Show only options that are relevant to the format
			$(".fileOptions").hide();
			var optionSetting = optionSettings[format];
			$.each(optionSetting, function(index, val) {
				$("#" + val).show();
			});
			if(format == "JSONFile" || format == "XMLFile") {
				$('#lblMaxNumLines').text("Objects to import");
				$(".help-block", $("#colMaxNumLines")).text("Enter 0 to import all objects");
			} else {
				$('#lblMaxNumLines').text("Rows to import");
				$(".help-block", $("#colMaxNumLines")).text("Enter 0 to import all rows");
			}
			
			var headers = null;
			var previewTable = $("#previewTable", dialog);
			$("thead", previewTable).remove();
			$("tr", previewTable).remove();
			  
			if (responseJSON) {
			    headers = responseJSON["elements"][0]["headers"];
				var encoding = responseJSON["elements"][0]["encoding"];
				$("#encoding", dialog).val(encoding);
				var maxNumLines = responseJSON["elements"][0]["maxNumLines"];
				$("#maxNumLines", dialog).val(maxNumLines);
				if(maxNumLines == -1)
					$("#colMaxNumLines").hide();
				else
					$("#colMaxNumLines").show();
				
				var rows = responseJSON["elements"][0]["rows"];
				if(rows) {
					generatePreview(headers, rows);
					$("#previewTableDiv").show();
				} else {
					$("#previewTableDiv").hide();
				}
				dialog.data("commandId", responseJSON["elements"][0]["commandId"]);
			}
		}

        // Pedro added the if(index>0) to not show the index of the table.
        // It is weird to have to do this, I guess the better solution is that
        // the data that came from the server would not include the index.
        // No big deal.
		function generatePreview(headers, rows) {
            var previewTable = $("#previewTable", dialog);
            //previewTable.append($("<thead>").append("<tr>").append($("<th colspan='4'>").text("File Row Number")));
            if(headers != null)  {
                var trTag = $("<tr>");
                $.each(headers, function(index, val) {
                    if (index > 0){
                        trTag.append($("<th>").text(val));
                    }
                });
                previewTable.append(trTag);
            } else {
                // Put empty column names
                var trTag = $("<tr>");
                $.each(rows[0], function(index, val) {
                    if (index>0){
                        trTag.append($("<th>").text("Column_" + index));
                    }
                });
                previewTable.append(trTag);
            }


            $.each(rows, function(index, row) {
                var trTag = $("<tr>");
                $.each(row, function(index2, val) {
                    if (index2 >0){
                        var displayVal = val;
                        if(displayVal.length > 20) {
                            displayVal = displayVal.substring(0,20) + "...";
                        }
                        trTag.append($("<td>").text(displayVal));
                    }
                });
                previewTable.append(trTag);
            });
			  
		}
		
		function reloadOptions(execute) {
			var format = dialog.data("format");
			var optionSetting = optionSettings[format];
			var options = new Object();
			options["command"] = "Import" + format + "Command";
			options["commandId"] = dialog.data("commandId");
			if($.inArray( "colDelimiterSelector", optionSetting ) != -1)
				options["delimiter"] = $("#delimiterSelector").val();
			if($.inArray( "colHeaderStartIndex", optionSetting ) != -1)
				options["CSVHeaderLineIndex"] = $("#headerStartIndex").val();
			if($.inArray( "colStartRowIndex", optionSetting ) != -1)
				options["startRowIndex"] = $("#startRowIndex").val();
			if($.inArray( "colTextQualifier", optionSetting ) != -1)
				options["textQualifier"] = $("#textQualifier").val();
			if($.inArray( "colEncoding", optionSetting ) != -1)
				options["encoding"] = $("#encoding").val();
			if($.inArray( "colMaxNumLines", optionSetting ) != -1)
				options["maxNumLines"] = $("#maxNumLines").val();
			
			options["workspaceId"] = $.workspaceGlobalInformation.id;
			options["interactionType"] = "generatePreview";
			
			if(execute) {
				options["execute"] = true;
				options["interactionType"] = "importTable";
				showWaitingSignOnScreen();
			}
			
			$.ajax({
				    url: "RequestController", 
				    type: "POST",
				    data : options,
				    dataType : "json",
				    complete : function (xhr, textStatus) {
				    			if(!execute) {
							    	var json = $.parseJSON(xhr.responseText);
							    	console.log("Got json:" + json);
							    	showOptions(json);
				    			} else {
				    				var json = $.parseJSON(xhr.responseText);
				    		        parse(json);
				    		        hideWaitingSignOnScreen();
				    		        if(format !== "Ontology") 
				    		        	showDialogToLoadModel(); //This is giving a JS error. Should go after conversion of this dialog to bootstrap
				    		        dialog.modal('hide');
				    			}
				    		}
				  });	
				
		}
		
		
		function show(data, format) {
        	reset();
        	var fileName = data["elements"][0]["fileName"];
        	$("#filename", dialog).html(fileName);
        	dialog.data("format", format);
        	showOptions(data);
            
            dialog.modal({keyboard:true, show:true});
		}
		
		return {
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