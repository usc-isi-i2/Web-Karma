package edu.isi.karma.kr2rml;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ShortHandURIGenerator {

	private Set<Prefix> prefixes = new HashSet<>();
	private Map<String, Prefix> prefixMapping = new ConcurrentHashMap<>();

	public ShortHandURI getShortHand(String URI) {
		Prefix p = prefixMapping.get(URI);
		if (p != null) {
			return createShortHand(URI, p);
		}
		for (Prefix prefix : prefixes) {
			if (URI.indexOf(prefix.getNamespace()) >= 0) {
				prefixMapping.put(URI, prefix);
				return createShortHand(URI, prefix);
			}
		}
		return new ShortHandURI(null, URI);
	}

	private ShortHandURI createShortHand(String URI, Prefix p) {
		if(URI.startsWith("<") || URI.endsWith(">"))
		{
			URI = URI.substring(URI.startsWith("<") ?1: 0, URI.endsWith(">")?URI.length()-1:URI.length());
		}
		
		try{
			
		
			return new ShortHandURI(p.getPrefix(), URI.substring(p.getNamespace().length()));
		}
		catch(Exception e)
		{
			return new ShortHandURI(null, URI);
		}
	}

	public void addPrefixes(Collection<Prefix> prefixes) {
		this.prefixes.addAll(prefixes);
		
	}
}
