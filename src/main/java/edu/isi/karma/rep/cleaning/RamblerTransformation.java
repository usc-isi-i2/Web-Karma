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
package edu.isi.karma.rep.cleaning;

import edu.isi.karma.cleaning.InterpreterType;
import edu.isi.karma.cleaning.ProgramRule;



public class RamblerTransformation implements Transformation {
	public String signature = "";
	private ProgramRule prog;
	public RamblerTransformation(ProgramRule prog)
	{ 
		this.prog = prog;
		this.signature = prog.signString;
	}
	public String transform(String value) {
		InterpreterType worker = prog.getRuleForValue(value);
		String s = worker.execute(value);
		if(s.contains("_FATAL_ERROR_"))
			return value;
		else
			return s;
	}	
	public String getId() {
		// TODO Auto-generated method stub
		return this.signature;
	}
	public int hashCode()
	{
		return this.signature.hashCode();
	}
	public boolean equals(Object other) 
	{
		RamblerTransformation e = (RamblerTransformation)other;
		if(e.signature.compareTo(this.signature)==0)
		{
			return true;
		}
		return false;
	}
	public String getClassLabel(String value)
	{
		return prog.getClassForValue(value);
	}
	@Override
	public String transform_debug(String value) {
		InterpreterType worker = prog.getRuleForValue(value);
		if(value.length()==0)
			return "";
		String s = worker.execute_debug(value);
		return s;
	}

}
