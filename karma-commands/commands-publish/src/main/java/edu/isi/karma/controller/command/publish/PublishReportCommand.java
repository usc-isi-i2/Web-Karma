package edu.isi.karma.controller.command.publish;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.history.CommandHistory;
import edu.isi.karma.controller.history.CommandHistory.HistoryArguments;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class PublishReportCommand extends WorksheetCommand {

	private enum JsonKeys {
		updateType, fileUrl, worksheetId
	}
	
	private String worksheetName;
	
	// Logger object
	private static Logger logger = LoggerFactory.getLogger(PublishReportCommand.class);

	public PublishReportCommand(String id, String worksheetId) {
		super(id, worksheetId);
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
		
		//Map for py transforms, so only the latest pyTransform for a column gets written
		LinkedHashMap<String, String> pyTransformMap = new LinkedHashMap<>();
		String linebreak = System.getProperty("line.separator");
		
		String historyFile = CommandHistory.getHistorySaver(workspace.getId()).getHistoryFilepath(worksheetId);
		try {
			
		
			JSONArray history = CommandHistory.getHistorySaver(workspace.getId()).loadHistory(historyFile);
			
			for(int i=0; i<history.length(); i++) {
				JSONObject command = history.getJSONObject(i);
				JSONArray inputParamArr = command.getJSONArray(HistoryArguments.inputParameters.name());
				String commandName = command.getString(HistoryArguments.commandName.name());
		
				if(commandName.equalsIgnoreCase("SubmitPythonTransformationCommand") || commandName.equalsIgnoreCase("SubmitEditPythonTransformationCommand")) {
					String code = HistoryJsonUtil.getStringValue("transformationCode", inputParamArr);
					String columnName = HistoryJsonUtil.getStringValue("newColumnName", inputParamArr);
					if ("".equals(columnName)) {
						JSONArray columnNames = HistoryJsonUtil.getJSONArrayValue("targetHNodeId", inputParamArr);				
						for(int j=0; j<columnNames.length(); j++) {
							JSONObject columnNameObj = columnNames.getJSONObject(j);
							columnName =columnNameObj.getString("columnName");
						}
					}
					
					StringBuffer pyTransform = new StringBuffer("#### _" + columnName + "_");
					pyTransform.append(linebreak);
					
					//Pedro: throw-away code, ought to have a better way to construct column paths.
					JSONArray hNodeIdArray = HistoryJsonUtil.getJSONArrayValue("hNodeId", inputParamArr);
					String invocationColumnName = "";
					String sep = "";
					for(int j=0; j<hNodeIdArray.length(); j++) {
						JSONObject columnNameObj = hNodeIdArray.getJSONObject(j);
						String name =columnNameObj.getString("columnName");
						invocationColumnName += sep + name;
						sep = " / ";
					}
					pyTransform.append("From column: _" + invocationColumnName + "_").append(linebreak);
					
					pyTransform.append(">``` python").append(linebreak);
					pyTransform.append(code).append(linebreak);
					pyTransform.append("```").append(linebreak);
					pyTransform.append(linebreak);
					
					pyTransformMap.put(columnName, pyTransform.toString());
				}
			}
		} catch(Exception e) {
			logger.error("Error reading history file:" + historyFile);
		}
		
		//Now get all transforms from the map and write them to the writer
		for(String columnName : pyTransformMap.keySet()) {
			pw.print(pyTransformMap.get(columnName));
		}
	}
	
	
	
	private void writeSemanticTypes(Workspace workspace, Worksheet worksheet, PrintWriter pw) {
		
		pw.println("| Column | Property | Class |");
		pw.println("|  ----- | -------- | ----- |");

		Alignment alignment = AlignmentManager.Instance().getAlignment(workspace.getId(), worksheet.getId());
		if(alignment != null) {
			 List<Node> columnNodes = Arrays.asList(alignment.getNodesByType(NodeType.ColumnNode).toArray(new Node[0]));
			 Collections.sort(columnNodes, new Comparator<Node>() {
					@Override
					public int compare(Node n1, Node n2) {
						String l1name = getClassName(n1);
						String l2name = getClassName(n2);
						return l1name.compareTo(l2name);
					}
				});
			 
			 if(columnNodes != null) {
				 for(Node node : columnNodes) {
					 if(node instanceof ColumnNode) {
						 ColumnNode columnNode = (ColumnNode)node;
						 String columnName = getClassName(columnNode);
						 List<LabeledLink> links = alignment.getIncomingLinks(node.getId());
						 for(LabeledLink link : links) {
							 String property = getPropertyName(link.getLabel());
							 String classname = getClassName(link.getSource());
							 pw.println("| _" + columnName + "_ | `" + property + "` | `" + classname + "`|");
						 }
					 }
				 }
			 }
		}
		pw.println();
	}
	
	private void writeLinks(Workspace workspace, Worksheet worksheet, PrintWriter pw) {
		
		pw.println("| From | Property | To |");
		pw.println("|  --- | -------- | ---|");
		
		Alignment alignment = AlignmentManager.Instance().getAlignment(workspace.getId(), worksheet.getId());
		if(alignment != null) {
			DirectedWeightedMultigraph<Node, LabeledLink> alignmentGraph = alignment.getSteinerTree();
			if(alignmentGraph != null) {
				List<LabeledLink> links = Arrays.asList(alignmentGraph.edgeSet().toArray(new LabeledLink[0]));
				Collections.sort(links, new Comparator<LabeledLink>() {
					@Override
					public int compare(LabeledLink l1, LabeledLink l2) {
						String l1name = getClassName(l1.getSource());
						String l2name = getClassName(l2.getSource());
						return l1name.compareTo(l2name);
					}
					
				});
				for (LabeledLink link : links) {
					Node target = link.getTarget();
					if(target instanceof ColumnNode) { //ALready covered in semantic types
						continue;
					}
					
					Node source = link.getSource();
					String property = getPropertyName(link.getLabel());
					String to = getClassName(target);
					String from = getClassName(source);
					
					pw.println("| `" + from + "` | `" + property + "` | `" + to + "`|");
				}
			}
		}
	}
	
	private String getClassName(Node node) {
		String name =  (node instanceof ColumnNode) ? ((ColumnNode)node).getColumnName() : node.getDisplayId();
		return name;
	}
	
	private String getPropertyName(Label label) {
		String name = label.getDisplayName();
		if(name.equals("km-dev:classLink"))
			name = "uri";
		return name;
	}
	
	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
