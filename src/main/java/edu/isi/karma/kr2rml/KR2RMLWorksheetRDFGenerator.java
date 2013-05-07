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

package edu.isi.karma.kr2rml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;

public class KR2RMLWorksheetRDFGenerator {
	private final Worksheet worksheet;
	private final R2RMLMapping mapping;
	private final RepFactory factory;
	
	private Logger logger = LoggerFactory.getLogger(KR2RMLWorksheetRDFGenerator.class);

	public KR2RMLWorksheetRDFGenerator(Worksheet worksheet,
			R2RMLMapping mapping, RepFactory factory) {
		super();
		this.worksheet = worksheet;
		this.mapping = mapping;
		this.factory = factory;
	}
	
	public void generate() {
		System.out.println(mapping.toString());
	}
}
