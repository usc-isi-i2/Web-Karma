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
package edu.isi.karma.rep.metadata;

import edu.isi.karma.rep.ColumnMetadata;
import edu.isi.karma.rep.alignment.SemanticTypes;
import edu.isi.karma.rep.sources.DataSource;
import edu.isi.karma.rep.sources.WebService;

public class MetadataContainer {
	
	@SuppressWarnings("unused")
	private String 			worksheetName;
	@SuppressWarnings("unused")
	private SemanticTypes 	semanticTypes;
	
	private WebService service;
	private DataSource source;
	
	private WorksheetProperties worksheetProperties;
	private SourceInformation sourceInfo;
	private ColumnMetadata columnMetadata;

	public WebService getService() {
		return service;
	}

	public void setService(WebService service) {
		this.service = service;
	}

	public DataSource getSource() {
		return source;
	}

	public void setSource(DataSource source) {
		this.source = source;
	}

	public SourceInformation getSourceInformation() {
		return sourceInfo;
	}

	public void setSourceInformation(SourceInformation sourceInfo) {
		this.sourceInfo = sourceInfo;
	}

	public WorksheetProperties getWorksheetProperties() {
		if (worksheetProperties == null) {
			worksheetProperties = new WorksheetProperties();
		}
		return worksheetProperties;
	}

	public void setWorksheetProperties(WorksheetProperties worksheetProperties) {
		this.worksheetProperties = worksheetProperties;
	}

	public ColumnMetadata getColumnMetadata() {
		if (columnMetadata == null) {
			columnMetadata = new ColumnMetadata();
		}
		return columnMetadata;
	}

	public void setColumnMetadata(ColumnMetadata columnMetadata) {
		this.columnMetadata = columnMetadata;
	}	
}
