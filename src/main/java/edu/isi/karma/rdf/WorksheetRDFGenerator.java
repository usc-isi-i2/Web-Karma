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

package edu.isi.karma.rdf;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.FileUtil;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.gav.util.MediatorUtil;

/**
 * Provides methods for generating RDF from a Karma Worksheet.
 * generateTriplesRow(Worksheet w) - generates RDF row by row
 * <br> *use only on tables without nesting
 * <br> *generates RDF from the SourceDescription defined for the entire table
 * generateTriplesCell(Worksheet w) - generates RDF cell by cell
 * <br> *can be used on tables with or without nested tables
 * <br> *generates RDF from subrules defined for each column 
 * @author Maria Muslea(USC/ISI)
 *
 */

public class WorksheetRDFGenerator extends TableRDFGenerator{

	static Logger logger = Logger.getLogger(WorksheetRDFGenerator.class);

	private RepFactory factory;
	
	/** Creates a WorksheetRDFGenerator.
	 * @param factory
	 * @param domainStr
	 * 		a string in the format of a mediator domain file that contains namespaces and the 
	 * 		source description for this worksheet.Should contain "NAMESPACES" 
	 * 		and "LAV_RULES" sections (containing one rule).
	 * @param outputFile
	 * 		location of output file OR null if output to Stdout
	 * @throws MediatorException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public WorksheetRDFGenerator(RepFactory factory, String domainStr, String outputFile)
			throws MediatorException, ClassNotFoundException, IOException {
		super(domainStr, outputFile);
		this.factory=factory;
	}
	/*
	 * Example:
	 * 	StringWriter outS = new StringWriter();
	 *  PrintWriter pw = new PrintWriter(outS);
	 *  
	 *  new PrintWriter(System.out) 
	 */

	/** Creates a WorksheetRDFGenerator.
	 * @param factory
	 * @param domainStr
	 * 		a string in the format of a mediator domain file that contains namespaces and the 
	 * 		source description for this worksheet.Should contain "NAMESPACES" 
	 * 		and "LAV_RULES" sections (containing one rule).
	 * @param writer
	 * 		PrintWriter to a String or to System.out
	 * @throws MediatorException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public WorksheetRDFGenerator(RepFactory factory, String domainStr, PrintWriter writer)
		throws MediatorException, ClassNotFoundException, IOException {
		super(domainStr, writer);
		this.factory=factory;
	}

	
	/** Generates RDF for the given worksheet by invoking the RDF generator row by row.
	 * <br>Only for tables WITHOUT nested tables.
	 * <br>The source description for the entire table is used.
	 * @param w
	 * 		a worksheet
	 * @param useInternalColumnNames
	 * 		true if the SD uses HPath as column names (HN6/HN8/ColumnName)
	 * 		false if SD uses actual column names
	 * @throws MediatorException
	 * @throws IOException
	 */
	public void generateTriplesRow(Worksheet w, boolean useInternalColumnNames) throws MediatorException, IOException{
		//generate all triples for this worksheet (row by row)
		//the RuleRDFGenerator for SD is rdfGenerator (from the superclass)

		//for each row
		//logger.debug("Number of rows="+w.getDataTable().getNumRows());
		ArrayList<Row> rows = w.getDataTable().getRows(0, w.getDataTable().getNumRows());
		for(Row r:rows){
			//construct the values map
			Map<String,String> values;
			if(useInternalColumnNames)
				values = getValueMap(r);
			else
				values = getValueMapColumnName(r);
			logger.debug("Values=" + values);
			generateTriples(values);
		}
		//all triples written, so close the writer
		closeWriter();
	}

	/** Generates RDF for the given worksheet by invoking the RDF generator row by row.
	 * <br>Only for tables WITHOUT nested tables.
	 * <br>The source description for the entire table is used.
	 * @param w
	 * 		a worksheet
	 * @param useInternalColumnNames
	 * 		true if the SD uses HPath as column names (HN6/HN8/ColumnName)
	 * 		false if SD uses actual column names
	 * @param appendToWriter
	 * 		false: close this writer after RDF is generated
	 * 		true: don't close the writer; additional calls to generateTriplesRow() with the same writer will append data to the writer
	 * @throws MediatorException
	 * @throws IOException
	 */
	public void generateTriplesRow(Worksheet w, boolean useInternalColumnNames, boolean appendToWriter) throws MediatorException, IOException{
		//generate all triples for this worksheet (row by row)
		//the RuleRDFGenerator for SD is rdfGenerator (from the superclass)

		//for each row
		//logger.debug("Number of rows="+w.getDataTable().getNumRows());
		ArrayList<Row> rows = w.getDataTable().getRows(0, w.getDataTable().getNumRows());
		for(Row r:rows){
			//construct the values map
			Map<String,String> values;
			if(useInternalColumnNames)
				values = getValueMap(r);
			else
				values = getValueMapColumnName(r);
			logger.debug("Values=" + values);
			generateTriples(values);
		}
		//all triples written, so close the writer
		if(!appendToWriter)
			closeWriter();
	}
	
	//used for testing
	public void generateTriplesRowLimit(Worksheet w) throws MediatorException, IOException{
		logger.info("Generate RDF row by row ...");
		//generate all triples for this worksheet (row by row)
		//the RuleRDFGenerator for SD is rdfGenerator (from the superclass)
		
		//for each row
		//logger.debug("Number of rows="+w.getDataTable().getNumRows());
		ArrayList<Row> rows = w.getDataTable().getRows(0, 3);
		for(Row r:rows){
			//construct the values map
			Map<String,String> values = getValueMap(r);
			logger.debug("Values=" + values);
			generateTriples(values);
		}
		//all triples written, so close the writer
		closeWriter();
	}

	/** Generates RDF for the given worksheet by invoking the RDF generator cell by cell.
	 * @param w
	 * 		a worksheet
	 * @param useInternalColumnNames
	 * 		true if the SD uses HPath as column names (HN6/HN8/ColumnName)
	 * 		false if SD uses actual column names
	 * @throws MediatorException
	 * @throws IOException
	 * @throws KarmaException 
	 */
	public void generateTriplesCell(Worksheet w, boolean useInternalColumnNames) throws MediatorException, IOException, KarmaException{
		generateTriplesCell(w.getDataTable(), useInternalColumnNames);
		//all triples written, so close the writer
		closeWriter();		
	}
	/** Generates RDF for the given worksheet by invoking the RDF generator cell by cell.
	 * @param w
	 * 		a worksheet
	 * @param useInternalColumnNames
	 * 		true if the SD uses HPath as column names (HN6/HN8/ColumnName)
	 * 		false if SD uses actual column names
	 * @param appendToWriter
	 * 		false: close this writer after RDF is generated
	 * 		true: don't close the writer; additional calls to generateTriplesCell() with the same writer will append data to the writer
	 * @throws MediatorException
	 * @throws IOException
	 * @throws KarmaException 
	 */
	public void generateTriplesCell(Worksheet w, boolean useInternalColumnNames, boolean appendToWriter) throws MediatorException, IOException, KarmaException{
		generateTriplesCell(w.getDataTable(), useInternalColumnNames);
		//all triples written, so close the writer
		if(!appendToWriter)
			closeWriter();		
	}
	//used for testing; generate only for first 3 rows
	public void generateTriplesCellLimit(Worksheet w) throws MediatorException, IOException, KarmaException{
		logger.info("Generate RDF cell by cell ...");
		generateTriplesCellLimit(w.getDataTable());
		//all triples written, so close the writer
		closeWriter();		
	}
	
	/** Generates RDF for the given data table by invoking the RDF generator cell by cell.
	 * @param dataTable
	 * 		a Karma data table
	 * @param useInternalColumnNames
	 * 		true if the SD uses HPath as column names (HN6/HN8/ColumnName)
	 * 		false if SD uses actual column names
	 * @throws MediatorException
	 * @throws IOException
	 * @throws KarmaException 
	 * <br> for each node n 
	 * <br> if n has NestedTable t generateTriplesCell(t)
	 * <br> else generateTriplesCell(n)
	 */
	public void generateTriplesCell(Table dataTable, boolean useInternalColumnNames) throws MediatorException, IOException, KarmaException{
		//generate all triples cell by cell
		//for each row
		//logger.info("Generate triples for table:" + dataTable);
		ArrayList<Row> rows = dataTable.getRows(0, dataTable.getNumRows());
		//int i=0;
		for(Row r:rows){
			//for each node in the row
			//logger.debug("Process ROW="+i++);
			for (Node n : r.getNodes()) {
				if(!n.hasNestedTable()){
					//no nested table
					generateTriplesCell(n.getId(), useInternalColumnNames);
					//I want to see the triples as they are generated 
					outWriter.flush();
				}
				else{
					//this node has a nested table, so I have to go inside the nested table
					generateTriplesCell(n.getNestedTable(), useInternalColumnNames);
				}
			}
		}
	}

	//same as generateTriplesCell: used for testing; generate only for first 3 rows
	public void generateTriplesCellLimit(Table dataTable) throws MediatorException, IOException, KarmaException{
		//generate all triples cell by cell
		//for each row
		ArrayList<Row> rows = dataTable.getRows(0, 3);
		//int i=0;
		for(Row r:rows){
			//for each node in the row
			//logger.debug("Process ROW="+i++);
			for (Node n : r.getNodes()) {
				if(!n.hasNestedTable()){
					//no nested table
					generateTriplesCell(n.getId(),true);
					//I want to see the triples as they are generated 
					outWriter.flush();
				}
				else{
					//this node has a nested table, so I have to go inside the nested table
					generateTriplesCell(n.getNestedTable(),true);
				}
			}
		}
	}

	/** Generates RDF for a given node that does not contain a nested table.
	 *  <br>Generates RDF using the subrule for this Node.
	 *  <br>The values for other variables used in the subrule, can be found
	 *  <br> either in the same row as the current node (at the same level in the row, not in a nested table)
	 *  <br> OR, if n belongs  to a nested table, the values for other vars can come from the row that this
	 *  <br> nested table belongs to.
	 * @param nodeId
	 * 		a nodeId
	 * @param useInternalColumnNames
	 * 		true if the SD uses HPath as column names (HN6/HN8/ColumnName)
	 * 		false if SD uses actual column names
	 * @throws MediatorException
	 * @throws IOException
	 * @throws KarmaException 
	 */
	public void generateTriplesCell(String nodeId, boolean useInternalColumnNames) throws MediatorException, IOException, KarmaException{
		Node n = factory.getNode(nodeId);
		//logger.info("Generate triples for node:"+n);
		if(n.hasNestedTable()){
			//This should not happen
			throw new KarmaException("Node " + n.getHNodeId() + " contains a nested table. " +
					"Method generateTriplesCell(Node n) should ne invoked only for Nodes without nested tables.");
		}
		//value map used by the RDF generator (stores, the values of all columns used in the rule definition)
		HashMap<String,String> values = new HashMap<String,String>();
		//the value for this node
		String val = n.getValue().asString();
		//get the column name of this node
		String columnName = factory.getHNode(n.getHNodeId()).getHNodePath(factory).toColumnNamePath();
		if(!useInternalColumnNames)
			columnName = factory.getHNode(n.getHNodeId()).getColumnName();
		//logger.info("Generate triples for node:"+columnName +" with value=" + val);
		//transform the null to "", so that I can handle it as ""; basically it will not be added as a triple to RDF
		if(val==null) val="";
		values.put(columnName, val);
		
		//get other columns used in the RDF rule associated with columnName
		Set<String> relatedVars = getRelatedRDFVariables(columnName);
		if(relatedVars==null){
			//this node was not included in the SD;the semantic type was unassigned.
			return;
		}
		for(String var:relatedVars){
			var = MediatorUtil.removeBacktick(var);
			//logger.info("Value for related var:"+var);
			//var is a HNodePath+columnName
			//look for values in the row that this node belongs to or in the parent row...
			String varValue = getValueInRow(var,n.getBelongsToRow(),useInternalColumnNames);
			//logger.info("Value:"+varValue);
			if(varValue==null){
				//try the parent row; this node could be in a nested table, so we can look at nodes
				//that are in the same row as this nested table
				//for ex: person name that has multiple phone numbers
				//if I look at phone number node, the value for person name is in the parent row
				//logger.info("Parent table" + n.getParentTable());
				if(n.getParentTable().getNestedTableInNode()==null){
					//this is not a nested table, so I can't go to the parent Row
					throw new KarmaException("No value was found for node:" + var + ". Alignment of table is not correct!");
				}
				Row parentRow = n.getParentTable().getNestedTableInNode().getBelongsToRow();
				varValue = getValueInRow(var,parentRow,useInternalColumnNames);
				if(varValue==null){
					throw new KarmaException("No value was found for node:" + var + ". Alignment of table is not correct!");
				}
			}
			values.put(var,varValue);
		}
		logger.debug("Value Map:"+values);
		//a gensym may be needed to generate the tuples. In order to have the same gensym for the same nodes
		//we will use the HNodePath up to the last node (removing the last node)
		//there is no reason to not use the hpath for gensym, so I just leave it like this
		String gensym = "";
		String hNodePath = factory.getHNode(n.getHNodeId()).getHNodePath(factory).toString();
		int index = hNodePath.lastIndexOf("/");
		if(index>0){
			gensym = hNodePath.substring(0, index);
			//replace all "/" with "_"
			gensym = gensym.replaceAll("/", "_");
		}
		//else this is a top level node, so the gensym is just ""
		generateTriples(columnName,values,gensym);
	}


	/**
	 * Returns the value of the node defined by nodePath in the given Row,
	 * or null if row does not contain the node.
	 * @param nodePath
	 * 		nodePath in the form HNodePath/columnName if useInternalColumnNames=true
	 * 		nodePath is a columnName if useInternalColumnNames=false
	 * @param r
	 * 		a row
	 * @param useInternalColumnNames
	 * 		true if the SD uses HPath as column names (HN6/HN8/ColumnName) - nodePath is of this form
	 * 		false if SD uses actual column names - nodePath is an actual column name
	 * @return
	 * 		the value of the node defined by nodePath in the given Row,
	 * 		or null if row does not contain the node.
	 * @throws KarmaException
	 * 		if the node contains a nested table.
	 */
	private String getValueInRow(String nodePath, Row r, boolean useInternalColumnNames) throws KarmaException {
		//logger.info("Row="+r.hashCode() + " node path=" + nodePath);
		//for each node in the row
		for (Node n : r.getNodes()) {
			//get HNodePtah for this node
			//logger.info("hnodepath="+factory.getHNode(n.getHNodeId()).getHNodePath(factory));
			String columnName = factory.getHNode(n.getHNodeId()).getHNodePath(factory).toColumnNamePath();
			if(!useInternalColumnNames)
				columnName = factory.getHNode(n.getHNodeId()).getColumnName();
			//logger.info("Path="+nodePath + "col name:" + columnName);
			//logger.info("Row for this node="+n.getBelongsToRow().hashCode());			
			if(columnName.equals(nodePath)){
				//this is the node
				if(n.hasNestedTable()){
					//not good; I cannot get the value from a nested table, it has to be part of this row
					throw new KarmaException("Node " + nodePath + " contains a nested table. This node should contain a value not a nested table."  +
							" Alignment of table is not correct!");
				}
				else{
					//contains a value
					//logger.info("Value for:"+nodePath + "=" + n.getValue().asString());
					return n.getValue().asString();
				}
			}
		}
		//if I reached here the node was not found
		return null;
	}
	
	/**
	 * Returns column name/value pairs for all columns in a given row.
	 * <br>Only for tables WITHOUT nested tables.
	 * <br>Column name is the HNodePath with the column name at the end (HN1/HN2/ColumnName) 
	 * @param r
	 * 		a Row.
	 * @return
	 * 		column name/value pairs for all columns in a given row.
	 * Used with Rows that don't contain any nested tables.
	 */
	private Map<String, String> getValueMap(Row r) {
		HashMap<String, String> values = new HashMap<String, String>();
		//for each node in the row
		//I may add nodes that are not used in the SD, but that's OK
		for(Map.Entry<String, Node> node: r.getNodesMap().entrySet()){
			String val = node.getValue().getValue().asString();
			//the HNodePath for this node is used in the SD
			String columnName = factory.getHNode(node.getKey()).getHNodePath(factory).toColumnNamePath();
			//System.out.println("val for " + columnName + "=" + val + " is it null?" + (val==null));
			//transform the null to "", so that I can handle it as ""; basically it will not be added as a triple to RDF
			if(val==null) val="";
			values.put(columnName,val);
		}
		return values;
	}	
	
	/**
	 * Returns column name/value pairs for all columns in a given row.
	 * <br>Only for tables WITHOUT nested tables.
	 * <br>Column name is the actual column name  
	 * @param r
	 * 		a Row.
	 * @return
	 * 		column name/value pairs for all columns in a given row.
	 * Used with Rows that don't contain any nested tables.
	 */
	private Map<String, String> getValueMapColumnName(Row r) {
		HashMap<String, String> values = new HashMap<String, String>();
		//for each node in the row
		//I may add nodes that are not used in the SD, but that's OK
		for(Map.Entry<String, Node> node: r.getNodesMap().entrySet()){
			String val = node.getValue().getValue().asString();
			//the HNodePath for this node is used in the SD
			String columnName = factory.getHNode(node.getKey()).getColumnName();
			//System.out.println("val for " + columnName + "=" + val);
			//transform the null to "", so that I can handle it as ""; basically it will not be added as a triple to RDF
			if(val==null) val="";
			values.put(columnName,val);
		}
		return values;
	}	

	//test the RDF generation
	static public void testRDFGeneration(Workspace workspace, Worksheet worksheet, Alignment alignment) throws KarmaException{
		try{
			// Write the source description
			//use true to generate a SD with column names (for use "outside" of Karma)
			//use false for internal use

			SourceDescription desc = new SourceDescription(workspace, alignment, worksheet,
					"http://localhost/source/",true,false);
			String descString = desc.generateSourceDescription();
			System.out.println("SD="+ descString);
			//generate RDF for the first 3 rows: mariam
			WorksheetRDFGenerator wrg1 = new WorksheetRDFGenerator(workspace.getFactory(), descString, "./publish/RDF/rdftestrow.n3");
			WorksheetRDFGenerator wrg2 = new WorksheetRDFGenerator(workspace.getFactory(), descString, "./publish/RDF/rdftestcell.n3");
			if(worksheet.getHeaders().hasNestedTables()){
				logger.info("Has nested tables!!!");
				wrg2.generateTriplesCellLimit(worksheet);
			}
			else{
				wrg1.generateTriplesRowLimit(worksheet);
				wrg2.generateTriplesCellLimit(worksheet);	
			}
			//String fileName = "./publish/Source Description/"+worksheet.getTitle()+".txt";
			String fileName = "./publish/Source Description/W"+worksheet.getId()+".txt";
			FileUtil.writeStringToFile(descString, fileName);
			logger.info("Source description written to file: " + fileName);			
			////////////////////
		}catch(Exception e){
			e.printStackTrace();
			throw new KarmaException(e.getMessage());
		}

	}
}
