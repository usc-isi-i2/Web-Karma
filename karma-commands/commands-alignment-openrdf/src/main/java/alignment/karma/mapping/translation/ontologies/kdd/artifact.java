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
public class artifact
{
	private static final String PREFIX = "artifact";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#");
	}

	public static String getNamespace()
	{
		return "http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource AgriculturalArtifactRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#AgriculturalArtifactRole");
	public static final Resource AgriculturalFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#AgriculturalFacility");
	public static final Resource Aircraft = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Aircraft");
	public static final Resource AircraftCarrier = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#AircraftCarrier");
	public static final Resource AircraftManufacturingFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#AircraftManufacturingFacility");
	public static final Resource Airliner = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Airliner");
	public static final Resource Airport = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Airport");
	public static final Resource AmmunitionDepot = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#AmmunitionDepot");
	public static final Resource AmphibiousAssaultShip = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#AmphibiousAssaultShip");
	public static final Resource ApartmentBuilding = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ApartmentBuilding");
	public static final Resource ArabicLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ArabicLanguage");
	public static final Resource ArmoredPersonnelCarrier = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ArmoredPersonnelCarrier");
	public static final Resource ArticleOfClothing = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ArticleOfClothing");
	public static final Resource ArticleOfSolidWaste = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ArticleOfSolidWaste");
	public static final Resource ArticleOfWasteMaterial = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ArticleOfWasteMaterial");
	public static final Resource Artifact = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Artifact");
	public static final Resource ArtifactByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ArtifactByFunction");
	public static final Resource ArtifactByRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ArtifactByRole");
	public static final Resource ArtificialEye = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ArtificialEye");
	public static final Resource Artillery = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Artillery");
	public static final Resource AutomaticRifle = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#AutomaticRifle");
	public static final Resource AutomaticWeapon = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#AutomaticWeapon");
	public static final Resource Automobile = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Automobile");
	public static final Resource AzerbajaniLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#AzerbajaniLanguage");
	public static final Resource BalochiLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#BalochiLanguage");
	public static final Resource BankBranchOffice = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#BankBranchOffice");
	public static final Resource Base = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Base");
	public static final Resource BaseOfOperations = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#BaseOfOperations");
	public static final Resource BengaliLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#BengaliLanguage");
	public static final Resource BillOfLading = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#BillOfLading");
	public static final Resource BiologicalWeapon = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#BiologicalWeapon");
	public static final Resource Biological_Depot = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Biological_Depot");
	public static final Resource Bomber = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Bomber");
	public static final Resource Bond = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Bond");
	public static final Resource Bridge = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Bridge");
	public static final Resource Bus = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Bus");
	public static final Resource Canal = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Canal");
	public static final Resource CargoRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#CargoRole");
	public static final Resource Cemetery = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Cemetery");
	public static final Resource ChannelPassLock = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ChannelPassLock");
	public static final Resource ChemicalDepot = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ChemicalDepot");
	public static final Resource ChemicalManufacturingFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ChemicalManufacturingFacility");
	public static final Resource ChemicalWeapon = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ChemicalWeapon");
	public static final Resource Church = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Church");
	public static final Resource CombatOutpost = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#CombatOutpost");
	public static final Resource CommandControlAircraft = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#CommandControlAircraft");
	public static final Resource CommandPostFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#CommandPostFacility");
	public static final Resource CommunicationInstrumentByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#CommunicationInstrumentByFunction");
	public static final Resource CommunicationsTower = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#CommunicationsTower");
	public static final Resource ComponentByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ComponentByFunction");
	public static final Resource ComponentByRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ComponentByRole");
	public static final Resource Consumable = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Consumable");
	public static final Resource ConsumableRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ConsumableRole");
	public static final Resource ContainerByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ContainerByFunction");
	public static final Resource ContainerByRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ContainerByRole");
	public static final Resource Corvette = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Corvette");
	public static final Resource CounterfeitFinancialInstrument = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#CounterfeitFinancialInstrument");
	public static final Resource CounterfeitLegalInstrument = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#CounterfeitLegalInstrument");
	public static final Resource CruiseShip = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#CruiseShip");
	public static final Resource Cruiser = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Cruiser");
	public static final Resource CuttingWeaponByRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#CuttingWeaponByRole");
	public static final Resource Dam = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Dam");
	public static final Resource DecontaminationFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#DecontaminationFacility");
	public static final Resource DecoyByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#DecoyByFunction");
	public static final Resource Denture = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Denture");
	public static final Resource Destroyer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Destroyer");
	public static final Resource DetaineeCollectionPoint = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#DetaineeCollectionPoint");
	public static final Resource DetaineeHoldingArea = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#DetaineeHoldingArea");
	public static final Resource DetonatorByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#DetonatorByFunction");
	public static final Resource DetonatorByRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#DetonatorByRole");
	public static final Resource DinkaLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#DinkaLanguage");
	public static final Resource DistributionPort = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#DistributionPort");
	public static final Resource DogriLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#DogriLanguage");
	public static final Resource DropHouse = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#DropHouse");
	public static final Resource DropHouseRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#DropHouseRole");
	public static final Resource DryBulkCarrier = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#DryBulkCarrier");
	public static final Resource DutchLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#DutchLanguage");
	public static final Resource Dynamite = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Dynamite");
	public static final Resource EducationArtifactRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#EducationArtifactRole");
	public static final Resource EducationalFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#EducationalFacility");
	public static final Resource Engine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Engine");
	public static final Resource EnglishLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#EnglishLanguage");
	public static final Resource EntertainmentFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#EntertainmentFacility");
	public static final Resource EssentialServiceArtifact = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#EssentialServiceArtifact");
	public static final Resource ExplosiveDeviceByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ExplosiveDeviceByFunction");
	public static final Resource ExplosiveDeviceByRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ExplosiveDeviceByRole");
	public static final Resource FacilityByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#FacilityByFunction");
	public static final Resource FacilityByRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#FacilityByRole");
	public static final Resource Farm = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Farm");
	public static final Resource Ferry = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Ferry");
	public static final Resource FighterAircraft = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#FighterAircraft");
	public static final Resource FinancialArtifactRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#FinancialArtifactRole");
	public static final Resource FinancialFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#FinancialFacility");
	public static final Resource FinancialInstrumentByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#FinancialInstrumentByFunction");
	public static final Resource FinancialInstrumentByRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#FinancialInstrumentByRole");
	public static final Resource FireHouse = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#FireHouse");
	public static final Resource Firearm = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Firearm");
	public static final Resource FordConcreteBed = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#FordConcreteBed");
	public static final Resource FordStoneBed = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#FordStoneBed");
	public static final Resource ForeignCargoRemainingOnBoard = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ForeignCargoRemainingOnBoard");
	public static final Resource ForeignCargoRemainingOnBoardRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ForeignCargoRemainingOnBoardRole");
	public static final Resource Fort = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Fort");
	public static final Resource ForwardOperationsBase = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ForwardOperationsBase");
	public static final Resource FossilFuelPowerPlant = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#FossilFuelPowerPlant");
	public static final Resource FrenchLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#FrenchLanguage");
	public static final Resource Frigate = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Frigate");
	public static final Resource FulaLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#FulaLanguage");
	public static final Resource GasProcessingFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#GasProcessingFacility");
	public static final Resource Gate = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Gate");
	public static final Resource GermanLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#GermanLanguage");
	public static final Resource GovernmentArtifactRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#GovernmentArtifactRole");
	public static final Resource GovernmentBuilding = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#GovernmentBuilding");
	public static final Resource GovernmentDocument = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#GovernmentDocument");
	public static final Resource Grenade = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Grenade");
	public static final Resource GroceryStore = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#GroceryStore");
	public static final Resource GroundAttackAircraft = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#GroundAttackAircraft");
	public static final Resource GroundVehicle = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#GroundVehicle");
	public static final Resource GujaratiLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#GujaratiLanguage");
	public static final Resource HandGrenade = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#HandGrenade");
	public static final Resource HandGun = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#HandGun");
	public static final Resource HealthcareArtifactRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#HealthcareArtifactRole");
	public static final Resource HealthcareFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#HealthcareFacility");
	public static final Resource HearingAid = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#HearingAid");
	public static final Resource Helicopter = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Helicopter");
	public static final Resource Heliport = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Heliport");
	public static final Resource Highway = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Highway");
	public static final Resource HighwayInterchange = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#HighwayInterchange");
	public static final Resource HindiLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#HindiLanguage");
	public static final Resource Hospital = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Hospital");
	public static final Resource HospitalityArtifactRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#HospitalityArtifactRole");
	public static final Resource Hostel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Hostel");
	public static final Resource Hotel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Hotel");
	public static final Resource House = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#House");
	public static final Resource Howitzer = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Howitzer");
	public static final Resource HydroelectricPowerPlant = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#HydroelectricPowerPlant");
	public static final Resource IncendiaryWeapon = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#IncendiaryWeapon");
	public static final Resource IndonesianLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#IndonesianLanguage");
	public static final Resource InfantryFightingVehicle = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#InfantryFightingVehicle");
	public static final Resource Infrastructure = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Infrastructure");
	public static final Resource InstrumentByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#InstrumentByFunction");
	public static final Resource InstrumentByRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#InstrumentByRole");
	public static final Resource ItalianLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ItalianLanguage");
	public static final Resource ItemOfCargo = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ItemOfCargo");
	public static final Resource JavaneseLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#JavaneseLanguage");
	public static final Resource KannadaLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#KannadaLanguage");
	public static final Resource KashmiriLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#KashmiriLanguage");
	public static final Resource Knife = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Knife");
	public static final Resource KurdishLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#KurdishLanguage");
	public static final Resource Landfill = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Landfill");
	public static final Resource LandlineTelephone = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#LandlineTelephone");
	public static final Resource Landmine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Landmine");
	public static final Resource Language = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Language");
	public static final Resource LegalArtifactRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#LegalArtifactRole");
	public static final Resource LegalInstrumentByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#LegalInstrumentByFunction");
	public static final Resource LegalInstrumentByRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#LegalInstrumentByRole");
	public static final Resource Lighthouse = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Lighthouse");
	public static final Resource LongGun = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#LongGun");
	public static final Resource MachineGun = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#MachineGun");
	public static final Resource MainExplosiveChargeByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#MainExplosiveChargeByFunction");
	public static final Resource MainExplosiveChargeByRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#MainExplosiveChargeByRole");
	public static final Resource Maintenance_Facility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Maintenance_Facility");
	public static final Resource MalayLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#MalayLanguage");
	public static final Resource MalayalamLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#MalayalamLanguage");
	public static final Resource MandarinLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#MandarinLanguage");
	public static final Resource ManufacturingArtifactRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ManufacturingArtifactRole");
	public static final Resource ManufacturingFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ManufacturingFacility");
	public static final Resource MarathiLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#MarathiLanguage");
	public static final Resource MedicalDepot = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#MedicalDepot");
	public static final Resource MilitaryArtifactRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#MilitaryArtifactRole");
	public static final Resource MilitaryFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#MilitaryFacility");
	public static final Resource MilitaryHeadquartersFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#MilitaryHeadquartersFacility");
	public static final Resource MilitaryTransportAircraft = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#MilitaryTransportAircraft");
	public static final Resource Mine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Mine");
	public static final Resource MissileSite = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#MissileSite");
	public static final Resource MobileTelephone = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#MobileTelephone");
	public static final Resource Money = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Money");
	public static final Resource Monument = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Monument");
	public static final Resource Mortar = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Mortar");
	public static final Resource Mosque = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Mosque");
	public static final Resource Motel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Motel");
	public static final Resource MotorVehicleManufacturingFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#MotorVehicleManufacturingFacility");
	public static final Resource Motorcycle = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Motorcycle");
	public static final Resource MultipurposeVessel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#MultipurposeVessel");
	public static final Resource NuclearFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#NuclearFacility");
	public static final Resource NuclearPowerPlant = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#NuclearPowerPlant");
	public static final Resource NuclearStorageDepot = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#NuclearStorageDepot");
	public static final Resource NuclearWeapon = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#NuclearWeapon");
	public static final Resource OfficeBuilding = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#OfficeBuilding");
	public static final Resource OpenSkyMine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#OpenSkyMine");
	public static final Resource PashtoLangauge = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PashtoLangauge");
	public static final Resource Passport = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Passport");
	public static final Resource PersianLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PersianLanguage");
	public static final Resource PetrochemicalRefinery = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PetrochemicalRefinery");
	public static final Resource PetroleumDepot = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PetroleumDepot");
	public static final Resource PetroleumManufacturingFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PetroleumManufacturingFacility");
	public static final Resource Pier = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Pier");
	public static final Resource Pipeline = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Pipeline");
	public static final Resource PoliceCheckPoint = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PoliceCheckPoint");
	public static final Resource PoliceStation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PoliceStation");
	public static final Resource Port = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Port");
	public static final Resource PortOfEntryRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PortOfEntryRole");
	public static final Resource PortionOfCocaine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PortionOfCocaine");
	public static final Resource PortionOfDrug = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PortionOfDrug");
	public static final Resource PortionOfFood = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PortionOfFood");
	public static final Resource PortionOfIllegalDrug = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PortionOfIllegalDrug");
	public static final Resource PortionOfMarijuana = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PortionOfMarijuana");
	public static final Resource PortionOfPrescriptionDrug = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PortionOfPrescriptionDrug");
	public static final Resource PortugeseLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PortugeseLanguage");
	public static final Resource PowerSourceByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PowerSourceByFunction");
	public static final Resource PowerSourceByRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PowerSourceByRole");
	public static final Resource PowerTransmissionLine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PowerTransmissionLine");
	public static final Resource Prosthesis = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Prosthesis");
	public static final Resource ProstheticArm = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ProstheticArm");
	public static final Resource ProstheticFoot = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ProstheticFoot");
	public static final Resource ProstheticHand = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ProstheticHand");
	public static final Resource ProstheticLeg = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ProstheticLeg");
	public static final Resource PublicSafetyArtifactRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PublicSafetyArtifactRole");
	public static final Resource PublicSafetyFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PublicSafetyFacility");
	public static final Resource PumpingStation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PumpingStation");
	public static final Resource PunjabiLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#PunjabiLanguage");
	public static final Resource QashqaiLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#QashqaiLanguage");
	public static final Resource RadiologicalWeapon = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#RadiologicalWeapon");
	public static final Resource RailFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#RailFacility");
	public static final Resource Railway = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Railway");
	public static final Resource RailwayCrossing = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#RailwayCrossing");
	public static final Resource RailwayJunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#RailwayJunction");
	public static final Resource RajasthaniLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#RajasthaniLanguage");
	public static final Resource RawMaterialExtractionArtifactRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#RawMaterialExtractionArtifactRole");
	public static final Resource ReligiousArtifactRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ReligiousArtifactRole");
	public static final Resource ReligiousFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ReligiousFacility");
	public static final Resource ResearchAndDevelopmentArtifactRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ResearchAndDevelopmentArtifactRole");
	public static final Resource Reservoir = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Reservoir");
	public static final Resource ResidentialArtifactRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ResidentialArtifactRole");
	public static final Resource ResidentialFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ResidentialFacility");
	public static final Resource RetailArtifactRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#RetailArtifactRole");
	public static final Resource RetailFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#RetailFacility");
	public static final Resource Revolver = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Revolver");
	public static final Resource Rifle = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Rifle");
	public static final Resource Road = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Road");
	public static final Resource RoadJunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#RoadJunction");
	public static final Resource RocketPropelledGrenade = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#RocketPropelledGrenade");
	public static final Resource RussianLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#RussianLanguage");
	public static final Resource SaraikiLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#SaraikiLanguage");
	public static final Resource School = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#School");
	public static final Resource SemiAutomaticPistol = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#SemiAutomaticPistol");
	public static final Resource ServiceArtifactRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ServiceArtifactRole");
	public static final Resource SewageTreatmentFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#SewageTreatmentFacility");
	public static final Resource Shop = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Shop");
	public static final Resource Shotgun = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Shotgun");
	public static final Resource SindhiLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#SindhiLanguage");
	public static final Resource SmallArm = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#SmallArm");
	public static final Resource Smokestack = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Smokestack");
	public static final Resource Spacecraft = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Spacecraft");
	public static final Resource SpanishLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#SpanishLanguage");
	public static final Resource Stage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Stage");
	public static final Resource StagingAreaRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#StagingAreaRole");
	public static final Resource Stock = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Stock");
	public static final Resource StorageFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#StorageFacility");
	public static final Resource SubmachineGun = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#SubmachineGun");
	public static final Resource Submarine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Submarine");
	public static final Resource SundaneseLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#SundaneseLanguage");
	public static final Resource SwitchByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#SwitchByFunction");
	public static final Resource SwitchByRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#SwitchByRole");
	public static final Resource SylhetiLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#SylhetiLanguage");
	public static final Resource Synagogue = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Synagogue");
	public static final Resource TagalogLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#TagalogLanguage");
	public static final Resource TamilLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#TamilLanguage");
	public static final Resource Tank = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Tank");
	public static final Resource Tanker = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Tanker");
	public static final Resource TelecommunicationNetwork = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#TelecommunicationNetwork");
	public static final Resource Telephone = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Telephone");
	public static final Resource TeluguLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#TeluguLanguage");
	public static final Resource TerroristTrainingCamp = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#TerroristTrainingCamp");
	public static final Resource ThermalPowerPlant = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ThermalPowerPlant");
	public static final Resource TitleDocument = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#TitleDocument");
	public static final Resource ToolByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ToolByFunction");
	public static final Resource ToolByRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#ToolByRole");
	public static final Resource TownHall = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#TownHall");
	public static final Resource Trail = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Trail");
	public static final Resource Train = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Train");
	public static final Resource TrainingCamp = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#TrainingCamp");
	public static final Resource Truck = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Truck");
	public static final Resource Tunnel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Tunnel");
	public static final Resource TurkishLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#TurkishLanguage");
	public static final Resource TurkmenLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#TurkmenLanguage");
	public static final Resource UndergroundMine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#UndergroundMine");
	public static final Resource UnderwaterMine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#UnderwaterMine");
	public static final Resource UnmannedAerialVehicle = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#UnmannedAerialVehicle");
	public static final Resource UrduLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#UrduLanguage");
	public static final Resource UzbekLanguage = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#UzbekLanguage");
	public static final Resource VehicleByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#VehicleByFunction");
	public static final Resource VehicleByRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#VehicleByRole");
	public static final Resource VehicleLicense = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#VehicleLicense");
	public static final Resource Wall = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Wall");
	public static final Resource Warehouse = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Warehouse");
	public static final Resource WashingFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#WashingFacility");
	public static final Resource WasteManagementArtifactRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#WasteManagementArtifactRole");
	public static final Resource WaterTower = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#WaterTower");
	public static final Resource WaterTreatmentFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#WaterTreatmentFacility");
	public static final Resource Watercraft = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#Watercraft");
	public static final Resource WeaponByFunction = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#WeaponByFunction");
	public static final Resource WeaponByRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#WeaponByRole");
	public static final Resource WeaponManufacturingFacility = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#WeaponManufacturingFacility");
	public static final Resource WeaponRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#WeaponRole");
	public static final Resource WindFarm = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/ArtifactOntology#WindFarm");
}

