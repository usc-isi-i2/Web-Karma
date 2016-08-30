var Settings = (function() {
	var instance = null;


	function PrivateConstructor() {
		var showRDFSLabel = false;
		var showRDFSLabel_labelFirst = false;
		var showRDFSLabel_idFirst = false;
		
		function init() {
			$("#displayRDFSLabel_labelFirst").on("click", function(e) {
				label_first = !$("#displayRDFSLabel_labelFirst span").is(":visible");
				if(label_first)
					id_first = false;
				else
					id_first = showRDFSLabel_idFirst;
				setDisplayRDFSLabel(label_first, id_first, true);
			});
			$("#displayRDFSLabel_idFirst").on("click", function(e) {
				id_first = !$("#displayRDFSLabel_idFirst span").is(":visible");
				if(id_first)
					label_first = false;
				else
					label_first = showRDFSLabel_labelFirst;
				setDisplayRDFSLabel(label_first, id_first, true);
			});
		}

		function setDisplayRDFSLabel(showLabelFirst, showIDFirst, update) {
			showRDFSLabel_idFirst = showIDFirst;
			showRDFSLabel_labelFirst = showLabelFirst;

			if(showLabelFirst || showIDFirst) {
				showRDFSLabel = true;
			} else {
				showRDFSLabel = false;
			}
			if(showLabelFirst) {
				$("#displayRDFSLabel_labelFirst span").show();
			} else {
				$("#displayRDFSLabel_labelFirst span").hide();
			}

			if(showIDFirst) {
				$("#displayRDFSLabel_idFirst span").show();
			} else {
				$("#displayRDFSLabel_idFirst span").hide();
			}

			if(update) {
				var info = generateInfoObject("", "", "UpdateUIConfigurationCommand");
				var newInfo = info['newInfo'];
				
				newInfo.push(getParamObject("show_rdfs_label_first", showRDFSLabel_labelFirst, "other"));
				newInfo.push(getParamObject("show_rdfs_id_first", showRDFSLabel_idFirst, "other"));
				info["newInfo"] = JSON.stringify(newInfo);
				showWaitingSignOnScreen();

				var returned = sendRequest(info);
			}
		}


		function showRDFSLabelWithLabelFirst() {
			return showRDFSLabel_labelFirst;
		}

		function showRDFSLabelWithIdFirst() {
			return showRDFSLabel_idFirst;
		}

		function getDisplayLabel(label, rdfsLabel) {
			if(rdfsLabel && rdfsLabel != "") {
				if(showRDFSLabel_idFirst)
					return label + " " + rdfsLabel;
				if(showRDFSLabel_labelFirst)
					return rdfsLabel + " " + label;
			}
			return label;
		}

		return { //Return back the public methods
			setDisplayRDFSLabel: setDisplayRDFSLabel,
			init: init,
			showRDFSLabel: showRDFSLabel,
			showRDFSLabelWithIdFirst: showRDFSLabelWithIdFirst,
			showRDFSLabelWithLabelFirst: showRDFSLabelWithLabelFirst,
			getDisplayLabel: getDisplayLabel
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