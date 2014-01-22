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