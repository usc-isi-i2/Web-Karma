// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.simplegraph;

import java.util.*;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.rule.ConstTerm;
import edu.isi.mediator.rule.FunctionPredicate;
import edu.isi.mediator.rule.FunctionTerm;
import edu.isi.mediator.rule.Term;
import edu.isi.mediator.domain.SourceAttribute;

public abstract class SimpleBaseNode
{
    public SimpleBaseNode parent;
    
    //for building the sql query
    protected ArrayList<String> sqlSelect = new ArrayList<String>();
    protected ArrayList<String> sqlFrom = new ArrayList<String>();
    protected ArrayList<String> sqlWhere = new ArrayList<String>();
    
    protected boolean alreadyPrinted = false;
    
	/**
	 * true if it is a distinct query and false otherwise
	 */
	boolean distinct = true; 
    public SimpleBaseNode(){}
    
    public abstract ArrayList<SimpleBaseNode> getSubNodes();
    public abstract String getString();
    //for each node set values in select, from and where
    public abstract void setSQL() throws MediatorException;
    
	public void setDistinct(boolean b){
		distinct=b;
	}
    //combine select, from and where into a  query
    /**
     * Returns the SQL for this query.
     * @return
     * 		the SQL for this query.
     * @throws MediatorException
     */
    public String getSQL() throws MediatorException{
    	
    	//sqlSelect = prepareSqlSelect();
    	
    	String distinctToken = "";
    	if(distinct){
    		distinctToken = "DISTINCT";
    	}
    	
    	String sql="";
    	sql += "select " + distinctToken + " " +sqlSelect.toString().substring(1,sqlSelect.toString().length()-1)+" from "+ sqlFrom.toString().substring(1,sqlFrom.toString().length()-1)+" ";
    	if (sqlWhere.size()>0)
    		sql += " where ";
    	for (int j=0;j<sqlWhere.size();j++)
    	{
    		String nextSelect = (String)sqlWhere.get(j);
    		//System.out.println("next select ..." + nextSelect + "\n SQL so far:" + sql);
    		if(sql.indexOf(nextSelect)<0){
    			if (j>0) sql += " and ";
    			//insert if not there
    			sql += nextSelect;
    		}
    	}
    	return sql;
    }
    
    //get the equiv SA for this attr
    //look at all DAN that are children of this node, and find the source that
    //references this attribute
    protected SourceAttribute getSourceAttribute(String attr){
    	SourceAttribute sAttr = null;
    	
    	ArrayList<SimpleDataAccessNode> allDan =  new ArrayList<SimpleDataAccessNode>();
    	getDataAccessNodes(allDan);
    	for(int i=0; i<allDan.size(); i++){
    		SimpleDataAccessNode dan = allDan.get(i);
    		sAttr = dan.getSourceAttribute(attr);
    		if(sAttr!=null) return sAttr;
    	}
    	
    	return sAttr;
    }
    
    
    //look in the select array, and find the actual name of attr
    // it will be tableName.attrName as attr
    protected String getActualName(String attr) throws MediatorException{
    	//System.out.println("Search for " + attr + " in " + sqlSelect);
    	for(int i=0; i<sqlSelect.size(); i++){
    		String oneSelect = sqlSelect.get(i);
    		//I want to make sure that I match with EXACTLY that attr name (that's why I have the " " at the end
    		int ind = oneSelect.indexOf(" as " + attr + " ");
    		if(ind>0)
    			return oneSelect.substring(0, ind);
    	}
    	//attr not found
		throw new MediatorException("Attribute " + attr + " not found in " + sqlSelect);
   }
    
    //look in the select array, and find the actual name of all the vars inside this predicate
    // it will be tableName.attrName as attr
    //return a string that is the predicate with the actualnamaes inside
    public String getActualName(FunctionPredicate p) throws MediatorException{
    	String actualName = p.getName() + "(";
    	ArrayList<Term> terms = p.getTerms();
    	for(int k=0; k<terms.size(); k++){
    		Term t = terms.get(k);
    		if(k!=0) actualName += ",";
    		if(t instanceof FunctionTerm)
    			actualName += getActualName(t.getFunction());
    		else if(t instanceof ConstTerm){
    			SourceAttribute sa = p.getSourceAttribute(k);
				actualName += t.getSqlVal(sa.isNumber());
    		}
    		else{
    			//it's a var that needs to be replaced with it's actual name
    			actualName += getActualName(t.getVar());
    		}
    	}
    	actualName += ")";
    	return actualName;
    }

    /** Gets all source attributes names present in the sqlSelect list.
     * <br> These are all the selected attributes at this node.
     * @return
     * 		all source attributes names present in the sqlSelect list.
     * @throws MediatorException
     */
    protected ArrayList<String> getAllActualVarNames() throws MediatorException{
    	ArrayList<String> allAttr = new ArrayList<String>();
    	for(int i=0; i<sqlSelect.size(); i++){
    		String oneSelect = sqlSelect.get(i);
    		int ind = oneSelect.indexOf(" as ");
    		if(ind>0){
    			String varWithTable = oneSelect.substring(0, ind);
    	    	ind = varWithTable.indexOf(".");
    	    	if(ind>0 && ind+1<varWithTable.length()-1) 
    	    		allAttr.add(varWithTable.substring(ind+1));
    		}
    	}
    	return allAttr;
    }
    
   //remove from select the attributes in joinAttributes
    protected ArrayList<String> removeFromSelect(ArrayList<String> joinAttributes){
    	ArrayList<String> newSelect = new ArrayList<String>();
    	for(int j=0; j<sqlSelect.size(); j++){
    		String oneSelect = sqlSelect.get(j);
    		boolean addSelect = true;
    		for(int i=0; i<joinAttributes.size(); i++){
    			String joinAttr = joinAttributes.get(i);
    			int ind = oneSelect.indexOf(" as " + joinAttr +" ");
    			if(ind>0) addSelect = false; 
    		}
    		if(addSelect) newSelect.add(oneSelect);
    	}
    	return newSelect;
    }
    
    public void getDataAccessNodes(ArrayList<SimpleDataAccessNode> dan){
    	ArrayList<SimpleBaseNode> nodes = getSubNodes();
    	if(nodes==null) return;
    	for(int i=0; i<nodes.size(); i++){
    		SimpleBaseNode pn = (SimpleBaseNode)nodes.get(i);
    		if(pn instanceof SimpleDataAccessNode)
    			dan.add((SimpleDataAccessNode)pn);
    		else pn.getDataAccessNodes(dan);
    	}
    }
    
    //returns "@" if this attribute is bound in the data source
    //this method is redefined in DataAccessNode
    String needsBinding(String attr){
    	return "";
    }
    
    public String getPrintName(){
    	return "------------ BaseNode " + this+ "---------------\n";
    }


}


