/*
 * Copyright (c) 2014 CUBRC, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *               http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package alignment.karma.mapping.translation.ontologies.kdd;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;


@SuppressWarnings("ALL")
public class ito
{
	private static final String PREFIX = "ito";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#");
	}

	public static String getNamespace()
	{
		return "http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource ASCIIEncoding = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ASCIIEncoding");
	public static final Resource ASCIIEncodingNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ASCIIEncodingNominalMeasurement");
	public static final Resource ASCIIEncodingNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ASCIIEncodingNominalMeasurementBearer");
	public static final Resource ActOfPublication = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ActOfPublication");
	public static final Resource ApplicationSoftware = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ApplicationSoftware");
	public static final Resource ArticleNumber = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ArticleNumber");
	public static final Resource ArticleNumberBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ArticleNumberBearer");
	public static final Resource ArticleOrder = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ArticleOrder");
	public static final Resource Assertion = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Assertion");
	public static final Resource BillOfLadingTypeCodeBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#BillOfLadingTypeCodeBearer");
	public static final Resource BinaryType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#BinaryType");
	public static final Resource BinaryTypeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#BinaryTypeNominalMeasurement");
	public static final Resource BinaryTypeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#BinaryTypeNominalMeasurementBearer");
	public static final Resource BooleanType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#BooleanType");
	public static final Resource BooleanTypeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#BooleanTypeNominalMeasurement");
	public static final Resource BooleanTypeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#BooleanTypeNominalMeasurementBearer");
	public static final Resource Byline = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Byline");
	public static final Resource ByteType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ByteType");
	public static final Resource ByteTypeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ByteTypeNominalMeasurement");
	public static final Resource ByteTypeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ByteTypeNominalMeasurementBearer");
	public static final Resource CalculatedDataColumn = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#CalculatedDataColumn");
	public static final Resource CargoTypeCodeBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#CargoTypeCodeBearer");
	public static final Resource CentralTendency = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#CentralTendency");
	public static final Resource CentralTendencyRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#CentralTendencyRatioMeasurement");
	public static final Resource CentralTendencyRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#CentralTendencyRatioMeasurementBearer");
	public static final Resource CharacterEncoding = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#CharacterEncoding");
	public static final Resource CharacterEncodingNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#CharacterEncodingNominalMeasurement");
	public static final Resource CharacterEncodingNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#CharacterEncodingNominalMeasurementBearer");
	public static final Resource ClassificationLevel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ClassificationLevel");
	public static final Resource ClassificationLevelCode = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ClassificationLevelCode");
	public static final Resource ClassificationLevelCodeBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ClassificationLevelCodeBearer");
	public static final Resource CodeList = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#CodeList");
	public static final Resource CollaborativeSoftware = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#CollaborativeSoftware");
	public static final Resource ComponentCount = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ComponentCount");
	public static final Resource ComponentCountRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ComponentCountRatioMeasurement");
	public static final Resource ContainerTypeCodeBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ContainerTypeCodeBearer");
	public static final Resource DataColumn = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataColumn");
	public static final Resource DataColumnCount = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataColumnCount");
	public static final Resource DataColumnCountRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataColumnCountRatioMeasurement");
	public static final Resource DataColumnCountRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataColumnCountRatioMeasurementBearer");
	public static final Resource DataColumnName = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataColumnName");
	public static final Resource DataColumnNameBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataColumnNameBearer");
	public static final Resource DataElement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataElement");
	public static final Resource DataElementFormat = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataElementFormat");
	public static final Resource DataElementFormatNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataElementFormatNominalMeasurement");
	public static final Resource DataElementFormatNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataElementFormatNominalMeasurementBearer");
	public static final Resource DataElementLength = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataElementLength");
	public static final Resource DataElementLengthMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataElementLengthMeasurementBearer");
	public static final Resource DataElementLengthNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataElementLengthNominalMeasurement");
	public static final Resource DataElementLengthNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataElementLengthNominalMeasurementBearer");
	public static final Resource DataElementLengthRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataElementLengthRatioMeasurement");
	public static final Resource DataElementLengthRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataElementLengthRatioMeasurementBearer");
	public static final Resource DataElementName = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataElementName");
	public static final Resource DataElementNameBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataElementNameBearer");
	public static final Resource DataElementQuality = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataElementQuality");
	public static final Resource DataElementType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataElementType");
	public static final Resource DataElementTypeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataElementTypeNominalMeasurement");
	public static final Resource DataElementTypeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataElementTypeNominalMeasurementBearer");
	public static final Resource DataFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataFunction");
	public static final Resource DataManagementSoftware = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataManagementSoftware");
	public static final Resource DataMapping = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataMapping");
	public static final Resource DataMappingName = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataMappingName");
	public static final Resource DataMappingNameBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataMappingNameBearer");
	public static final Resource DataMappingRule = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataMappingRule");
	public static final Resource DataMappingRuleName = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataMappingRuleName");
	public static final Resource DataMappingRuleNameBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataMappingRuleNameBearer");
	public static final Resource DataMappingSoftware = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataMappingSoftware");
	public static final Resource DataModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataModel");
	public static final Resource DataPrecision = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataPrecision");
	public static final Resource DataPrecisionRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataPrecisionRatioMeasurement");
	public static final Resource DataPrecisionRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataPrecisionRatioMeasurementBearer");
	public static final Resource DataRow = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataRow");
	public static final Resource DataRowCount = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataRowCount");
	public static final Resource DataRowCountRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataRowCountRatioMeasurement");
	public static final Resource DataRowCountRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataRowCountRatioMeasurementBearer");
	public static final Resource DataRowName = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataRowName");
	public static final Resource DataRowNameBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataRowNameBearer");
	public static final Resource DataScale = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataScale");
	public static final Resource DataScaleRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataScaleRatioMeasurement");
	public static final Resource DataScaleRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataScaleRatioMeasurementBearer");
	public static final Resource DataSet = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataSet");
	public static final Resource DataStructure = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataStructure");
	public static final Resource DataTable = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataTable");
	public static final Resource DataTableName = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataTableName");
	public static final Resource DataTableNameBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataTableNameBearer");
	public static final Resource DataTransformation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataTransformation");
	public static final Resource DataValueFrequencyOfOccurrence = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataValueFrequencyOfOccurrence");
	public static final Resource DataValueOrder = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataValueOrder");
	public static final Resource DataValueOrderOrdinalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataValueOrderOrdinalMeasurement");
	public static final Resource DataValueOrderOrdinalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataValueOrderOrdinalMeasurementBearer");
	public static final Resource DataView = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DataView");
	public static final Resource Database = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Database");
	public static final Resource DatabaseManagementSystemSoftware = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DatabaseManagementSystemSoftware");
	public static final Resource DatabaseName = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DatabaseName");
	public static final Resource DatabaseNameBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DatabaseNameBearer");
	public static final Resource DateTimeOffsetType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DateTimeOffsetType");
	public static final Resource DateTimeOffsetTypeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DateTimeOffsetTypeNominalMeasurement");
	public static final Resource DateTimeOffsetTypeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DateTimeOffsetTypeNominalMeasurementBearer");
	public static final Resource DateTimeType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DateTimeType");
	public static final Resource DateTimeTypeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DateTimeTypeNominalMeasurement");
	public static final Resource DateTimeTypeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DateTimeTypeNominalMeasurementBearer");
	public static final Resource Dateline = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Dateline");
	public static final Resource DecimalType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DecimalType");
	public static final Resource DecimalTypeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DecimalTypeNominalMeasurement");
	public static final Resource DecimalTypeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DecimalTypeNominalMeasurementBearer");
	public static final Resource DetaineeReportTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DetaineeReportTopic");
	public static final Resource Document = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Document");
	public static final Resource DocumentAndMediaExploitationReportTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentAndMediaExploitationReportTopic");
	public static final Resource DocumentBody = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentBody");
	public static final Resource DocumentFooter = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentFooter");
	public static final Resource DocumentHeader = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentHeader");
	public static final Resource DocumentMetadata = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentMetadata");
	public static final Resource DocumentName = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentName");
	public static final Resource DocumentNameBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentNameBearer");
	public static final Resource DocumentPart = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentPart");
	public static final Resource DocumentPartAddressBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentPartAddressBearer");
	public static final Resource DocumentPartAddressNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentPartAddressNominalMeasurement");
	public static final Resource DocumentPartHeading = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentPartHeading");
	public static final Resource DocumentPartHeadingBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentPartHeadingBearer");
	public static final Resource DocumentPartLocation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentPartLocation");
	public static final Resource DocumentPartOrderOfAppearance = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentPartOrderOfAppearance");
	public static final Resource DocumentPartOrderOfAppearanceBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentPartOrderOfAppearanceBearer");
	public static final Resource DocumentPartOrderOfAppearanceOrdinalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentPartOrderOfAppearanceOrdinalMeasurement");
	public static final Resource DocumentPartRelevanceNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentPartRelevanceNominalMeasurement");
	public static final Resource DocumentPartRelevanceNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentPartRelevanceNominalMeasurementBearer");
	public static final Resource DocumentReference = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentReference");
	public static final Resource DocumentationSoftware = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DocumentationSoftware");
	public static final Resource DoubleFloatingPointDecimalType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DoubleFloatingPointDecimalType");
	public static final Resource DoubleFloatingPointDecimalTypeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DoubleFloatingPointDecimalTypeNominalMeasurement");
	public static final Resource DoubleFloatingPointDecimalTypeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#DoubleFloatingPointDecimalTypeNominalMeasurementBearer");
	public static final Resource EBCDICEncoding = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#EBCDICEncoding");
	public static final Resource EBCDICEncodingNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#EBCDICEncodingNominalMeasurement");
	public static final Resource EBCDICEncodingNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#EBCDICEncodingNominalMeasurementBearer");
	public static final Resource EmailSoftware = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#EmailSoftware");
	public static final Resource Endnote = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Endnote");
	public static final Resource ExtensibleMarkupLanguageDocument = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ExtensibleMarkupLanguageDocument");
	public static final Resource ExtensibleMarkupLanguageDocumentName = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ExtensibleMarkupLanguageDocumentName");
	public static final Resource ExtensibleMarkupLanguageDocumentNameBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ExtensibleMarkupLanguageDocumentNameBearer");
	public static final Resource FirstQuartileValue = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#FirstQuartileValue");
	public static final Resource FirstQuartileValueOrdinalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#FirstQuartileValueOrdinalMeasurement");
	public static final Resource FirstQuartileValueOrdinalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#FirstQuartileValueOrdinalMeasurementBearer");
	public static final Resource FixedLength = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#FixedLength");
	public static final Resource FixedLengthDecimalType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#FixedLengthDecimalType");
	public static final Resource FixedLengthDecimalTypeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#FixedLengthDecimalTypeNominalMeasurement");
	public static final Resource FixedLengthDecimalTypeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#FixedLengthDecimalTypeNominalMeasurementBearer");
	public static final Resource FixedLengthNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#FixedLengthNominalMeasurement");
	public static final Resource FixedLengthNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#FixedLengthNominalMeasurementBearer");
	public static final Resource FlatDataModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#FlatDataModel");
	public static final Resource FlatDataStructure = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#FlatDataStructure");
	public static final Resource FloatingPointDecimalType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#FloatingPointDecimalType");
	public static final Resource FloatingPointDecimalTypeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#FloatingPointDecimalTypeNominalMeasurement");
	public static final Resource FloatingPointDecimalTypeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#FloatingPointDecimalTypeNominalMeasurementBearer");
	public static final Resource Folder = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Folder");
	public static final Resource Footnote = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Footnote");
	public static final Resource ForeignKey = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ForeignKey");
	public static final Resource ForeignKeyBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ForeignKeyBearer");
	public static final Resource GloballyUniqueIdentifierType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#GloballyUniqueIdentifierType");
	public static final Resource GloballyUniqueIdentifierTypeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#GloballyUniqueIdentifierTypeNominalMeasurement");
	public static final Resource GloballyUniqueIdentifierTypeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#GloballyUniqueIdentifierTypeNominalMeasurementBearer");
	public static final Resource HierarchicalDataModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#HierarchicalDataModel");
	public static final Resource HierarchicalDataStructure = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#HierarchicalDataStructure");
	public static final Resource HumanIntelligenceReportTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#HumanIntelligenceReportTopic");
	public static final Resource ImageryAnalysisTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ImageryAnalysisTopic");
	public static final Resource InBondEntryTypeCodeBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#InBondEntryTypeCodeBearer");
	public static final Resource IncidentReportTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#IncidentReportTopic");
	public static final Resource InformationBearingEntityConnection = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#InformationBearingEntityConnection");
	public static final Resource InformationBearingEntityConnectionBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#InformationBearingEntityConnectionBearer");
	public static final Resource InformationRetrievalRelevance = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#InformationRetrievalRelevance");
	public static final Resource InformationTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#InformationTopic");
	public static final Resource Integer16BitType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Integer16BitType");
	public static final Resource Integer16BitTypeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Integer16BitTypeNominalMeasurement");
	public static final Resource Integer16BitTypeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Integer16BitTypeNominalMeasurementBearer");
	public static final Resource Integer32BitType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Integer32BitType");
	public static final Resource Integer32BitTypeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Integer32BitTypeNominalMeasurement");
	public static final Resource Integer32BitTypeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Integer32BitTypeNominalMeasurementBearer");
	public static final Resource Integer64BitType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Integer64BitType");
	public static final Resource Integer64BitTypeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Integer64BitTypeNominalMeasurement");
	public static final Resource Integer64BitTypeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Integer64BitTypeNominalMeasurementBearer");
	public static final Resource IntegerType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#IntegerType");
	public static final Resource IntegerTypeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#IntegerTypeNominalMeasurement");
	public static final Resource IntegerTypeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#IntegerTypeNominalMeasurementBearer");
	public static final Resource IntelligenceAnnexTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#IntelligenceAnnexTopic");
	public static final Resource IntelligenceCollectionPlanTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#IntelligenceCollectionPlanTopic");
	public static final Resource IntelligenceEstimateTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#IntelligenceEstimateTopic");
	public static final Resource IntelligenceInformationReport = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#IntelligenceInformationReport");
	public static final Resource IntelligenceReportTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#IntelligenceReportTopic");
	public static final Resource IntelligenceSummaryTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#IntelligenceSummaryTopic");
	public static final Resource InternetRelayChatTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#InternetRelayChatTopic");
	public static final Resource InterquartileRange = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#InterquartileRange");
	public static final Resource InterquartileRangeRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#InterquartileRangeRatioMeasurement");
	public static final Resource InterquartileRangeRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#InterquartileRangeRatioMeasurementBearer");
	public static final Resource Journal = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Journal");
	public static final Resource JournalArticle = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#JournalArticle");
	public static final Resource JournalArticleTitle = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#JournalArticleTitle");
	public static final Resource JournalArticleTitleBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#JournalArticleTitleBearer");
	public static final Resource JournalIssue = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#JournalIssue");
	public static final Resource JournalIssueNumber = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#JournalIssueNumber");
	public static final Resource JournalIssueNumberBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#JournalIssueNumberBearer");
	public static final Resource JournalName = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#JournalName");
	public static final Resource JournalNameBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#JournalNameBearer");
	public static final Resource JournalVolume = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#JournalVolume");
	public static final Resource JournalVolumeNumber = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#JournalVolumeNumber");
	public static final Resource JournalVolumeNumberBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#JournalVolumeNumberBearer");
	public static final Resource Key = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Key");
	public static final Resource KnowledgeRepresentationSoftware = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#KnowledgeRepresentationSoftware");
	public static final Resource LineNumber = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#LineNumber");
	public static final Resource LineNumberBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#LineNumberBearer");
	public static final Resource LineOfText = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#LineOfText");
	public static final Resource LineOrder = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#LineOrder");
	public static final Resource List = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#List");
	public static final Resource ManufacturingCompany = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ManufacturingCompany");
	public static final Resource ManufacturingFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ManufacturingFunction");
	public static final Resource ManufacturingProcess = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ManufacturingProcess");
	public static final Resource MappingRuleInput = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MappingRuleInput");
	public static final Resource MappingRuleOutput = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MappingRuleOutput");
	public static final Resource MaximumLength = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MaximumLength");
	public static final Resource MaximumLengthRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MaximumLengthRatioMeasurement");
	public static final Resource MaximumLengthRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MaximumLengthRatioMeasurementBearer");
	public static final Resource MaximumValue = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MaximumValue");
	public static final Resource MaximumValueOrdinalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MaximumValueOrdinalMeasurement");
	public static final Resource MaximumValueOrdinalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MaximumValueOrdinalMeasurementBearer");
	public static final Resource MeanRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MeanRatioMeasurement");
	public static final Resource MeanRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MeanRatioMeasurementBearer");
	public static final Resource MedianValue = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MedianValue");
	public static final Resource MedianValueOrdinalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MedianValueOrdinalMeasurement");
	public static final Resource MedianValueOrdinalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MedianValueOrdinalMeasurementBearer");
	public static final Resource Message = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Message");
	public static final Resource MessageName = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MessageName");
	public static final Resource MessageNameBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MessageNameBearer");
	public static final Resource MinimumValue = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MinimumValue");
	public static final Resource MinimumValueOrdinalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MinimumValueOrdinalMeasurement");
	public static final Resource MinimumValueOrdinalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MinimumValueOrdinalMeasurementBearer");
	public static final Resource MissionReportTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#MissionReportTopic");
	public static final Resource ModalValue = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ModalValue");
	public static final Resource ModeOfTransportCodeBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ModeOfTransportCodeBearer");
	public static final Resource ModeValueOrdinalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ModeValueOrdinalMeasurement");
	public static final Resource ModeValueOrdinalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ModeValueOrdinalMeasurementBearer");
	public static final Resource NonFixedLength = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#NonFixedLength");
	public static final Resource NonFixedLengthNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#NonFixedLengthNominalMeasurement");
	public static final Resource NonFixedLengthNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#NonFixedLengthNominalMeasurementBearer");
	public static final Resource NonNullable = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#NonNullable");
	public static final Resource NonNullableNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#NonNullableNominalMeasurement");
	public static final Resource NonNullableNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#NonNullableNominalMeasurementBearer");
	public static final Resource Note = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Note");
	public static final Resource NoteNumber = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#NoteNumber");
	public static final Resource NoteNumberBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#NoteNumberBearer");
	public static final Resource NoteOrder = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#NoteOrder");
	public static final Resource Nullability = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Nullability");
	public static final Resource NullabilityNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#NullabilityNominalMeasurement");
	public static final Resource NullabilityNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#NullabilityNominalMeasurementBearer");
	public static final Resource Nullable = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Nullable");
	public static final Resource NullableNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#NullableNominalMeasurement");
	public static final Resource NullableNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#NullableNominalMeasurementBearer");
	public static final Resource Ontology = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Ontology");
	public static final Resource OntologyEditingSoftware = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#OntologyEditingSoftware");
	public static final Resource OntologyName = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#OntologyName");
	public static final Resource OntologyNameBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#OntologyNameBearer");
	public static final Resource OperationalInformationTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#OperationalInformationTopic");
	public static final Resource Page = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Page");
	public static final Resource PageNumber = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#PageNumber");
	public static final Resource PageNumberBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#PageNumberBearer");
	public static final Resource PageOrder = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#PageOrder");
	public static final Resource Paragraph = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Paragraph");
	public static final Resource PassageOfText = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#PassageOfText");
	public static final Resource PersonDemographicsTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#PersonDemographicsTopic");
	public static final Resource PrimaryKey = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#PrimaryKey");
	public static final Resource PrimaryKeyBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#PrimaryKeyBearer");
	public static final Resource ProcessableContentOfText = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ProcessableContentOfText");
	public static final Resource PropagandaTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#PropagandaTopic");
	public static final Resource Property = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Property");
	public static final Resource PublicationDate = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#PublicationDate");
	public static final Resource PublicationDateIdentifier = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#PublicationDateIdentifier");
	public static final Resource PublicationDateIdentifierBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#PublicationDateIdentifierBearer");
	public static final Resource Range = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Range");
	public static final Resource RangeRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#RangeRatioMeasurement");
	public static final Resource RangeRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#RangeRatioMeasurementBearer");
	public static final Resource RelationalDataModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#RelationalDataModel");
	public static final Resource RelationalDataStructure = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#RelationalDataStructure");
	public static final Resource RelationalDatabase = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#RelationalDatabase");
	public static final Resource Report = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Report");
	public static final Resource ReportName = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ReportName");
	public static final Resource ReportNameBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ReportNameBearer");
	public static final Resource SectionOfText = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#SectionOfText");
	public static final Resource Sentence = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Sentence");
	public static final Resource SignalIntelligenceReportTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#SignalIntelligenceReportTopic");
	public static final Resource SingleFloatingPointDecimalType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#SingleFloatingPointDecimalType");
	public static final Resource SingleFloatingPointDecimalTypeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#SingleFloatingPointDecimalTypeNominalMeasurement");
	public static final Resource SingleFloatingPointDecimalTypeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#SingleFloatingPointDecimalTypeNominalMeasurementBearer");
	public static final Resource SituationReportTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#SituationReportTopic");
	public static final Resource Software = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Software");
	public static final Resource SoftwareAndProgrammingCompany = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#SoftwareAndProgrammingCompany");
	public static final Resource SoftwareDevelopmentFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#SoftwareDevelopmentFunction");
	public static final Resource SoftwareDevelopmentProcess = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#SoftwareDevelopmentProcess");
	public static final Resource SoftwareEngineeringSoftware = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#SoftwareEngineeringSoftware");
	public static final Resource SoftwareFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#SoftwareFunction");
	public static final Resource SoftwareProcess = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#SoftwareProcess");
	public static final Resource SourceInformationBearingEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#SourceInformationBearingEntity");
	public static final Resource SourceInformationBearingEntityRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#SourceInformationBearingEntityRole");
	public static final Resource SpotReportTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#SpotReportTopic");
	public static final Resource Spreadsheet = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Spreadsheet");
	public static final Resource SpreadsheetApplicationSoftware = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#SpreadsheetApplicationSoftware");
	public static final Resource SpreadsheetName = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#SpreadsheetName");
	public static final Resource SpreadsheetNameBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#SpreadsheetNameBearer");
	public static final Resource StandardDeviationRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#StandardDeviationRatioMeasurement");
	public static final Resource StandardDeviationRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#StandardDeviationRatioMeasurementBearer");
	public static final Resource StatisticalDispersion = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#StatisticalDispersion");
	public static final Resource StatisticalDispersionRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#StatisticalDispersionRatioMeasurement");
	public static final Resource StatisticalDispersionRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#StatisticalDispersionRatioMeasurementBearer");
	public static final Resource StringType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#StringType");
	public static final Resource StringTypeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#StringTypeNominalMeasurement");
	public static final Resource StringTypeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#StringTypeNominalMeasurementBearer");
	public static final Resource TableOfContents = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#TableOfContents");
	public static final Resource TacticalReportTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#TacticalReportTopic");
	public static final Resource TacticsTrainingProceduresTopic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#TacticsTrainingProceduresTopic");
	public static final Resource TargetInformationBearingEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#TargetInformationBearingEntity");
	public static final Resource TargetInformationBearingEntityRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#TargetInformationBearingEntityRole");
	public static final Resource Tearline = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Tearline");
	public static final Resource TearlineReport = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#TearlineReport");
	public static final Resource TextEditingSoftware = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#TextEditingSoftware");
	public static final Resource ThirdQuartileValue = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ThirdQuartileValue");
	public static final Resource ThirdQuartileValueOrdinalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ThirdQuartileValueOrdinalMeasurement");
	public static final Resource ThirdQuartileValueOrdinalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#ThirdQuartileValueOrdinalMeasurementBearer");
	public static final Resource TimeType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#TimeType");
	public static final Resource TimeTypeNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#TimeTypeNominalMeasurement");
	public static final Resource TimeTypeNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#TimeTypeNominalMeasurementBearer");
	public static final Resource Transcript = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Transcript");
	public static final Resource TranscriptName = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#TranscriptName");
	public static final Resource TranscriptNameBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#TranscriptNameBearer");
	public static final Resource UnicodeEncoding = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#UnicodeEncoding");
	public static final Resource UnicodeEncodingNominalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#UnicodeEncodingNominalMeasurement");
	public static final Resource UnicodeEncodingNominalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#UnicodeEncodingNominalMeasurementBearer");
	public static final Resource Value = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Value");
	public static final Resource Variance = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#Variance");
	public static final Resource VarianceRatioMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#VarianceRatioMeasurement");
	public static final Resource VarianceRatioMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#VarianceRatioMeasurementBearer");
	public static final Resource VersionNumberOrdinalMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#VersionNumberOrdinalMeasurement");
	public static final Resource VersionNumberOrdinalMeasurementBearer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#VersionNumberOrdinalMeasurementBearer");
	public static final Resource WordProcessingSoftware = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#WordProcessingSoftware");

	// DatatypeProperty
	public static final Property has_character_length = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#has_character_length");
	public static final Property has_character_offset = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#has_character_offset");

	// ObjectProperty
	public static final Property has_foreign_key = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#has_foreign_key");
	public static final Property has_primary_key = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#has_primary_key");
	public static final Property is_foreign_key_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#is_foreign_key_of");
	public static final Property is_primary_key_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationTechnologyOntology#is_primary_key_of");
}

