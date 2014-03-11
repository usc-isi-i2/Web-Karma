package edu.isi.karma.modeling.semantictypes;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler.ColumnFeature;
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
	private final CRFModelHandler crfModelHandler;
	private OntologyManager ontologyManager;
	private Alignment alignment;
	
	
	private final Logger logger = LoggerFactory.getLogger(SemanticTypePredictionThread.class);
	
	public SemanticTypePredictionThread(Worksheet worksheet,
			List<HNodePath> hNodePaths,
			CRFModelHandler crfModelHandler, 
			OntologyManager ontologyManager, 
			Alignment alignment) {
		this.worksheet = worksheet;
		this.hNodePaths = hNodePaths;
		this.crfModelHandler = crfModelHandler;
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
	
				Map<ColumnFeature, Collection<String>> columnFeatures = new HashMap<ColumnFeature, Collection<String>>();
	
				// Prepare the column name feature
				String columnName = path.getLeaf().getColumnName();
				Collection<String> columnNameList = new ArrayList<String>();
				columnNameList.add(columnName);
				columnFeatures.put(ColumnFeature.ColumnHeaderName, columnNameList);
				
				// // Prepare the table name feature
				// String tableName = worksheetName;
				// Collection<String> tableNameList = new ArrayList<String>();
				// tableNameList.add(tableName);
				// columnFeatures.put(ColumnFeature.TableName, tableNameList);
	
				// Stores the probability scores
				ArrayList<Double> scores = new ArrayList<Double>();
				// Stores the predicted labels
				ArrayList<String> labels = new ArrayList<String>();
				boolean predictResult = crfModelHandler.predictLabelForExamples(
						trainingExamples, 4, labels, scores, null, columnFeatures);
				if (!predictResult) {
					logger.debug("Error occured while predicting semantic type.");
					continue;
				}
				if (labels.size() == 0) {
					continue;
				}
	
				logger.debug("Examples: " + trainingExamples + " Type: " + labels
						+ " ProbL " + scores);
				
				/** Remove the labels that are not in the ontology or are already used as the semantic type **/
				List<String> removeLabels = new ArrayList<String>();
				String domainUri, typeUri;
				Label domain, type;
				for (int i=0; i<labels.size(); i++) {
					String label = labels.get(i);
					/** Check if not in ontology **/
					if (label.contains("|")) {
						
						domainUri = label.split("\\|")[0].trim();
						typeUri = label.split("\\|")[1].trim();
						
						domain = ontologyManager.getUriLabel(domainUri);
						type = ontologyManager.getUriLabel(typeUri);
						
						// Remove from the list if URI not present in the model
						if (domain == null || type == null) {
							removeLabels.add(label);
							continue;
						}
										
					} else {
						domain = ontologyManager.getUriLabel(label);
						// Remove from the list if URI not present in the model
						if (domain == null) {
							removeLabels.add(label);
							continue;
						}
					}
				}
				for (String removeLabel : removeLabels) {
					int idx = labels.indexOf(removeLabel);
					labels.remove(removeLabel);
					scores.remove(idx);
				}
				if (labels.size() == 0) {
					continue;
				}
	
				CRFColumnModel columnModel = new CRFColumnModel(labels, scores);
				worksheet.getCrfModel().addColumnModel(path.getLeaf().getId(),
						columnModel);
			} catch (Exception e) {}
		} 			
		addSemanticTypesToColumnNodes();
	}
	
	private void addSemanticTypesToColumnNodes() {
		
		FullCRFModel fullCRFModel = worksheet.getCrfModel();
		
		// Create column nodes for the alignment
		List<SemanticType> crfSuggestedSemanticTypes;
		CRFColumnModel columnCRFModel;
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
