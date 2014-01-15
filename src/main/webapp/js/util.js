function isDialogInitialized(dialog) {
	if(dialog.hasClass("ui-dialog-content"))
		return true;
	return false;
}

function getColumnHeadings(worksheetId) {
	var columnNames = [];
	
	var columnNameDivs = $("#" + worksheetId + " div.wk-header a.ColumnTitle");
    $.each(columnNameDivs, function(index, element) {
        columnNames.push($.trim($(element).text()));
    });
    
    return columnNames;
}