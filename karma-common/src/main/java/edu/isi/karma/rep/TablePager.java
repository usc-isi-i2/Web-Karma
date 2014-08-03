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

import java.util.List;

import edu.isi.karma.controller.command.selection.SuperSelectionManager;

public class TablePager {

	private final Table table;

	private int currentEndIndex;
	
	private int pagerSize;

	public TablePager(Table table, int desiredSize) {
		super();
		this.table = table;
		this.pagerSize = desiredSize;
		currentEndIndex = (desiredSize < table.getNumRows()) ? desiredSize-1 : table.getNumRows();
	}

	public int getCurrentEndIndex() {
		return currentEndIndex;
	}

	public List<Row> getRows() {
		return table.getRows(0, currentEndIndex+1, SuperSelectionManager.DEFAULT_SELECTION);
	}

	public int getPagerSize() {
		return pagerSize;
	}

	public void setPagerSize(int pagerSize) {
		this.pagerSize = pagerSize;
	}

	public List<Row> loadAdditionalRows() {
		int previousEndIndex = currentEndIndex;
		
		currentEndIndex = Math.min(currentEndIndex + pagerSize, table.getNumRows()-1);
		return table.getRows(previousEndIndex+1, pagerSize, SuperSelectionManager.DEFAULT_SELECTION);
	}
	
	public boolean isAtEndOfTable() {
		return (currentEndIndex + 1) >= table.getNumRows(); 
	}
	
	public int getAdditionalRowsLeftCount() {
		if (isAtEndOfTable()) {
			return 0;
		} else {
			return table.getNumRows() - (currentEndIndex + 1);
		}
	}
}
