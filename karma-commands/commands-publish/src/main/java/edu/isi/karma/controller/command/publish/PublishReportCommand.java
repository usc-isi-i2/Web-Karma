package edu.isi.karma.controller.command.publish;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.history.CommandHistoryWriter.HistoryArguments;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.util.FileUtil;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class PublishReportCommand extends Command {

	private enum JsonKeys {
		updateType, fileUrl, worksheetId
	}
	
	private final String worksheetId;
	private String worksheetName;
	
	// Logger object
	private static Logger logger = LoggerFactory
			.getLogger(PublishReportCommand.class.getSimpleName());

	public PublishReportCommand(String id, String worksheetId) {
		super(id);
		this.worksheetId = worksheetId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Publish Report";
	}

	@Override
	public String getDescription() {
		return worksheetName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		final Worksheet worksheet = workspace.getWorksheet(worksheetId);
		this.worksheetName = worksheet.getTitle();
		
		// Prepare the file path and names
		final String newWorksheetName = worksheetName;
		final String fileName =  newWorksheetName + ".md"; 
		final String fileLocalPath = ServletContextParameterMap.getParameterValue(ContextParameter.REPORT_PUBLISH_DIR) +  
				fileName;
		final String relFilename = ServletContextParameterMap.getParameterValue(ContextParameter.REPORT_PUBLISH_RELATIVE_DIR) + fileName;
		final Workspace finalWorkspace = workspace;
		
		UpdateContainer uc = new UpdateContainer(new AbstractUpdate() {
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				try {
					File f = new File(fileLocalPath);
					File parentDir = f.getParentFile();
					parentDir.mkdirs();
					PrintWriter fileWriter = new PrintWriter(f);
					
					fileWriter.println("## " + worksheet.getTitle());
					
					fileWriter.println();
					fileWriter.println("### PyTransforms");
					writePyTransforms(finalWorkspace, worksheet, fileWriter);
					
					fileWriter.println();
					fileWriter.println("### Semantic Types");
					writeSemanticTypes(finalWorkspace, worksheet, fileWriter);
					
					fileWriter.println();
					fileWriter.println("### Links");
					writeLinks(finalWorkspace, worksheet, fileWriter);
					
					fileWriter.close();
					
					JSONObject outputObject = new JSONObject();
					outputObject.put(JsonKeys.updateType.name(), "PublishReportUpdate");
					outputObject.put(JsonKeys.fileUrl.name(), relFilename);
					outputObject.put(JsonKeys.worksheetId.name(), worksheetId);
					pw.println(outputObject.toString(4));
					pw.println(",");
					new InfoUpdate("Succesfully generated report").generateJson(prefix, pw, vWorkspace);
				} catch(Exception e) {
					pw.println("Error generting report");
					logger.error("Error generating report", e);
				}
				
			}
		});
		
		return uc;
	}

	private void writePyTransforms(Workspace workspace, Worksheet worksheet, PrintWriter pw) throws IOException {
		String historyFilePath = HistoryJsonUtil.constructWorksheetHistoryJsonFilePath(
				worksheet.getTitle(), workspace.getCommandPreferencesId());
		File historyFile = new File(historyFilePath);
		if (historyFile.exists()) {
			String encoding = EncodingDetector.detect(historyFile);
			String historyJsonStr = FileUtil.readFileContentsToString(historyFile, encoding);
			JSONArray history = new JSONArray(historyJsonStr);
			
			for(int i=0; i<history.length(); i++) {
				JSONObject command = history.getJSONObject(i);
				JSONArray inputParamArr = command.getJSONArray(HistoryArguments.inputParameters.name());
				String commandName = command.getString(HistoryArguments.commandName.name());
		
				if(commandName.equalsIgnoreCase("SubmitPythonTransformationCommand") || commandName.equalsIgnoreCase("SubmitEditPythonTransformationCommand")) {
					String code = HistoryJsonUtil.getStringValue("transformationCode", inputParamArr);
					JSONArray columnNames = HistoryJsonUtil.getJSONArrayValue("hNodeId", inputParamArr);
					String columnName = "";
					String sep = "";
					for(int j=0; j<columnNames.length(); j++) {
						JSONObject columnNameObj = columnNames.getJSONObject(j);
						String name =columnNameObj.getString("columnName");
						columnName += sep + name;
						sep = " --> ";
					}
					pw.println("**" + columnName + "**");
					pw.println("``` python");
					pw.println(code);
					pw.println("```");
				}
			}
		}
	}
	
	
	
	private void writeSemanticTypes(Workspace workspace, Worksheet worksheet, PrintWriter pw) {
		
		pw.println("| _Column_ | _Property_ | _Class_ |");
		pw.println("|  -------- | --------- | ------- |");

		Alignment alignment = AlignmentManager.Instance().getAlignment(workspace.getId(), worksheet.getId());
		if(alignment != null) {
			 Set<Node> columnNodes = alignment.getNodesByType(NodeType.ColumnNode);
			 if(columnNodes != null) {
				 for(Node node : columnNodes) {
					 if(node instanceof ColumnNode) {
						 ColumnNode columnNode = (ColumnNode)node;
						 String columnName = columnNode.getColumnName();
						 List<LabeledLink> links = alignment.getIncomingLinks(node.getId());
						 for(LabeledLink link : links) {
							 String property = link.getLabel().getUri();
							 String classname = link.getSource().getId();
							 pw.println("| `" + columnName + "` | `" + property + "` | `" + classname + "`|");
						 }
					 }
				 }
			 }
		}
	}
	
	private void writeLinks(Workspace workspace, Worksheet worksheet, PrintWriter pw) {
		
		pw.println("| _From_ | _Property_ | _To_ |");
		pw.println("|  -------- | --------- | ------- |");
		
		Alignment alignment = AlignmentManager.Instance().getAlignment(workspace.getId(), worksheet.getId());
		if(alignment != null) {
			DirectedWeightedMultigraph<Node, LabeledLink> alignmentGraph = alignment.getSteinerTree();
			if(alignmentGraph != null) {
				Set<LabeledLink> links = alignmentGraph.edgeSet();
				for (LabeledLink link : links) {
					Node source = link.getSource();
					Node target = link.getTarget();
					String property = link.getLabel().getUri();
					
					if(target instanceof ColumnNode) { //ALready covered in semantic types
						continue;
					}
					String to = target.getDisplayId();
					String from = (source instanceof ColumnNode) ? ((ColumnNode)source).getColumnName() : source.getDisplayId();
					
					pw.println("| `" + from + "` | `" + property + "` | `" + to + "`|");
				}
			}
		}
	}
	
	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
