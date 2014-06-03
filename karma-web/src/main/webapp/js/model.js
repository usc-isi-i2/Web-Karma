$(document).on("click", "#saveModelButton", function() {
	console.log("Save Model");
	saveModelDialog.getInstance().show();
});

$(document).on("click", "#clearModelButton", function() {
    console.log("Clear Model");
    clearModelDialog.getInstance().show();
});

var saveModelDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
        var dialog = $("#saveModelDialog");
        
        function init() {
            //Initialize what happens when we show the dialog
            dialog.on('show.bs.modal', function (e) {
                hideError();
                 $('#txtR2RML_URL_Save').val('http://'+window.location.host + '/openrdf-sesame/repositories/karma_models');
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
            var checkboxes = dialog.find(":checked");
            if ($('#txtGraph_URL_Save').val() === '' && checkboxes[0]['value'] === 'URL') {
            	alert("No graph name!");
            	return;
            }
            if ($('#txtModel_URL_Save').val() === '') {
            	alert("No model URL!");
            	return;
            }
            if ($('#txtR2RML_URL_Save').val() === '') {
            	alert("No triplestore URL!");
            	return;
            }
            var info = new Object();
            
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["command"] = "SaveR2RMLModelCommand";
            info['tripleStoreUrl'] = $('#txtR2RML_URL_Save').val();
            info['modelUrl'] = $('#txtModel_URL_Save').val();
            info['graphContext'] = $('#txtGraph_URL_Save').val();
            info['collection'] = checkboxes[0]['value'];
            console.log(info['collection']);
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
                        alert("Error occured while saving models!" + textStatus);
                    }
            });
        };
        
        function hide() {
            dialog.modal('hide');
        }
        
        function show() {
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

var clearModelDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
        var dialog = $("#clearModelDialog");
        
        function init() {
            //Initialize what happens when we show the dialog
            dialog.on('show.bs.modal', function (e) {
                hideError();
                 $('#txtR2RML_URL_Clear').val('http://'+window.location.host + '/openrdf-sesame/repositories/karma_models');
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
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["command"] = "ClearTripleStoreCommand";
            info['tripleStoreUrl'] = $('#txtR2RML_URL_Clear').val();
            info['graphContext'] = $('#txtGraph_URL_Clear').val();
            console.log(info['graphContext']);
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
                        alert("Error occured while clearing model!" + textStatus);
                    }
            });
        };
        
        function hide() {
            dialog.modal('hide');
        }
        
        function show() {
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