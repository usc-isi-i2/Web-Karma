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
package edu.isi.karma.kr2rml.formatter;

import java.util.HashMap;
import java.util.Map;

public abstract class KR2RMLColumnNameFormatter {

	protected Map<String, String> formattedColumnNames = new HashMap<>();
	protected Map<String, String> formattingRemovedColumnNames = new HashMap<>();
	
	public String getFormattedColumnName(String columnName) {
		if(!isFormattedColumnNameMemoized(columnName))
		{
			memoizeColumnName(columnName, format(columnName));
		}
		return getMemoizedFormattedColumnName(columnName);
	}

	protected abstract String format(String columnName);

	private void memoizeColumnName(String columnName, String formattedColumnName) {
		
		if(null != columnName)
		{
			formattedColumnNames.put(columnName, formattedColumnName);
		}
		if(null != formattedColumnName)
		{
			formattingRemovedColumnNames.put(formattedColumnName, columnName);
		}		
	}

	private String getMemoizedFormattedColumnName(String columnName) {
		return formattedColumnNames.get(columnName);
	}

	private boolean isFormattedColumnNameMemoized(String columnName) {
		return formattedColumnNames.containsKey(columnName);
	}

	public String getColumnNameWithoutFormatting(String formattedColumnName) {
		if(!isColumnNameWithoutFormattingMemoized(formattedColumnName))
		{
			memoizeColumnName(removeFormatting(formattedColumnName), formattedColumnName);
		}
		return getMemoizedColumnNameWithoutFormatting(formattedColumnName);
	}

	private String getMemoizedColumnNameWithoutFormatting(
			String formattedColumnName) {
		return formattingRemovedColumnNames.get(formattedColumnName);
	}

	private boolean isColumnNameWithoutFormattingMemoized(
			String formattedColumnName) {
		return formattingRemovedColumnNames.containsKey(formattedColumnName);
	}

	protected abstract String removeFormatting(String formattedColumnName);


}
