/*******************************************************************************
 * Copyright 2012 University of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This code was developed by the Information Integration Group as part
 * of the Karma project at the Information Sciences Institute of the
 * University of Southern California.  For more information, publications,
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
function clickUndoButton() {
	var commandDivElem = $(this).parents(".CommandDiv");
	// Prepare the data to be sent to the server
	var edits = generateInfoObject("", "", "UndoRedoCommand");
	edits["commandId"] = commandDivElem.attr("id");

	// Invoke the UNDO command on the server
	showWaitingSignOnScreen();
	sendRequest(edits);
	// Change the state
	var index = $(".CommandDiv").index($(commandDivElem));
	if (commandDivElem.hasClass("undo-state")) {
		$.each($(".CommandDiv:gt(" + index + ")").add($(commandDivElem)), function(index, elem) {
			$(elem).find("img").attr("src", "images/edit_redo.png");
			$(elem).removeClass("undo-state").addClass("redo-state");
		});
	} else {
		$.each($(".CommandDiv:lt(" + index + ")").add($(commandDivElem)), function(index, elem) {
			$(elem).find("img").attr("src", "images/edit_undo.png");
			$(elem).removeClass("redo-state").addClass("undo-state");
		});
	}
}

function commandDivHoverIn() {
	$(this).children(".iconDiv").css({
		"visibility": "visible"
	});

	if ($(this).hasClass("undo-state")) {
		var index = $(".CommandDiv").index($(this));
		$(".CommandDiv:gt(" + index + ")").add($(this)).removeClass("redoselected").addClass("undoselected");
	} else {
		var index = $(".CommandDiv").index($(this));
		$(".CommandDiv:lt(" + index + ")").add($(this)).removeClass("undoselected").addClass("redoselected");
	}
}

function commandDivHoverOut() {
	$(this).children(".iconDiv").css({
		"visibility": "hidden"
	});
	$(".CommandDiv").removeClass("undoselected").removeClass("redoselected");
}