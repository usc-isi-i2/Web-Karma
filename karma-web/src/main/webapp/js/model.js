$(document).on("click", "#modelManagerButton", function() {
    console.log("Manage Model");
    modelManagerDialog.getInstance().show();
});

$('#txtModel_URL').val('http://'+window.location.host + '/openrdf-sesame/repositories/karma_models');
$('#txtData_URL').val('http://'+window.location.host + '/openrdf-sesame/repositories/karma_data');

var saveModelDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
        var dialog = $("#saveModelDialog");
        
        function init() {
            //Initialize what happens when we show the dialog
            
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
            info['tripleStoreUrl'] = $('#txtModel_URL').val();
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
            info['tripleStoreUrl'] = $('#txtModel_URL').val();
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

var modelManagerDialog = (function() {
    var instance = null;

    function PrivateConstructor() {
        var dialog = $("#modelManagerDialog");
        var filterDialog = $("#modelFilterDialog");
        var availableModels;
        var filteredModels;
        var filterName;
        function init() {
            //Initialize what happens when we show the dialog
            refresh();
            var dialogContent = $("#modelManagerDialogHeaders", dialog);
            dialogContent.empty();
            var div = $("<div>").css("display","table-row")
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
            $('#btnLoadModel', dialog).on('click', function (e) {
                e.preventDefault();
                hide();
                saveModelDialog.getInstance().show();
            });

            $('#btnClearModel', dialog).on('click', function (e) {
                e.preventDefault();
                hide();
                clearModelDialog.getInstance().show();
            });

            $('#btnSave', filterDialog).on('click', function (e) {
                e.preventDefault();
                applyFilter(e);
            });
            $('#btnCancel', filterDialog).on('click', function (e) {
                e.preventDefault();
                cancelFilter(e);
            });

            $('#btnClearFilter', dialog).on('click', function (e) {
                filteredModels = availableModels;
                instance.show();
            });   
                
        }
        
        function refresh() {
            var info = new Object();
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["command"] = "FetchR2RMLModelsListCommand";
            info['tripleStoreUrl'] = $('#txtModel_URL').val();
            info['graphContext'] = "";
            var returned = $.ajax({
                url: "RequestController",
                type: "POST",
                data : info,
                dataType : "json",
                async : false,
                complete :
                    function (xhr, textStatus) {
                        //alert(xhr.responseText);
                        var json = $.parseJSON(xhr.responseText);
                        json = json.elements[0];
                        console.log(json);
                        availableModels = json;
                        filteredModels = availableModels;
                    },
                error :
                    function (xhr, textStatus) {
                        alert("Error occured while Fetching Models!" + textStatus);
                        hideLoading(info["worksheetId"]);
                    }
            });
        }

        function hideError() {
            $("div.error", dialog).hide();
        }
        
        function showError() {
            $("div.error", dialog).show();
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
            filteredModels = tmp;
            instance.show();
            dialog.show();
        };

        function cancelFilter(e) {
            console.log("cancelFilter");
            instance.show();
            dialog.show();
        };
        
        function hide() {
            dialog.modal('hide');
        }
        
        function deleteModel(e) {
            e.preventDefault();
            console.log(e['currentTarget']['value']);
            var tmpJSON = jQuery.parseJSON(e['currentTarget']['value']);
            var info = new Object();
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["command"] = "DeleteModelFromTripleStoreCommand";
            info['tripleStoreUrl'] = $('#txtModel_URL').val();
            info['graphContext'] = tmpJSON['context'];
            info['mappingURI'] = tmpJSON['url'];
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
                        refresh();
                        instance.show();
                        //parse(json);
                    },
                error :
                    function (xhr, textStatus) {
                        alert("Error occured while clearing model!" + textStatus);
                    }
            });
        }

        function refreshModel(e) {
            e.preventDefault();
            console.log(e['currentTarget']['value']);
            var tmpJSON = jQuery.parseJSON(e['currentTarget']['value']);
            var info = new Object();
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["command"] = "RefreshModelFromTripleStoreCommand";
            info['tripleStoreUrl'] = $('#txtModel_URL').val();
            info['graphContext'] = tmpJSON['context'];
            info['mappingURI'] = tmpJSON['url'];
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
                        refresh();
                        instance.show();
                        //parse(json);
                    },
                error :
                    function (xhr, textStatus) {
                        alert("Error occured while clearing model!" + textStatus);
                    }
            });
        }

        function show() {
            var dialogContent = $("#modelManagerDialogColumns", dialog);
            dialogContent.empty();
            for (var i = 0; i < filteredModels.length; i++) {
                var name = filteredModels[i]['name'];
                var time = new Date(filteredModels[i].publishTime*1).toDateString();
                var url = filteredModels[i].url;
                var context = filteredModels[i].context;
                var div = $("<div>").css("display","table-row");
                var row = $("<div>").addClass("FileNameProperty").css("overflow", "scroll");
                var label = $("<label>").text(name).css("overflow", "scroll");
                row.append(label);
                div.append(row);
                var row = $("<div>").addClass("PublishTimeProperty").css("overflow", "scroll");
                var label = $("<label>").text(time).css("overflow", "scroll");
                row.append(label);
                div.append(row);
                var row = $("<div>").addClass("URLProperty").css("overflow", "scroll");
                var label = $("<label>").text(url).css("overflow", "scroll");
                row.append(label);
                div.append(row);
                var row = $("<div>").addClass("ContextProperty").css("overflow", "scroll");
                var label = $("<label>").text(context).css("overflow", "scroll");
                row.append(label);
                div.append(row);
                var tmp = new Object();
                tmp['url'] = url;
                tmp['context'] = context;
                var row = $("<div>").addClass("DeleteProperty");
                var label = $("<button>").text("Delete")
                            .addClass("btn btn-primary DeleteButtonProperty")
                            .attr("id","btnDeleteModel")
                            .attr("value", JSON.stringify(tmp))
                            .on('click', deleteModel);
                row.append(label);
                div.append(row);
                var row = $("<div>").addClass("RefreshProperty");
                var label = $("<button>").text("Refresh")
                            .addClass("btn btn-primary RefreshButtonProperty")
                            .attr("id","btnRefreshModel")
                            .attr("value", JSON.stringify(tmp))
                            .on('click', refreshModel);
                row.append(label);
                div.append(row);
                dialogContent.append(div);                
            }
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
        console.log(instance);
        instance.refresh();
        return instance;
    }
   
    return {
        getInstance : getInstance
    };
    
})();