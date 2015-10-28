package edu.isi.karma.controller.command.publish;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.config.UIConfiguration;
import edu.isi.karma.config.UIConfigurationRegistry;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetCleaningUpdate;
import edu.isi.karma.controller.update.WorksheetDataUpdate;
import edu.isi.karma.controller.update.WorksheetHeadersUpdate;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.TablePager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VHNode;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class PublishJSONCommand extends WorksheetCommand {
	private enum JsonKeys {
		updateType, fileUrl, worksheetId
	}

	private String worksheetName;
	private boolean importAsWorksheet;
	// Logger object
	private static Logger logger = LoggerFactory
			.getLogger(PublishJSONCommand.class.getSimpleName());

	public PublishJSONCommand(String id, String model, String worksheetId, boolean importAsWorksheet) {
		super(id, model, worksheetId);
		this.importAsWorksheet = importAsWorksheet;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Publish JSON";
	}

	@Override
	public String getDescription() {
		return worksheetName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		final Worksheet worksheet = workspace.getWorksheet(worksheetId);
		this.worksheetName = worksheet.getTitle();

		// Prepare the file path and names
		final String newWorksheetName = worksheetName + "-" + workspace.getCommandPreferencesId() + worksheetId;
		final String fileName =  newWorksheetName + ".json"; 
		final String fileLocalPath = contextParameters.getParameterValue(ContextParameter.JSON_PUBLISH_DIR) +  
				fileName;
		final String relFilename = contextParameters.getParameterValue(ContextParameter.JSON_PUBLISH_RELATIVE_DIR) + fileName;
		final Workspace finalWorkspace = workspace;

		final class PublishJSONUpdate extends AbstractUpdate {
			String newWSId = null;
			String errorOnUpdate = null;

			@Override
			public void applyUpdate(VWorkspace vWorkspace) {
				VWorksheet vWorksheet =  vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId);
				try {
					PrintWriter fileWriter = new PrintWriter(new FileWriter(new File(fileLocalPath)));
					TablePager pager = vWorksheet.getTopTablePager();
					generateRowsUsingPager(pager, vWorksheet, vWorksheet.getHeaderViewNodes(), fileWriter,"");
					fileWriter.close();

					if(importAsWorksheet) {
						JsonImport jsonImp = new JsonImport(new File(fileLocalPath), newWorksheetName, finalWorkspace, "utf-8", -1, null,false);
						jsonImp.generateWorksheet();
						newWSId = jsonImp.getWorksheet().getId();
						new WorksheetListUpdate().applyUpdate(vWorkspace);
					}


				} catch(Exception ie) {
					logger.error("Error writing JSON to file: " + fileLocalPath);
					errorOnUpdate = "Error exporting to JSON";
				}
			}

			@Override
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				if(errorOnUpdate != null) {
					new ErrorUpdate(errorOnUpdate).generateJson(prefix, pw, vWorkspace);
				} else {
					JSONObject outputObject = new JSONObject();
					outputObject.put(JsonKeys.updateType.name(), "PublishJSONUpdate");
					outputObject.put(JsonKeys.fileUrl.name(), relFilename);
					outputObject.put(JsonKeys.worksheetId.name(), worksheetId);
					pw.println(outputObject.toString(4));
					pw.println(",");
					new InfoUpdate("Succesfully exported to JSON").generateJson(prefix, pw, vWorkspace);
					UIConfiguration uiConfiguration = UIConfigurationRegistry.getInstance().getUIConfiguration(vWorkspace.getWorkspace().getContextId());
					boolean showCleaningCharts = uiConfiguration.isD3ChartsEnabled();
					if(importAsWorksheet && newWSId != null) {
						pw.println(",");
						new WorksheetListUpdate().generateJson(prefix, pw, vWorkspace);
						pw.println(",");
						if (showCleaningCharts) {
							new WorksheetCleaningUpdate(newWSId, true, SuperSelectionManager.DEFAULT_SELECTION).generateJson(prefix, pw, vWorkspace);
							pw.println(",");
						}
						new WorksheetHeadersUpdate(newWSId, SuperSelectionManager.DEFAULT_SELECTION).generateJson(prefix, pw, vWorkspace);
						pw.println(",");
						new WorksheetDataUpdate(newWSId, SuperSelectionManager.DEFAULT_SELECTION).generateJson(prefix, pw, vWorkspace);
					}
				}
			}
		}

		PublishJSONUpdate update = new PublishJSONUpdate();
		UpdateContainer uc = new UpdateContainer(update);

		return uc;
	}

	private void generateRowsUsingPager(TablePager pager, VWorksheet vWorksheet, 
			List<VHNode> orderedHnodeIds, PrintWriter pw, 
			String space) throws JSONException {
		List<Row> rows = pager.getRows();
		pw.print(space + "[");
		while(true) {

			generateRowsJSONArray(rows, vWorksheet, orderedHnodeIds, pw, space + " ");
			if(pager.isAtEndOfTable()) {
				break;
			}
			pw.print(",");
			rows = pager.loadAdditionalRows();
		}
		pw.println();
		pw.println(space + "]");
	}

	private void generateRowsJSONArray(List<Row> rows, VWorksheet vWorksheet, List<VHNode> orderedHnodeIds, 
			PrintWriter pw, String space) throws JSONException {
		String sep = "";
		for (Row row:rows) {
			pw.print(sep);
			String rowSep = "";
			pw.println();
			pw.print(space + "{");
			for (VHNode vNode : orderedHnodeIds) {
				if(vNode.isVisible()) {
					Node rowNode = row.getNode(vNode.getId());
					if (vNode.hasNestedTable()) {
						JSONObject nodeObj = new JSONObject();
						String columnName = vNode.getColumnName();
						if(columnName == null || columnName.length() == 0)
							continue;
						nodeObj.put(columnName, "");
						String str = nodeObj.toString();
						pw.print(rowSep);
						pw.print(str.substring(1, str.length()-3));

						Table nestedTable = rowNode.getNestedTable();
						generateRowsUsingPager( 
								vWorksheet.getNestedTablePager(nestedTable), 
								vWorksheet,
								vNode.getNestedNodes(), 
								pw, space + "  ");
					} else {
						JSONObject nodeObj = new JSONObject();
						String columnName = vNode.getColumnName();
						if(columnName == null || columnName.length() == 0)
							continue;
						nodeObj.put(columnName, rowNode.getValue().asString());
						String str = nodeObj.toString();
						pw.print(rowSep);
						pw.print(str.substring(1, str.length()-1));
					}
					rowSep = ",";
				}

			}
			pw.print(space + "}");
			sep = ",";
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}


}
