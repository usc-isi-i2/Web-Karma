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
public class info
{
	private static final String PREFIX = "info";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#");
	}

	public static String getNamespace()
	{
		return "http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource Adventists = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Adventists");
	public static final Resource Anabaptists = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Anabaptists");
	public static final Resource Anglicanism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Anglicanism");
	public static final Resource ArtifactModel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#ArtifactModel");
	public static final Resource AssyrianChurchoftheEast = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#AssyrianChurchoftheEast");
	public static final Resource BahaiFaith = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#BahaiFaith");
	public static final Resource Baptists = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Baptists");
	public static final Resource BillOfLadingType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#BillOfLadingType");
	public static final Resource Buddhism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Buddhism");
	public static final Resource CargoMarksAndNumberings = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#CargoMarksAndNumberings");
	public static final Resource CargoType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#CargoType");
	public static final Resource Catholicism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Catholicism");
	public static final Resource Certificate = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Certificate");
	public static final Resource Christianity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Christianity");
	public static final Resource Congregationalist = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Congregationalist");
	public static final Resource ConservativeJudaism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#ConservativeJudaism");
	public static final Resource ContainerLoadNominalMeasurementInformationBearingEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#ContainerLoadNominalMeasurementInformationBearingEntity");
	public static final Resource ContainerLocationNominalMeasurementInformationBearingEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#ContainerLocationNominalMeasurementInformationBearingEntity");
	public static final Resource ContainerType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#ContainerType");
	public static final Resource ContainerTypeDescriptiveInformationBearingEntity_ = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#ContainerTypeDescriptiveInformationBearingEntity_");
	public static final Resource DescriptiveInformationBearingEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#DescriptiveInformationBearingEntity");
	public static final Resource DescriptiveInformationContentEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#DescriptiveInformationContentEntity");
	public static final Resource DesignativeInformationBearingEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#DesignativeInformationBearingEntity");
	public static final Resource DesignativeInformationContentEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#DesignativeInformationContentEntity");
	public static final Resource DirectiveInformationBearingEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#DirectiveInformationBearingEntity");
	public static final Resource DirectiveInformationContentEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#DirectiveInformationContentEntity");
	public static final Resource EasternOrthodox = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#EasternOrthodox");
	public static final Resource Hinduism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Hinduism");
	public static final Resource HolinessMovement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#HolinessMovement");
	public static final Resource Ideology = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Ideology");
	public static final Resource InBondEntryType = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#InBondEntryType");
	public static final Resource Indictment = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Indictment");
	public static final Resource InformationBearingEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#InformationBearingEntity");
	public static final Resource InformationContentEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#InformationContentEntity");
	public static final Resource IntervalMeasurementInformationBearingEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#IntervalMeasurementInformationBearingEntity");
	public static final Resource IntervalMeasurementInformationContentEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#IntervalMeasurementInformationContentEntity");
	public static final Resource Islam = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Islam");
	public static final Resource Ismailiyah = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Ismailiyah");
	public static final Resource IthnaAshariyah = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#IthnaAshariyah");
	public static final Resource Jainism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Jainism");
	public static final Resource Judaism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Judaism");
	public static final Resource Lutheranism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Lutheranism");
	public static final Resource Mahayana = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Mahayana");
	public static final Resource MeasurementInformationBearingEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#MeasurementInformationBearingEntity");
	public static final Resource MeasurementInformationContentEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#MeasurementInformationContentEntity");
	public static final Resource MeasurementUnit = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#MeasurementUnit");
	public static final Resource Mennonites = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Mennonites");
	public static final Resource Methodists = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Methodists");
	public static final Resource ModeOfTransport = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#ModeOfTransport");
	public static final Resource Mormonism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Mormonism");
	public static final Resource NominalMeasurementInformationBearingEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#NominalMeasurementInformationBearingEntity");
	public static final Resource NominalMeasurementInformationContentEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#NominalMeasurementInformationContentEntity");
	public static final Resource Objective = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Objective");
	public static final Resource OrdinalMeasurementInformationBearingEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#OrdinalMeasurementInformationBearingEntity");
	public static final Resource OrdinalMeasurementInformationContentEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#OrdinalMeasurementInformationContentEntity");
	public static final Resource OrientalOrthodox = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#OrientalOrthodox");
	public static final Resource OrthodoxJudaism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#OrthodoxJudaism");
	public static final Resource Pentecostalism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Pentecostalism");
	public static final Resource PlanSpecification = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#PlanSpecification");
	public static final Resource Presbyterianism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Presbyterianism");
	public static final Resource Protestantism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Protestantism");
	public static final Resource RatioMeasurementInformationBearingEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#RatioMeasurementInformationBearingEntity");
	public static final Resource RatioMeasurementInformationContentEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#RatioMeasurementInformationContentEntity");
	public static final Resource ReformJudaism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#ReformJudaism");
	public static final Resource ReformedFaith = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#ReformedFaith");
	public static final Resource Religion = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Religion");
	public static final Resource RepresentationalInformationBearingEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#RepresentationalInformationBearingEntity");
	public static final Resource RepresentationalInformationContentEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#RepresentationalInformationContentEntity");
	public static final Resource RomanCatholic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#RomanCatholic");
	public static final Resource Shaivism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Shaivism");
	public static final Resource Shaktism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Shaktism");
	public static final Resource ShiaIslam = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#ShiaIslam");
	public static final Resource Shinto = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Shinto");
	public static final Resource Sikhism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Sikhism");
	public static final Resource Smartism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Smartism");
	public static final Resource SunniIslam = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#SunniIslam");
	public static final Resource Theravada = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Theravada");
	public static final Resource TibetanBuddhism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#TibetanBuddhism");
	public static final Resource UnitarianUniversalism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#UnitarianUniversalism");
	public static final Resource Vaishnavism = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Vaishnavism");
	public static final Resource Vajrayana = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Vajrayana");
	public static final Resource Zaydiyah = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#Zaydiyah");

	// DatatypeProperty
	public static final Property contains_token_information = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#contains_token_information");
	public static final Property has_URI_value = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#has_URI_value");
	public static final Property has_boolean_value = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#has_boolean_value");
	public static final Property has_date_value = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#has_date_value");
	public static final Property has_datetime_value = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#has_datetime_value");
	public static final Property has_decimal_value = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#has_decimal_value");
	public static final Property has_integer_value = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#has_integer_value");
	public static final Property has_text_value = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#has_text_value");

	// FunctionalProperty
	public static final Property designates = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#designates");
	public static final Property is_a_interval_measurement_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#is_a_interval_measurement_of");
	public static final Property is_a_measurement_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#is_a_measurement_of");
	public static final Property is_a_nominal_measurement_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#is_a_nominal_measurement_of");
	public static final Property is_a_ordinal_measurement_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#is_a_ordinal_measurement_of");
	public static final Property is_a_ratio_measurement_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#is_a_ratio_measurement_of");
	public static final Property represents = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#represents");

	// InverseFunctionalProperty
	public static final Property designated_by = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#designated_by");
	public static final Property is_measured_by = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#is_measured_by");
	public static final Property is_measured_by_interval = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#is_measured_by_interval");
	public static final Property is_measured_by_nominal = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#is_measured_by_nominal");
	public static final Property is_measured_by_ordinal = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#is_measured_by_ordinal");
	public static final Property is_measured_by_ratio = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#is_measured_by_ratio");
	public static final Property represented_by = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#represented_by");

	// ObjectProperty
	public static final Property described_by = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#described_by");
	public static final Property describes = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#describes");
	public static final Property is_about = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#is_about");
	public static final Property is_endophoric_reference_to = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#is_endophoric_reference_to");
	public static final Property is_endophorically_referred_to_by = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#is_endophorically_referred_to_by");
	public static final Property is_excerpted_from = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#is_excerpted_from");
	public static final Property is_subject_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#is_subject_of");
	public static final Property prescribed_by = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#prescribed_by");
	public static final Property prescribes = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#prescribes");
	public static final Property uses_measurement_unit = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/InformationEntityOntology#uses_measurement_unit");
}

