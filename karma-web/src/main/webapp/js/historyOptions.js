function HistoryOptions(wsId) {

	var worksheetId = wsId;
	var historyOptionsDiv;

	var options = [
		{
			name: "Delete Commands",
			func: deleteHistory
		},
	    {
			name: "Export Commands",
			func: exportHistory
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
		newInfo.push(getParamObject("tripleStoreUrl", $('#txtModel_URL').text(), "other"));
		newInfo.push(getParamObject("requestUrl", window.location.href.replace("/#", ""), "other"));
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
		newInfo.push(getParamObject("tripleStoreUrl", $('#txtModel_URL').text(), "other"));
		newInfo.push(getParamObject("requestUrl", window.location.href.replace("/#", ""), "other"));
		info["newInfo"] = JSON.stringify(newInfo);
		showLoading(info["worksheetId"]);
		sendRequest(info, worksheetId);
	}

	function extractHistoryCheckboxes() {
		var checkboxes = $("#commandHistory").find(":checked");
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
				.text("Command History")
				.append($("<span>").addClass("caret"))
			);


		var ul = $("<ul>").addClass("dropdown-menu");
		//console.log("There are " + options.length + " menu items");
		for (var i = 0; i < options.length; i++) {
			var option = options[i];
			var li = $("<li>");
			//console.log("Got option" +  option);
			var title = option.name;
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