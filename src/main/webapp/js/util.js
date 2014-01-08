function isDialogInitialized(dialog) {
	if(dialog.hasClass("ui-dialog-content"))
		return true;
	return false;
}