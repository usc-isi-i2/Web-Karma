package edu.isi.karma.rdf;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.gav.util.MediatorUtil;
import edu.isi.mediator.rdf.TableRDFGenerator;

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
	 * @throws MediatorException
	 * @throws IOException
	 */
	public void generateTriplesRow(Worksheet w) throws MediatorException, IOException{
		//generate all triples for this worksheet (row by row)
		//the RuleRDFGenerator for SD is rdfGenerator (from the superclass)
		
		//for each row
		//logger.debug("Number of rows="+w.getDataTable().getNumRows());
		ArrayList<Row> rows = w.getDataTable().getRows(0, w.getDataTable().getNumRows());
		for(Row r:rows){
			//construct the values map
			Map<String,String> values = getValueMap(r);
			logger.debug("Values=" + values);
			generateTriples(values);
		}
		//all triples written, so close the writer
		closeWriter();
	}
	//used for testing
	public void generateTriplesRowLimit(Worksheet w) throws MediatorException, IOException{
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
	 * @throws MediatorException
	 * @throws IOException
	 * @throws KarmaException 
	 */
	public void generateTriplesCell(Worksheet w) throws MediatorException, IOException, KarmaException{
		generateTriplesCell(w.getDataTable());
		//all triples written, so close the writer
		closeWriter();		
	}
	//used for testing; generate only for first 3 rows
	public void generateTriplesCellLimit(Worksheet w) throws MediatorException, IOException, KarmaException{
		generateTriplesCellLimit(w.getDataTable());
		//all triples written, so close the writer
		closeWriter();		
	}
	
	/** Generates RDF for the given data table by invoking the RDF generator cell by cell.
	 * @param dataTable
	 * 		a Karma data table
	 * @throws MediatorException
	 * @throws IOException
	 * @throws KarmaException 
	 * <br> for each node n 
	 * <br> if n has NestedTable t generateTriplesCell(t)
	 * <br> else generateTriplesCell(n)
	 */
	public void generateTriplesCell(Table dataTable) throws MediatorException, IOException, KarmaException{
		//generate all triples cell by cell
		//for each row
		ArrayList<Row> rows = dataTable.getRows(0, dataTable.getNumRows());
		//int i=0;
		for(Row r:rows){
			//for each node in the row
			//logger.debug("Process ROW="+i++);
			for (Node n : r.getNodes()) {
				if(!n.hasNestedTable()){
					//no nested table
					generateTriplesCell(n.getId());
					//I want to see the triples as they are generated 
					outWriter.flush();
				}
				else{
					//this node has a nested table, so I have to go inside the nested table
					generateTriplesCell(n.getNestedTable());
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
					generateTriplesCell(n.getId());
					//I want to see the triples as they are generated 
					outWriter.flush();
				}
				else{
					//this node has a nested table, so I have to go inside the nested table
					generateTriplesCell(n.getNestedTable());
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
	 * @throws MediatorException
	 * @throws IOException
	 * @throws KarmaException 
	 */
	public void generateTriplesCell(String nodeId) throws MediatorException, IOException, KarmaException{
		Node n = factory.getNode(nodeId);
		//logger.debug("Generate triples for node:"+n);
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
		String columnName = factory.getHNode(n.getHNodeId()).getHNodePath(factory).toColumnNames();
		//logger.info("Generate triples for node:"+columnName +" with value=" + val);
		values.put(columnName, val);
		//get other columns used in the RDF rule associated with columnName
		Set<String> relatedVars = getRelatedRDFVariables(columnName);
		if(relatedVars==null){
			//this node was not included in the SD;the semantic type was unassigned.
			return;
		}
		for(String var:relatedVars){
			var = MediatorUtil.removeBacktick(var);
			logger.info("Value for:"+var);
			//var is a HNodePath+columnName
			//look for values in the row that this node belongs to or in the parent row...
			String varValue = getValueInRow(var,n.getBelongsToRow());
			logger.info("Value:"+varValue);
			if(varValue==null){
				//try the parent row; this node could be in a nested table, so we can look at nodes
				//that are in the same row as this nested table
				//for ex: person name that has multiple phone numbers
				//if I look at phone number node, the value for person name is in the parent row
				Row parentRow = n.getParentTable().getNestedTableInNode().getBelongsToRow();
				varValue = getValueInRow(var,parentRow);
				if(varValue==null){
					throw new KarmaException("No value was found for node:" + var);
				}
			}
			values.put(var,varValue);
		}
		logger.debug("Value Map:"+values);
		//a gensym may be needed to generate the tuples. In order to have the same gensym for the same nodes
		//we will use the HNodePath up to the last node (removing the last node)
		String gensym = "";
		String hNodePath = factory.getHNode(n.getHNodeId()).getHNodePath(factory).toString();
		int index = hNodePath.lastIndexOf("/");
		if(index>0){
			gensym = hNodePath.substring(0, index);
		}
		//else this is a top level node, so the gensym is just ""
		generateTriples(columnName,values,gensym);
	}


	/**
	 * Returns the value of the node defined by nodePath in the given Row,
	 * or null if row does not contain the node.
	 * @param nodePath
	 * 		nodePath in the form HNodePath/columnName
	 * @param r
	 * 		a row
	 * @return
	 * 		the value of the node defined by nodePath in the given Row,
	 * 		or null if row does not contain the node.
	 * @throws KarmaException
	 * 		if the node contains a nested table.
	 */
	private String getValueInRow(String nodePath, Row r) throws KarmaException {
		//logger.debug("Row="+r.hashCode());
		//for each node in the row
		for (Node n : r.getNodes()) {
			//get HNodePtah for this node
			String path = factory.getHNode(n.getHNodeId()).getHNodePath(factory).toColumnNames();
			//logger.debug("Path="+path);
			//logger.debug("Row for this node="+n.getBelongsToRow().hashCode());			
			if(path.equals(nodePath)){
				//this is the node
				if(n.hasNestedTable()){
					//not good; I cannot get the value from a nested table, it has to be part of this row
					throw new KarmaException("Node " + nodePath + " contains a nested table. This node should contain a value.");
				}
				else{
					//contains a value
					//logger.debug("Value for:"+path + "=" + n.getValue().asString());
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
			String columnName = factory.getHNode(node.getKey()).getHNodePath(factory).toColumnNames();
			//System.out.println("val for " + columnName + "=" + val);
			values.put(columnName,val);
		}
		return values;
	}	
	
}
