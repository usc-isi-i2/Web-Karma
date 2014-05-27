package edu.isi.karma.kr2rml;


public class KR2RMLVersion implements Comparable<KR2RMLVersion>{

	public static final KR2RMLVersion unknown = new KR2RMLVersion(0, 0);
	public static final KR2RMLVersion current = new KR2RMLVersion(1, 2);
	
	public final int major;
	public final int minor;
	public final String value;
	
	public KR2RMLVersion(int major, int minor)
	{
		this.major = major;
		this.minor = minor;
		this.value = major + "." + minor;
	}

	public KR2RMLVersion(String version)
	{
		if(null == version )
		{
			throw new NullPointerException("No version to parse");
		}
		String[] components = version.split("\\.");
		if(components.length < 2)
		{
			throw new IllegalArgumentException("Not enough components in version string " + version);
		}
		major = Integer.parseInt(components[0]);
		minor = Integer.parseInt(components[1]);
		value = version;
	}
	@Override
	public int compareTo(KR2RMLVersion o) {
		
		if(this.getMajor() < o.getMajor())
		{
			return -1;
		}
		if(this.getMajor() == o.getMajor())
		{
			return this.getMinor() - o.getMinor();
		}
		else
		{
			return 1;
		}
	}
	
	public int getMajor()
	{
		return major;
	}
	public int getMinor()
	{
		return minor;
	}
	public String toString()
	{
		return value;
	}
	
	public static KR2RMLVersion getCurrent()
	{
		return current;
	}
}
