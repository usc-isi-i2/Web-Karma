package edu.isi.karma.controller.command.importdata;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.EncodingDetector;

public class ImportJSONLinesFileCommand extends ImportFileCommand implements IPreviewable{

	private static Logger logger = LoggerFactory
			.getLogger(ImportJSONLinesFileCommand.class);
	public ImportJSONLinesFileCommand(String id, String model, File file) {
		super(id, model, file);
	}
	
	public ImportJSONLinesFileCommand(String id, String model, String revisedId, File file) {
		super(id, model, revisedId, file);
		this.encoding = EncodingDetector.detect(file);
	}

	@Override
	public String getTitle() {
		return "Import JSON Lines";
	}

	@Override
	protected Import createImport(Workspace workspace, int sampleSize) {
		
		Import imp = new JsonImport(getFile(), getFile().getName(), workspace, encoding, sampleSize, null,true);
		//return new JsonImport(getFile(), getFile().getName(), workspace, encoding, sampleSize, null,true);
		
		logger.error("FIRST:"+ imp);
		return imp;
	}

	@Override
	protected Import createImport(Workspace workspace) {
		JSONArray tree = generateSelectTree(columnsJson, true);
		Import imp = new JsonImport(getFile(), getFile().getName(), workspace, encoding, maxNumLines, tree,true);
		logger.error("SECOND: " + imp);
		//return new JsonImport(getFile(), getFile().getName(), workspace, encoding, maxNumLines, tree,true);
		return imp;
	}
	
	@Override
	public UpdateContainer handleUserActions(HttpServletRequest request) {
		columnsJson = request.getParameter("columnsJson");
		savePreset = Boolean.parseBoolean(request.getParameter("savePreset"));
		return super.handleUserActions(request);
	}

}
