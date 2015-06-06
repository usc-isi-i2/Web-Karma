/*******************************************************************************
 * Copyright 2013 University of Southern California
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

package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.view.VWorkspace;

public class InvokeDataMiningServiceUpdate extends AbstractUpdate {

	private static Logger logger = LoggerFactory.getLogger(InvokeDataMiningServiceUpdate.class);
	
	private String dataFormat;
	private JSONObject data;
	
	private enum JsonKeys {
		results
	}
	
	public enum DataPrcessingFormats {
		testFormat
	}
	
	private enum JsonValues {
		InvokeDataMiningServiceUpdate
	}

	public InvokeDataMiningServiceUpdate(JSONObject output, String dataformat)
	{
		this.data = output;
		if (dataformat == null || dataformat.isEmpty()) {
			dataformat = DataPrcessingFormats.testFormat.name();
		}
		this.dataFormat = dataformat;
		
	}
	@Override
	public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) 
	{
		try { 
			if (this.dataFormat.equalsIgnoreCase(DataPrcessingFormats.testFormat.name()) ) {
				pw.print(processTestFormat());
			}
		} catch (Exception e) {
			logger.error("Error generating JSON!", e);
		}
	}
	
	private String processTestFormat() {
		JSONObject obj = new JSONObject();
		try {
			obj.put(GenericJsonKeys.updateType.name(), JsonValues.InvokeDataMiningServiceUpdate.name());
			obj.put(JsonKeys.results.name(), this.data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj.toString();
	}
}
