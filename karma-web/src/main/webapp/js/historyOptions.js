var HistoryManager = (function() {
	var instance = null;

	function PrivateConstructor() {
		var options;
		
		function init() {
			options = [];
		}
		
		function getHistoryOptions(worksheetId) {
			if(options[worksheetId]) {
			} else { 
				options[worksheetId] = new HistoryOptions(worksheetId);
			}
			return options[worksheetId];	
		};
		
		return { //Return back the public methods
			init: init,
			getHistoryOptions: getHistoryOptions
		};
	};

	function getInstance() {
		if (!instance) {
			instance = new PrivateConstructor();
			instance.init();
		}
		return instance;
	}
	
	return {
		getInstance: getInstance
	};

})();


function HistoryOptions(wsId) {

	var worksheetId = wsId;
	var historyOptionsDiv;
	var lastCommand;
	
	var options = [
		{
			name: "Reapply",
			id: "refreshHistory",
			func: refreshHistory,
			visible: true
		},
		{
			name: "Export",
			id: "exportCommands",
			func: exportHistory,
			visible: true
		},
		{
			name: "Delete",
			id: "deleteCommands",
			func: deleteHistory,
			visible: true
		},
		{
			name: "Undo Delete",
			id:"undoDeleteHistory",
			func: undoDeleteHistory,
			visible: false
		}
	];
		
	function hideDropdown() {
		$('.dropdown.open .dropdown-toggle').dropdown('toggle');
	}

	function getCheckboxState(event) {
		var target = event.target;
		var checkbox;
		if (target.localName == "input") {
			checkbox = target;
		} else {
			checkbox = $("input[type='checkbox']", target)[0];
			$(checkbox).prop('checked', !checkbox.checked);
		}
		return checkbox.checked;
	}

	function deleteHistory() {
		hideDropdown();
		var response = extractHistoryCheckboxes();
		var info = generateInfoObject(worksheetId, "", "ExportOrDeleteHistoryCommand");

		var newInfo = info['newInfo'];
		newInfo.push(getParamObject("commandList", response, "other"));
		newInfo.push(getParamObject("isDelete", "true", "other"));
		info["newInfo"] = JSON.stringify(newInfo);
		showLoading(info["worksheetId"]);
		sendRequest(info, worksheetId);
	}

	function exportHistory() {
		hideDropdown();
		var response = extractHistoryCheckboxes();
		var info = generateInfoObject(worksheetId, "", "ExportOrDeleteHistoryCommand");

		var newInfo = info['newInfo'];
		newInfo.push(getParamObject("commandList", response, "other"));
		newInfo.push(getParamObject("isDelete", "false", "other"));
		info["newInfo"] = JSON.stringify(newInfo);
		showLoading(info["worksheetId"]);
		sendRequest(info, worksheetId);
	}

	function undoDeleteHistory() {
		var edits = generateInfoObject("", "", "UndoRedoCommand");
		edits["commandId"] = lastCommand.commandId;
		edits["worksheetId"] = lastCommand.worksheetId;
		showWaitingSignOnScreen();
		sendRequest(edits);
	}
	
	function refreshHistory() {
		hideDropdown();
		var info = generateInfoObject(worksheetId, "", "RefreshHistoryCommand");

		var newInfo = info['newInfo'];
		newInfo.push(getParamObject("worksheetId", worksheetId, "other"));
		info["newInfo"] = JSON.stringify(newInfo);
		showLoading(info["worksheetId"]);
		sendRequest(info, worksheetId);
	}
	
	function extractHistoryCheckboxes() {
		var checkboxes = $("#commandHistoryBody_" + worksheetId).find(":checked");
		var response = "";
		for (var i = 0; i < checkboxes.length; i++) {
			if (i == 0) {
				response += checkboxes[i].value;
			}
			else {
				response += "," + checkboxes[i].value;
			}
		}
		console.log(response);
		return response;
	}
	
	
	function showOption(id) {
		$("#" + id, historyOptionsDiv).show();
	};
	
	function hideOption(id) {
		$("#" + id, historyOptionsDiv).hide();
	};
	
	this.setLastCommand = function(command) {
		lastCommand = command;
		if(command.title == "Delete History") {
			showOption("undoDeleteHistory");
		} else {
			hideOption("undoDeleteHistory");
		}
	};
	
	this.generateJS = function() {
		var div =
			$("<div>")
			.attr("id", "HistoryOptionsDiv")
			.data("worksheetId", worksheetId)
			.addClass("dropdown")
			.append($("<a>")
				.attr("href", "#")
				.addClass("dropdown-toggle")
				.attr("id", "optionsButton" + worksheetId)
				.data("worksheetId", worksheetId)
				.attr("data-toggle", "dropdown")
				.text("Commands")
				.append($("<span>").addClass("caret"))
			);


		var ul = $("<ul>").addClass("dropdown-menu");
		//console.log("There are " + options.length + " menu items");
		for (var i = 0; i < options.length; i++) {
			var option = options[i];
			var li = $("<li>").css("text-align", "left");
			//console.log("Got option" +  option);
			var title = option.name;
			if(option.id) {
				li.attr("id", option.id)
			}
			if(option.visible) {
				li.show();
			} else {
				li.hide();
			}
			if (title == "divider")
				li.addClass("divider");
			else {
				var func = option.func;
				var a = $("<a>")
					.attr("href", "#");
				if (option.showCheckbox) {
					var checkbox = $("<input>").attr("type", "checkbox");
					if (option.defaultChecked)
						checkbox.attr("checked", "checked");
					var label = $("<span>").append(checkbox).append("&nbsp;").append(title);
					a.append(label);
					a.click(func);
				} else if (option.addLevel) {
					addLevels(li, a, option, worksheetId);
				} else {
					a.text(title);
					a.click(func);
				}

				li.append(a);
			}
			if (option.initFunc)
				option.initFunc();
			ul.append(li);
		};
		div.append(ul);
		historyOptionsDiv = div;
		return div;
	};
}