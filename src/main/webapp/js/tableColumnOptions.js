function TableColumnOptions(wsId, wsColumnId, wsColumnTitle) {
	 
	var worksheetId = wsId;
	var columnTitle = wsColumnTitle;
	var columnId = wsColumnId;
	
	var options = [
	        //Title, function to call, needs file upload       
			[	"Add Column" , addColumn ],
			[ "Rename", renameColumn ],
			[ "Split Column", splitColumn ],
			[ "divider" , null ],
			
			[ "PyTransform" , pyTransform ],
			[ "Transform", transform],
			[ "divider" , null ],
			
			[ "Invoke Service" , invokeService ],
			[ "Show Chart", showChart],
			//[ "Apply R2RML Model" , applyR2RMLModel, true, "applyWorksheetHistory" ],
			//[ "divider" , null ],
			
	];
	
	function hideDropdown() {
		$('.dropdown.open .dropdown-toggle').dropdown('toggle');
	}
	
	function addColumn() {
		
	}
	
	function pyTransform() {
		
	}
	
	function invokeService() {
		
	}
	
	function renameColumn() {
		
	}
	
	function splitColumn() {
		
	}
	
	function transform() {
		
	}
	
	function showChart() {
		
	}
	
	this.generateJS = function() {
		var dropdownId = "columnOptionsButton" + worksheetId + "_" + columnId;
		var div = 
			$("<div>")
				.attr("id", "TableOptionsDiv")
				.data("worksheetId", worksheetId)
				.addClass("tableDropdown")
				.addClass("dropdown")
				.append($("<a>")
						.attr("href", "#")
						.addClass("dropdown-toggle")
						.addClass("ColumnTitle")
						.attr("id", dropdownId)
						.data("worksheetId", worksheetId)
						.attr("data-toggle", "dropdown")
						.text(columnTitle)
						.append($("<span>").addClass("caret")
						)
				);
                

		var ul = $("<ul>").addClass("dropdown-menu");
		ul.attr("role", "menu")
			.attr("aria-labelledby", dropdownId);
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
};