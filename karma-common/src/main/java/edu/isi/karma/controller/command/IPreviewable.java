package edu.isi.karma.controller.command;

import edu.isi.karma.controller.update.UpdateContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * All commands that need a preview before executing should implement this interface
 * 
 * @author mielvandersande
 */
public interface IPreviewable {
    
    /*
     * Show the preview
     */
    public UpdateContainer showPreview(HttpServletRequest request)
            throws CommandException;

    /*
     * React to the user actions in the preview
     */
    public UpdateContainer handleUserActions(HttpServletRequest request);
}
