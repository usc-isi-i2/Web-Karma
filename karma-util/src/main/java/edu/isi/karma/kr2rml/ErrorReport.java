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
		this.reports = new HashSet<>();
	}
	
	public void addReportMessage(ReportMessage errMsg) {
		reports.add(errMsg);
	}
	
	public void combine(ErrorReport other)
	{
		for(ReportMessage msg : other.reports)
		{
			reports.add(msg);
		}
	}
	public static ErrorReport merge(ErrorReport first, ErrorReport second)
	{
		ErrorReport newReport = new ErrorReport();
		for(ReportMessage msg : first.reports)
		{
			newReport.addReportMessage(msg);
		}
		for(ReportMessage otherMsg :second.reports)
		{
			newReport.addReportMessage(otherMsg);
		}
		return newReport;
	}
	public String toJSONString() throws JSONException {
		JSONArray repArr = new JSONArray();
		for (ReportMessage rep:reports) {
			JSONObject repObj = new JSONObject();
			repObj.put(JsonKeys.title.name(), rep.getTitle());
			repObj.put(JsonKeys.description.name(), rep.getDescription());
			repObj.put(JsonKeys.priority.name(), rep.getPriority());
			
			repArr.put(repObj);
		}
		return repArr.toString();
	}
	
	public boolean hasReports() {
		return !reports.isEmpty();
	}
}


