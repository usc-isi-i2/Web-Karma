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

package edu.isi.karma.kr2rml;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ErrorReport {
	private Set<ReportMessage> reports;
	private enum JsonKeys {
		title, description, priority
	}
	
	public enum Priority {
		high, medium, low
	}
	
	public ErrorReport() {
		this.reports = new HashSet<ErrorReport.ReportMessage>();
	}
	
	public void addReportMessage(String title, String description, Priority priority) {
		ReportMessage rep = new ReportMessage(title, description, priority);
		reports.add(rep);
	}
	
	private class ReportMessage {
		private String title;
		private String description;
		private Priority priority;
		
		public ReportMessage(String title, String description, Priority priority) {
			super();
			this.title = title;
			this.description = description;
			this.priority = priority;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((description == null) ? 0 : description.hashCode());
			result = prime * result
					+ ((priority == null) ? 0 : priority.hashCode());
			result = prime * result + ((title == null) ? 0 : title.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof ReportMessage))
				return false;
			ReportMessage other = (ReportMessage) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (description == null) {
				if (other.description != null)
					return false;
			} else if (!description.equals(other.description))
				return false;
			if (priority != other.priority)
				return false;
			if (title == null) {
				if (other.title != null)
					return false;
			} else if (!title.equals(other.title))
				return false;
			return true;
		}

		private ErrorReport getOuterType() {
			return ErrorReport.this;
		}
	}
	
	public String toJSONString() throws JSONException {
		JSONArray repArr = new JSONArray();
		for (ReportMessage rep:reports) {
			JSONObject repObj = new JSONObject();
			repObj.put(JsonKeys.title.name(), rep.title);
			repObj.put(JsonKeys.description.name(), rep.description);
			repObj.put(JsonKeys.priority.name(), rep.priority);
			
			repArr.put(repObj);
		}
		return repArr.toString();
	}
	
	public boolean hasReports() {
		return reports.size() != 0;
	}
}


