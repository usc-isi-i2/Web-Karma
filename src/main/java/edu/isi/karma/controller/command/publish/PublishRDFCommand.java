package edu.isi.karma.controller.command.publish;

import java.io.PrintWriter;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.alignment.Vertex;
import edu.isi.karma.rdf.SourceDescription;
import edu.isi.karma.rdf.WorksheetRDFGenerator;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.util.FileUtil;
import edu.isi.karma.view.VWorkspace;

public class PublishRDFCommand extends Command {
	private final String vWorksheetId;
	private String publicRDFAddress;
	private String rdfSourcePrefix;

	public enum JsonKeys {
		updateType, fileUrl
	}

	private static Logger logger = LoggerFactory
			.getLogger(PublishRDFCommand.class);

	protected PublishRDFCommand(String id, String vWorksheetId, String publicRDFAddress,
								String rdfSourcePrefix) {
		super(id);
		this.vWorksheetId = vWorksheetId;
		this.publicRDFAddress=publicRDFAddress;
		this.rdfSourcePrefix=rdfSourcePrefix;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Publish RDF";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();

		final String rdfFileName = "./src/main/webapp/RDF/" + vWorksheetId + ".rdf";
		
		//get alignment for this worksheet
		System.out.println("Get alignment for " + vWorksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(vWorksheetId + "AL");
		if(alignment==null){
			System.out.println("Alignment is NULL for " + vWorksheetId);
			return new UpdateContainer(new ErrorUpdate(
			"Please align the worksheet before generating RDF!"));			
		}

		DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> tree = 
			alignment.getSteinerTree();
		Vertex root = alignment.GetTreeRoot();

		try{
			if (root != null) {
				// Write the source description
				//use true to generate a SD with column names (for use "outside" of Karma)
				//use false for internal use
				SourceDescription desc = new SourceDescription(vWorkspace.getRepFactory(), tree, root,rdfSourcePrefix, false);
				String descString = desc.generateSourceDescription();
				System.out.println("SD="+ descString);
				WorksheetRDFGenerator wrg = new WorksheetRDFGenerator(vWorkspace.getRepFactory(), descString, rdfFileName);
				if(worksheet.getHeaders().hasNestedTables()){
					wrg.generateTriplesCell(worksheet);
				}
				else{
					wrg.generateTriplesRow(worksheet);
					wrg.generateTriplesCell(worksheet);	
				}
				String fileName = "./publish/Source Description/"+worksheet.getTitle()+".txt";
				FileUtil.writeStringToFile(descString, fileName);
				logger.info("Source description written to file: " + fileName);
				logger.info("RDF written to file: " + rdfFileName);
				////////////////////

			} else {
				return new UpdateContainer(new ErrorUpdate(
				"Alignment returned null root!!"));			
			}


			return new UpdateContainer(new AbstractUpdate() {
				@Override
				public void generateJson(String prefix, PrintWriter pw,
						VWorkspace vWorkspace) {
					JSONObject outputObject = new JSONObject();
					try {
						outputObject.put(JsonKeys.updateType.name(),
						"PublishRDFUpdate");
						outputObject.put(JsonKeys.fileUrl.name(),
								publicRDFAddress + vWorksheetId +".rdf");
						pw.println(outputObject.toString(4));
					} catch (JSONException e) {
						logger.error("Error occured while generating JSON!");
					}
				}
			});
		}catch(Exception e){
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));						
		}
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
