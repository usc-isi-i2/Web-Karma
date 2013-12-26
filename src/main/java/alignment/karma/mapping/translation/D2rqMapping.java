package alignment.karma.mapping.translation;

import alignment.karma.mapping.translation.ontologies.*;
import alignment.karma.mapping.translation.ontologies.kdd.info;
import alignment.karma.mapping.translation.ontologies.kdd.ito;
import alignment.karma.mapping.translation.ontologies.kdd.quality;
import alignment.karma.mapping.translation.ontologies.kdd.ro;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import java.io.Writer;
import java.util.*;

/**
 * Class D2rqMapping
 *
 * @since 12/02/2013
 */
public class D2rqMapping
{
	private static final String mapNameSpaceToReplaceOn = "http://www.cubrc.org/ontologies/KDD/<DATA-SET-NAME>/alignment#";
	private static final String dataServiceUrlToReplaceOn = "http://kdd-dataservice:8080/KDDDataService/resources/retrieve/<DATA-SET-NAME>/@@<DATA-SET-TABLE-NAME>.<PK-COLUMN>@@?format=Raw";
	private String dataServiceUrl;
	private String baseUri;
	private Resource dataSourceInformation;// connection information for d2rq engine
	private String dataSourceName;
	private String d2rqTableName;
	private String d2rqPkColumn;
	private String d2rqBasePattern;
	private Map<String,Resource> dataElementMap = new HashMap<>();
	private Resource dataRow;
	private Model d2rqMapping;
	public D2rqMapping(Model model) {
		Resource dataSource = model.listStatements(null, RDF.type, kmdev.R2RMLMapping).nextStatement().getSubject();
		dataSourceName = model.listStatements(dataSource,kmdev.source_name,(RDFNode) null).nextStatement().getObject().asLiteral().getString().replace(".csv","");
		populateD2rqParameters(model);
		d2rqMapping = ModelFactory.createDefaultModel(); // init empty model
		createDataSourceInformation();
		populateDataRowAndElements(model);
	}

	private void populateD2rqParameters(Model model)
	{
		//TODO determine identifier in row + how to markup this will exist in all uris for each individual created
		d2rqPkColumn="id";//should be determined by the mapping process
		d2rqTableName = dataSourceName+"_table";
		baseUri = mapNameSpaceToReplaceOn.replace("<DATA-SET-NAME>",dataSourceName);
		dataServiceUrl = dataServiceUrlToReplaceOn.replace("<DATA-SET-NAME>",dataSourceName).replace("<DATA-SET-TABLE-NAME>",d2rqTableName).replace("<PK-COLUMN>",d2rqPkColumn);
		d2rqBasePattern = "/@@"+d2rqTableName+"."+d2rqPkColumn+"|urlify@@";
	}

	public void createDataSourceInformation() {
		dataSourceInformation = d2rqMapping.createResource(baseUri + dataSourceName, d2rq.Database);
		StringBuilder comment = new StringBuilder();
		comment.append("{\"dataSourceName\":\""+dataSourceName+"\",\"" +
				               "dataSourceId\":\""+dataSourceName+"\",\"" +
				               "tableName\":\""+d2rqTableName+"\",\"" +
				               "protocolType\":\"DSS_2_0\",\"" +
				               "dataSourceType\":\"DataSourceAPI\",\"" +
				               "schemaName\":\"CUBRC-Interpol\",\"" +
				               "topLevelClassMap\":\"map:"+dataSourceName+"BaseIndividual\"}");
		dataSourceInformation.addProperty(RDFS.comment,comment.toString());
		dataSourceInformation.addProperty(d2rq.jdbcDSN,"http://kdd-dataservice:8080/dss/resources");
		dataSourceInformation.addProperty(d2rq.jdbcDriver,"kdddataservice");
		dataSourceInformation.addProperty(d2rq.password,"Foo");
		dataSourceInformation.addProperty(d2rq.username,"Foo");
	}

	public void populateDataRowAndElements(Model model)
	{
		List<String> columnNames = new ArrayList<>();
		List<Statement> stmts = model.listStatements(null, r2rml.column, (RDFNode) null).toList();
		for(Statement s:stmts) {
			columnNames.add(s.getObject().asLiteral().getString());
		}
		dataRow = d2rqMapping.createResource(baseUri+"DataRow", d2rq.ClassMap);
		dataRow.addProperty(d2rq.clazz, ito.DataRow);
		dataRow.addProperty(d2rq.dataStorage, dataSourceInformation);
		dataRow.addProperty(d2rq.uriPattern,"DataRow"+ d2rqBasePattern);

		Resource dataRowUri = d2rqMapping.createResource(baseUri+"DataRow_has_uri_value",d2rq.propertyBridge);
		dataRowUri.addProperty(d2rq.belongsToClassMap,dataRow);
		dataRowUri.addProperty(d2rq.datatype,xsd.String);
		dataRowUri.addProperty(d2rq.property, info.has_URI_value);
		dataRowUri.addProperty(d2rq.pattern,dataServiceUrl);

		for(String columnName:columnNames) {
			populateDataElement(columnName,dataRow);
		}
	}

	private void populateDataElement(String columnName, Resource dataRow)
	{
		Resource dataElement = d2rqMapping.createResource(baseUri+columnName,d2rq.classMap);
		dataElement.addProperty(d2rq.clazz,ito.DataElement);
		dataElement.addProperty(d2rq.dataStorage,dataSourceInformation);
		dataElement.addProperty(d2rq.uriPattern,columnName+ d2rqBasePattern);
		dataElementMap.put(columnName,dataElement);

		Resource dataRowDataElement = d2rqMapping.createResource(baseUri + "DataRow_"+ columnName, d2rq.propertyBridge);
		dataRowDataElement.addProperty(d2rq.belongsToClassMap,dataRow);
		dataRowDataElement.addProperty(d2rq.property, ro.has_part);
		dataRowDataElement.addProperty(d2rq.refersToClassMap,dataElement);

		Resource dataElementDataRow = d2rqMapping.createResource(baseUri + columnName+ "_DataRow" , d2rq.propertyBridge);
		dataElementDataRow.addProperty(d2rq.belongsToClassMap,dataElement);
		dataElementDataRow.addProperty(d2rq.property, ro.part_of);
		dataElementDataRow.addProperty(d2rq.refersToClassMap,dataRow);

		Resource dataElementValue = d2rqMapping.createResource(baseUri + columnName + "_textValue",d2rq.propertyBridge);
		dataElementValue.addProperty(d2rq.belongsToClassMap,dataElement);
		dataElementValue.addProperty(d2rq.property, info.has_text_value);
		dataElementValue.addProperty(d2rq.datatype,xsd.String);
		dataElementValue.addProperty(d2rq.column,d2rqTableName+"."+columnName);

		Resource dataElementUri = d2rqMapping.createResource(baseUri+columnName+"_has_uri_value",d2rq.propertyBridge);
		dataElementUri.addProperty(d2rq.belongsToClassMap,dataRow);
		dataElementUri.addProperty(d2rq.datatype,xsd.String);
		dataElementUri.addProperty(d2rq.property, info.has_URI_value);
		dataElementUri.addProperty(d2rq.pattern,dataServiceUrl+"&column="+columnName);
	}

	public void writeD2rqMapping(Writer w) {
		d2rqMapping.setNsPrefixes(LightWeightOntologyLoader.getPrefixes());
		d2rqMapping.setNsPrefix("map", baseUri);
		d2rqMapping.write(w,"TURTLE");
	}

	public Resource createClassMap(String classMapName, Resource rdfType) {
		Resource classMap = d2rqMapping.createResource(baseUri+classMapName,d2rq.classMap);
		classMap.addProperty(d2rq.clazz,rdfType);
		classMap.addProperty(d2rq.dataStorage,dataSourceInformation);
		classMap.addProperty(d2rq.uriPattern,classMapName+ d2rqBasePattern);
		return classMap;
	}

	Model ontologyModel = LightWeightOntologyLoader.getRootModel();
	/**
	 * This creates the property bridge required to represent the link between subject and object.
	 * It also asserts the inverse relation between the two resources if it exists
	 * @param subjectClassmap ClassmapClassmap
	 * @param property
	 * @param objectClassmap ClassmapClassmap
	 */
	public void createPropertyBridge(Resource subjectClassmap, Property property, Resource objectClassmap) {
		Resource propertyBridge = d2rqMapping.createResource(baseUri + subjectClassmap.getLocalName() +"_"+ objectClassmap.getLocalName(), d2rq.propertyBridge);
		propertyBridge.addProperty(d2rq.belongsToClassMap,subjectClassmap);
		propertyBridge.addProperty(d2rq.property, property);
		propertyBridge.addProperty(d2rq.refersToClassMap,objectClassmap);

		StmtIterator stmt = ontologyModel.listStatements(property.asResource(), owl.inverseOf,(RDFNode)null);
		if(stmt.hasNext()) {
			Resource inverseProperty = stmt.nextStatement().getObject().asResource();
			Resource propertyBridgeInverse = d2rqMapping.createResource(baseUri + objectClassmap.getLocalName() +"_"+ subjectClassmap.getLocalName(), d2rq.propertyBridge);
			propertyBridgeInverse.addProperty(d2rq.belongsToClassMap,objectClassmap);
			propertyBridgeInverse.addProperty(d2rq.property, inverseProperty);
			propertyBridgeInverse.addProperty(d2rq.refersToClassMap,subjectClassmap);
		}
	}


	public Resource getDataRow()
	{
		return dataRow;
	}

	public Resource getDataElement(String columnName)
	{
		return dataElementMap.get(columnName);
	}

	public void createPropertyBridge(Resource subjectClassmap, Property property, List<String> columnNames)
	{
		Resource propertyBridge = d2rqMapping.createResource(baseUri + subjectClassmap.getLocalName() +"_"+ property.getLocalName(), d2rq.propertyBridge);
		propertyBridge.addProperty(d2rq.belongsToClassMap,subjectClassmap);
		propertyBridge.addProperty(d2rq.property, property);
		propertyBridge.addProperty(d2rq.datatype, xsd.String);
		if(columnNames.size()==1) {
			propertyBridge.addProperty(d2rq.column,d2rqTableName+"."+columnNames.get(0));
		}
		else {
			StringBuilder pattern = new StringBuilder();
			for(String columnName:columnNames) {
				pattern.append("@@").append(d2rqTableName).append(".").append(columnName).append("@@ ");
			}
			propertyBridge.addProperty(d2rq.pattern,ResourceFactory.createPlainLiteral(pattern.toString().trim()));
		}
	}

	private Resource colorTranslationTable;

	public void addValueTranslationTable(Resource subject, Property predicate, translationTables tranlationTableType, List<String> columnNames)
	{
		Resource translationTable = initilizeTranslationTable(tranlationTableType);
		for(String columnName:columnNames) {
			Resource translateProperty = d2rqMapping.createResource(baseUri+subject.getLocalName()+"_Translation_"+columnName,d2rq.PropertyBridge);
			translateProperty.addProperty(d2rq.belongsToClassMap,subject);
			translateProperty.addProperty(d2rq.property,predicate);
			translateProperty.addProperty(d2rq.uriColumn,dataSourceName+"."+columnName);
			translateProperty.addProperty(d2rq.translateWith,translationTable);
		}
	}

	private Resource initilizeTranslationTable(translationTables tranlationTableType)
	{
		switch (tranlationTableType) {
			case Color:
				return colorTranslationTable();
			default:
				return null;
		}
	}

	public void connectToColumn(Resource subject, Property property, List<String> columnNames)
	{
		for(String columnName: columnNames) {
			createPropertyBridge(subject, ro.part_of, getDataElement(columnName));
		}
		createPropertyBridge(subject,info.has_text_value, columnNames);
	}

	public enum translationTables {
		Color
	}

	public Resource colorTranslationTable()
	{
		if(colorTranslationTable==null) {
			Map<String,Resource> colorTranslations = new HashMap<>();
			colorTranslations.put("brown", quality.Brown);
			colorTranslations.put("red", quality.Red);
			colorTranslations.put("black", quality.Black);
			colorTranslations.put("hazel", quality.Hazel);
			colorTranslationTable = createTranslationTable(colorTranslations,"Color");
		}
		return colorTranslationTable;
	}

	private Resource createTranslationTable(Map<String,Resource> translations, String translationTableName ){
		Resource translationTable = d2rqMapping.createResource(baseUri+translationTableName+"_"+d2rq.TranslationTable.getLocalName(),d2rq.TranslationTable);
		Iterator<Map.Entry<String,Resource>> it = translations.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String,Resource> e = it.next();
			Resource translation = d2rqMapping.createResource();
			translation.addProperty(d2rq.valueContains,e.getKey());
			translation.addProperty(d2rq.rdfValue,e.getValue());
			translationTable.addProperty(d2rq.translation,translation);
		}
		return translationTable;
	}
}
