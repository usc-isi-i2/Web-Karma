var SetPropertiesDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
    	var dialog = $("#setPropertiesDialog");
    	var worksheetId;
    	
    	function init() {
    		//Initialize what happens when we show the dialog
    		dialog.on('show.bs.modal', function (e) {
				hideError();
				$("#serviceOptions").prop('checked', false);
	    		$('#worksheetServiceOptions').hide();
	    		$("#servicePostOptions").hide();
				fetchExistingWorksheetOptions();
			});
			
			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function (e) {
				e.preventDefault();
				saveDialog(e);
			});
			
			$('#serviceOptions').on('click',  function(e) {
		        $('#worksheetServiceOptions').toggle();
		    });


		    $('#serviceRequestMethod').on("change", function(e) {
		    	console.log("serviceRequestMethod change: " + $(this).val() );
		        if ($(this).val() == "POST") {
		            $("#servicePostOptions").show();
		        } else {
		            $("#servicePostOptions").hide();
		        }
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
        	
    	    // Prepare the input data
    	    var worksheetProps = new Object();
    	    worksheetProps["graphName"] = $("#graphNameInput").val();

    	    // Set service options if the window is visible
    	    if ($('#worksheetServiceOptions').is(':visible')) {
    	        worksheetProps["hasServiceProperties"] = true;
    	        worksheetProps["serviceUrl"] = $("#serviceUrlInput").val();
    	        worksheetProps["serviceRequestMethod"] = $("#serviceRequestMethod option:selected").text();
    	        if ($("#serviceRequestMethod option:selected").text() == "POST") {
    	            worksheetProps["serviceDataPostMethod"] = $("input:radio[name=serviceDataPostMethod]:checked").val();
    	        }

    	    } else {
    	        worksheetProps["hasServiceProperties"] = false;
    	    }

    	    var info = new Object();
    	    info["workspaceId"] = $.workspaceGlobalInformation.id;
    	    info["command"] = "SetWorksheetPropertiesCommand";

    	    var newInfo = [];   // for input parameters
    	    newInfo.push(getParamObject("worksheetId", worksheetId ,"worksheetId"));
    	    newInfo.push(getParamObject("properties", worksheetProps, "other"));
    	    info["newInfo"] = JSON.stringify(newInfo);
    	    // Store the data to be shown later when the dialog is opened again
    	    $("div#" + info["worksheetId"]).data("worksheetProperties", worksheetProps);

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
    	            },
    	        error :
    	            function (xhr, textStatus) {
    	                $.sticky("Error occurred while setting properties!");
    	            }
    	    });
    	    hide();
        };
        
        function fetchExistingWorksheetOptions() {
            
            var info = new Object();
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["command"] = "FetchExistingWorksheetPropertiesCommand";
            info["worksheetId"] = worksheetId;

            var returned = $.ajax({
                url: "RequestController",
                type: "POST",
                data : info,
                dataType : "json",
                complete :
                    function (xhr, textStatus) {
                        var json = $.parseJSON(xhr.responseText);
                        var props = json["elements"][0]["properties"];

                        // Set model name
                        if (props["graphName"] != null) {
                            $("#graphNameInput").val(props["graphName"]);
                        } else {
                            $("#graphNameInput").val("");
                        }
                        // Set service options if present
                        if (props["hasServiceProperties"]) {
                            // Select the service option checkbox
                            $("#serviceOptions").trigger("click");

                            // Set the service URL
                            if (props["serviceUrl"] != null) {
                                $("#serviceUrlInput").val(props["serviceUrl"]);
                            } else {
                                $("#serviceUrlInput").val("");
                            }

                            // Set the request method
                            var index = (props["serviceRequestMethod"] === "GET") ? 0 : 1;
                            $('#serviceRequestMethod option').eq(index).prop('selected', true);

                            // Set the POST request invocation method
                            if (props["serviceRequestMethod"] === "POST") {
                                $("#servicePostOptions").show();
                                $(":radio[value=" +props["serviceDataPostMethod"]+"]").prop('checked',true);
                            }

                        } else {
                            $("#serviceUrlInput").val("");
                            $('#serviceRequestMethod option').eq(0).prop('selected', true);
                        }
                    },
                error :
                    function (xhr, textStatus) {
                        $.sticky("Error occurred while fetching worksheet properties!");
                    }
            });
        }
        
        function hide() {
        	dialog.modal('hide');
        }
        
        function show(wsId) {
        	worksheetId = wsId;
        	dialog.modal({keyboard:true, show:true, backdrop:'static'});
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



var PublishModelDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
    	var dialog = $("#publishModelDialog");
    	var worksheetId;
    	
    	function init() {
    		//Initialize what happens when we show the dialog
    		dialog.on('show.bs.modal', function (e) {
				hideError();
				 $('#txtR2RML_URL').val('http://'+window.location.host + '/openrdf-sesame/repositories/karma_models');
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
        	hide();
        	 if(!testSparqlEndPoint($("input#txtR2RML_URL").val(), worksheetId)) {
    	        alert("Invalid sparql end point. Could not establish connection.");
    	        return;
    	    }

    	    var info = new Object();
    	    info["worksheetId"] = worksheetId;
    	    info["workspaceId"] = $.workspaceGlobalInformation.id;
    	    info["command"] = "GenerateR2RMLModelCommand";
    	    info['tripleStoreUrl'] = $('#txtR2RML_URL').val();

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
        };
        
        function hide() {
        	dialog.modal('hide');
        }
        
        function show(wsId) {
        	worksheetId = wsId;
        	dialog.modal({keyboard:true, show:true, backdrop:'static'});
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


var FetchModelDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
    	var dialog = $("#fetchModelDialog");
    	var worksheetId;
    	
    	function init() {
    		//Initialize what happens when we show the dialog
    		dialog.on('show.bs.modal', function (e) {
				hideError();
				 $('#txtR2RML_URL_fetch').val('http://'+window.location.host + '/openrdf-sesame/repositories/karma_models');
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
        	hide();
        	
    	    var info = new Object();
    	    info["worksheetId"] = worksheetId;
    	    info["workspaceId"] = $.workspaceGlobalInformation.id;
    	    info["command"] = "FetchR2RMLModelsCommand";
    	    info['tripleStoreUrl'] = $('#txtR2RML_URL_fetch').val();

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
    	                alert("Error occured while generating the automatic model!" + textStatus);
    	                hideLoading(info["worksheetId"]);
    	            }
    	    }); 
        };
        
        function hide() {
        	dialog.modal('hide');
        }
        
        function show(wsId) {
        	worksheetId = wsId;
        	dialog.modal({keyboard:true, show:true, backdrop:'static'});
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





var ExportCSVModelDialog = (function() {
    var instance = null;

    function setOpeartingMode(mode) {
    	opeartingMode = mode;
    };
    
    function PrivateConstructor(mode) {
    	var dialog = $("#exportCSVDialog");
    	var worksheetId;
    	var alignmentNodeId;
    	var columnId;
    	var operatingMode = mode;
    	
    	function init() {
    		//Initialize what happens when we show the dialog
    		dialog.on('show.bs.modal', function (e) {
				hideError();
				
				var url2 = 'http://'+window.location.host + '/openrdf-sesame/repositories/karma_data';
				$("input#csvDataEndPoint").val(url2);	    		
				fetchGraphsFromTripleStore(url2, $('#csvDataGraphList') );
				
				//$("input#csvSPAQRLEndPoint").val('http://'+window.location.host + '/openrdf-sesame/repositories/karma_models');
				//window.csvSPAQRLEndPoint = 'http://'+window.location.host + '/openrdf-sesame/repositories/karma_models';
				//fetchGraphsFromTripleStore(window.csvSPAQRLEndPoint, $("#csvModelGraphList"));
				
				//Initialize handler for ExportCSV button
				$('#btnExportCSV', dialog).unbind('click');
				
				if (operatingMode === "invokeMLService") {
					$('#exportCSV_ModelTitle').html('Invoke Machine Learning Service');
					$('#btnExportCSV', dialog).html('Invoke');
					$('div.formDivDMUrl', dialog).show();
					
					//Initialize handler for ExportCSV button
					$('#btnExportCSV', dialog).on('click', function (e) {
						performInvokeMLService();
					});
					
				} else {
					$('#exportCSV_ModelTitle').html('Export CSV');
					$('#btnExportCSV', dialog).html('Export');
					$('div.formDivDMUrl', dialog).hide();
					
					//Initialize handler for ExportCSV button
					$('#btnExportCSV', dialog).on('click', function (e) {
						performExportCSV();
					});
				}
				
				getColumnList();
				//$('#csvDialogContent').show();
				$('#csvDialogColumnList').html('');
				//$('#csvDialogColumnList').hide();
				//$('#btnExportCSV').hide();
				
				$('#csvDataDialogContent').hide();
				
			});
    		
//			//Initialize handler for Save button
//			$('#btnSave', dialog).on('click', function (e) {
//				e.preventDefault();
//				getColumnList();
//			});
			
			//Initialize change handler for sparql end point text box for url
			$('input#csvSPAQRLEndPoint').on('focusout', function(event){
				fetchGraphsFromTripleStore($("#csvSPAQRLEndPoint").val(), $("#csvModelGraphList"));
	        });
			
			//Initialize change handler for sparql end point text box for url
			$('input#csvDataEndPoint').on('focusout', function(event){
				fetchGraphsFromTripleStore($("#csvDataEndPoint").val(), $("#csvDataGraphList"));
	        });
    	}
    	
    	// this method will fetch all the context (graphs) from the given endpoint
    	// it takes in the url and the input element object
    	function fetchGraphsFromTripleStore(url, modelGraphList) {
    		
    		var info = new Object();
    		info["workspaceId"] = $.workspaceGlobalInformation.id;
    		info["command"] = "FetchGraphsFromTripleStoreCommand";
    		info["tripleStoreUrl"] = url;
    		var returned = $.ajax({
    		   	url: "RequestController", 
    		   	type: "POST",
    		   	data : info,
    		   	dataType : "json",
    		   	complete : 
    		   		function (xhr, textStatus) {
    		   			var json = $.parseJSON(xhr.responseText);
    		   			graphs = [];
    		   			if(json["elements"] && json["elements"][0]['graphs']) {
    		   				graphs = json["elements"][0]['graphs'];
    		   			}
    		   			//var modelGraphList = $("#csvModelGraphList");
    		   			modelGraphList.html('<option value="000">Current Worksheet</option>');
    		   			for (var x in graphs) {
    		   				var str = graphs[x];
    		   				modelGraphList.append('<option value="'+graphs[x]+'">'+str+'</option>');
    		   			}
    			   	},
    			error :
    				function (xhr, textStatus) {
    		   			alert("Error occurred with fetching graphs! " + textStatus);
    			   	}		   
    		});
    	}
    	
    	
    	function performInvokeMLService() {
    		
    		var graphUri = $('#csvDataGraphList').val().trim();
    		
    		var list = {};
			$('#csv_columns').find('li').each(function(index){
				list[index] = {'name' : $(this).attr('rel'), 'url':$(this).attr('name') };
			});
			
    		var info = new Object();
    		info["workspaceId"] = $.workspaceGlobalInformation.id;
    		info["worksheetId"] = worksheetId;
    		info["command"] = "ExportCSVCommand";
    		info["rootNodeId"] = $('#csv_columns').attr('rel');
    		info["tripleStoreUrl"] = $("#csvDataEndPoint").val();
    		info["graphUrl"] =  graphUri ;
    		info["columnList"] = JSON.stringify(list);
    		
    		var returned = $.ajax({
    		   	url: "RequestController", 
    		   	type: "POST",
    		   	data : info,
    		   	dataType : "json",
    		   	complete : 
    		   		function (xhr, textStatus) {
    		   			var json = $.parseJSON(xhr.responseText);
    		   			
    		   			var fileName = json["elements"][0]['fileUrl'];
    		   			
    		   			var dmURL = $('#dataMiningUrl').val().trim();
    		   			if(dmURL.length < 2) {
    		   				dmURL = 'http://localhost:8088/train';
    		   			}
    		   			var info = new Object();
    		    		info["workspaceId"] = $.workspaceGlobalInformation.id;
    		    		info["worksheetId"] = worksheetId;
    		    		info["command"] = "InvokeDataMiningServiceCommand";
    		    		info["dataMiningURL"] = dmURL;
    		    		info["csvFileName"] = fileName;
    		    		info["isTestingPhase"] = $('#testingService').is(':checked');
    		    		
    		    		
    		    		$.ajax({
    		    		   	url: "RequestController", 
    		    		   	type: "POST",
    		    		   	data : info,
    		    		   	dataType : "json",
    		    		   	complete : 
    		    		   		function (xhr, textStatus) {
    		    		   			var json = $.parseJSON(xhr.responseText);
    		    		   			parse(json);
    		    		   			hide();
    		    		   			alert("This results are loaded in a new worksheet");
//    		    		   			if(json["elements"][0]['updateType'] && json["elements"][0]['updateType']=="KarmaError") {
//    		    		   				alert("Error while invoking service");
//    		    		   				hide();
//    		    		   			} else if(json["elements"][0]['status'] && json["elements"][0]['status'] == true) {
//    		    		   				hide();
////    		    		   				$('#DMresults').html(json["elements"][0]['results']);
////    		    		   				$('#DMresults').show();
//    		    		   			}
//    		    		   			else {
//    		    		   				var model_name = json["elements"][0]['model_name'];
//    		    		   				hide();
//    		    		   				alert('Model Name: '+model_name);
//    		    		   			}
    		    		   			
    		    			   	},
    		    			error :
    		    				function (xhr, textStatus) {
    		    		   			alert("Error occurred while invoking service! " + textStatus);
    		    			   	}		   
    		    		});
    		   			
    			   	},
    			error :
    				function (xhr, textStatus) {
    		   			alert("Error occurred while exporting CSV! " + textStatus);
    			   	}		   
    		});
    	}
    	
    	function performExportCSV() {
    		
    		var graphUri = $('#csvDataGraphList').val().trim();
    		//graphUri = (graphUri == '000') ? '' : graphUri;
    		
    		var list = {};
			$('#csv_columns').find('li').each(function(index){
				list[index] = {'name' : $(this).attr('rel'), 'url':$(this).attr('name') };
			});
			
    		var info = new Object();
    		info["workspaceId"] = $.workspaceGlobalInformation.id;
    		info["worksheetId"] = worksheetId;
    		info["command"] = "ExportCSVCommand";
    		info["rootNodeId"] = $('#csv_columns').attr('rel');
    		info["tripleStoreUrl"] = $("#csvDataEndPoint").val();
    		info["graphUrl"] =  graphUri ;
    		info["columnList"] = JSON.stringify(list);
    		
    		var returned = $.ajax({
    		   	url: "RequestController", 
    		   	type: "POST",
    		   	data : info,
    		   	dataType : "json",
    		   	complete : 
    		   		function (xhr, textStatus) {
    		   			var json = $.parseJSON(xhr.responseText);
    		   			parse(json);
    		   			hide();
    			   	},
    			error :
    				function (xhr, textStatus) {
    		   			alert("Error occurred with fetching graphs! " + textStatus);
    			   	}		   
    		});
    	}
    	
    	function initCSVDataDialog() {
    		$("input#csvDataEndPoint").val('http://'+window.location.host + '/openrdf-sesame/repositories/karma_data');
    		var url2 = 'http://'+window.location.host + '/openrdf-sesame/repositories/karma_data';
			fetchGraphsFromTripleStore(url2, $('#csvDataGraphList') );
    		$('#csvDataDialogContent').show();
    	}
    	
    	// this method will fetch the columns that are reachable from this node
    	function getColumnList() {
//    		var graphUri = $('#csvModelGraphList').val().trim();
//    		graphUri = (graphUri == '000') ? '' : graphUri; 
    		var info = new Object();
    		info["workspaceId"] = $.workspaceGlobalInformation.id;
    		info["worksheetId"] = worksheetId;
    		info["command"] = "FetchColumnCommand";
//    		info["alignmentNodeId"] = alignmentNodeId;
//    		info["tripleStoreUrl"] = $("#csvSPAQRLEndPoint").val();
//    		info["graphUrl"] =  graphUri ;
    		info["nodeId"] = columnId;
    		var returned = $.ajax({
    		   	url: "RequestController", 
    		   	type: "POST",
    		   	data : info,
    		   	dataType : "json",
    		   	complete : 
    		   		function (xhr, textStatus) {
    		   			var data = $.parseJSON(xhr.responseText);
    		   			$('#csvDialogContent').hide();
    		   			
    		   			var ele = $('#csvDialogColumnList');
    		   			var content = '<ol id="csv_columns" rel="'+data['elements'][0]['rootId']+'">';
    		   			var list = data['elements'][0]['columns'];
    		   			for(var x in list) {
//    		   				var index = 0;
//    		   				var str = list[x];
//    		   				if(str.lastIndexOf('#') > 0 ) {
//    		   					index = str.lastIndexOf('#') + 1;
//    		   				} else if(str.lastIndexOf('/') > 0 ) {
//    		   					index = str.lastIndexOf('/') + 1;
//    		   				}
//    		   				str = str.substr(index, (str.length - index));
//    		   				
    		   				content += '<li style="padding=4px;" name="'+list[x]['url']+'" rel="'+list[x]['name']+'">'
    		   					+ list[x]['name']+' &nbsp; <a class="icon-remove pull-right">X</a>'  
    		   					+'</li>';
    		   			}
    		   			ele.html(content + '</ol>');
    		   			$("#csv_columns").delegate('a.icon-remove','click',function(event){
    		   				$(this).parent().remove();
    		   			});
    		   			
    		   			//$('#btnExportCSV').show();
    		   			$("#csv_columns").sortable();
    		   			
    		   			initCSVDataDialog();
    		   	}	   
    		});
    	}
    	
		function hideError() {
			$("div.error", dialog).hide();
		}
		
		function showError() {
			$("div.error", dialog).show();
		}
        
        function hide() {
        	dialog.modal('hide');
        }
        
        function show(wsId, algnId, colId,mode) {
        	worksheetId = wsId;
        	alignmentNodeId = algnId;
        	columnId = colId;
        	operatingMode = mode;
        	dialog.modal({keyboard:true, show:true, backdrop:'static'});
        };
        
        
        return {	//Return back the public methods
        	show : show,
        	init : init
        };
    };

    function getInstance(mode) {
    	if( ! instance ) {
    		instance = new PrivateConstructor(mode);
    		instance.init();
    	}
    	return instance;
    }
   
    return {
    	getInstance : getInstance
    };
    
})();


var FoldDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
    	var dialog = $("#foldDialog");
    	var worksheetId;
    	
    	function init() {
    		//Initialize what happens when we show the dialog
    		dialog.on('show.bs.modal', function (e) {
				hideError();
				var worksheetPanel = $("div.Worksheet#" + worksheetId);
		        var tableHeaderContainer = $("div.table-header-container", worksheetPanel);
		        var headersTable = $("table.wk-table", tableHeaderContainer);
		        var headersRow = $("tr.wk-row-odd", headersTable);
		        
		        var dialogContent = $("#foldDialogColumns", dialog);
		        dialogContent.empty();
		        
			    headersRow.each(function (index, element) {
		            var a = element.children;
		            
		            for (var i = 0; i < a.length; i++) {
		                var cell = $("div.tableDropdown", a[i]);
		                var name = $("a.dropdown-toggle", cell)
		                console.log(name.text());
		                if (name.html().indexOf("td") == -1) {
		                	var row = $("<div>").addClass("checkbox");
		                	var label = $("<label>").text(name.text());
		                	var input = $("<input>")
											.attr("type", "checkbox")
											.attr("id", "selectcolumns")
											.attr("value", element.cells[i].id);
		                	label.append(input);
		                	row.append(label);
		                	dialogContent.append(row);
		                }
		            }
		        });
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
        	
        	var checkboxes = dialog.find(":checked");
	        var checked = [];
	        for (var i = 0; i < checkboxes.length; i++) {
	            var checkbox = checkboxes[i];
	            checked.push(getParamObject("checked", checkbox['value'], "other"));    
	        }
	        
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
	        
    	    hide();
        };
        
        function hide() {
        	dialog.modal('hide');
        }
        
        function show(wsId) {
        	worksheetId = wsId;
        	dialog.modal({keyboard:true, show:true, backdrop:'static'});
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


var GroupByDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
        var dialog = $("#groupByDialog");
        var worksheetId;
        
        function init() {
            //Initialize what happens when we show the dialog
            dialog.on('show.bs.modal', function (e) {
                hideError();
                var worksheetPanel = $("div.Worksheet#" + worksheetId);
                var tableHeaderContainer = $("div.table-header-container", worksheetPanel);
                var headersTable = $("table.wk-table", tableHeaderContainer);
                var headersRow = $("tr.wk-row-odd", headersTable);
                
                var dialogContent = $("#groupByDialogColumns", dialog);
                dialogContent.empty();
                
                headersRow.each(function (index, element) {
                    var a = element.children;
                    console.log(a);
                    for (var i = 0; i < a.length; i++) {
                        var cell = $("div.tableDropdown", a[i]);
                        var name = $("a.dropdown-toggle", cell)
                        console.log(name.text());
                        if (name.html().indexOf("td") == -1) {
                            var row = $("<div>").addClass("checkbox");
                            var label = $("<label>").text(name.text());
                            var input = $("<input>")
                                            .attr("type", "checkbox")
                                            .attr("id", "selectcolumns")
                                            .attr("value", element.cells[i].id);
                            label.append(input);
                            row.append(label);
                            dialogContent.append(row);
                        }
                    }
                });
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
            
            var checkboxes = dialog.find(":checked");
            var checked = [];
            for (var i = 0; i < checkboxes.length; i++) {
                var checkbox = checkboxes[i];
                checked.push(getParamObject("checked", checkbox['value'], "other"));    
            }
            
            //console.log(checked);
            var info = new Object();
            info["worksheetId"] = worksheetId;
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["command"] = "UnfoldCommand";

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
            
            hide();
        };
        
        function hide() {
            dialog.modal('hide');
        }
        
        function show(wsId) {
            worksheetId = wsId;
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