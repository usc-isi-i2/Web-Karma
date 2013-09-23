
package edu.isi.karma.mvs;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.UpdateContainer;
import javax.servlet.http.HttpServletRequest;
/**
 *
 * @author mielvandersande
 */
public interface IPreviewable {
    	public UpdateContainer showPreview()
			throws CommandException;

	public UpdateContainer handleUserActions(HttpServletRequest request);
}
