/*******************************************************************************
 * Copyright 2014 University of Southern California
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
package edu.isi.karma.kr2rml.planning;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.isi.karma.kr2rml.template.PopulatedTemplateTermSet;
import edu.isi.karma.rep.Row;

public class TriplesMapPlan {

	protected Collection<TriplesMapWorker> workers;
	protected Row r;
	protected Map<String, List<PopulatedTemplateTermSet>> triplesMapSubjects;
	
	public TriplesMapPlan(Collection<TriplesMapWorker> workers, Row r, Map<String, List<PopulatedTemplateTermSet>>triplesMapSubjects)
	{
		this.workers = workers;
		this.r = r;
		this.triplesMapSubjects = triplesMapSubjects;
	}
}
