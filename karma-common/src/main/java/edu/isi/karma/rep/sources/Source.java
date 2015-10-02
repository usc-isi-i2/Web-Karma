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

package edu.isi.karma.rep.sources;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;

public abstract class Source {

	private String id;
	private String name;
	private String description;

	public abstract void print();

	public Source(String id) {
		this.id = id;
	}
	
	public Source(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public Source(String id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}
	
	public String getUri() {
		
		//TODO this is not the right way to get the modeling configuration
		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getDefault();
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(contextParameters.getId());
		return modelingConfiguration.getKarmaSourcePrefix() + getId() + "#";
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
