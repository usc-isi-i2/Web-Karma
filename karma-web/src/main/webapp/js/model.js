$(document).on("click", "#modelManagerButton", function() {
    console.log("Manage Model");
    modelManagerDialog.getInstance().show();
});

$('#txtModel_URL').text('http://'+window.location.host + '/openrdf-sesame/repositories/karma_models');
$('#txtData_URL').text('http://'+window.location.host + '/openrdf-sesame/repositories/karma_data');
$('#txtData_URL').editable({
    type: 'text',
    pk: 1,
    success: function(response, newValue) {
        console.log("Set new value:" + newValue);
        $('#txtData_URL').text(newValue);
    },
    title: 'Enter Data Endpoint'
});

$('#txtModel_URL').editable({
    type: 'text',
    pk: 1,
    success: function(response, newValue) {
        console.log("Set new value:" + newValue);
        $('#txtModel_URL').text(newValue);
    },
    title: 'Enter Model Endpoint'
});

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

            $('#txtModel_URL_Save', dialog).on('keyup', function (e) {
                $('#txtGraph_URL_Save').val($('#txtModel_URL_Save').val());
            });

            $('#txtModel_URL_Save', dialog).bind('input paste', function (e) {
                console.log("here");
                console.log($('#txtModel_URL_Save').val());
                $('#txtGraph_URL_Save').val($('#txtModel_URL_Save').val());
            });

            $('#txtModel_URL_Save', dialog).on('change', function (e) {
                $('#txtGraph_URL_Save').val($('#txtModel_URL_Save').val());
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
            info['tripleStoreUrl'] = $('#txtModel_URL').html();
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
            info['tripleStoreUrl'] = $('#txtModel_URL').html();
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
        var availableModels;
        var filteredModels;
        var table;
        function init() {
            //Initialize what happens when we show the dialog
            refresh();
            var dialogContent = $("#modelManagerDialogColumns", dialog);
            table = $("<table>")
                        .addClass("table table-striped table-condensed");
            var tr = getHeaderRow();
            table.append(tr);
            dialogContent.append(table);
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

            $('#btnDeleteModel', dialog)
                .on('click', deleteModel)
                .attr("disabled", "disabled");

            $('#btnRefreshModel', dialog)
                .on('click', refreshModel)
                .attr("disabled", "disabled");
                
        }
        
        function onClickSelectAllCheckbox() {
            var overall = $("#modelManagerSelectAllCheckbox").prop("checked");
            $('.modelManagerCheckbox', table).each(function(i, obj) {
                obj.checked = overall;
            });
            disableButton();
        }
        
        function getHeaderRow() {
        	var tr = $("<tr>");
            var th = $("<th>"); //.addClass("CheckboxProperty");
            var checkbox = $("<input>")
					            .attr("type", "checkbox")                           
					            .attr("id", "modelManagerSelectAllCheckbox")
					            .prop('checked', false)
					            .change(onClickSelectAllCheckbox);
            th.append(checkbox);
            tr.append(th);
            
            var th = $("<th>"); //.addClass("FileNameProperty");
            var label = $("<label>").text("File Name"); //.addClass("FileNameProperty");
            th.append(label);
            var label = $("<input>").text("")
	            .addClass("form-control")
	            .attr("id","txtFilterFileName")
	            .attr("type", "text")
	            .on('keyup', applyFilter);
            th.append(label);
            tr.append(th);
            
            var th = $("<th>"); //.addClass("PublishTimeProperty");
            var label = $("<label>").text("Publish Time"); //.addClass("PublishTimeProperty");
            th.append(label);
            var label = $("<input>").text("")
            .addClass("form-control")
            .attr("id","txtFilterPublishTime")
            .attr("type", "text")
            .on('keyup', applyFilter);
            th.append(label);
            tr.append(th);
            
            var th = $("<th>"); //.addClass("URLProperty");
            var label = $("<label>").text("URL"); //.addClass("URLProperty");
            th.append(label);
            var label = $("<input>").text("")
				            .addClass("form-control")
				            .attr("id","txtFilterURL")
				            .attr("type", "text")
				            .on('keyup', applyFilter);
            th.append(label);
            tr.append(th);
            return tr;
        }
        function refresh() {
            var info = new Object();
            info["workspaceId"] = $.workspaceGlobalInformation.id;
            info["command"] = "FetchR2RMLModelsListCommand";
            info['tripleStoreUrl'] = $('#txtModel_URL').html();
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

        function applyFilter(e) {
            console.log("applyFilter");
            var tmp = [];
            var filterFilename = $('#txtFilterFileName').val();
            var filterTime = $('#txtFilterPublishTime').val();
            var filterURL = $('#txtFilterURL').val();
            for (var i = 0; i < availableModels.length; i++) {
                var name = availableModels[i]['name'].toLowerCase();
                var time = new Date(availableModels[i].publishTime*1).toString();
                time = time.substring(0, time.indexOf("GMT") - 1).toLowerCase();
                var url = availableModels[i].url.toLowerCase();
                var flag = true;
                if (name.indexOf(filterFilename) == -1) {
                    flag = false;
                }
                else if (time.indexOf(filterTime) == -1) {
                    flag = false;
                }
                else if (url.indexOf(filterURL) == -1) {
                    flag = false;
                }
                if (flag) {
                    tmp.push(availableModels[i]);
                }
            }
            filteredModels = tmp;
            instance.show();
        };
        
        function hide() {
            dialog.modal('hide');
        }
        
        function deleteModel(e) {
            e.preventDefault();
            var checkboxes = dialog.find(":checked");
            for (var i = 0; i < checkboxes.length; i++) {
                var checkbox = checkboxes[i];
                var info = new Object();
                info["workspaceId"] = $.workspaceGlobalInformation.id;
                info["command"] = "DeleteModelFromTripleStoreCommand";
                info['tripleStoreUrl'] = $('#txtModel_URL').html();
                info['graphContext'] = checkbox['value'];
                info['mappingURI'] = checkbox['src'];
                console.log(info['graphContext']);
                console.log(info['mappingURI']);
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
                        //parse(json);
                        },
                    error :
                        function (xhr, textStatus) {
                            alert("Error occured while clearing model!" + textStatus);
                        }
                });
            }       
            refresh();
            instance.show();    
        }

        function refreshModel(e) {
            e.preventDefault();
            var checkboxes = dialog.find(":checked");
            for (var i = 0; i < checkboxes.length; i++) {
                var checkbox = checkboxes[i];
                var info = new Object();
                info["workspaceId"] = $.workspaceGlobalInformation.id;
                info["command"] = "RefreshModelFromTripleStoreCommand";
                info['tripleStoreUrl'] = $('#txtModel_URL').html();
                info['graphContext'] = checkbox['value'];
                info['mappingURI'] = checkbox['src'];
                console.log(info['graphContext']);
                console.log(info['mappingURI']);
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
                        //parse(json);
                    },
                    error :
                    function (xhr, textStatus) {
                        alert("Error occured while clearing model!" + textStatus);
                    }
                });
            }
            refresh();
            instance.show();      
        }

        function disableButton(e) {
            var checkboxes = dialog.find(":checked");
            if (checkboxes.length == 0) {
                $('#btnDeleteModel', dialog)
                    .attr("disabled", "disabled");

                $('#btnRefreshModel', dialog)
                    .attr("disabled", "disabled");
            }
            else {
                $('#btnDeleteModel', dialog)
                    .removeAttr("disabled");

                $('#btnRefreshModel', dialog)
                    .removeAttr("disabled");
            }
        }

        function show() {
            table.find("tr:gt(0)").remove();
            $('#btnDeleteModel', dialog)
                .attr("disabled", "disabled");

            $('#btnRefreshModel', dialog)
                .attr("disabled", "disabled");
            console.log(filteredModels.length);
            for (var i = 0; i < filteredModels.length; i++) {                
                var name = filteredModels[i]['name'];
                var time = new Date(filteredModels[i].publishTime*1).toString();
                time = time.substring(0, time.indexOf("GMT") - 1);
                var url = filteredModels[i].url;
                var context = filteredModels[i].context;
                var tr = $("<tr>");
                var td = $("<td>");
                         //.addClass("CheckboxProperty");
                var checkbox = $("<input>")
                               .attr("type", "checkbox")                           
                               .attr("id", "modelManagerCheckbox")
                               .attr("value", context)
                               .attr("src", url)
                               .change(disableButton)
                               .addClass("modelManagerCheckbox");
                td.append(checkbox);
                tr.append(td);
                var td = $("<td>");
                        // .addClass("FileNameProperty");
                var label = $("<label>").text(name);
                            //.addClass("FileNameProperty");
                td.append(label);
                tr.append(td);
                var td = $("<td>")
                         .css("overflow", "scroll");
                         //.addClass("PublishTimeProperty");
                var label = $("<label>").text(time);
                           // .addClass("PublishTimeProperty");
                td.append(label);
                tr.append(td);
                var td = $("<td>");//.addClass("URLProperty");
                var label = $("<label>").text(url);
                            //.addClass("URLProperty");
                td.append(label);
                tr.append(td);
                table.append(tr);    
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