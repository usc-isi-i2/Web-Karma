package edu.isi.karma.kr2rml;

public class KR2RMLMapping extends R2RMLMapping{
	
	private KR2RMLVersion version;
	private KR2RMLMappingAuxillaryInformation auxInfo;
	
	public KR2RMLMapping(R2RMLMappingIdentifier id, KR2RMLVersion version)
	{
		super(id);
		this.version = version;
		this.auxInfo = new KR2RMLMappingAuxillaryInformation();	
	}

	public KR2RMLVersion getVersion()
	{
		return version;
	}
	
	public KR2RMLMappingAuxillaryInformation getAuxInfo() {
		return auxInfo;
	}
	
	
}
