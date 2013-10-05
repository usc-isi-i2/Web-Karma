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
/**
 * 
 */
package edu.isi.karma.rep;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * @author szekely All entities in the representation that have ids and get
 *         translated into data sent to the client.
 */
public abstract class RepEntity extends Entity {
	public abstract void prettyPrint(String prefix, PrintWriter pw,
			RepFactory factory);

	public String prettyPrint(RepFactory factory) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		prettyPrint("", pw, factory);
		return sw.toString();
	}

	protected RepEntity(String id) {
		super(id);
	}
}
