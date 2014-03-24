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

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;


@SuppressWarnings("ALL")
public class xo
{
	private static final String PREFIX = "xo";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.cubrc.org/ontologies/KDD/Mid/Extended#");
	}

	public static String getNamespace()
	{
		return "http://www.cubrc.org/ontologies/KDD/Mid/Extended#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource Abbreviation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Abbreviation");
	public static final Resource ActOfAggregating = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfAggregating");
	public static final Resource ActOfAggregatingArtifacts = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfAggregatingArtifacts");
	public static final Resource ActOfAmalgamating = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfAmalgamating");
	public static final Resource ActOfArtifactMovement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfArtifactMovement");
	public static final Resource ActOfAssistance = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfAssistance");
	public static final Resource ActOfAssuring = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfAssuring");
	public static final Resource ActOfAvoiding = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfAvoiding");
	public static final Resource ActOfBuilding = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfBuilding");
	public static final Resource ActOfBusiness = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfBusiness");
	public static final Resource ActOfCheating = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfCheating");
	public static final Resource ActOfCivilDisturbance = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfCivilDisturbance");
	public static final Resource ActOfCollaboration = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfCollaboration");
	public static final Resource ActOfCombining = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfCombining");
	public static final Resource ActOfComputing = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfComputing");
	public static final Resource ActOfConcealment = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfConcealment");
	public static final Resource ActOfCreation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfCreation");
	public static final Resource ActOfCreativeCommunication = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfCreativeCommunication");
	public static final Resource ActOfDentalHealth = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfDentalHealth");
	public static final Resource ActOfDisappearance = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfDisappearance");
	public static final Resource ActOfDisassembling = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfDisassembling");
	public static final Resource ActOfDiscovery = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfDiscovery");
	public static final Resource ActOfDressing = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfDressing");
	public static final Resource ActOfEating = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfEating");
	public static final Resource ActOfEmission = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfEmission");
	public static final Resource ActOfEnergyEmission = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfEnergyEmission");
	public static final Resource ActOfEngagingServices = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfEngagingServices");
	public static final Resource ActOfEnticement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfEnticement");
	public static final Resource ActOfExertingForce = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfExertingForce");
	public static final Resource ActOfFinancialMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfFinancialMeasurement");
	public static final Resource ActOfFlossing = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfFlossing");
	public static final Resource ActOfFoodPreparation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfFoodPreparation");
	public static final Resource ActOfFormalDressing = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfFormalDressing");
	public static final Resource ActOfGiving = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfGiving");
	public static final Resource ActOfGrooming = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfGrooming");
	public static final Resource ActOfGrowing = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfGrowing");
	public static final Resource ActOfHeatEmission = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfHeatEmission");
	public static final Resource ActOfHoldingTemporaryPossession = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfHoldingTemporaryPossession");
	public static final Resource ActOfIngestion = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfIngestion");
	public static final Resource ActOfIntimidation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfIntimidation");
	public static final Resource ActOfLightEmission = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfLightEmission");
	public static final Resource ActOfMaintainingHealth = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfMaintainingHealth");
	public static final Resource ActOfMakingAHole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfMakingAHole");
	public static final Resource ActOfMassMurder = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfMassMurder");
	public static final Resource ActOfMeasurement = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfMeasurement");
	public static final Resource ActOfMotorVehicleCrime = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfMotorVehicleCrime");
	public static final Resource ActOfNuclearEnergyEmission = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfNuclearEnergyEmission");
	public static final Resource ActOfObserving = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfObserving");
	public static final Resource ActOfPerception = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfPerception");
	public static final Resource ActOfPersonalHealth = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfPersonalHealth");
	public static final Resource ActOfPoison = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfPoison");
	public static final Resource ActOfPreparation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfPreparation");
	public static final Resource ActOfPresentation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfPresentation");
	public static final Resource ActOfReceiving = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfReceiving");
	public static final Resource ActOfRummage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfRummage");
	public static final Resource ActOfSearching = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfSearching");
	public static final Resource ActOfSeizing = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfSeizing");
	public static final Resource ActOfSmellEmission = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfSmellEmission");
	public static final Resource ActOfSoundEmission = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfSoundEmission");
	public static final Resource ActOfSplitting = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfSplitting");
	public static final Resource ActOfStalking = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfStalking");
	public static final Resource ActOfStealing = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfStealing");
	public static final Resource ActOfStriking = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfStriking");
	public static final Resource ActOfThreatening = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfThreatening");
	public static final Resource ActOfThrowing = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfThrowing");
	public static final Resource ActOfTouching = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfTouching");
	public static final Resource ActOfTransfer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfTransfer");
	public static final Resource ActOfTransformation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfTransformation");
	public static final Resource ActOfTranslation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfTranslation");
	public static final Resource ActOfVerifyingCommunication = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfVerifyingCommunication");
	public static final Resource ActOfWar = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ActOfWar");
	public static final Resource AdministractiveUnitOfGovernmentAgency = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#AdministractiveUnitOfGovernmentAgency");
	public static final Resource Admire = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Admire");
	public static final Resource AirCombat = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#AirCombat");
	public static final Resource AirConditioner = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#AirConditioner");
	public static final Resource AirSpace = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#AirSpace");
	public static final Resource Alloy = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Alloy");
	public static final Resource Amuse = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Amuse");
	public static final Resource ArchaeologistRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ArchaeologistRole");
	public static final Resource ArchitectRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ArchitectRole");
	public static final Resource ArmedCombat = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ArmedCombat");
	public static final Resource AssassinRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#AssassinRole");
	public static final Resource Assessment = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Assessment");
	public static final Resource AssistantRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#AssistantRole");
	public static final Resource Biography = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Biography");
	public static final Resource Blueprint = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Blueprint");
	public static final Resource Bow = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Bow");
	public static final Resource Breathe = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Breathe");
	public static final Resource Bridge = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Bridge");
	public static final Resource Caliber = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Caliber");
	public static final Resource Camp = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Camp");
	public static final Resource Car = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Car");
	public static final Resource Chemical = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Chemical");
	public static final Resource CitizenOfContinentAfrica = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CitizenOfContinentAfrica");
	public static final Resource CitizenOfContinentAsia = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CitizenOfContinentAsia");
	public static final Resource CitizenOfContinentAustralia = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CitizenOfContinentAustralia");
	public static final Resource CitizenOfContinentEurope = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CitizenOfContinentEurope");
	public static final Resource CitizenOfContinentNorthAmerica = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CitizenOfContinentNorthAmerica");
	public static final Resource CitizenOfContinentSouthAmerica = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CitizenOfContinentSouthAmerica");
	public static final Resource CitizenOfKoreanPeninsula = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CitizenOfKoreanPeninsula");
	public static final Resource Colony = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Colony");
	public static final Resource CommercialFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CommercialFacility");
	public static final Resource Computer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Computer");
	public static final Resource ComputerNetwork = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ComputerNetwork");
	public static final Resource ComputingDevice = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ComputingDevice");
	public static final Resource Concrete = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Concrete");
	public static final Resource ConcreteCover = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ConcreteCover");
	public static final Resource Consumer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Consumer");
	public static final Resource Copyright = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Copyright");
	public static final Resource Crane = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Crane");
	public static final Resource CriminalActAgainstCyber = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CriminalActAgainstCyber");
	public static final Resource CriminalActAgainstTheMilitary = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CriminalActAgainstTheMilitary");
	public static final Resource CriminalActAgainstTheOrganization = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CriminalActAgainstTheOrganization");
	public static final Resource CriminalActAgainstTheReligion = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CriminalActAgainstTheReligion");
	public static final Resource CriminalActAgainstTheState = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CriminalActAgainstTheState");
	public static final Resource CryptographicInformation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CryptographicInformation");
	public static final Resource CurriculumOfAerospaceEngineering = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfAerospaceEngineering");
	public static final Resource CurriculumOfAudioEngineering = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfAudioEngineering");
	public static final Resource CurriculumOfAutomotiveEngineering = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfAutomotiveEngineering");
	public static final Resource CurriculumOfBioEngineering = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfBioEngineering");
	public static final Resource CurriculumOfCeramicEngineering = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfCeramicEngineering");
	public static final Resource CurriculumOfChemicalEngineering = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfChemicalEngineering");
	public static final Resource CurriculumOfCivilEngineering = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfCivilEngineering");
	public static final Resource CurriculumOfComputerEngineering = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfComputerEngineering");
	public static final Resource CurriculumOfElectricalEngineering = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfElectricalEngineering");
	public static final Resource CurriculumOfEnergyEngineering = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfEnergyEngineering");
	public static final Resource CurriculumOfEnvironmentalEngineering = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfEnvironmentalEngineering");
	public static final Resource CurriculumOfGeneticEngineering = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfGeneticEngineering");
	public static final Resource CurriculumOfIndustrialEngineering = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfIndustrialEngineering");
	public static final Resource CurriculumOfMarineEngineering = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfMarineEngineering");
	public static final Resource CurriculumOfMechanicalEngineering = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfMechanicalEngineering");
	public static final Resource CurriculumOfMiningEngineering = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfMiningEngineering");
	public static final Resource CurriculumOfOperationsResearch = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfOperationsResearch");
	public static final Resource CurriculumOfPoliticalEngineering = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfPoliticalEngineering");
	public static final Resource CurriculumOfSystemsEngineering = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CurriculumOfSystemsEngineering");
	public static final Resource Curtsey = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Curtsey");
	public static final Resource CyberAttack = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CyberAttack");
	public static final Resource CyberCombat = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CyberCombat");
	public static final Resource CyberFraud = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CyberFraud");
	public static final Resource CyberPiracy = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#CyberPiracy");
	public static final Resource Darkness = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Darkness");
	public static final Resource DataAnalystRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#DataAnalystRole");
	public static final Resource DesignAgentRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#DesignAgentRole");
	public static final Resource DesignSchema = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#DesignSchema");
	public static final Resource DetachedRestroom = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#DetachedRestroom");
	public static final Resource Detainee = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Detainee");
	public static final Resource DirectionByRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#DirectionByRole");
	public static final Resource EducationDegree = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#EducationDegree");
	public static final Resource Engine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Engine");
	public static final Resource Estate = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Estate");
	public static final Resource FarmingRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#FarmingRole");
	public static final Resource FineArtByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#FineArtByFunction");
	public static final Resource Flinch = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Flinch");
	public static final Resource GeographicSpace = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#GeographicSpace");
	public static final Resource Gesture = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Gesture");
	public static final Resource GroundCombat = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#GroundCombat");
	public static final Resource GroupOfArtifacts = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#GroupOfArtifacts");
	public static final Resource GroupOfCountries = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#GroupOfCountries");
	public static final Resource GroupOfReligiousPersons = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#GroupOfReligiousPersons");
	public static final Resource GroupOfTerrorists = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#GroupOfTerrorists");
	public static final Resource Hiccup = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Hiccup");
	public static final Resource Highland = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Highland");
	public static final Resource HistoricalAgentRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#HistoricalAgentRole");
	public static final Resource HolidayParticipation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#HolidayParticipation");
	public static final Resource HydroElectricPowerPlant = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#HydroElectricPowerPlant");
	public static final Resource Ideology = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Ideology");
	public static final Resource InformationByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#InformationByFunction");
	public static final Resource InformationQuality = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#InformationQuality");
	public static final Resource InformationSummary = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#InformationSummary");
	public static final Resource Isle = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Isle");
	public static final Resource Isthmus = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Isthmus");
	public static final Resource Judging = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Judging");
	public static final Resource Jungle = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Jungle");
	public static final Resource Lowland = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Lowland");
	public static final Resource MapByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#MapByFunction");
	public static final Resource Market = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Market");
	public static final Resource Marvel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Marvel");
	public static final Resource MaterialArtifactByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#MaterialArtifactByFunction");
	public static final Resource MeasurementTool = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#MeasurementTool");
	public static final Resource Metal = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Metal");
	public static final Resource MilitaryResidenceFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#MilitaryResidenceFacility");
	public static final Resource Missile = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Missile");
	public static final Resource NavalCombat = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#NavalCombat");
	public static final Resource Noise = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Noise");
	public static final Resource NumericalQuantity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#NumericalQuantity");
	public static final Resource OuterSpace = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#OuterSpace");
	public static final Resource OuterSpaceCombat = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#OuterSpaceCombat");
	public static final Resource Pain = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Pain");
	public static final Resource Path = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Path");
	public static final Resource Peninsula = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Peninsula");
	public static final Resource PhysicalOccurrent = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#PhysicalOccurrent");
	public static final Resource PhysicalProcess = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#PhysicalProcess");
	public static final Resource Plain = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Plain");
	public static final Resource Plant = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Plant");
	public static final Resource Plateau = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Plateau");
	public static final Resource Pond = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Pond");
	public static final Resource PowerFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#PowerFacility");
	public static final Resource ProjectileArtifactByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ProjectileArtifactByFunction");
	public static final Resource QualityOfTheEnvironment = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#QualityOfTheEnvironment");
	public static final Resource RailroadFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#RailroadFacility");
	public static final Resource Representative = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Representative");
	public static final Resource Republic = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Republic");
	public static final Resource Resevoir = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Resevoir");
	public static final Resource Ruler = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Ruler");
	public static final Resource ShipPilotRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#ShipPilotRole");
	public static final Resource Sign = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Sign");
	public static final Resource Sleep = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Sleep");
	public static final Resource SocialIntroduction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#SocialIntroduction");
	public static final Resource Spacecraft = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Spacecraft");
	public static final Resource Speak = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Speak");
	public static final Resource StopProcess = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#StopProcess");
	public static final Resource Suffocate = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Suffocate");
	public static final Resource SurfaceGroundSpace = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#SurfaceGroundSpace");
	public static final Resource Taxi = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Taxi");
	public static final Resource Television = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Television");
	public static final Resource Territory = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Territory");
	public static final Resource Tower = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Tower");
	public static final Resource Town = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Town");
	public static final Resource Trader = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Trader");
	public static final Resource TransportationFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#TransportationFacility");
	public static final Resource Tribe = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Tribe");
	public static final Resource UndergroundSpace = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#UndergroundSpace");
	public static final Resource Wadi = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Wadi");
	public static final Resource WasteFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#WasteFacility");
	public static final Resource WeaponOfMassDestruction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#WeaponOfMassDestruction");
	public static final Resource Wink = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/Extended#Wink");
}

