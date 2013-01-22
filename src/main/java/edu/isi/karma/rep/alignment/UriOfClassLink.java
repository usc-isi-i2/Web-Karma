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

package edu.isi.karma.rep.alignment;

import edu.isi.karma.modeling.FixedUris;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;


public class UriOfClassLink extends Link {

	private static final long serialVersionUID = 1L;
	private static final Label label = 
			new Label(FixedUris.URI_OF_CLASS_LINK_URI, Namespaces.KARMA_DEV, Prefixes.KARMA_DEV);

	public UriOfClassLink(String id) {
		super(id, label);
	}
	
	public static Label getLabel() {
		return label;
	}
}
