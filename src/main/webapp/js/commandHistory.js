function clickUndoButton() {
	var commandDivElem = $(this).parents(".CommandDiv");
	// Prepare the data to be sent to the server
	var edits = new Object();
	edits["command"] = "UndoRedoCommand";
	edits["commandId"] = commandDivElem.attr("id");
	edits["workspaceId"] = $.workspaceGlobalInformation.id;
	
	// Invoke the UNDO command on the server
	$.ajax({
	   	url: "/RequestController", 
	   	type: "POST",
	   	data : edits,
	   	dataType : "json",
	   	complete : 
	   		function (xhr, textStatus) {
	    		var json = $.parseJSON(xhr.responseText);
	    		parse(json);
		   	}
		});
	// Change the state
	var index =  $(".CommandDiv").index($(commandDivElem));
	if(commandDivElem.hasClass("undo-state")){
		$.each($(".CommandDiv:gt(" +index+ ")").add($(commandDivElem)), function(index, elem) {
			$(elem).find("img").attr("src", "../images/edit_redo.png");
			$(elem).removeClass("undo-state").addClass("redo-state");
		});
	} else {
		$.each($(".CommandDiv:lt(" +index+ ")").add($(commandDivElem)), function(index, elem) {
			$(elem).find("img").attr("src", "../images/edit_undo.png");
			$(elem).removeClass("redo-state").addClass("undo-state");
		});
	}
}

function commandDivHoverIn() {
	$(this).children(".iconDiv").css({"visibility": "visible"});
	
	if($(this).hasClass("undo-state")){
		var index =  $(".CommandDiv").index($(this));
		$(".CommandDiv:gt(" +index+ ")").add($(this)).removeClass("redoselected").addClass("undoselected");
	} else {
		var index =  $(".CommandDiv").index($(this));
		$(".CommandDiv:lt(" +index+ ")").add($(this)).removeClass("undoselected").addClass("redoselected");
	}
}

function commandDivHoverOut() {
	$(this).children(".iconDiv").css({"visibility": "hidden"});
	$(".CommandDiv").removeClass("undoselected").removeClass("redoselected");
}