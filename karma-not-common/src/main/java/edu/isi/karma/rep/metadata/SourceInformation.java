package edu.isi.karma.rep.metadata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceInformation {
	private Map<InfoAttribute, String> attributeValueMap = new HashMap<InfoAttribute, String>();
	
	public enum InfoAttribute {
		/*** Database source information attributes ***/
		dbType, hostname, portnumber, username, dBorSIDName, tableName
	}
	
	public Map<InfoAttribute, String> getAttributeValueMap() {
		return attributeValueMap;
	}
	
	public void setAttributeValue(InfoAttribute attr, String val) {
		attributeValueMap.put(attr, val);
	}
	
	public static List<InfoAttribute> getDatabaseInfoAttributeList() {
		InfoAttribute[] attrArr = {InfoAttribute.dbType, 
				InfoAttribute.hostname, InfoAttribute.portnumber, 
				InfoAttribute.username, InfoAttribute.dBorSIDName, 
				InfoAttribute.tableName};
		return Arrays.asList(attrArr);
	}
}
