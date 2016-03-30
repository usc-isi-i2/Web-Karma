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

package edu.isi.karma.modeling.ontology;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Worksheet;

public class AutoOntology {
	static Logger logger = LoggerFactory.getLogger(AutoOntology.class.getName());

	private Worksheet worksheet;
	public AutoOntology(Worksheet worksheet) {
		this.worksheet = worksheet;
	}
	public void Build(String path) throws IOException {
		
		List<HNode> sortedLeafHNodes = new ArrayList<>();
		worksheet.getHeaders().getSortedLeafHNodes(sortedLeafHNodes);
		OntModel autoOntology = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
		String ns = Namespaces.KARMA;
		autoOntology.setNsPrefix("karma", ns);
		OntClass topClass = autoOntology.createClass( ns + worksheet.getTitle().replaceAll(" ", "_")); // replace blank spaces with undrscore
		for (HNode hNode : sortedLeafHNodes){
			DatatypeProperty dp = autoOntology.createDatatypeProperty(ns+hNode.getColumnName().trim().replaceAll(" ", "_"));
			dp.addDomain(topClass);
			dp.addRange(XSD.xstring);
		}
		
//		OntClass thingClass = autoOntology.createClass(Uris.THING_URI);
		ObjectProperty op = autoOntology.createObjectProperty(ns + "relatedTo");
		op.addDomain(topClass);
//		op.addRange(thingClass);
		
		Writer outUTF8;
		try {
			outUTF8 = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(path), "UTF8"));
			autoOntology.write(outUTF8, null);
			outUTF8.flush();
			outUTF8.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
