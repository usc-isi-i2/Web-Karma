package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.mapping.WorksheetR2RMLJenaModelParser;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;


public class SaveR2RMLModelCommand extends Command{

	private String modelUrl;
	private String tripleStoreUrl;
	private String graphContext;
	private String collection;
	private String graphBaseUrl = "";
	private boolean successful;
	private static Logger logger = LoggerFactory.getLogger(SaveR2RMLModelCommand.class);

	protected SaveR2RMLModelCommand(String id, String model, String modelUrl, String url, String context, String collection) {
		super(id, model);
		this.modelUrl = modelUrl;
		this.tripleStoreUrl = url;
		this.graphContext = context;
		this.collection = collection;
		successful = false;
	}


	private enum JsonKeys {
		updateType, fileUrl, worksheetId
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Save R2RML Model";
	}

	@Override
	public String getDescription() {
		return "Save " + modelUrl;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}
	
	public boolean getSuccessful() {
		return successful;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		UpdateContainer uc = new UpdateContainer();
		if (collection.compareTo("Collection") == 0) {
			try {
				URL url = new URL(graphContext);
				graphBaseUrl = url.getProtocol() + "://" + url.getHost() + "/worksheets/";
				Scanner in = new Scanner(url.openStream());
				JSONArray array = new JSONArray(in.nextLine());
				in.close();
				boolean result = true;
				for (int i = 0; i < array.length(); i++) {
					JSONObject obj = array.getJSONObject(i);
					String modelUrl = obj.getString("url");
					String context = graphContext;
					if (context == null || context.trim().isEmpty()) {
						String filename = modelUrl.substring(modelUrl.indexOf('-') + 1);
						int location = filename.indexOf("-auto-model.ttl");
						if (location == -1)
							location = filename.indexOf("-model.ttl");
						filename = filename.substring(0, location);
						context = graphBaseUrl + filename;
					}
					result &= saveMapping(modelUrl, context);
					//System.out.println("here: " + graphBaseUrl + filename);
				}
				if (result) {
					logger.info("Saved collection to triple store");
					uc.add(new AbstractUpdate() {
						public void generateJson(String prefix, PrintWriter pw,	
								VWorkspace vWorkspace) {
							JSONObject outputObject = new JSONObject();
							try {
								outputObject.put(JsonKeys.updateType.name(), "SaveCollection");								
								pw.println(outputObject.toString());
							} catch (JSONException e) {
								e.printStackTrace();
								logger.error("Error occured while generating JSON!");
							}
						}
					});
					successful = result;
					return uc;
				}
			}catch(Exception e) {
				System.out.println("here");
				e.printStackTrace();
				logger.error("Error occured while saving R2RML Model!");
				return new UpdateContainer(new ErrorUpdate("Error occured while saving R2RML model!"));
			}

		}
		else {
			boolean result = saveMapping(modelUrl, graphContext);
			if (result) {
				logger.info("Saved model to triple store");
				uc.add(new AbstractUpdate() {
					public void generateJson(String prefix, PrintWriter pw,	
							VWorkspace vWorkspace) {
						JSONObject outputObject = new JSONObject();
						try {
							outputObject.put(JsonKeys.updateType.name(), "SaveModel");
							pw.println(outputObject.toString());
						} catch (JSONException e) {
							e.printStackTrace();
							logger.error("Error occured while generating JSON!");
						}
					}
				});
				successful = result;
				return uc;
			}
		}
		logger.error("Error occured while saving R2RML Model!");
		return new UpdateContainer(new ErrorUpdate("Error occured while saving R2RML model!"));
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

	private boolean saveMapping(String modelUrl, String graphContext) {
		try {
			TripleStoreUtil utilObj = new TripleStoreUtil();
			if (graphContext == null || graphContext.trim().compareTo("") == 0)
				return false;
			URL url = new URL(modelUrl);
			StringWriter test = new StringWriter();
			R2RMLMappingIdentifier modelId = new R2RMLMappingIdentifier(modelUrl, url, null);
			Model model = WorksheetR2RMLJenaModelParser.loadSourceModelIntoJenaModel(modelId);
			Property rdfTypeProp = model.getProperty(Uris.RDF_TYPE_URI);

			RDFNode node = model.getResource(Uris.KM_R2RML_MAPPING_URI);
			ResIterator res = model.listResourcesWithProperty(rdfTypeProp, node);
			List<Resource> resList = res.toList();
			for(Resource r: resList)
			{
				model.add(r, model.getProperty(Uris.OWL_SAMEAS_URI), model.getResource(url.toString()));
			}
			model.write(test,"TTL");
			model.close();
			String content = test.getBuffer().toString();
			test.close();
			if (utilObj.testURIExists(tripleStoreUrl, graphContext, modelUrl)) {
				utilObj.deleteMappingFromTripleStore(tripleStoreUrl, graphContext, modelUrl);
			}
			boolean result = utilObj.saveToStoreFromString(content, tripleStoreUrl, graphContext, new Boolean(false), null);
			return result;
		}catch (Exception e) {
			return false;
		}
	}
}
