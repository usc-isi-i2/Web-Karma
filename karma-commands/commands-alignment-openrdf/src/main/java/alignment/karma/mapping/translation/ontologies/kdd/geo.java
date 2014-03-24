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
public class geo
{
	private static final String PREFIX = "geo";

	// Utility function to add this prefix to a Jena PrefixMapping (incl models)
	public static void addToPrefixMapping(PrefixMapping pm)
	{
		pm.setNsPrefix(PREFIX, "http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#");
	}

	public static String getNamespace()
	{
		return "http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#";
	}

	public static String getPrefix()
	{
		return PREFIX;
	}

	// Class
	public static final Resource Aimpoint = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Aimpoint");
	public static final Resource AimpointRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#AimpointRole");
	public static final Resource AreaOfInfluence = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#AreaOfInfluence");
	public static final Resource AreaOfInfluenceRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#AreaOfInfluenceRole");
	public static final Resource AreaOfOperations = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#AreaOfOperations");
	public static final Resource AreaOfOperationsRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#AreaOfOperationsRole");
	public static final Resource Atmosphere = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Atmosphere");
	public static final Resource AtmosphericFeature = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#AtmosphericFeature");
	public static final Resource Bay = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Bay");
	public static final Resource Beach = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Beach");
	public static final Resource Bog = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Bog");
	public static final Resource BoundingBoxEastPoint = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#BoundingBoxEastPoint");
	public static final Resource BoundingBoxNorthPoint = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#BoundingBoxNorthPoint");
	public static final Resource BoundingBoxPoint = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#BoundingBoxPoint");
	public static final Resource BoundingBoxSouthPoint = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#BoundingBoxSouthPoint");
	public static final Resource BoundingBoxWestPoint = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#BoundingBoxWestPoint");
	public static final Resource Cave = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Cave");
	public static final Resource Channel = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Channel");
	public static final Resource City = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#City");
	public static final Resource CityPart = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#CityPart");
	public static final Resource Cliff = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Cliff");
	public static final Resource Coastline = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Coastline");
	public static final Resource Continent = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Continent");
	public static final Resource CoralReef = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#CoralReef");
	public static final Resource Country = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Country");
	public static final Resource CountryPart = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#CountryPart");
	public static final Resource Desert = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Desert");
	public static final Resource Ecosystem = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Ecosystem");
	public static final Resource Fan = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Fan");
	public static final Resource Fault = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Fault");
	public static final Resource Flume = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Flume");
	public static final Resource Forest = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Forest");
	public static final Resource GeographicFeature = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#GeographicFeature");
	public static final Resource GeopoliticalEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#GeopoliticalEntity");
	public static final Resource GeopoliticalEntityBorder = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#GeopoliticalEntityBorder");
	public static final Resource GeopoliticalEntityPart = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#GeopoliticalEntityPart");
	public static final Resource GeospatialBoundary = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#GeospatialBoundary");
	public static final Resource GeospatialLocation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#GeospatialLocation");
	public static final Resource GeospatialRegion = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#GeospatialRegion");
	public static final Resource GeospatialRegionBoundingBox = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#GeospatialRegionBoundingBox");
	public static final Resource Glacier = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Glacier");
	public static final Resource Gorge = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Gorge");
	public static final Resource Hill = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Hill");
	public static final Resource IceCliff = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#IceCliff");
	public static final Resource IceField = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#IceField");
	public static final Resource IceShelf = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#IceShelf");
	public static final Resource Island = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Island");
	public static final Resource Isthmus = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Isthmus");
	public static final Resource Lagoon = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Lagoon");
	public static final Resource Lake = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Lake");
	public static final Resource Landform = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Landform");
	public static final Resource Marsh = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Marsh");
	public static final Resource Moraine = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Moraine");
	public static final Resource Mountain = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Mountain");
	public static final Resource MountainPass = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#MountainPass");
	public static final Resource NaturalFord = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#NaturalFord");
	public static final Resource NaturalHarbor = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#NaturalHarbor");
	public static final Resource Ocean = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Ocean");
	public static final Resource Peninsula = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Peninsula");
	public static final Resource Plain = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Plain");
	public static final Resource Plateau = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Plateau");
	public static final Resource PointOfDestination = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#PointOfDestination");
	public static final Resource PointOfDestinationRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#PointOfDestinationRole");
	public static final Resource PointOfOrigin = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#PointOfOrigin");
	public static final Resource PointOfOriginRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#PointOfOriginRole");
	public static final Resource PolarIce = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#PolarIce");
	public static final Resource Pond = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Pond");
	public static final Resource PopulatedPlace = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#PopulatedPlace");
	public static final Resource PortOfEntry = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#PortOfEntry");
	public static final Resource Province = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Province");
	public static final Resource ProvincePart = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#ProvincePart");
	public static final Resource Rainforest = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Rainforest");
	public static final Resource Rapids = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Rapids");
	public static final Resource Ridge = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Ridge");
	public static final Resource River = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#River");
	public static final Resource RiverBank = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#RiverBank");
	public static final Resource Rock = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Rock");
	public static final Resource RockFormation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#RockFormation");
	public static final Resource Route = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Route");
	public static final Resource RouteRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#RouteRole");
	public static final Resource SaltPan = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#SaltPan");
	public static final Resource SandDune = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#SandDune");
	public static final Resource SatelliteLocation = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#SatelliteLocation");
	public static final Resource SatelliteLocationRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#SatelliteLocationRole");
	public static final Resource Sea = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Sea");
	public static final Resource Shrubland = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Shrubland");
	public static final Resource Spring = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Spring");
	public static final Resource StagingArea = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#StagingArea");
	public static final Resource State = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#State");
	public static final Resource StatePart = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#StatePart");
	public static final Resource SubNationalEntity = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#SubNationalEntity");
	public static final Resource Subcontinent = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Subcontinent");
	public static final Resource Swamp = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Swamp");
	public static final Resource Tundra = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Tundra");
	public static final Resource Valley = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Valley");
	public static final Resource Volcano = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Volcano");
	public static final Resource Wadi = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Wadi");
	public static final Resource Waterfall = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Waterfall");
	public static final Resource Waypoint = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Waypoint");
	public static final Resource WaypointRole = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#WaypointRole");
	public static final Resource Wetland = ResourceFactory.createResource("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#Wetland");

	// DatatypeProperty
	public static final Property has_latitude_value = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#has_latitude_value");
	public static final Property has_longitude_value = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#has_longitude_value");

	// ObjectProperty
	public static final Property coincides_with = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#coincides_with");
	public static final Property connected_with = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#connected_with");
	public static final Property disconnected_with = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#disconnected_with");
	public static final Property externally_connects_with = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#externally_connects_with");
	public static final Property has_nontangential_proper_part = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#has_nontangential_proper_part");
	public static final Property has_spatial_part_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#has_spatial_part_of");
	public static final Property has_spatial_proper_part = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#has_spatial_proper_part");
	public static final Property has_tangential_proper_part = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#has_tangential_proper_part");
	public static final Property nontangential_proper_part_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#nontangential_proper_part_of");
	public static final Property overlaps_with = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#overlaps_with");
	public static final Property partially_overlaps_with = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#partially_overlaps_with");
	public static final Property spatial_part_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#spatial_part_of");
	public static final Property spatial_proper_part_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#spatial_proper_part_of");
	public static final Property tangential_proper_part_of = ResourceFactory.createProperty("http://www.cubrc.org/ontologies/KDD/Mid/GeospatialOntology#tangential_proper_part_of");
}

