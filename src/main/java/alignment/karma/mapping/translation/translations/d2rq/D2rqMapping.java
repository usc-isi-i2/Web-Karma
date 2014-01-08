package alignment.karma.mapping.translation.translations.d2rq;

import alignment.karma.mapping.translation.ontologies.*;
import alignment.karma.mapping.translation.ontologies.kdd.*;
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

		Resource dataRowUri = d2rqMapping.createResource(baseUri + "DataRow_has_uri_value", d2rq.PropertyBridge);
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
		Resource dataElement = d2rqMapping.createResource(baseUri + columnName, d2rq.ClassMap);
		dataElement.addProperty(d2rq.clazz,ito.DataElement);
		dataElement.addProperty(d2rq.dataStorage,dataSourceInformation);
		dataElement.addProperty(d2rq.uriPattern,columnName+ d2rqBasePattern);
		dataElementMap.put(columnName,dataElement);

		Resource dataRowDataElement = d2rqMapping.createResource(baseUri + "DataRow_" + columnName, d2rq.PropertyBridge);
		dataRowDataElement.addProperty(d2rq.belongsToClassMap,dataRow);
		dataRowDataElement.addProperty(d2rq.property, ro.has_part);
		dataRowDataElement.addProperty(d2rq.refersToClassMap,dataElement);

		Resource dataElementDataRow = d2rqMapping.createResource(baseUri + columnName + "_DataRow", d2rq.PropertyBridge);
		dataElementDataRow.addProperty(d2rq.belongsToClassMap,dataElement);
		dataElementDataRow.addProperty(d2rq.property, ro.part_of);
		dataElementDataRow.addProperty(d2rq.refersToClassMap,dataRow);

		Resource dataElementValue = d2rqMapping.createResource(baseUri + columnName + "_textValue", d2rq.PropertyBridge);
		dataElementValue.addProperty(d2rq.belongsToClassMap,dataElement);
		dataElementValue.addProperty(d2rq.property, info.has_text_value);
		dataElementValue.addProperty(d2rq.datatype,xsd.String);
		dataElementValue.addProperty(d2rq.column,d2rqTableName+"."+columnName);

		Resource dataElementUri = d2rqMapping.createResource(baseUri + columnName + "_has_uri_value", d2rq.PropertyBridge);
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
		Resource classMap = d2rqMapping.createResource(baseUri + classMapName, d2rq.ClassMap);
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
		Resource propertyBridge = d2rqMapping.createResource(baseUri + subjectClassmap.getLocalName() + "_" + objectClassmap.getLocalName(), d2rq.PropertyBridge);
		propertyBridge.addProperty(d2rq.belongsToClassMap,subjectClassmap);
		propertyBridge.addProperty(d2rq.property, property);
		propertyBridge.addProperty(d2rq.refersToClassMap,objectClassmap);

		StmtIterator stmt = ontologyModel.listStatements(property.asResource(), owl.inverseOf,(RDFNode)null);
		if(stmt.hasNext()) {
			Resource inverseProperty = stmt.nextStatement().getObject().asResource();
			Resource propertyBridgeInverse = d2rqMapping.createResource(baseUri + objectClassmap.getLocalName() + "_" + subjectClassmap.getLocalName(), d2rq.PropertyBridge);
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
		Resource propertyBridge = d2rqMapping.createResource(baseUri + subjectClassmap.getLocalName() + "_" + property.getLocalName(), d2rq.PropertyBridge);
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
			case EventType:
				return eventTranslationTable();
			case Citizenship:
				return citizenTranslationTable();
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

	public void translationTableTypeTranslation(Resource subject, translationTables translationTableType, List<String> columnNames)
	{
		for (String columnName : columnNames)
		{
			createPropertyBridge(subject, meta.is_mentioned_by, getDataElement(columnName));
		}
		addValueTranslationTable(subject, RDF.type, translationTableType, columnNames);
	}

	public enum translationTables {
		Color,
		Citizenship,
		EventType
	}

	private Resource colorTranslationTable;

	private Resource colorTranslationTable()
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

	private Resource citizenshipTranslationTable;

	private Resource citizenTranslationTable()
	{
		if (citizenshipTranslationTable == null)
		{
			Map<String, Resource> translations = new HashMap<>();
			translations.put("Germany", agent.CitizenRoleFederalRepublicOfGermany);
			translations.put("Trinidad", agent.CitizenRoleTrinidadAndTobago);
			translations.put("Congo", agent.CitizenRoleCongo);
			translations.put("Nicaragua", agent.CitizenRoleNicaragua);
			translations.put("Mauritius", agent.CitizenRoleMauritius);
			translations.put("Niue", agent.CitizenRoleNiue);
			translations.put("Iceland", agent.CitizenRoleIceland);
			translations.put("Libyan Arab Jamahiriya", agent.CitizenRoleLibyanArabJamahiriya);
			translations.put("Tonga", agent.CitizenRoleTonga);
			translations.put("Egypt", agent.CitizenRoleEgypt);
			translations.put("Turkmenistan", agent.CitizenRoleTurkmenistan);
			translations.put("Mozambique", agent.CitizenRoleMozambique);
			translations.put("Cambodia", agent.CitizenRoleCambodia);
			translations.put("United Kingdom", agent.CitizenRoleUnitedKingdomOfGreatBritainAndNorthernIreland);
			translations.put("Faroe Islands", agent.CitizenRoleFaroeIslands);
			translations.put("Taiwan", agent.CitizenRoleTaiwan);
			translations.put("Equatorial Guinea", agent.CitizenRoleEquatorialGuinea);
			translations.put("Palau", agent.CitizenRolePalau);
			translations.put("Dominican Republic", agent.CitizenRoleDominicanRepublic);
			translations.put("Greece", agent.CitizenRoleGreece);
			translations.put("Gabon", agent.CitizenRoleGabon);
			translations.put("Burkina Faso", agent.CitizenRoleBurkinaFaso);
			translations.put("Zimbabwe", agent.CitizenRoleZimbabwe);
			translations.put("Azerbaijan", agent.CitizenRoleAzerbaijan);
			translations.put("Saint Vincent And The Grenadines", agent.CitizenRoleSaintVincentAndTheGrenadines);
			translations.put("Djibouti", agent.CitizenRoleDjibouti);
			translations.put("Hungary", agent.CitizenRoleHungary);
			translations.put("Belarus", agent.CitizenRoleBelarus);
			translations.put("Saint Lucia", agent.CitizenRoleSaintLucia);
			translations.put("Saint Kitts And Nevis", agent.CitizenRoleSaintKittsAndNevis);
			translations.put("Slovenia", agent.CitizenRoleSlovenia);
			translations.put("Malaysia", agent.CitizenRoleMalaysia);
			translations.put("Iran", agent.CitizenRoleIslamicRepublicOfIran);
			translations.put("Singapore", agent.CitizenRoleSingapore);
			translations.put("United Arab Emirates", agent.CitizenRoleUnitedArabEmirates);
			translations.put("Yemen", agent.CitizenRoleDemocraticYemen);
			translations.put("Georgia", agent.CitizenRoleGeorgia);
			translations.put("Guinea", agent.CitizenRoleGuinea);
			translations.put("Colombia", agent.CitizenRoleColombia);
			translations.put("Albania", agent.CitizenRoleAlbania);
			translations.put("Paraguay", agent.CitizenRoleParaguay);
			translations.put("Zaire", agent.CitizenRoleZaire);
			translations.put("Guyana", agent.CitizenRoleGuyana);
			translations.put("Central African Republic", agent.CitizenRoleCentralAfricanRepublic);
			translations.put("Afghanistan", agent.CitizenRoleAfghanistan);
			translations.put("Antigua And Barbuda", agent.CitizenRoleAntiguaAndBarbuda);
			translations.put("Tajikistan", agent.CitizenRoleTajikistan);
			translations.put("Nauru", agent.CitizenRoleNauru);
			translations.put("Finland", agent.CitizenRoleFinland);
			translations.put("Tuvalu", agent.CitizenRoleTuvalu);
			translations.put("Andorra", agent.CitizenRoleAndorra);
			translations.put("Honduras", agent.CitizenRoleHonduras);
			translations.put("Timor Leste", agent.CitizenRoleTimorLeste);
			translations.put("Lebanon", agent.CitizenRoleLebanon);
			translations.put("Luxembourg", agent.CitizenRoleLuxembourg);
			translations.put("Saudi Arabia", agent.CitizenRoleSaudiArabia);
			translations.put("Myanmar", agent.CitizenRoleMyanmar);
			translations.put("Kyrgyzstan", agent.CitizenRoleKyrgyzstan);
			translations.put("Monaco", agent.CitizenRoleMonaco);
			translations.put("China", agent.CitizenRoleChina);
			translations.put("Ukraine", agent.CitizenRoleUkraine);
			translations.put("Togo", agent.CitizenRoleTogo);
			translations.put("Mauritania", agent.CitizenRoleMauritania);
			translations.put("Sudan", agent.CitizenRoleSudan);
			translations.put("Ghana", agent.CitizenRoleGhana);
			translations.put("Switzerland", agent.CitizenRoleSwitzerland);
			translations.put("Ireland", agent.CitizenRoleIreland);
			translations.put("Portugal", agent.CitizenRolePortugal);
			translations.put("Peru", agent.CitizenRolePeru);
			translations.put("Montenegro", agent.CitizenRoleMontenegro);
			translations.put("Serbia And Montenegro", agent.CitizenRoleSerbiaAndMontenegro);
			translations.put("SanMarino", agent.CitizenRoleSanMarino);
			translations.put("Marshall Islands", agent.CitizenRoleMarshallIslands);
			translations.put("South Africa", agent.CitizenRoleSouthAfrica);
			translations.put("Liberia", agent.CitizenRoleLiberia);
			translations.put("Cape Verde", agent.CitizenRoleCapeVerde);
			translations.put("Denmark", agent.CitizenRoleDenmark);
			translations.put("Mexico", agent.CitizenRoleMexico);
			translations.put("Rwanda", agent.CitizenRoleRwanda);
			translations.put("Benin", agent.CitizenRoleBenin);
			translations.put("France", agent.CitizenRoleFrance);
			translations.put("Fiji", agent.CitizenRoleFiji);
			translations.put("Guatemala", agent.CitizenRoleGuatemala);
			translations.put("Zambia", agent.CitizenRoleZambia);
			translations.put("Czech Republic", agent.CitizenRoleCzechRepublic);
			translations.put("Latvia", agent.CitizenRoleLatvia);
			translations.put("Former Yugoslav Republic Of Macedonia", agent.CitizenRoleFormerYugoslavRepublicOfMacedonia);
			translations.put("Chad", agent.CitizenRoleChad);
			translations.put("Brunei Darussalam", agent.CitizenRoleBruneiDarussalam);
			translations.put("Republic Of Moldova", agent.CitizenRoleRepublicOfMoldova);
			translations.put("Serbia", agent.CitizenRoleSerbia);
			translations.put("Vanuatu", agent.CitizenRoleVanuatu);
			translations.put("Lithuania", agent.CitizenRoleLithuania);
			translations.put("Bahrain", agent.CitizenRoleBahrain);
			translations.put("Eritrea", agent.CitizenRoleEritrea);
			translations.put("United States", agent.CitizenRoleUnitedStates);
			translations.put("Congo", agent.CitizenRoleDemocraticRepublicOfTheCongo);
			translations.put("Uganda", agent.CitizenRoleUganda);
			translations.put("Bosnia", agent.CitizenRoleBosniaAndHerzegovina);
			translations.put("Lao Peoples Democratic Republic", agent.CitizenRoleLaoPeoplesDemocraticRepublic);
			translations.put("India", agent.CitizenRoleIndia);
			translations.put("SierraLeone", agent.CitizenRoleSierraLeone);
			translations.put("Malta", agent.CitizenRoleMalta);
			translations.put("Nigeria", agent.CitizenRoleNigeria);
			translations.put("Uruguay", agent.CitizenRoleUruguay);
			translations.put("Liechtenstein", agent.CitizenRoleLiechtenstein);
			translations.put("Republic Of Korea", agent.CitizenRoleRepublicOfKorea);
			translations.put("Lesotho", agent.CitizenRoleLesotho);
			translations.put("CostaRica", agent.CitizenRoleCostaRica);
			translations.put("Senegal", agent.CitizenRoleSenegal);
			translations.put("Tunisia", agent.CitizenRoleTunisia);
			translations.put("Romania", agent.CitizenRoleRomania);
			translations.put("Madagascar", agent.CitizenRoleMadagascar);
			translations.put("Jordan", agent.CitizenRoleJordan);
			translations.put("Algeria", agent.CitizenRoleAlgeria);
			translations.put("Chile", agent.CitizenRoleChile);
			translations.put("Kiribati", agent.CitizenRoleKiribati);
			translations.put("Iraq", agent.CitizenRoleIraq);
			translations.put("Ecuador", agent.CitizenRoleEcuador);
			translations.put("Syria", agent.CitizenRoleSyrianArabRepublic);
			translations.put("Sri Lanka", agent.CitizenRoleSriLanka);
			translations.put("Kazakhstan", agent.CitizenRoleKazakhstan);
			translations.put("Netherlands", agent.CitizenRoleNetherlands);
			translations.put("Swaziland", agent.CitizenRoleSwaziland);
			translations.put("Spain", agent.CitizenRoleSpain);
			translations.put("Angola", agent.CitizenRoleAngola);
			translations.put("Dominica", agent.CitizenRoleDominica);
			translations.put("Norway", agent.CitizenRoleNorway);
			translations.put("Tanzania", agent.CitizenRoleUnitedRepublicOfTanzania);
			translations.put("Micronesia", agent.CitizenRoleFederatedStatesOfMicronesia);
			translations.put("Grenada", agent.CitizenRoleGrenada);
			translations.put("Cameroon", agent.CitizenRoleCameroon);
			translations.put("Estonia", agent.CitizenRoleEstonia);
			translations.put("Italy", agent.CitizenRoleItaly);
			translations.put("Nepal", agent.CitizenRoleNepal);
			translations.put("Bangladesh", agent.CitizenRoleBangladesh);
			translations.put("Comoros", agent.CitizenRoleComoros);
			translations.put("Cook Islands", agent.CitizenRoleCookIslands);
			translations.put("Armenia", agent.CitizenRoleArmenia);
			translations.put("Belgium", agent.CitizenRoleBelgium);
			translations.put("Solomon Islands", agent.CitizenRoleSolomonIslands);
			translations.put("Gambia", agent.CitizenRoleGambia);
			translations.put("Barbados", agent.CitizenRoleBarbados);
			translations.put("Maldives", agent.CitizenRoleMaldives);
			translations.put("Brazil", agent.CitizenRoleBrazil);
			translations.put("New Zealand", agent.CitizenRoleNewZealand);
			translations.put("Seychelles", agent.CitizenRoleSeychelles);
			translations.put("Korea", agent.CitizenRoleDemocraticPeoplesRepublicOfKorea);
			translations.put("Belize", agent.CitizenRoleBelize);
			translations.put("Mali", agent.CitizenRoleMali);
			translations.put("Samoa", agent.CitizenRoleSamoa);
			translations.put("Bolivia", agent.CitizenRoleBolivia);
			translations.put("Venezuela", agent.CitizenRoleVenezuela);
			translations.put("Japan", agent.CitizenRoleJapan);
			translations.put("Jamaica", agent.CitizenRoleJamaica);
			translations.put("Namibia", agent.CitizenRoleNamibia);
			translations.put("Thailand", agent.CitizenRoleThailand);
			translations.put("Uzbekistan", agent.CitizenRoleUzbekistan);
			translations.put("Morocco", agent.CitizenRoleMorocco);
			translations.put("Australia", agent.CitizenRoleAustralia);
			translations.put("Niger", agent.CitizenRoleNiger);
			translations.put("El Salvador", agent.CitizenRoleElSalvador);
			translations.put("Haiti", agent.CitizenRoleHaiti);
			translations.put("Poland", agent.CitizenRolePoland);
			translations.put("Cyprus", agent.CitizenRoleCyprus);
			translations.put("Israel", agent.CitizenRoleIsrael);
			translations.put("Papua New Guinea", agent.CitizenRolePapuaNewGuinea);
			translations.put("Croatia", agent.CitizenRoleCroatia);
			translations.put("Philippines", agent.CitizenRolePhilippines);
			translations.put("Oman", agent.CitizenRoleOman);
			translations.put("Kenya", agent.CitizenRoleKenya);
			translations.put("Sao Tome And Principe", agent.CitizenRoleSaoTomeAndPrincipe);
			translations.put("Argentina", agent.CitizenRoleArgentina);
			translations.put("Burundi", agent.CitizenRoleBurundi);
			translations.put("Bahamas", agent.CitizenRoleBahamas);
			translations.put("Botswana", agent.CitizenRoleBotswana);
			translations.put("Pakistan", agent.CitizenRolePakistan);
			translations.put("Panama", agent.CitizenRolePanama);
			translations.put("Kuwait", agent.CitizenRoleKuwait);
			translations.put("Viet Nam", agent.CitizenRoleVietNam);
			translations.put("Yemen", agent.CitizenRoleYemen);
			translations.put("Indonesia", agent.CitizenRoleIndonesia);
			translations.put("Bhutan", agent.CitizenRoleBhutan);
			translations.put("Austria", agent.CitizenRoleAustria);
			translations.put("Suriname", agent.CitizenRoleSuriname);
			translations.put("Canada", agent.CitizenRoleCanada);
			translations.put("Cuba", agent.CitizenRoleCuba);
			translations.put("Qatar", agent.CitizenRoleQatar);
			translations.put("Sweden", agent.CitizenRoleSweden);
			translations.put("Russian", agent.CitizenRoleRussianFederation);
			translations.put("Mongolia", agent.CitizenRoleMongolia);
			translations.put("Turkey", agent.CitizenRoleTurkey);
			translations.put("Cote DIvoire", agent.CitizenRoleCoteDIvoire);
			translations.put("Slovakia", agent.CitizenRoleSlovakia);
			translations.put("Ethiopia", agent.CitizenRoleEthiopia);
			translations.put("Bulgaria", agent.CitizenRoleBulgaria);
			translations.put("Malawi", agent.CitizenRoleMalawi);
			translations.put("Guinea Bissau", agent.CitizenRoleGuineaBissau);
			translations.put("Somalia", agent.CitizenRoleSomalia);
			citizenshipTranslationTable = createTranslationTable(translations, "CitizenshipRole");
		}
		return citizenshipTranslationTable;
	}

	private Resource eventTypeTranslationTable;

	private Resource eventTranslationTable()
	{
		if (eventTypeTranslationTable == null)
		{
			Map<String, Resource> translations = new HashMap<>();
			translations.put("firearm", event.ActOfAttack);
			translations.put("small arm", event.ActOfAttack);
			translations.put("explosi", event.ActOfExplosiveDeviceUse);
			translations.put("landmine", event.ActOfExplosiveDeviceUse);
			translations.put("vbied", event.ActOfVehicleUse);
			translations.put("vehicle", event.ActOfVehicleUse);
			translations.put("vandal", event.ActOfVandalism);
			translations.put("mortar", event.ActOfIndirectFire);
			translations.put("artillery", event.ActOfIndirectFire);
			translations.put("rpg", event.ActOfExplosiveDeviceUse);
			translations.put("fire", event.ActOfArson);
			translations.put("bomb", event.ActOfExplosiveDeviceUse);
			translations.put("grenade", event.ActOfExplosiveDeviceUse);
			translations.put("kidnap", event.ActOfKidnapping);
			translations.put("abduct", event.ActOfKidnapping);
			translations.put("assault", event.ActOfAssault);
			translations.put("arson", event.ActOfArson);
			translations.put("hazel", quality.Hazel);
			eventTypeTranslationTable = createTranslationTable(translations, "EventType");
		}
		return eventTypeTranslationTable;
	}

	private Resource createTranslationTable(Map<String,Resource> translations, String translationTableName ){
		Resource translationTable = d2rqMapping.createResource(baseUri+translationTableName+"_"+d2rq.TranslationTable.getLocalName(),d2rq.TranslationTable);
		Iterator<Map.Entry<String,Resource>> it = translations.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String,Resource> e = it.next();
			Resource translation = d2rqMapping.createResource();
			translation.addProperty(d2rq.databaseValue, e.getKey());
			translation.addProperty(d2rq.rdfValue,e.getValue());
			translationTable.addProperty(d2rq.translation,translation);
		}
		return translationTable;
	}
}
