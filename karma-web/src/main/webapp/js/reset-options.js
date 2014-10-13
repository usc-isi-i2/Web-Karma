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

$(document).on("click", "#resetButton", function() {
	ResetDialog.getInstance().show();
});

var ResetDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#resetDialog");

		function init() {
			//Initialize what happens when we show the dialog
			dialog.on('show.bs.modal', function(e) {
				$("input", dialog).attr("checked", false);
				hideErrMsg();
			});

			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);

			});
		}

		function showErrMsg(msg) {
			if (msg) {
				$("#resetErrMsg", dialog).HTMLAnchorElement(msg);
			}
			$("#resetErrMsg", dialog).show();
		}

		function hideErrMsg() {
			$("#resetErrMsg", dialog).hide();
		}

		function saveDialog(event) {
			hideErrMsg();

			if (!($("#forgetSemanticTypes").is(':checked')) 
					&& !($("#forgetModels").is(':checked'))
					&& !($("#forgetAlignment").is(':checked'))
					) {
				showErrMsg();
				return false;
			}

			dialog.modal('hide');

			var info = generateInfoObject("", "", "ResetKarmaCommand");
			info["forgetSemanticTypes"] = $("#forgetSemanticTypes").is(':checked');
			info["forgetModels"] = $("#forgetModels").is(':checked');
			info["forgetAlignment"] = $("#forgetAlignment").is(':checked');

			showWaitingSignOnScreen();
			var returned = sendRequest(info);
		}

		function show(data) {
			dialog.modal({
				keyboard: true,
				show: true,
				backdrop: 'static'
			});
		};


		return { //Return back the public methods
			show: show,
			init: init
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