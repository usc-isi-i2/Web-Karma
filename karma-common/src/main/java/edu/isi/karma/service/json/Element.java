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

import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.rep.sources.Attribute;
import edu.isi.karma.rep.sources.Table;

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
	
    public void print() {
    	this.print(this, 0);
    }
    
    private void print(Element root, int depth) {
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

    public void computeFullPaths(List<String> result) {
    	this.computeFullPaths(this, result);
    }
    
    private void computeFullPaths(Element root, List<String> result) {
    	if (root.getFullPath().length() > 0) 
    		result.add(root.getFullPath());
    	if (root.valueType == ValueType.SINGLE) {
    	} else {
    		for (int i = 0; i < ((ArrayValue)root.value).getElements().size(); i++)
    			computeFullPaths(((ArrayValue)root.value).getElements().get(i), result);
    	}
    }

    public void updateHeaders() {
    	this.updateHeaders(this,"");
    }
    
    private void updateHeaders(Element root, String prefix) {
    	if (prefix.trim().length() > 0) {
    		if (root.key.trim().length() > 0)
    			root.key = prefix + ":" + root.key;
    		else
    			root.key = prefix;
    	}
    	if (root.valueType == ValueType.SINGLE) {
    		return;
    	} else {
			for (Element children : ((ArrayValue)root.getValue()).getElements()) 
				updateHeaders(children, root.key);
    	}
    }
    
    public Table getFlatTable() {
    	Table t = new Table();
    	this.getFlatTable(this, t);
    	return t;
    }
    
    private void getFlatTable(Element root, Table t) {
		
    	Table innerT = new Table();
    	
    	if (root.valueType == ValueType.SINGLE) { 
    		innerT.getHeaders().add(new Attribute(root.getKey(), root.getKey()));
    		List<String> v = new ArrayList<>();
    		v.add(((SingleValue)root.getValue()).getValueString());
    		innerT.getValues().add(v);
    		t.cartesianProductOrUnionIfSameHeaders(innerT);
    		return;
    	} else {
    		List<String> v = new ArrayList<>();
			for (Element child : ((ArrayValue)root.getValue()).getElements()) 
			{
				if (child.valueType == ValueType.SINGLE) {
		    		innerT.getHeaders().add(new Attribute(child.getKey(), child.getKey()));
		    		v.add(((SingleValue)child.getValue()).getValueString());
				}
			}
			if (!v.isEmpty()) {
				innerT.getValues().add(v);
				t.cartesianProductOrUnionIfSameHeaders(innerT);
			}		
			
			Table unionT = new Table();
			for (Element child : ((ArrayValue)root.getValue()).getElements()) 
			{
				if (child.valueType == ValueType.ARRAY) {
					innerT = new Table();
		    		getFlatTable(child, innerT);
		    		unionT.cartesianProductOrUnionIfSameHeaders(innerT);
				}
			}
			if (unionT.getColumnsCount() > 0)
				t.cartesianProductOrUnionIfSameHeaders(unionT);
		}
    }
    
    public void moveUpOneValueElements() {
    	this.moveUpOneValueElements(this);
    }
    
    private void moveUpOneValueElements(Element root) {

    	if (root.valueType == ValueType.ARRAY && ((ArrayValue)root.getValue()).getElements().size() <= 1) {
    		while (true) {
	    		
    			if (((ArrayValue)root.getValue()).getElements().size() > 1) 
	    			break;
	    		
    			if (((ArrayValue) root.getValue()).getElements().isEmpty()) {
//    				if (root.getParent() != null)
//    					((ArrayValue)root.getParent().getValue()).getElements().remove(root);
    				return;
    			}
    			
    			Element e = ((ArrayValue)root.getValue()).getElements().get(0);
    			
				root.setValue(e.getValue());
				root.setValueType(e.getValueType());
				root.setKey(e.getKey());
				if (e.valueType == ValueType.SINGLE) 
					return;
    		}
    	}
    	if (root.valueType == ValueType.SINGLE)
    		return;
    	
    	for (int i = 0; i < ((ArrayValue)root.value).getElements().size(); i++) { 
			moveUpOneValueElements(((ArrayValue)root.value).getElements().get(i));
    	}
    }
}
