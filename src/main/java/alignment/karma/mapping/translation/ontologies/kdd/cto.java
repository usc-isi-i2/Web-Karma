/**************************************************************************************************************************************
 * Copyright (c) 2013 CUBRC, Inc.                                                                                                     *
 * Unpublished Work - all rights reserved under the copyright laws of the United States.                                              *
 * CUBRC, Inc. does not grant permission to any party outside the Government                                                          *
 * to use, disclose, copy, or make derivative works of this software.                                                                 *
 **************************************************************************************************************************************/

package alignment.karma.mapping.translation.ontologies.kdd;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;


@SuppressWarnings("ALL")
public class cto
{
	private static final String PREFIX = "cto";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#");
	}

	public static String getNamespace()
	{
		return "http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource ActOfBarring = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#ActOfBarring");
	public static final Resource ActOfHoaxImprovisedExplosiveDeviceUse = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#ActOfHoaxImprovisedExplosiveDeviceUse");
	public static final Resource ActOfImprovisedExplosiveDeviceUse = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#ActOfImprovisedExplosiveDeviceUse");
	public static final Resource AirborneDeliveryOfImprovisedExplosiveDeviceToTarget = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#AirborneDeliveryOfImprovisedExplosiveDeviceToTarget");
	public static final Resource AirborneImprovisedExplosiveDevice = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#AirborneImprovisedExplosiveDevice");
	public static final Resource AlcoholFueledImprovisedIncendiaryWeapon = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#AlcoholFueledImprovisedIncendiaryWeapon");
	public static final Resource AntiAircraftImprovisedExplosiveDevice = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#AntiAircraftImprovisedExplosiveDevice");
	public static final Resource AntiGroundVehicleImprovisedExplosiveDevice = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#AntiGroundVehicleImprovisedExplosiveDevice");
	public static final Resource AntiInfrastructureImprovisedExplosiveDevice = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#AntiInfrastructureImprovisedExplosiveDevice");
	public static final Resource AntiPersonnelImprovisedExplosiveDevice = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#AntiPersonnelImprovisedExplosiveDevice");
	public static final Resource AntiWatercraftImprovisedExplosiveDevice = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#AntiWatercraftImprovisedExplosiveDevice");
	public static final Resource BarredOrganization = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#BarredOrganization");
	public static final Resource DeliveryOfImprovisedExplosiveDeviceToTarget = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#DeliveryOfImprovisedExplosiveDeviceToTarget");
	public static final Resource EmplacedImprovisedExplosiveDevice = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#EmplacedImprovisedExplosiveDevice");
	public static final Resource EmplacementOfImprovisedExplosiveDevice = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#EmplacementOfImprovisedExplosiveDevice");
	public static final Resource GasolineFueledImprovisedIncendiaryWeapon = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#GasolineFueledImprovisedIncendiaryWeapon");
	public static final Resource GroupOfChildrenKilled = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#GroupOfChildrenKilled");
	public static final Resource GroupOfChildrenWounded = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#GroupOfChildrenWounded");
	public static final Resource GroupOfCiviliansKilled = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#GroupOfCiviliansKilled");
	public static final Resource GroupOfCiviliansWounded = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#GroupOfCiviliansWounded");
	public static final Resource GroupOfOrganizationMembersKilled = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#GroupOfOrganizationMembersKilled");
	public static final Resource GroupOfOrganizationMembersWounded = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#GroupOfOrganizationMembersWounded");
	public static final Resource HoaxImprovisedExplosiveDevice = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#HoaxImprovisedExplosiveDevice");
	public static final Resource ImprovisedExplosiveDevice = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#ImprovisedExplosiveDevice");
	public static final Resource ImprovisedIncendiaryWeapon = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#ImprovisedIncendiaryWeapon");
	public static final Resource Perpetrator = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#Perpetrator");
	public static final Resource PersonborneDeliveryOfImprovisedExplosiveDeviceToTarget = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#PersonborneDeliveryOfImprovisedExplosiveDeviceToTarget");
	public static final Resource PersonborneImprovisedExplosiveDevice = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#PersonborneImprovisedExplosiveDevice");
	public static final Resource PostalServiceDeliveryOfImprovisedExplosiveDeviceToTarget = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#PostalServiceDeliveryOfImprovisedExplosiveDeviceToTarget");
	public static final Resource PrematureImprovisedExplosiveDeviceDetonation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#PrematureImprovisedExplosiveDeviceDetonation");
	public static final Resource ProjectedDeliveryOfImprovisedExplosiveDeviceToTarget = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#ProjectedDeliveryOfImprovisedExplosiveDeviceToTarget");
	public static final Resource SuicideBomber = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#SuicideBomber");
	public static final Resource SuicideBomberRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#SuicideBomberRole");
	public static final Resource UnexplodedExplosiveOrdnance = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#UnexplodedExplosiveOrdnance");
	public static final Resource VehicleborneDeliveryOfImprovisedExplosiveDeviceToTarget = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#VehicleborneDeliveryOfImprovisedExplosiveDeviceToTarget");
	public static final Resource VehicleborneImprovisedExplosiveDevice = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#VehicleborneImprovisedExplosiveDevice");
	public static final Resource WaterborneDeliveryOfImprovisedExplosiveDeviceToTarget = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#WaterborneDeliveryOfImprovisedExplosiveDeviceToTarget");
	public static final Resource WaterborneImprovisedExplosiveDevice = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Domain/CounterterrorismOntology#WaterborneImprovisedExplosiveDevice");
}

