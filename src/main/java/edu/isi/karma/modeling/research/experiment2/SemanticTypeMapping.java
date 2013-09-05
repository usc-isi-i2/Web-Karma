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

package edu.isi.karma.modeling.research.experiment2;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import edu.isi.karma.rep.alignment.SemanticType;

public class SemanticTypeMapping {

	private SemanticType semanticType;
	private MappingType type;
	private Set<MappingStruct> mappingStructs;
	
	public SemanticTypeMapping(SemanticType semanticType, MappingType type) {
		this.semanticType = semanticType;
		this.mappingStructs = new HashSet<MappingStruct>();
		this.type = type;
	}

	public SemanticTypeMapping(SemanticType semanticType, MappingType type, Set<MappingStruct> mappingStructs) {
		this.semanticType = semanticType;
		this.type = MappingType.ClassNode;
		this.mappingStructs = mappingStructs;
	}

	public SemanticType getSemanticType() {
		return semanticType;
	}
	
	public Set<MappingStruct> getMappingStructs() {
		return Collections.unmodifiableSet(mappingStructs);
	}

	public MappingType getType() {
		return type;
	}
	
	public void addMappingStruct(MappingStruct mappingStruct) {
		this.mappingStructs.add(mappingStruct);
	}
	
	public double getConfidence() {
		// FIXME
		return 1.0;
	}
}
