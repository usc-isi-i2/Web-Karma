/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.isi.karma.controller.command.importdata;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.controller.update.ImportPropertiesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.util.EncodingDetector;

/**
 * This abstract class in an interface to all Commands that import data from files
 * 
 * @author mielvandersande
 */
public abstract class ImportFileCommand extends ImportCommand implements IPreviewable{

	private static Logger logger = LoggerFactory
			.getLogger(ImportFileCommand.class);
	protected File file;
	protected String encoding;
	protected int maxNumLines = 1000;

	public ImportFileCommand(String id, File file) {
		super(id);
		this.file = file;
		this.encoding = EncodingDetector.detect(file);
	}

	public ImportFileCommand(String id, String revisionId, File file) {
		super(id, revisionId);
		this.file = file;
		this.encoding = EncodingDetector.detect(file);
	}

	@Override
	public String getDescription() {
		if (isExecuted()) {
			return getFile().getName() + " imported";
		}
		return "";
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	public File getFile() {
		return file;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setMaxNumLines(int lines) {
		this.maxNumLines = lines;
	}


	@Override
	public UpdateContainer handleUserActions(HttpServletRequest request) {

		String strEncoding = request.getParameter("encoding");
		if(strEncoding == null || strEncoding == "") {
			try {
				strEncoding = EncodingDetector.detect(getFile());
			} catch(Exception e) {
				strEncoding = EncodingDetector.DEFAULT_ENCODING;
			}
		}
		setEncoding(strEncoding);

		String maxNumLines = request.getParameter("maxNumLines");
		if(maxNumLines != null && maxNumLines != "") {
			try {
				int num = Integer.parseInt(maxNumLines);
				setMaxNumLines(num);
			} catch (Throwable t) {
				logger.error("Wrong user input for Data Number of Lines to import");
				return null;
			}
		}
		/**
		 * Send response based on the interaction type *
		 */
		UpdateContainer c = null;
		ImportFileInteractionType type = ImportFileInteractionType.valueOf(request
				.getParameter("interactionType"));
		switch (type) {
		case generatePreview: {
			try {

				c = showPreview(request);
			} catch (CommandException e) {
				logger.error(
						"Error occured while creating output",
						e);
			}
			return c;
		}
		case importTable:
			return c;
		}
		return c;
	}

	@Override
	public UpdateContainer showPreview(HttpServletRequest request) throws CommandException {

		UpdateContainer c = new UpdateContainer();
		c.add(new ImportPropertiesUpdate(getFile(), encoding, maxNumLines, id));
		return c;

	}
}
