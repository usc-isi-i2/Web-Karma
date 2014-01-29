package edu.isi.karma.kr2rml;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

import edu.isi.karma.kr2rml.formatter.KR2RMLColumnNameFormatter;
import edu.isi.karma.rep.metadata.WorksheetProperties.SourceTypes;

public class KR2RMLMapping extends R2RMLMapping{
	
	private KR2RMLVersion version;
	private KR2RMLMappingAuxillaryInformation auxInfo;
	private Map<String, SubjectMap> subjectMapIndex;
	private Map<String, TriplesMap> triplesMapIndex;
	private JSONArray worksheetHistory;
	private KR2RMLColumnNameFormatter formatter;
	private boolean isR2RMLCompatible;
	private boolean isRMLCompatible;
	private SourceTypes sourceType; 
	
	public KR2RMLMapping(R2RMLMappingIdentifier id, KR2RMLVersion version)
	{
		super(id);
		this.version = version;
		this.auxInfo = new KR2RMLMappingAuxillaryInformation();	
		this.subjectMapIndex = new HashMap<String, SubjectMap>();
		this.triplesMapIndex = new HashMap<String, TriplesMap>();
	}

	public KR2RMLVersion getVersion()
	{
		return version;
	}
	
	public KR2RMLMappingAuxillaryInformation getAuxInfo() {
		return auxInfo;
	}
	
	public Map<String, SubjectMap> getSubjectMapIndex() {
		return subjectMapIndex;
	}

	public Map<String, TriplesMap> getTriplesMapIndex() {
		return triplesMapIndex;
	}

	public JSONArray getWorksheetHistory() {
		return worksheetHistory;
	}

	public void setWorksheetHistory(JSONArray worksheetHistory) {
		this.worksheetHistory = worksheetHistory;
	}

	public void setColumnNameFormatter(KR2RMLColumnNameFormatter formatter) {
		this.formatter = formatter;
	}
	public KR2RMLColumnNameFormatter getColumnNameFormatter()
	{
		return formatter;
	}

	public boolean isR2RMLCompatible() {
		return isR2RMLCompatible;
	}

	public boolean isRMLCompatible()
	{
		return isRMLCompatible;
	}
	
	public void setR2RMLCompatible(boolean isR2RMLCompatible) {
		this.isR2RMLCompatible = isR2RMLCompatible;
	}

	public void setRMLCompatible(boolean isRMLCompatible)
	{
		this.isRMLCompatible = isRMLCompatible;
	}

	public SourceTypes getSourceType() {
		return sourceType;
	}

	public void setSourceType(SourceTypes sourceType) {
		this.sourceType = sourceType;
	}
}
