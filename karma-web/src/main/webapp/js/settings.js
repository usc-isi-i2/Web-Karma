var Settings = (function() {
	var instance = null;


	function PrivateConstructor() {
		var showRDFSLabel = false;
		var showRDFSLabel_labelFirst = false;
		var showRDFSLabel_idFirst = false;

		var isSemanticLabeling_online = false;

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

			$("#displaySemanticLabeling_Online").on("click", function(e) {
			    setIsSemanticLabelingOnline(!$("#displaySemanticLabeling_Online span").is(":visible"));
			});
			$("#displaySemanticLabeling_Offline").on("click", function(e) {
			    setIsSemanticLabelingOnline($("#displaySemanticLabeling_Offline span").is(":visible"));
			});
		}

		function setIsSemanticLabelingOnline(isOnline){
		    isSemanticLabeling_online = isOnline;
		    if (isOnline){
		        $("#displaySemanticLabeling_Online span").show();
		        $("#displaySemanticLabeling_Offline span").hide();
		    } else {
		        $("#displaySemanticLabeling_Online span").hide();
		        $("#displaySemanticLabeling_Offline span").show();
		    }

			var info = generateInfoObject("", "", "UpdateModelingConfigurationCommand");
			info["value"] = isOnline;
			info["property"] = "online.semantic.typing";
			showWaitingSignOnScreen();
			var returned = sendRequest(info);
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

		function getDisplayLabel(label, rdfsLabel, noStyle) {
			if(rdfsLabel && rdfsLabel != "") {
				if(noStyle)
					rdfsLabelShow = rdfsLabel;
				else
					rdfsLabelShow = "<span class='rdfsLabel'>" + rdfsLabel + "</span>"
				if(showRDFSLabel_idFirst)
					return label + " " + rdfsLabelShow;
				if(showRDFSLabel_labelFirst)
					return rdfsLabelShow + " " + label;
			}
			return label;
		}

		return { //Return back the public methods
			setDisplayRDFSLabel: setDisplayRDFSLabel,
			init: init,
			showRDFSLabel: showRDFSLabel,
			showRDFSLabelWithIdFirst: showRDFSLabelWithIdFirst,
			showRDFSLabelWithLabelFirst: showRDFSLabelWithLabelFirst,
			getDisplayLabel: getDisplayLabel,
			setIsSemanticLabelingOnline: setIsSemanticLabelingOnline
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