package edu.isi.karma.modeling.semantictypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class SemanticTypePredictionThread implements Runnable {

	private final CRFModelHandler crfModelHandler;
	private final Worksheet worksheet;
	private Alignment alignment;
	private OntologyManager ontologyManager;
	
	
	private final Logger logger = LoggerFactory.getLogger(SemanticTypePredictionThread.class);
	
	public SemanticTypePredictionThread(Worksheet worksheet,
			CRFModelHandler crfModelHandler, OntologyManager ontologyManager, Alignment alignment) {
		this.crfModelHandler = crfModelHandler;
		this.worksheet = worksheet;
		this.alignment = alignment;
		this.ontologyManager = ontologyManager;
	}

	
	public void run() {

		List<HNodePath> paths = worksheet.getHeaders().getAllPaths();

		for (HNodePath path : paths) {

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

			CRFColumnModel columnModel = new CRFColumnModel(labels, scores);
			worksheet.getCrfModel().addColumnModel(path.getLeaf().getId(),
					columnModel);
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
		for (HNodePath path : worksheet.getHeaders().getAllPaths()) {
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
			if (c != null && c instanceof ColumnNode) 
				((ColumnNode)c).setCrfSuggestedSemanticTypes(crfSuggestedSemanticTypes);
		}

	}

}
