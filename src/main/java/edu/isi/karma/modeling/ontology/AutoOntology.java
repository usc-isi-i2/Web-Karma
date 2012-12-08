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

import org.apache.log4j.Logger;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.isi.karma.controller.history.WorksheetCommandHistoryReader;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.AlignToOntology;
import edu.isi.karma.modeling.alignment.URI;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.rep.semantictypes.SemanticType.ClientJsonKeys;

public class AutoOntology {
	static Logger logger = Logger.getLogger(AutoOntology.class.getName());

	private Worksheet worksheet;
	public AutoOntology(Worksheet worksheet) {
		this.worksheet = worksheet;
	}
	public void Build(String path) throws IOException {
		List<HNode> sortedLeafHNodes = new ArrayList<HNode>();
		worksheet.getHeaders().getSortedLeafHNodes(sortedLeafHNodes);
		OntModel autoOntology = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
		String ns = "http://www.isi.edu/karma/automodel/"+worksheet.getTitle()+"#";
		OntClass topClass = autoOntology.createClass( ns + worksheet.getTitle());
		URI domainName = new URI(topClass.getURI()); 
		for (HNode hNode : sortedLeafHNodes){
			DatatypeProperty dp = autoOntology.createDatatypeProperty(ns+hNode.getColumnName());
			dp.addDomain(topClass);
			dp.addRange(XSD.xstring);
		}

		Writer outUTF8 =null;
		try {
			outUTF8 = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(path), "UTF8"));
			autoOntology.write(outUTF8);
			outUTF8.flush();
			outUTF8.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		 
	}
}
