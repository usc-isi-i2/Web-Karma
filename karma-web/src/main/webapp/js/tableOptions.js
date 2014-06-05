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
            
    	    var info = new Object();
    	    info["worksheetId"] = worksheetId;
    	    info["workspaceId"] = $.workspaceGlobalInformation.id;
    	    info["command"] = "GenerateR2RMLModelCommand";
            info['tripleStoreUrl'] = $('#txtR2RML_URL').val();
            info['localTripleStoreUrl'] = $('#txtModel_URL').val();
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

var applyModelDialog = (function() {
    var instance = null;
    function PrivateConstructor() {
        var dialog = $("#applyModelDialog");
        var worksheetId;
        var availableModels;
        var filteredModels;
        var filterDialog = $("#modelFilterDialog");
        var filterName;
        function init() {
            refresh();
            $('#btnSave', dialog).on('click', function (e) {
                e.preventDefault();
                saveDialog(e);
            });
            $('#btnClearFilter', dialog).on('click', function (e) {
                filteredModels = availableModels;
                instance.show(worksheetId);
            });
            var dialogContent = $("#applyModelDialogHeaders", dialog);
            dialogContent.empty();
            var div = $("<div>").css("display","table-row");
                var row = $("<div>").addClass("FileNameProperty");
                var label = $("<button>").text("Filter")
                        .addClass("btn btn-primary FileNameButtonProperty")
                        .attr("id","btnFilterName")
                        .attr("value","File Name");
                row.append(label);
                div.append(row);
                var row = $("<div>").addClass("PublishTimeProperty");
                var label = $("<button>").text("Filter")
                        .addClass("btn btn-primary PublishTimeButtonProperty")
                        .attr("id","btnFilterPublishTime")
                        .attr("value","Publish Time");
                row.append(label);
                div.append(row);
                var row = $("<div>").addClass("URLProperty");
                var label = $("<button>").text("Filter")
                        .addClass("btn btn-primary URLButtonProperty")
                        .attr("id","btnFilterURL")
                        .attr("value","URL");
                row.append(label);
                div.append(row);
                var row = $("<div>").addClass("ContextProperty");
                var label = $("<button>").text("Filter")
                        .addClass("btn btn-primary ContextButtonProperty")
                        .attr("id","btnFilterContext")
                        .attr("value","Context");
                row.append(label);
                div.append(row);
                dialogContent.append(div);
                var div = $("<div>").css("display","table-row");
                var row = $("<div>").addClass("FileNameProperty");
                var label = $("<label>").text("File Name");
                row.append(label);
                div.append(row);
                var row = $("<div>").addClass("PublishTimeProperty");
                var label = $("<label>").text("Publish Time");
                row.append(label);
                div.append(row);
                var row = $("<div>").addClass("URLProperty");
                var label = $("<label>").text("URL");
                row.append(label);
                div.append(row);
                var row = $("<div>").addClass("ContextProperty");
                var label = $("<label>").text("Context");
                row.append(label);
                div.append(row);
                dialogContent.append(div);
                $('#btnFilterName', dialog).on('click', function (e) {
                    e.preventDefault();
                    showFilterDialog(e);
                });
                $('#btnFilterPublishTime', dialog).on('click', function (e) {
                    e.preventDefault();
                    showFilterDialog(e);
                });
                $('#btnFilterURL', dialog).on('click', function (e) {
                    e.preventDefault();
                    showFilterDialog(e);
                });
                $('#btnFilterContext', dialog).on('click', function (e) {
                    e.preventDefault();
                    showFilterDialog(e);
                });
                $('#btnSave', filterDialog).on('click', function (e) {
                    e.preventDefault();
                    applyFilter(e);
                });
                $('#btnCancel', filterDialog).on('click', function (e) {
                    e.preventDefault();
                    cancelFilter(e);
                });      
        }
        function refresh() {
            console.log("refresh");
            var info = new Object();
            info["worksheetId"] = worksheetId;
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["command"] = "FetchR2RMLModelsListCommand";
            info['tripleStoreUrl'] = $('#txtModel_URL').val();
            info['graphContext'] = "";
            var returned = $.ajax({
                url: "RequestController",
                type: "POST",
                data : info,
                dataType : "json",
                async: false,
                complete :
                    function (xhr, textStatus) {
                        //alert(xhr.responseText);
                        var json = $.parseJSON(xhr.responseText);
                        json = json.elements[0];
                        console.log(json);
                        //parse(json);
                        availableModels = json;
                        filteredModels = availableModels;
                    },
                error :
                    function (xhr, textStatus) {
                        alert("Error occured while Fetching Models!" + textStatus);
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
            hide();

            console.log("here");
            var checkboxes = dialog.find(":checked");
            if (checkboxes.length == 0) {
                hide();
                return;
            }
            var checked = checkboxes[0];
            var info = new Object();
            info["worksheetId"] = worksheetId;
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["command"] = "ApplyModelFromURLCommand";
            info['modelRepository'] = $('#txtR2RML_URL_Fetch').val();
            info['modelContext'] = checked['src'];
            info['modelUrl'] = checked['value'];
            console.log(checked['src']);
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
                        refresh();

                    },
                error :
                    function (xhr, textStatus) {
                        alert("Error occured while applying models!" + textStatus);
                        hideLoading(info["worksheetId"]);
                        refresh();
                    }
            });
        };
        
        function hide() {
            dialog.modal('hide');
        }

        function showFilterDialog(e) {
            dialog.modal('hide');
            console.log("showFilterDialog");
            filterName = e.currentTarget['value'];
            console.log(filterName);
            $('#txtFilter').val("");
            filterDialog.modal({keyboard:true, show:true, backdrop:'static'});
            filterDialog.show();
        };

        function applyFilter(e) {
            console.log("applyFilter");
            console.log(filterName);
            var tmp = [];
            var filterText = $('#txtFilter').val();
            console.log(filterText);
            for (var i = 0; i < filteredModels.length; i++) {
                var name = filteredModels[i]['name'];
                var time = new Date(filteredModels[i].publishTime*1).toDateString();
                var url = filteredModels[i].url;
                var context = filteredModels[i].context;
                if (filterName === "File Name" && name.indexOf(filterText) > -1)
                    tmp.push(filteredModels[i]);
                if (filterName === "Publish Time" && time.indexOf(filterText) > -1)
                    tmp.push(filteredModels[i]);
                if (filterName === "URL" && url.indexOf(filterText) > -1)
                    tmp.push(filteredModels[i]);
                if (filterName === "Context" && context.indexOf(filterText) > -1)
                    tmp.push(filteredModels[i]);
            }
            console.log(tmp);
            filteredModels = tmp;
            instance.show(worksheetId);
            dialog.show();
        };

        function cancelFilter(e) {
            console.log("cancelFilter");
            instance.show(worksheetId);
            dialog.show();
        };
        
        function show(wsId) {
            worksheetId = wsId;
            dialog.on('show.bs.modal', function (e) {
                hideError();
                var dialogContent = $("#applyModelDialogColumns", dialog);
                dialogContent.empty();
                
                for (var i = 0; i < filteredModels.length; i++) {
                    var name = filteredModels[i]['name'];
                    var time = new Date(filteredModels[i].publishTime*1).toDateString();
                    var url = filteredModels[i].url;
                    var context = filteredModels[i].context;
                    var div = $("<div>").css("display","table-row");
                    var row = $("<div>").css("overflow", "scroll").addClass("FileNameProperty");
                    var label = $("<label>").text(name).css("overflow", "scroll");
                    row.append(label);
                    div.append(row);
                    var row = $("<div>").css("overflow", "scroll").addClass("PublishTimeProperty");
                    var label = $("<label>").text(time).css("overflow", "scroll");
                    row.append(label);
                    div.append(row);
                    var row = $("<div>").css("overflow", "scroll").addClass("URLProperty");
                    var label = $("<label>").text(url).css("overflow", "scroll");
                    row.append(label);
                    div.append(row);
                    var row = $("<div>").css("overflow", "scroll").addClass("ContextProperty");
                    var label = $("<label>").text(context).css("overflow", "scroll");
                    row.append(label);
                    div.append(row);
                    var row = $("<div>").css("float", "left").css("padding", "5px");;
                    var label = $("<input>")
                                .attr("type", "radio")
                                .attr("id", "selectcolumns")
                                .attr("value", url)
                                .attr("name", "selectGraphs")
                                .attr("src", context);
                    row.append(label);
                    div.append(row);
                    dialogContent.append(div);
                }
            });
            dialog.modal({keyboard:true, show:true, backdrop:'static'});
        };
        
        
        return {    //Return back the public methods
            show : show,
            init : init,
            refresh: refresh
        };
    };

    function getInstance() {
        if( ! instance ) {
            instance = new PrivateConstructor();
            instance.init();
        }
        instance.refresh();
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
				$("body").css("cursor", "auto");
				
				
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
                        console.log("got graphs ..");
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
            console.log("initCSVDataDialog..");
            
    	}
    	
    	// this method will fetch the columns that are reachable from this node
    	function getColumnList(showCallback, dialog) {
//    		var graphUri = $('#csvModelGraphList').val().trim();
//    		graphUri = (graphUri == '000') ? '' : graphUri; 
            console.log("getting columns ..");
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
                            console.log("done fetching columns..");
    		   			}
    		   			ele.html(content + '</ol>');
    		   			$("#csv_columns").delegate('a.icon-remove','click',function(event){
    		   				$(this).parent().remove();
    		   			});
    		   			
    		   			//$('#btnExportCSV').show();
    		   			$("#csv_columns").sortable();
    		   			
    		   			initCSVDataDialog();
                        showCallback(dialog);
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

            // here we need to get the list of columns before showing the dialog
            $("body").css("cursor", "progress");

            console.log("showing modal..");

            // var url2 = 'http://'+window.location.host + '/openrdf-sesame/repositories/karma_data';
            // $("input#csvDataEndPoint").val(url2);               
            // fetchGraphsFromTripleStore(url2, $('#csvDataGraphList') );
            
            //$("input#csvSPAQRLEndPoint").val('http://'+window.location.host + '/openrdf-sesame/repositories/karma_models');
            //window.csvSPAQRLEndPoint = 'http://'+window.location.host + '/openrdf-sesame/repositories/karma_models';
            //fetchGraphsFromTripleStore(window.csvSPAQRLEndPoint, $("#csvModelGraphList"));
            
            //Initialize handler for ExportCSV button
            $('#btnExportCSV', dialog).unbind('click');
            
            if (operatingMode === "invokeMLService") {
                $('#exportCSV_ModelTitle').html('Invoke Table Service');
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
            
            $('#csvDialogColumnList').html('');
            $('#csvDataDialogContent').hide();

            var showDialog = function(dialog){
                console.log("callback....executing......");
                dialog.modal({keyboard:true, show:true, backdrop:'static'});
            };

            getColumnList(showDialog, dialog);
            //$('#csvDialogContent').show();
            
            //$('#csvDialogColumnList').hide();
            //$('#btnExportCSV').hide();
            
            


        	
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
			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function (e) {
				e.preventDefault();
				saveDialog(e);
			});    
    	}

        function getHeaders() {
            var info = new Object();
            info["worksheetId"] = worksheetId;
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["hNodeId"] = "";
            info["commandName"] = "Fold";
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
	            checked.push(getParamObject("checked", checkbox['value'], "hNodeId"));    
	        }
	        if (checked.length == 0) {
                hide();
                return;
            }
	        //console.log(checked);
	        var info = new Object();
	        info["worksheetId"] = worksheetId;
	        info["workspaceId"] = $.workspaceGlobalInformation.id;
	        info["command"] = "FoldCommand";

	        var newInfo = [];
	        newInfo.push(getParamObject("worksheetId", worksheetId, "worksheetId"));
            newInfo.push(getParamObject("hNodeId", checkboxes[0]['value'], "hNodeId"));
	        newInfo.push(getParamObject("values", JSON.stringify(checked), "hNodeIdList"));
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
	                    alert("Error occured while folding!" + textStatus);
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
            dialog.on('show.bs.modal', function (e) {
                hideError();
                var dialogContent = $("#foldDialogColumns", dialog);
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

var GroupByDialog2 = (function() {
    var instance = null;

    function PrivateConstructor() {
        var dialog = $("#groupByDialog2");
        var worksheetId;
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
                checked.push(getParamObject("checked", checkbox['value'], "hNodeId"));    
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
            newInfo.push(getParamObject("hNodeId", checkboxes[0]['value'], "hNodeId"));
            newInfo.push(getParamObject("values", JSON.stringify(checked), "hNodeIdList"));
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
                        alert("Error occured while grouping by!" + textStatus);
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
            info["hNodeId"] = "";
            info["commandName"] = "GroupBy"
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
        function show(wsId) {
            worksheetId = wsId;
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

var GlueDialog2 = (function() {
    var instance = null;

    function PrivateConstructor() {
        var dialog = $("#glueDialog2");
        var worksheetId;
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
                checked.push(getParamObject("checked", checkbox['value'], "hNodeId"));    
            }
            if (checked.length == 0) {
                hide();
                return;
            }
            //console.log(checked);
            var info = new Object();
            info["worksheetId"] = worksheetId;
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["command"] = "GlueCommand";

            var newInfo = [];
            newInfo.push(getParamObject("worksheetId", worksheetId, "worksheetId"));
            newInfo.push(getParamObject("hNodeId", checkboxes[0]['value'], "hNodeId"));
            newInfo.push(getParamObject("values", JSON.stringify(checked), "hNodeIdList"));
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
                        alert("Error occured while gluing!" + textStatus);
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
            info["hNodeId"] = "";
            info["commandName"] = "Glue"
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
        function show(wsId) {
            worksheetId = wsId;
            dialog.on('show.bs.modal', function (e) {
                hideError();
                var dialogContent = $("#glueDialogColumns", dialog);
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

var OrganizeColumnsDialog = (function() {
    var instance = null;
    
    function PrivateConstructor() {
    	var dialog = $("#organizeColumnsDialog");
    	var _worksheetId;

    	
    	function init() {
    		//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function (e) {
				$(".error").hide();
				
				var wsColumnsJson = getAllWorksheetHeaders();
				console.log(window.JSON.stringify(wsColumnsJson));
				
                var columns = $('#organizeColumns_body', dialog);
                var nestableDiv = $("#nestable", columns);
                nestableDiv.empty();
                createColumnList(wsColumnsJson, nestableDiv, true);
                nestableDiv.nestable({
                    group: 1
                })
//                .on("change", function(e) {
//                	var list   = e.length ? e : $(e.target);
//                    if (window.JSON) {
//                        console.log(window.JSON.stringify(list.nestable('serialize')));
//                    } else {
//                       alert('JSON browser support required for this functionality.');
//                    }
//                })
                ;
			});
			
			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function (e) {
				e.preventDefault();
				saveDialog(e);
                dialog.modal('hide');
			});
    	}
    	
    	function getAllWorksheetHeaders() {
    		//console.log(checked);
	        var info = new Object();
	        info["worksheetId"] = _worksheetId;
	        info["workspaceId"] = $.workspaceGlobalInformation.id;
	        info["command"] = "GetAllWorksheetHeadersCommand";
	        
	        showLoading(info["worksheetId"]);
	        var headers = [];
	        var returned = $.ajax({
	            url: "RequestController",
	            type: "POST",
	            data : info,
	            dataType : "json",
	            async : false,
	            complete :
	                function (xhr, textStatus) {
		            	var json = $.parseJSON(xhr.responseText);
		            	var updates = json.elements;
		            	for(var i=0; i<updates.length; i++) {
		            		var update = updates[i];
		                	if(update.updateType == "AllWorksheetHeadersUpdate") {
		                		headers = update.columns;
		                		break;
		                	}
		                }
	                    hideLoading(info["worksheetId"]);
	                },
	            error :
	                function (xhr, textStatus) {
	                    alert("Error occured while getting worksheet headers!" + textStatus);
	                    hideLoading(info["worksheetId"]);
	                }
	        });
	        return headers;
    	}
    	
		function createColumnList(json, outer, parentVisible) {
			var list = $("<ol>").addClass("dd-list");
			outer.append(list);
			$.each(json, function(i, element) {
				var li = $("<li>").addClass("dd-item")
							.attr("data-name", element.name)
							.attr("data-id", element.id)
							.attr("data-visible", element.visible)
							.attr("data-hideable", element.hideable)
							.attr("data-toggle", "tooltip")
							.attr("data-placement", "auto bottom")
							;
				var eye = $("<span>").addClass("glyphicon").css("margin-right", "5px");
				if(element.visible) {
					eye.addClass("glyphicon-eye-open")
				} else {
					eye.addClass("glyphicon-eye-close");
				}
				var eyeOuter = $("<span>");
				eyeOuter.append(eye);
				var div = $("<div>").addClass("dd-handle").append(eyeOuter).append(element.name);
				if(!parentVisible) {
					div.addClass("dd-handle-hide-all");
					li.addClass("dd-item-hidden-all");
				} else if(!element.visible) {
					div.addClass("dd-handle-hide");
					li.attr("title", element.name)
					li.addClass("dd-item-hidden");
				}
				if(!element.hideable) {
					eye.css("color", "#DDDDDD");
					eye.addClass("glyphicon-noclick");
				}
				
				list.append(li);
				li.append(div);
			
				if(element.children) {
					if(element.children.length > 0)
						createColumnList(element.children, li, element.visible && parentVisible);
				}
			});
		
			$(".dd-item-hidden").tooltip(); //activate the bootstrap tooltip
		}
		
        function saveDialog(e) {
        	console.log("Save clicked");
        	var columns = $('#organizeColumns_body', dialog);
            var nestableDiv = $("#nestable", columns);
            var columnsJson = nestableDiv.nestable('serialize');
            
            var info = new Object();
		    info["worksheetId"] = _worksheetId;
		    info["workspaceId"] = $.workspaceGlobalInformation.id;
		    info["command"] = "OrganizeColumnsCommand";

		    var newInfo = [];
		    newInfo.push(getParamObject("worksheetId", _worksheetId, "worksheetId"));
		    newInfo.push(getParamObject("orderedColumns", columnsJson, "orderedColumns"));
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
		                alert("Error occured while organizing columns " + textStatus);
		                hideLoading(info["worksheetId"]);
		            }
		    });
		    
        };
        
        function show(worksheetId) {
        	_worksheetId = worksheetId;
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



var PublishJSONDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
    	var dialog = $("#publishJSONDialog");
    	var worksheetId;
    	
    	function init() {
    		//Initialize what happens when we show the dialog
    		dialog.on('show.bs.modal', function (e) {
				hideError();
			});
			
			//Initialize handler for Save button
			//var me = this;
			$('#btnYes', dialog).on('click', function (e) {
				e.preventDefault();
				saveDialog(e, true);
			});
			$('#btnNo', dialog).on('click', function (e) {
				e.preventDefault();
				saveDialog(e, false);
			});
			    
    	}
    	
		function hideError() {
			$("div.error", dialog).hide();
		}
		
		function showError() {
			$("div.error", dialog).show();
		}
        
        function saveDialog(e, importAsWorksheet) {
        	hide();
        	
        	var info = new Object();
            info["worksheetId"] = worksheetId;
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["importAsWorksheet"] = importAsWorksheet;
            info["command"] = "PublishJSONCommand";

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
                        var lastWorksheetLoaded = $("div.Worksheet").last();
    		        	if(lastWorksheetLoaded) {
    		        		var lastWorksheetId = lastWorksheetLoaded.attr("id");
    		        		ShowExistingModelDialog.getInstance().showIfNeeded(lastWorksheetId);
    		        	}
                    },
                error :
                    function (xhr, textStatus) {
                        alert("Error occured while exporting spatial data!" + textStatus);
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