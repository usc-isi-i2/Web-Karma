/* Load and parse initial JSON */
// Get the preferences Id from cookies if present
var bootupURL = "KarmaServlet?rand=" + (new Date()).valueOf();
if ($.cookie("workspacePreferencesId") != null) {
	bootupURL += "&hasPreferenceId=" + true + "&workspacePreferencesId=" + $.cookie("workspacePreferencesId");
} else {
	bootupURL += "&hasPreferenceId=" + false;
}

$("div#WaitingDiv").show();
window.setTimeout(function() {
	$.ajax({
		dataType: "json",
		url: bootupURL,
		async: false,
		success: function(data) {
			$.workspaceGlobalInformation = {
				"id": data["workspaceId"],
				"UISettings": null
			};

			// Set the preferences workspace cookie if null
			if ($.cookie("workspacePreferencesId") == null)
				$.cookie("workspacePreferencesId", $.workspaceGlobalInformation.id, {
					expires: 7000
				});

			parse(data);
			$("div#WaitingDiv").hide();
			console.log("done bootup");
		},
		error: function() {
			alert("Trouble connecting to server!");
		}
	});
}, 10);