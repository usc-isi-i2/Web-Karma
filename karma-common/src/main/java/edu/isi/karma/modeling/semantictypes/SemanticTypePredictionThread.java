package edu.isi.karma.modeling.semantictypes;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.Origin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

public class SemanticTypePredictionThread implements Runnable {

	private final Worksheet worksheet;
	List<HNodePath> hNodePaths;
	private final ISemanticTypeModelHandler modelHandler;
	private OntologyManager ontologyManager;
	private Alignment alignment;
	
	
	private final Logger logger = LoggerFactory.getLogger(SemanticTypePredictionThread.class);
	
	public SemanticTypePredictionThread(Worksheet worksheet,
			List<HNodePath> hNodePaths,
			ISemanticTypeModelHandler modelHandler, 
			OntologyManager ontologyManager, 
			Alignment alignment) {
		this.worksheet = worksheet;
		this.hNodePaths = hNodePaths;
		this.modelHandler = modelHandler;
		this.ontologyManager = ontologyManager;
		this.alignment = alignment;
	}

	
	public void run() {

		if (hNodePaths == null)
			return;
		
		for (HNodePath path : this.hNodePaths) {
				
			try {
				logger.debug("predict labels for the column " + path.getLeaf().getColumnName());
	
				ArrayList<String> trainingExamples = SemanticTypeUtil.getTrainingExamples(worksheet,
						path);
				if (trainingExamples.size() == 0)
					continue;
	
				List<SemanticTypeLabel> result = modelHandler.predictType(trainingExamples, 4);
				if (result == null) {
					logger.debug("Error occured while predicting semantic type.");
					continue;
				}
				if (result.size() == 0) {
					continue;
				}
	
				/** Remove the labels that are not in the ontology or are already used as the semantic type **/
				List<SemanticTypeLabel> removeLabels = new ArrayList<SemanticTypeLabel>();
				String domainUri, typeUri;
				Label domain, type;
				for (int i=0; i<result.size(); i++) {
					SemanticTypeLabel semLabel = result.get(i);
					String label = semLabel.getLabel();
					/** Check if not in ontology **/
					if (label.contains("|")) {
						
						domainUri = label.split("\\|")[0].trim();
						typeUri = label.split("\\|")[1].trim();
						
						domain = ontologyManager.getUriLabel(domainUri);
						type = ontologyManager.getUriLabel(typeUri);
						
						// Remove from the list if URI not present in the model
						if (domain == null || type == null) {
							removeLabels.add(semLabel);
							continue;
						}
										
					} else {
						domain = ontologyManager.getUriLabel(label);
						// Remove from the list if URI not present in the model
						if (domain == null) {
							removeLabels.add(semLabel);
							continue;
						}
					}
				}
				for (SemanticTypeLabel removeLabel : removeLabels) {
					result.remove(removeLabel);
				}
				if (result.size() == 0) {
					continue;
				}
	
				SemanticTypeColumnModel columnModel = new SemanticTypeColumnModel(result);
				worksheet.getSemanticTypeModel().addColumnModel(path.getLeaf().getId(),
						columnModel);
			} catch (Exception e) {}
		} 			
		addSemanticTypesToColumnNodes();
	}
	
	private void addSemanticTypesToColumnNodes() {
		
		SemanticTypeWorksheetModel fullCRFModel = worksheet.getSemanticTypeModel();
		
		// Create column nodes for the alignment
		List<SemanticType> crfSuggestedSemanticTypes;
		SemanticTypeColumnModel columnCRFModel;
		String domainUri, propertyUri;
		Label domainLabel, propertyLabel;
		SemanticType semanticType;
		Double confidence;
		String[] parts;
		String key;
		for (HNodePath path : this.hNodePaths) {
			HNode node = path.getLeaf();
			String hNodeId = node.getId();
			crfSuggestedSemanticTypes = new ArrayList<SemanticType>();
			// retrieving CRF suggested semantic types to the column nodes
			if (fullCRFModel != null) {
				columnCRFModel = fullCRFModel.getModelByHNodeId(hNodeId);
				if (columnCRFModel != null) {
					for (Entry<String, Double> entry : columnCRFModel.getScoreMap().entrySet()) {
						
						key = entry.getKey();
						confidence = entry.getValue();
						if (key == null || key.isEmpty()) continue;
						
						parts = key.split("\\|");
						if (parts == null || parts.length != 2) continue;
						
						domainUri = parts[0].trim();
						propertyUri = parts[1].trim();
						
						domainLabel = ontologyManager.getUriLabel(domainUri);
						if (domainLabel == null) continue;
						
						propertyLabel = ontologyManager.getUriLabel(propertyUri);
						if (propertyLabel == null) continue;

						semanticType = new SemanticType(hNodeId, propertyLabel, domainLabel, Origin.CRFModel, confidence, false);
						crfSuggestedSemanticTypes.add(semanticType);
					}
				}
			}
			Node c = alignment.getNodeById(hNodeId);
			if (c != null && c instanceof ColumnNode) {
				((ColumnNode)c).setCrfSuggestedSemanticTypes(crfSuggestedSemanticTypes);
				logger.debug("CRF semantic types added to the column node " + ((ColumnNode)c).getColumnName());
			}
		}
		

	}

}
