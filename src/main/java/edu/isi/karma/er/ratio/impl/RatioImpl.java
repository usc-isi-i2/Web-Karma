package edu.isi.karma.er.ratio.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.isi.karma.er.ratio.IRatio;

public class RatioImpl implements IRatio {

	private Map<String, Integer> calcRatio(List<String> list) {
		Map<String, Integer> map = new TreeMap<String, Integer>();
		Collections.sort(list);
		
		String lastStr = null;
		int count = 0;
		int total = list.size();
		map.put("__total__", total);		// first put total result into map
		for (Iterator<String> it = list.iterator(); it.hasNext(); ) {
			String str = it.next();
			if (!str.equals(lastStr)) {
				if (lastStr != null) {
					map.put(lastStr, count);
				}
				lastStr = str;
				count = 1;
			} else {
				count ++;
			}
			//if (i % 100000 == 0)
				//System.out.println(i + " rows processed ");
		}
		
		return map;
	}

	@Override
	public Map<String, Integer> calcRatio(Model model, String predicateUri) {
		if (model == null || predicateUri == null || predicateUri.trim().length() <= 0) {
			throw new IllegalArgumentException("Null or empty arguments exception. Please check arguments first.");
		}
		
		StmtIterator iter = model.listStatements(null, ResourceFactory.createProperty(predicateUri), (RDFNode) null);
		List<String> list = new ArrayList<String>();
		
		while (iter.hasNext()) {
			Statement res = iter.next();
			list.add(res.getObject().toString());
		}
		
		model.close();
		
		Map<String, Integer> map = this.calcRatio(list);
		
		return map;
	}

	

}
