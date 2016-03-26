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

package edu.isi.karma.imp.mdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.healthmarketscience.jackcess.ColumnBuilder;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.ImportFilter;
import com.healthmarketscience.jackcess.ImportUtil;
import com.healthmarketscience.jackcess.SimpleImportFilter;
import com.healthmarketscience.jackcess.TableBuilder;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class MDBFileExport {
	private Worksheet worksheet;
	private Workspace workspace;
	private static final Logger logger = LoggerFactory
			.getLogger(MDBFileExport.class);
	public MDBFileExport(Workspace workspace, Worksheet worksheet) {
		this.workspace = workspace;
		this.worksheet = worksheet;
	}
	public String publishMDB(String csvFileName) throws FileNotFoundException {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		String outputFile = "publish/MDB/" + worksheet.getTitle() + ".mdb";
		logger.info("MDB file exported. Location:"
				+ outputFile);

		HashMap<String, String> modeledColumnTable = new HashMap<>();
		for (SemanticType type : worksheet.getSemanticTypes().getListOfTypes()) {
			modeledColumnTable.put(type.getHNodeId(),"");
		}
		int numRows = worksheet.getDataTable().getNumRows();
		if(numRows==0) 
			return "";
		
		Database db;
		try {
			db = Database.create(new File(contextParameters.getParameterValue(ContextParameter.USER_DIRECTORY_PATH)+outputFile));
			
			TableBuilder tb = new TableBuilder(worksheet.getTitle().replaceAll("\\.", "_"));
			
			List<HNode> sortedLeafHNodes = new ArrayList<>();
			List<String> hNodeIdList = new ArrayList<>();
			worksheet.getHeaders().getSortedLeafHNodes(sortedLeafHNodes);
			for (HNode hNode : sortedLeafHNodes) {
				if (modeledColumnTable.containsKey(hNode.getId())) {
					String columnName = hNode.getColumnName();
					if (columnName.equals(""))
						columnName = "NA";
					tb.addColumn(new ColumnBuilder(columnName).setType(
							DataType.MEMO).toColumn());
					hNodeIdList.add(hNode.getId());
				}
			}
			
			tb.toTable(db);
			
			ImportFilter _filter = SimpleImportFilter.INSTANCE;
			ImportUtil.importFile(new File(contextParameters.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) +csvFileName),
					db,worksheet.getTitle().replaceAll("\\.", "_"), ",",'\"',_filter,true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return outputFile;
	}
}
