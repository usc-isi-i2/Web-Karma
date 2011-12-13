// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.graph;

import java.util.*;

import edu.isi.mediator.gav.main.MediatorException;

public class UnionNode extends BaseNode
{
    public ArrayList<BaseNode> nodesToBeUnioned = new ArrayList<BaseNode>();
    
    /**
     * Specifies a Union between domain level queries
     */
    public boolean domainLevelUnion = false;
    
    public UnionNode(){}
    
    public ArrayList<BaseNode> getSubNodes(){
    	return nodesToBeUnioned;
    }
    
    public void addSubNode(BaseNode n){
    	nodesToBeUnioned.add(n);
    }
    
    //don't do anything for union nodes; e will just do UNION of getSQL() for the children
    public void setSQL() throws MediatorException{
    	for (int i=0;i<nodesToBeUnioned.size();i++){
    	    BaseNode bn = (BaseNode)nodesToBeUnioned.get(i);
    	    bn.setSQL();
    	    //sqlSelect.add(bn.sqlSelect);
    	    //sqlWhere.add(bn.sqlWhere);
    	    //sqlFrom.add(bn.sqlFrom);
    	}
    }
    
    public String getSQL() throws MediatorException{
    	//do the union
    	String s = "";
    	for (int i=0;i<nodesToBeUnioned.size();i++){
    	    BaseNode bn = (BaseNode)nodesToBeUnioned.get(i);
    	    String q = bn.getSQL();
    	    if(i>0) s += " UNION ";
    	    /*
    		s += "( select " + select.toString().substring(1,select.toString().length()-1)+
    			" from "+ from.toString().substring(1,from.toString().length()-1)+
    			" where "+ where.toString().substring(1,where.toString().length()-1) + " )";
    			*/
    	    s += "(" + q + ")";
    	}
		//apply operations to the core query (functions, grouping, etc.)
    	//for the top union that links domain queries I only want to add order by if it exists
    	if(queryDecomposition!=null && !domainLevelUnion)
    		s = queryDecomposition.getSQL(s);

    	return s;
    }
    
    /**
     * Returns the SQL for this query.
     * @return
     * 		the SQL for this query.
     * @throws MediatorException
     */
    public String getSQLWithOrderBy() throws MediatorException{
    	String sql = getSQL();
    	if(queryDecomposition!=null){
    		if(domainLevelUnion)
    			sql=queryDecomposition.addOrderBy(sql, true);
    		else 
    			sql=queryDecomposition.addOrderBy(sql, false);
    	}
    	return sql;
    }

    public String getSQLOld(){
    	//do the union
    	String s = "";
    	/*
    	for(int i=0; i<sqlSelect.size(); i++){
    		ArrayList<String> select = sqlSelect.get(i);
    		ArrayList<String> from = sqlFrom.get(i);
    		ArrayList<String> where = sqlWhere.get(i);
    		if(i>0) s += " UNION ";
    		s += "( select " + select.toString().substring(1,select.toString().length()-1)+
    			" from "+ from.toString().substring(1,from.toString().length()-1)+
    			" where "+ where.toString().substring(1,where.toString().length()-1) + " )";
    	}
    	*/
    	return s;
    }

    public String getString(){
    	String s = "";
    	s += "---------- Union " + this+ "----------\n";
    	for (int i=0;i<nodesToBeUnioned.size();i++){
    	    BaseNode bn = (BaseNode)nodesToBeUnioned.get(i);
    	    s += "child " + i + "=" + bn.getString();
    	}
    	s += " QD:" + queryDecomposition;
    	s += "-------------------------------------\n";
    	return s;
    }
}
