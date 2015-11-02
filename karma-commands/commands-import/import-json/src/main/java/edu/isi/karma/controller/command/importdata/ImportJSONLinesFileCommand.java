package edu.isi.karma.controller.command.importdata;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;

import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.EncodingDetector;

public class ImportJSONLinesFileCommand extends ImportFileCommand implements IPreviewable{

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
		try {
			return new JsonImport(getFile(), getFile().getName(), workspace, encoding, sampleSize, null,true);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected Import createImport(Workspace workspace) {
		JSONArray tree = generateSelectTree(columnsJson, true);
		try {
			return new JsonImport(getFile(), getFile().getName(), workspace, encoding, maxNumLines, tree,true);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	@Override
	public UpdateContainer handleUserActions(HttpServletRequest request) {
		columnsJson = request.getParameter("columnsJson");
		savePreset = Boolean.parseBoolean(request.getParameter("savePreset"));
		return super.handleUserActions(request);
	}

}
