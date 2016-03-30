/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.karma.geospatial;

import org.geotools.factory.Hints;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class SpatialReferenceSystemTransformationUtil {
	private static final Logger logger = LoggerFactory
			.getLogger(SpatialReferenceSystemTransformationUtil.class);

	private SpatialReferenceSystemTransformationUtil() {

	}

	public static Geometry Transform(Geometry inGeom,
			CoordinateReferenceSystem sourceCRS,
			CoordinateReferenceSystem targetCRS) {
		
		Hints.putSystemDefault(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,
				Boolean.TRUE);

		if(sourceCRS.equals(targetCRS))
			return inGeom;
		
		MathTransform transform;
		
		try {
			transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
		} catch (FactoryException e) {
			logger.error("Cannot find a transformation!", e);
			return inGeom;
		}

		Geometry outGeom = null;
		
		try {
			outGeom = JTS.transform(inGeom, transform);
		} catch (MismatchedDimensionException e) {
			logger.error("Cannot perform the transformation!", e);
			return inGeom;
		} catch (TransformException e) {
			logger.error("Cannot perform the transformation!", e);
			return outGeom;
		}

		return outGeom;
	}

	public static String Transform(String inGeomWKT, String fromSRID,
			String toSRID) {
		
		Hints.putSystemDefault(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,
				Boolean.TRUE);
		
		if(fromSRID.equals(toSRID))
			return inGeomWKT;
		
		WKTReader reader = new WKTReader();
		Geometry JTSGeometry;
		try {
			JTSGeometry = reader.read(inGeomWKT);
		} catch (ParseException e1) {
			logger.error("Cannot read the input WKT feature!");
			return inGeomWKT;
		}

		if (!fromSRID.contains(":"))
			fromSRID = "EPSG:" + fromSRID;
		if (!toSRID.contains(":"))
			toSRID = "EPSG:" + toSRID;

		CoordinateReferenceSystem sourceCRS;
		CoordinateReferenceSystem targetCRS;
		try {
			sourceCRS = CRS.decode(fromSRID, true);
			targetCRS = CRS.decode(toSRID, true);
		} catch (NoSuchAuthorityCodeException e) {
			logger.error("Cannot read SRID!", e);
			return inGeomWKT;
		} catch (FactoryException e) {
			logger.error("Cannot read SRID!", e);
			return inGeomWKT;
		}

		MathTransform transform;
		try {
			transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
		} catch (FactoryException e) {
			logger.error("Cannot find a transformation!", e);
			return inGeomWKT;
		}

		Geometry outGeom;
		try {
			outGeom = JTS.transform(JTSGeometry, transform);
		} catch (MismatchedDimensionException e) {
			logger.error("Cannot perform the transformation!", e);
			return inGeomWKT;
		} catch (TransformException e) {
			logger.error("Cannot perform the transformation!", e);
			return inGeomWKT;
		}

		return outGeom.toText();
	}
}
