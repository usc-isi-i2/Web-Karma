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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Table {
	
	static Logger logger = LoggerFactory.getLogger(Table.class);

	private List<Attribute> headers;
	private List<List<String>> values;
	// the row id associated to each table row
	private List<String> rowIds;
	
	public Table() {
		headers = new ArrayList<>();
		values = new ArrayList<>();
		rowIds = new ArrayList<>();
	}
	
	public Table(Table table) {
		this.headers = new ArrayList<>(table.headers);
		this.values = new ArrayList<>(table.values);
		this.rowIds = new ArrayList<>(table.rowIds);
	}
	
	public int getRowsCount() {
		if (this.values == null) return 0;
		return this.values.size();
	}
	
	public int getColumnsCount() {
		if (this.headers == null) return 0;
		return this.headers.size();
	}
	
	public List<Attribute> getHeaders() {
		return headers;
	}

	public void setHeaders(List<Attribute> headers) {
		this.headers = headers;
	}

	public List<List<String>> getValues() {
		return values;
	}
	public void setValues(List<List<String>> values) {
		this.values = values;
	}
	
	public List<String> getRowIds() {
		return rowIds;
	}

	public void setRowIds(List<String> rowIds) {
		this.rowIds = rowIds;
	}

	public void print() {
		
		for (Attribute p: headers) {
			System.out.print(p.getIOType() + ",");
		}
		System.out.println();

		for (Attribute p: headers) {
			System.out.print(p.getId() + ",");
		}
		System.out.println();
		
		for (Attribute p: headers) {
			System.out.print(p.getName() + ",");
		}
		System.out.println();

		for (List<String> value: values) {
			for (String v:value) {
				System.out.print(v + ",");
			}
			System.out.println();
		}
	}

	public String getPrintInfo() {
		
		String s = "";
		s += "Table data: \n";
		for (Attribute p: headers) {
			s += p.getIOType() + ",";
		}
		s += "\n";
		
		for (Attribute p: headers) {
			s += p.getName() + ",";
		}
		s += "\n";

//		for (Attribute p: headers) {
//			s += p.getId() + ",";
//		}
//		s += "\n";

		for (List<String> value: values) {
			for (String v:value) {
				s += v + ",";
			}
			s += "\n";
		}
		
		return s;
	}

	private boolean sameHeaders(Table t) {
		if (t == null)
			return false;
		
		if (this.getColumnsCount() == 0 || t.getColumnsCount() == 0)
			return false;
		
		if (this.getColumnsCount() != t.getColumnsCount())
			return false;
		
		List<String> attNames = new ArrayList<>();
		List<String> tAttNames = new ArrayList<>();
		
		for (Attribute att : this.getHeaders()) 
			attNames.add(att.getName());
		
		for (Attribute att : t.getHeaders()) 
			tAttNames.add(att.getName());
		
		if (attNames.containsAll(tAttNames) && tAttNames.containsAll(attNames))
			return true;
		
		return false;
	}
	
    public void cartesianProductOrUnionIfSameHeaders(Table t) {
    	
    	int t1Cols = this.getColumnsCount();
    	int t2Cols = t.getColumnsCount();
    	
    	if (t2Cols == 0)
    		return;
    	
    	else if (t1Cols == 0) {
    		
    		if (this.headers == null)
    			this.headers = new ArrayList<>();
    		
    		for (Attribute att : t.getHeaders())
    			this.headers.add(new Attribute(att));

    		if (this.values == null)
    			this.values = new ArrayList<>();

    		if (t.getValues() != null)
    			for (List<String> v : t.getValues())
    				if (v != null)
    					values.add(new ArrayList<>(v));
    	} else {
    		
    		if (sameHeaders(t)) {
    			List<Table> tables = new ArrayList<>();
    			tables.add(this);
    			tables.add(t);
    			Table result = union(tables);
    			this.setHeaders(result.getHeaders());
    			this.setValues(result.getValues());
    			return;
    		}
    		
    		for (Attribute att : t.getHeaders())
    			headers.add(new Attribute(att));
    		
    		int t1Rows = this.getRowsCount();
        	int t2Rows = t.getRowsCount();
        	int totalRows = t1Rows == 0 || t2Rows == 0 ? t1Rows + t2Rows : t1Rows * t2Rows;
        	
        	List<List<String>> values = new ArrayList<>();

    		for (int i = 0; i < totalRows; i++) {
        		
        		List<String> row = new ArrayList<>();
        		
        		for (int j = 0; j < t1Cols; j++) {
        			int index = t1Rows == 0 ? -1 : i % t1Rows;
        			if (index == -1 || this.values == null || this.values.get(index) == null)
        				row.add(null);
        			else
        				row.add(this.values.get(index).get(j));
        		}
        		for (int j = 0; j < t2Cols; j++) {
        			int index = t2Rows == 0 ? -1 : i % t2Rows;
        			if (index == -1 || t.getValues() == null || t.getValues().get(index) == null)
        				row.add(null);
        			else
        				row.add(t.getValues().get(index).get(j));
        		}
        		
        		values.add(row);

        	}
    		
    		this.values = values;
    	}
    }
    	
	/**
	 * Each service invocation might have different columns than other other invocations.
	 * This method integrates all results into one table.
	 * For the invocations that don't have a specific column, we put null values in corresponding column. 
	 */
	
    public static Table union(List<Table> srcTables) {
    	
    	if (srcTables == null)
    		return null;
    	
    	Table resultTable = new Table();
		String attributeId;
		List<String> uniqueAttributeIDs = new ArrayList<>();
		
		List<List<Attribute>> srcAttributes = new ArrayList<>();
		List<List<List<String>>> srcValues = new ArrayList<>();
		List<List<String>> srcRowIds = new ArrayList<>();
		
		List<Attribute> resultAttributes = new ArrayList<>();
		List<List<String>> resultValues = new ArrayList<>();
		List<String> resultRowIds = new ArrayList<>();
		
		for (Table t : srcTables) {
			srcAttributes.add(t.getHeaders());
			srcRowIds.add(t.getRowIds());
			srcValues.add(t.getValues());
		}

		for (int i = 0; i < srcAttributes.size(); i++) {
			for (int j = 0; j < srcAttributes.get(i).size(); j++)
			{
				attributeId = srcAttributes.get(i).get(j).getId().toString();
				if (uniqueAttributeIDs.indexOf(attributeId) == -1) {
					uniqueAttributeIDs.add(attributeId);
					resultAttributes.add(srcAttributes.get(i).get(j));
				}
			}
		}
		
		List<Attribute> rawAttributes;
		List<String> rawAttributeIDs = new ArrayList<>();
		List<String> rawValues;
		String singleValue;

		for (int i = 0; i < srcAttributes.size(); i++) {
			
			rawAttributes = srcAttributes.get(i);
			rawAttributeIDs.clear();
			for (Attribute p : rawAttributes)
				rawAttributeIDs.add(p.getId());
			
//			logger.debug("table " + i);
			for (int j = 0; j < srcValues.get(i).size(); j++) {
				
				List<String> populatedValues = new ArrayList<>();
				rawValues = srcValues.get(i).get(j);
				
//				logger.debug("\t row " + j);
				for (int k = 0; k < resultAttributes.size(); k++) {
//					logger.debug("\t\t column " + k);
					int index = rawAttributeIDs.indexOf(resultAttributes.get(k).getId());
					if (index == -1)
						singleValue = null;
//						singleValue = "";
					else
						singleValue = rawValues.get(index);
					populatedValues.add(singleValue);
				}
				
				if (srcRowIds != null && !srcRowIds.isEmpty() &&
						srcRowIds.get(i) != null && !srcRowIds.get(i).isEmpty() &&  
						srcRowIds.get(i).get(j) != null)
					resultRowIds.add(srcRowIds.get(i).get(j));
				resultValues.add(populatedValues);
			}
			
		}
		
		resultTable.setHeaders(resultAttributes);
		resultTable.setRowIds(resultRowIds);
		resultTable.setValues(resultValues);
		
		return resultTable;
    }

	public String asCSV() {
		return asCSV(null, null, null);
	}

	public String asCSV(Character separator, Character quotechar, Character endlinechar) {
		String csv = "";
		if (separator == null) separator = ',';
		if (quotechar == null) quotechar = '"';
		if (endlinechar == null) endlinechar = '\n';
		
		try {
			
			if (this.headers != null && !this.headers.isEmpty()) {
				for (int i = 0; i < this.headers.size(); i++) {
					if (i != 0)
						csv += separator.charValue();
					csv += quotechar + this.headers.get(i).getName() + quotechar;
				}
				csv += endlinechar;
			} else {
				logger.error("Table does not have any header.");
			}

			if (values == null) {
				logger.error("Table does not have any rows.");
				return csv;
			}
			
			for (int i = 0; i < this.values.size(); i++) {
				for (int j = 0; j < this.values.get(i).size(); j++) {
					if (j != 0)
						csv += separator;
					csv += quotechar + values.get(i).get(j) + quotechar;
				}
				csv += endlinechar;
			}

			return csv;
			
		} catch (Exception e) {
			logger.error("Error in generating CSV from the table.");
			e.printStackTrace();
			return null;
		}

	}
    
}
