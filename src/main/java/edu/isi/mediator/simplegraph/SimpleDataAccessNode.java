// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.simplegraph;

import java.util.*;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.gav.util.MediatorConstants;
import edu.isi.mediator.rule.ConstTerm;
import edu.isi.mediator.rule.FunctionTerm;
import edu.isi.mediator.rule.RelationPredicate;
import edu.isi.mediator.rule.Term;
import edu.isi.mediator.rule.VarTerm;
import edu.isi.mediator.gav.source.SourceAttribute;

public class SimpleDataAccessNode extends SimpleBaseNode
{
	
	private RelationPredicate source;
	private SimpleBaseNode child;
	
	
	/**
	 * Used to generate unique table names
	 */
	
	public SimpleDataAccessNode(RelationPredicate p){
		source = p;
	}
	
	public void setChild(SimpleBaseNode n){
		child=n;
	}
	
	public ArrayList<SimpleBaseNode> getSubNodes() {
		ArrayList<SimpleBaseNode> subnodes = new ArrayList<SimpleBaseNode>();
		if (child != null)
			subnodes.add(child);
		return subnodes;
	}

    protected SourceAttribute getSourceAttribute(String attr){
    	return source.getSourceAttribute(attr);
    }
    
    //returns "@" if this attribute is bound in the data source
    String needsBinding(String attr){
    	return source.needsBinding(attr);
    }
    
	//go to all DAN in dn and see if we find common attributes
	//returns the common attributes in commonAttrs
	public void findCommonAttrs(SimpleBaseNode dn, ArrayList<String> commonAttrs){
		//System.out.println("Find common DN=" + this.getString());
		if(dn instanceof SimpleDataAccessNode){
			//System.out.println("try with DN=" + dn.getString());
			ArrayList<String> joinAttrs = source.findCommonAttrs(((SimpleDataAccessNode)dn).source);
			for(int i=0; i<joinAttrs.size(); i++){
				String attr = joinAttrs.get(i);
				if(!commonAttrs.contains(attr))
					commonAttrs.add(attr);
			}
			//System.out.println(" commonAttrs " + commonAttrs);
		}
		else{
			ArrayList subnodes = dn.getSubNodes();
			for(int i=0; i<subnodes.size(); i++){
				SimpleBaseNode child = (SimpleBaseNode)subnodes.get(i);
				findCommonAttrs(child, commonAttrs);
			}	
		}
	}
	
	public void setSQL() throws MediatorException{
		//if (childNode != null)
			//return new ArrayList();
		
		//I already did setSQL() in this node
    	//because of binding patterns there may be 2 ways to this node
		if(!sqlSelect.isEmpty())
			return;
		
		if(child!=null){
			child.setSQL();
		}
		
		//MariaM:120810
		//if the table name is escaped, escape the entire alias
		String sourceName = source.getName();
		int sourceId = MediatorConstants.getTableId();
		String newName = sourceName + "_" + sourceId;
		//we use endsWith instead of startsWith because for sources that include the OGSA-DAI resource
		// it looks like: TestR2_`table name`
		if(sourceName.endsWith("`"))
			newName = sourceName.substring(0, sourceName.length()-1) + "_" + sourceId + "`";
		
		if(newName.length()>29)
			newName = "T"+MediatorConstants.getTableId();
		sqlFrom.add(source.getSQLName()+" "+ newName);
		String tableName = newName;
		

		//set select
		sqlSelect = getSelect(tableName);
		
		//System.out.println("DN select=" + sqlSelect);
		
		//set where
		sqlWhere = getWhere(tableName, child);
		
	}

	/**
	 * Returns the "select" SQL statement based on terms and source attributes
	 * @param tableName
	 * 			the source name of this relation
	 * @return
	 * 		"select" SQL statement
	 */
	public ArrayList<String> getSelect(String tableName){
		//System.out.println("P in getSelect():" + this + "***" + this.hashCode());
		ArrayList<String> select = new ArrayList<String>();
		for(int i=0; i<source.getTerms().size(); i++){
			Term t = source.getTerms().get(i);
			if(t.getVar()==null) continue;
			SourceAttribute sourceAttr = source.getSourceAttribute(i);
			//I'm adding a space at then end, so it's easier when I have to search for the var name
			//otherwise if I search for "S" it will return true for any variable that starts with "S"
			select.add(tableName+"."+sourceAttr.getSQLName()+" as "+t.getVar() + " ");
		}
		return select;
	}

	//if I have 2 terms that are the same variable we will have a join
	//if I have a bound attr that is not a constant it will be handled in the Join
	/**
	 * Returns the "where" SQL statement based on terms and sourceAttrs.
	 * <br> If the term is a constant or it is a bound variable that is satisfied by a constant
	 * <br> a comparison is added to the where clause
	 * @param tableName
	 * 		the source name of this relation
	 * @param child
	 * 		the child of the DataAccessNode that represents this relation
	 * @return
	 * 		"where" SQL statement
	 * @throws MediatorException
	 */
	public ArrayList<String> getWhere(String tableName, SimpleBaseNode child) throws MediatorException{
		ArrayList<String> where = new ArrayList<String>();
		for(int i=0; i<source.getTerms().size(); i++){
			Term t = source.getTerms().get(i);
			SourceAttribute sourceAttr = source.getSourceAttribute(i);
			boolean isBound = sourceAttr.needsBinding();
			//System.out.println("Pred=" + name + " SourceAttr is bound " + sourceAttr + " " + isBound);
			String bindMarker="";
			if(isBound){
				bindMarker="@";
			}
			if(t instanceof ConstTerm){
				String val = ((ConstTerm)t).getSqlVal(sourceAttr.isNumber());
				if(val.equals(MediatorConstants.NULL_VALUE))
					where.add(bindMarker + tableName+"."+sourceAttr.getSQLName()+" IS "+val);
				else
					where.add(bindMarker + tableName+"."+sourceAttr.getSQLName()+" = "+val);
			}
			if(t instanceof FunctionTerm){
				if(child!=null){
					//I have attribute binding patterns in the function
					where.add(bindMarker + tableName+"."+sourceAttr.getSQLName()+" = "+child.getActualName(((FunctionTerm)t).getFunction()));
				}
				else{
					//the function has only constants as input
					where.add(bindMarker + tableName+"."+sourceAttr.getSQLName()+" = "+((FunctionTerm)t).getFunctionForSQL());
				}
			}
		}
		for(int i=0; i<source.getTerms().size(); i++){
			Term t = source.getTerms().get(i);
			SourceAttribute sourceAttr1 = source.getSourceAttribute(i);
			//System.out.println("Find duplicate terms:" + t);
			if(t instanceof VarTerm){
				//System.out.println("Not Constant...");
				int j = i+1;
				while(j>0){
					j = source.findTerm(t, j);
					if(j>0){
						SourceAttribute sourceAttr2 = source.getSourceAttribute(j);
						where.add(tableName+"."+sourceAttr1.getSQLName()+" = "+tableName + "." + sourceAttr2.getSQLName());
						j++;
					}
				}
			}
		}
		return where;
	}

	public String getString(){
		alreadyPrinted=true;
		String s = "";
		s += getPrintName();
		s += source + "\n";
		if(child == null)
			s += "child=NULL\n";
		else{
    		if(child.alreadyPrinted)
    			s += "child=" + child.getPrintName();
    		else
    			s += "child="+ child.getString();
		}
		s += "-------------------------------------\n";
		return s;
	}
	
    public String getPrintName(){
    	return "------------ DataAccess " + this+ "---------------\n";
    }

}
