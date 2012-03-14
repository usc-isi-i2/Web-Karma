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
package edu.isi.karma.service.json;

import java.util.List;

public class Element {
	private String key;
	private int valueType;
	private Value value;
	private String fullPath = "";
	private Element parent;
	
	
	public Element getParent() {
		return parent;
	}
	public void setParent(Element parent) {
		this.parent = parent;
	}
	public String getLocalName(int depth) {
		String result = "";
		if (valueType == ValueType.SINGLE)
			result += "d=" + depth + ",k=" + getKey() + ",v=" + ((SingleValue)getValue()).getValueString();
		return result;
	}
	public String getFullPath() {
		return fullPath.trim();
	}
	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}
	public String getKey() {
		// to remove @ in front of keys in process of converting xml to json
		if (key.startsWith("@"))
			key = key.substring(1);
		return key.trim();
	}
	public void setKey(String key) {
		this.key = key;
	}
	public int getValueType() {
		return valueType;
	}
	public void setValueType(int valueType) {
		this.valueType = valueType;
	}
	public Value getValue() {
		return value;
	}
	public void setValue(Value value) {
		this.value = value;
	}
	
    public void print(Element root, int depth) {
    	String tabs = "";
    	for (int i = 0; i < depth; i++)
    		tabs += "\t";
    	System.out.println(tabs + "name:" + root.key);
    	System.out.print(tabs + "value:");

    	if (root.valueType == ValueType.SINGLE) {
    		System.out.println(((SingleValue)root.value).getValueString());
    		System.out.println();
    	} else {
    		System.out.println();
    		for (int i = 0; i < ((ArrayValue)root.value).getElements().size(); i++)
    			print(((ArrayValue)root.value).getElements().get(i), depth + 1);
    	}
    }

    public void printFullPaths(Element root, List<String> result) {
    	if (root.getFullPath().length() > 0) 
    		result.add(root.getFullPath());
    	if (root.valueType == ValueType.SINGLE) {
    	} else {
    		for (int i = 0; i < ((ArrayValue)root.value).getElements().size(); i++)
    			printFullPaths(((ArrayValue)root.value).getElements().get(i), result);
    	}
    }
    
    public void moveUpOneValueElements(Element root) {
    	if (root.valueType == ValueType.SINGLE) {
    		while (true) {
	    		if (root.getParent() == null || ((ArrayValue)root.getParent().getValue()).getElements().size() != 1) 
	    			break;
				root.getParent().setValue(root.getValue());
				if (root.getParent().getKey().equalsIgnoreCase("ROOT"))
					root.getParent().setKey(root.getKey());
				root.getParent().setValueType(ValueType.SINGLE);
				root = root.getParent();
    		}
    	} else for (int i = 0; i < ((ArrayValue)root.value).getElements().size(); i++) { 
    			moveUpOneValueElements(((ArrayValue)root.value).getElements().get(i));
        		if (root.valueType == ValueType.SINGLE)
        			return;
    	}
    }
}
