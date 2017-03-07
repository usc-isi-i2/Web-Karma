var Settings = (function() {
	var instance = null;


	function PrivateConstructor() {
		var showRDFSLabel = false;
		var showRDFSLabel_labelFirst = false;
		var showRDFSLabel_idFirst = false;
		var r2rml_export_superclass = false;

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
			$("#displayGithubSettings").on("click", function(e) {
				GithubSettingsDialog.getInstance().show();
			});
			$("#r2rmlExportSuperclass").on("click", function(e) {
        		r2rml_export_superclass = !$("#r2rmlExportSuperclass span").is(":visible");
                setDisplaySuperclass(r2rml_export_superclass, true);
      		});
		}

		function getGithubUsername() {
			username = $.cookie("github-username");
			if(username)
				return username;
			return "";
		}

		function setGithubUsername(username) {
			$.cookie("github-username", username);
		}

		function getGithubAuth() {
			return $.cookie("github-auth");
		}

		function setGithubAuth(auth) {
			$.cookie("github-auth", auth);
		}

		function validateGithubSettings(githubUrl) {
			var me = this;
			var auth = this.getGithubAuth();
			if(auth) {
				if(githubUrl) {
					if(githubUrl.indexOf("github.com") == -1)
						return {"code": false, "msg": "Please enter a valid Github URL"};
				} else {
					return {"code": false, "msg": "Please enter a valid Github URL"};
				}
				var repo_username = githubUrl.split("github.com")[1].split("/")[1];
 				var repo_name = githubUrl.split("github.com")[1].split("/")[2];
        
        		var repo = "https://api.github.com/repos/" + repo_username + "/" + repo_name;
				var returnValue = {"code": false, "msg": ""};
				$.ajax ({
	              type: "GET",
	              url: repo,
	              dataType: 'json',
	              async: false,
	              beforeSend: function (xhr) {
	                xhr.setRequestHeader("Authorization", "Basic " + auth);
	              },
	              success: function (data){
	                // If we have push permission, then move forward else show error statement
	                if (data.permissions && data.permissions.push == true){
	                	returnValue = {"code": true, "msg": ""};
	                } else {
	                    returnValue = {"code": false, "msg": "User " + me.getGithubUsername() + " is not authorized to push to this repository" };
	                }
	              },
	              error: function(e) {
	              	var msg  = "Invalid Github URL";
	              	if(e.responseJSON) {
	              		msg  = e.responseJSON["message"];
	              		if(msg == "Bad credentials") {
	              			msg = "Invalid Username/Password. \nPlease update in Settings -> Github."
	              		}
	              	}
	                returnValue = {"code": false, "msg": msg};
	              },
	            });
	            return returnValue;
	        } else {
	        	var returnValue = {"code": false, "msg": "Please first enter the authentication information in Settings -> Github"};
	        	return returnValue;
	        }
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

	    function setDisplaySuperclass(r2rmlExportSuperclass, update) {
            r2rml_export_superclass = r2rmlExportSuperclass;

	      if(r2rmlExportSuperclass) {
	        $("#r2rmlExportSuperclass span").show();
	      } else {
	        $("#r2rmlExportSuperclass span").hide();
	      }

	      if(update) {
	        var info = generateInfoObject("", "", "UpdateModelConfigurationCommand");
	        var newInfo = info['newInfo'];

	        newInfo.push(getParamObject("r2rml_export_superclass", r2rmlExportSuperclass, "other"));
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

		function showExportSuperclass() {
			return r2rml_export_superclass;
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
			setDisplaySuperclass: setDisplaySuperclass,
			showExportSuperclass: showExportSuperclass,
			getGithubUsername: getGithubUsername,
			setGithubUsername: setGithubUsername,
			getGithubAuth: getGithubAuth,
			setGithubAuth: setGithubAuth,
			validateGithubSettings: validateGithubSettings
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


var GithubSettingsDialog = (function() {
	var instance = null;

	function PrivateConstructor() {
		var dialog = $("#githubSettingsDialog");
		var callback;

		function init() {
			//Initialize what happens when we show the dialog

			//Initialize handler for Save button
			//var me = this;
			$('#btnSave', dialog).on('click', function(e) {
				e.preventDefault();
				saveDialog(e);
			});

			$('#btnCancel', dialog).on('click', function(e) {
				e.preventDefault();
				hide();
			});

			$('#btnDelete', dialog).on('click', function(e) {
				e.preventDefault();
				deleteSettings(e);
			});

			dialog.on('show.bs.modal', function(e) {
				hideError();
				$("#txtGithubUsername", dialog).val(Settings.getInstance().getGithubUsername());
			});
		}

		function hideError() {
			$("div.error", dialog).hide();
		}

		function showError() {
			$("div.error", dialog).show();
		}

		function deleteSettings(e) {
			Settings.getInstance().setGithubAuth(null);
			Settings.getInstance().setGithubUsername(null);

			$('.githubUrlLabel').each(function(){           
		       var githubLabel = $(this);
		       var value = githubLabel.text();
				if(value != "" && value != "Empty" && value != "disabled") {
					value = value + " (disabled)";
					githubLabel.text(value);
				}
		   	});

			hide();
		}

		function saveDialog(e) {
			var username = $("#txtGithubUsername", dialog);
        	var password = $("#txtGithubPassword", dialog);

        	if (username[0].checkValidity() && password[0].checkValidity()){
				var auth = btoa(username.val() + ":" + password.val());
				Settings.getInstance().setGithubAuth(auth);
				Settings.getInstance().setGithubUsername(username.val());

				$('.githubUrlLabel').each(function(){           
			       var githubLabel = $(this);
			       var value = githubLabel.text();
			       var idx = value.indexOf(" (disabled)");
					if(idx != -1) {
						value = value.substring(0, idx);
						githubLabel.text(value);
					}
			   	});

				if(callback)
					callback();
				hide();
			} else {
				showError();
			}
		};

		function hide() {
			dialog.modal('hide');
		}

		function show(callbackFunction) {
			if(callbackFunction)
				callback = callbackFunction;
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