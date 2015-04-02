package edu.isi.karma.cleaning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.perf4j.StopWatch;
import org.perf4j.log4j.Log4JStopWatch;

import edu.isi.karma.cleaning.Research.Prober;

public class ProgramAdaptator {
	public ParseTreeNode program;

	public String adapt(HashMap<String, Traces> exp2Space,
			HashMap<String, String> exp2program, ArrayList<String[]> examples) {
		ArrayList<Integer> keys = chooseLargestSubset(exp2program, examples);
		String key2 = UtilTools.createkey(examples);
		if(exp2program.containsKey(key2))
		{
			return exp2program.get(key2);
		}
		if(examples.size() == 1 || keys.size() == 0)
		{
			ExampleTraces tool = new ExampleTraces();
			Traces t1 = tool.createTrace(examples.get(0));
			String prog = t1.toProgram();
			ArrayList<String[]> tmpKey = new ArrayList<String[]>();
			tmpKey.add(examples.get(0));
			String tK = UtilTools.createkey(tmpKey);
			exp2Space.put(tK, t1);
			exp2program.put(tK, prog);
			keys.add(0);
			//return prog;
		}
		ArrayList<String[]> inExps = new ArrayList<String[]>();
		for (Integer i : keys) {
			inExps.add(examples.get(i));
		}
		String subkey = this.formKey(examples, keys, true);
		Traces wholespace = getProgramSpace(subkey, exp2Space);
		ArrayList<String[]> outExps = getRestExamples(examples, keys);
		String prog =  getProgram(subkey, exp2program);
		
		for(String[] exp:outExps)
		{
			if(prog.compareTo("null")==0)
			{
				System.out.println("cannot adapt the program");
				break;
			}
			prog = adapteOneExample(wholespace, exp, prog, inExps, exp2Space, exp2program);
			inExps.add(exp);
			String nkey = formKey(inExps, new ArrayList<Integer>(), false);
			wholespace = exp2Space.get(nkey);			
		}
		return prog;
	}

	public String adapteOneExample(Traces wholespace, String[] exp,
			String program,ArrayList<String[]> inExps,HashMap<String, Traces> exp2Space, HashMap<String, String> exp2program) {
		ProgramParser programParser = new ProgramParser();
		ParseTreeNode ptree = programParser.parse(program);
		// add unknown examples incrementally
		String evres = "";
		evres = ptree.eval(exp[0]);
		ArrayList<String[]> exps = new ArrayList<String[]>();
		String skey = UtilTools.createkey(exps);
		Traces tIn = null;
		if(exp2Space.containsKey(skey))
		{
			tIn = exp2Space.get(skey); 
		}
		else
		{
			tIn = createSingleTrace(exp); 
		}
		HashMap<String, ArrayList<String>> prog2Evals = new HashMap<String,ArrayList<String>>();
		HashMap<String, String> prog2Nevals = new HashMap<String,String>();
		if(evres.compareTo(exp[1]) == 0)
		{
			Traces nTraces = wholespace.mergewith(tIn);
			ArrayList<String[]> expr = new ArrayList<String[]>();
			expr.add(exp);
			expr.addAll(inExps);
			String nKey = formKey(expr,new ArrayList<Integer>(), false);				
			exp2program.put(nKey, program);
			exp2Space.put(nKey, nTraces);
			return program;
		}
		ArrayList<ArrayList<Patcher>> tree_refers = createCandidates(ptree, tIn);
		//prepare the necessary information 
		Vector<String> orgs = new Vector<String>();
		//get all original values
		for(GrammarTreeNode node: wholespace.traceline.values().iterator().next().body)
		{
			Segment xn = (Segment)node;
			if(!xn.isConstSegment())
			{
				orgs = xn.section.get(0).orgStrings;
				break;
			}
		}
		ArrayList<ParseTreeNode> progs = ptree.children;
		for (ParseTreeNode node : progs) {
			ArrayList<String> tmpTars = new ArrayList<String>();
			for (String org : orgs) {
				ProgramRule tpp = new ProgramRule(node.value);
				String tmpSeg= tpp.transform(org);
				tmpTars.add(tmpSeg);
			}
			prog2Evals.put(node.value, tmpTars);
		}
		for (ParseTreeNode node : progs) {
			ProgramRule tpp = new ProgramRule(node.value);
			String tmpSeg= tpp.transform(exp[0]);			
			prog2Nevals.put(node.value, tmpSeg);
		}
		//nothing to change	
		for (ArrayList<Patcher> tTree : tree_refers) {
			Traces cwspace = wholespace;// clone the original space
			ArrayList<Patcher> errNodes = align(tTree, cwspace, inExps,prog2Evals,prog2Nevals);
			if(errNodes == null)
			{
				return "null";
			}
			// get the correct parts
			boolean valid = true;
			if(errNodes.size() == 0) // no solution exists
				valid = false;
			for (Patcher pat : errNodes) {
				GrammarTreeNode newSpace = updateNewSpace(pat);
				boolean succeed = updateProgram(pat, newSpace);
				if (!succeed) {
					valid = false;
					break;
				}
			}
			Prober.tracePatchers(program, errNodes, inExps,exp,valid,exp2program);
			if (valid) {
				// NEED TO UPDATE THE SUBSPACE RATHER THAN THE WHOLE ONE!!!
				Traces nTraces = cwspace.mergewith(tIn);
				ArrayList<String[]> expr = new ArrayList<String[]>();
				expr.add(exp);
				expr.addAll(inExps);
				String nKey = formKey(expr,new ArrayList<Integer>(), false);				
				// return the successfully adapted program
				String fprog = this.applyPatch(ptree, errNodes);
				//update session repo 
				exp2program.put(nKey, fprog);
				exp2Space.put(nKey, nTraces);
				return fprog;
			}
		}
		return "null";
	}
	public ArrayList<Patcher> refinePositionLevel(Vector<GrammarTreeNode> gtruth, Vector<ParseTreeNode> pNodes, Vector<GrammarTreeNode> gNodes)
	{
		ArrayList<Patcher> res = new ArrayList<Patcher>();
		if(pNodes.size()!= gtruth.size())
		{
			Patcher nPatcher = new Patcher();
			nPatcher.groundTruth = new Vector<GrammarTreeNode>(gtruth);
			nPatcher.patchSpace = gNodes;
			nPatcher.programNodes = pNodes;
			res.add(nPatcher);
			return res;
		}
		else
		{
			for(int i = 0; i < gtruth.size(); i++)
			{
				Patcher nPatcher = new Patcher();
				Segment seg = ((Segment)gtruth.get(i));
				//check if it constant segment
				if(seg.section.size() == 0)
				{
					Vector<GrammarTreeNode> gs = new Vector<GrammarTreeNode>();
					gs.add(gNodes.get(i));
					Vector<ParseTreeNode> ps = new Vector<ParseTreeNode>();
					ps.add(pNodes.get(i));
					Vector<GrammarTreeNode> ts = new Vector<GrammarTreeNode>();
					ts.add(seg);
					nPatcher.groundTruth = ts;
					nPatcher.patchSpace = gs;
					nPatcher.programNodes = ps;
					res.add(nPatcher);
					return res;
				}				
				String sVal = pNodes.get(i).children.get(0).tmpEval;
				String eVal = pNodes.get(i).children.get(1).tmpEval;
				String tsVal =""+seg.section.get(0).pair[0].absPosition.get(0);
				String teVal =""+seg.section.get(0).pair[1].absPosition.get(0);
				Segment segSpace = (Segment)gNodes.get(i);
				Vector<Section> secs = segSpace.section;//
				//fix end position expression
				if(tsVal.compareTo(sVal)==0)
				{
					ArrayList<Position> ap = new ArrayList<Position>();
					for(int k = 0; k < secs.size(); k++)
					{
						if(secs.get(k).pair[1]!=null)
							ap.add(secs.get(k).pair[1]);
					}
					PositionSet pos = new PositionSet(ap);
					Vector<GrammarTreeNode> gs = new Vector<GrammarTreeNode>();
					gs.add(pos);
					Vector<ParseTreeNode> ps = new Vector<ParseTreeNode>();
					ps.add(pNodes.get(i).children.get(1));
					Vector<GrammarTreeNode> ts = new Vector<GrammarTreeNode>();
					ts.add(seg.section.get(0).pair[1]);
					nPatcher.groundTruth = ts;
					nPatcher.patchSpace = gs;
					nPatcher.programNodes = ps;
					res.add(nPatcher);
				}
				else if(teVal.compareTo(eVal)==0)
				{
					ArrayList<Position> ap = new ArrayList<Position>();
					for(int k = 0; k < secs.size(); k++)
					{
						if(secs.get(k).pair[0]!=null)
							ap.add(secs.get(k).pair[0]);
					}
					PositionSet pos = new PositionSet(ap);
					Vector<GrammarTreeNode> gs = new Vector<GrammarTreeNode>();
					gs.add(pos);
					Vector<ParseTreeNode> ps = new Vector<ParseTreeNode>();
					ps.add(pNodes.get(i).children.get(0));
					Vector<GrammarTreeNode> ts = new Vector<GrammarTreeNode>();
					ts.add(seg.section.get(0).pair[0]);
					nPatcher.groundTruth = ts;
					nPatcher.patchSpace = gs;
					nPatcher.programNodes = ps;
					res.add(nPatcher);
				}
				else
				{
					//directly update the segment
					Vector<GrammarTreeNode> gs = new Vector<GrammarTreeNode>();
					gs.add(gNodes.get(i));
					Vector<ParseTreeNode> ps = new Vector<ParseTreeNode>();
					ps.add(pNodes.get(i));
					Vector<GrammarTreeNode> ts = new Vector<GrammarTreeNode>();
					ts.add(seg);
					nPatcher.groundTruth = ts;
					nPatcher.patchSpace = gs;
					nPatcher.programNodes = ps;
					res.add(nPatcher);
				}	
			}
		}
		return res;
	}
	public ArrayList<Patcher> refinePather(Patcher pat, Vector<GrammarTreeNode> gtruth, ArrayList<Path> pathes)
	{
		ArrayList<Patcher> res = new ArrayList<Patcher>();
		if(pathes.size() == 0)
		{
			return res;
		}
		HashMap<Integer, int[]> prog2Sections = new HashMap<Integer,int[]>();
		//consolidate the paths into one arraylist
		//only one segmentation exists
		Vector<GrammarTreeNode> xtemp = convertPathtoSpace(pathes);
		//detect the parts that are correct
		for(Path p:pathes)
		{
			int[] pog2Sects = p.indicator;
			for(int i = 0; i < pog2Sects.length-1; i ++)
			{
				//match the (i-1)-th end and i-th start position
				if(pog2Sects[i] >= 0 && pog2Sects[i+1] >=0)
				{
					//check whehther the evaluated result is correct.
					int[] secR = {pog2Sects[i],pog2Sects[i+1]};					
					String expected = "";
					for(int j = secR[0]; j <secR[1]; j++ )
					{
						expected += ((Segment)gtruth.get(j)).tarString;
					}
					String evaled = p.evalNewExp.get(i);
					//if yes, detect a reusable subprogram
					if(evaled.compareTo(expected) == 0)
					{
						prog2Sections.put(i, secR);		
					}											
				}
			}		
		}
		//split the arraylist into several smaller lists
		int start = 0;
		int progStart = 0;
		for(int i = 0; i < pat.programNodes.size(); i++)
		{
			if(prog2Sections.containsKey(i))
			{
				int end = prog2Sections.get(i)[0];
				Vector<GrammarTreeNode> gtru = new Vector<GrammarTreeNode>(pat.groundTruth.subList(start, end+1));
				Vector<GrammarTreeNode> x = new Vector<GrammarTreeNode>(xtemp.subList(start, end+1));
				Vector<ParseTreeNode> pNodes = new Vector<ParseTreeNode>(pat.programNodes.subList(progStart, i+1));
				ArrayList<Patcher> tres = refinePositionLevel(gtru, pNodes, x);
				start = prog2Sections.get(i)[1]+1;
				progStart = i+1;
				res.addAll(tres);
			}
		}
		//add the last section if there is any
		if(progStart < pat.programNodes.size())
		{
			int end = pat.groundTruth.size();
			Vector<GrammarTreeNode> gtru = new Vector<GrammarTreeNode>(pat.groundTruth.subList(start, end));
			Vector<GrammarTreeNode> x = new Vector<GrammarTreeNode>(xtemp.subList(start, end));
			Vector<ParseTreeNode> pNodes = new Vector<ParseTreeNode>(pat.programNodes.subList(progStart, pat.programNodes.size()));
			ArrayList<Patcher> tres = refinePositionLevel(gtru, pNodes, x);
			res.addAll(tres);
		}
		return res;
	}
	public String applyPatch(ParseTreeNode tree, ArrayList<Patcher> pats) {
		Iterator<Patcher> xiter = pats.iterator();
		while(xiter.hasNext())
		{
			Patcher xPatcher = xiter.next();
			if(xPatcher.replaceNodes.size() == 1 && xPatcher.programNodes.size()==1)
			{
				ParseTreeNode pNode = xPatcher.programNodes.get(0);
				ParseTreeNode rpNode = xPatcher.replaceNodes.get(0);
				int x = pNode.parent.children.indexOf(pNode);
				pNode.parent.children.remove(pNode);
				pNode.parent.children.add(x, rpNode);
				xiter.remove();
			}
		}
		HashMap<String, ArrayList<ParseTreeNode>> map = new HashMap<String, ArrayList<ParseTreeNode>>();
		String[] bitmap = new String[tree.children.size()];
		for (int i = 0; i < bitmap.length; i++) {
			bitmap[i] = "null";
		}
		for (Patcher pat : pats) {
			Vector<ParseTreeNode> xNodes = pat.programNodes;
			int start = tree.children.indexOf(xNodes.get(0));
			int end = tree.children.indexOf(xNodes.get(xNodes.size() - 1));
			String key = start + "||" + end;
			for (int i = start; i <= end; i++) {
				bitmap[i] = key;
			}
			map.put(key, new ArrayList<ParseTreeNode>(pat.replaceNodes));
		}
		//
		ArrayList<ParseTreeNode> nChildren = new ArrayList<ParseTreeNode>();
		HashSet<String> visited = new HashSet<String>();
		for (int i = 0; i < bitmap.length; i++) {
			if (bitmap[i].compareTo("null") != 0 && !visited.contains(bitmap[i])) {
				String key = bitmap[i];
				nChildren.addAll(map.get(key));
				visited.add(key);
			} else {
				nChildren.add(tree.children.get(i));
			}
		}
		tree.children = nChildren;
		return tree.toProgram();
	}

	public boolean updateProgram(Patcher pat, GrammarTreeNode space) {
		if (space == null)
			return false;
		// replace the previous programNode with new nodes
		StopWatch stopWatch = new Log4JStopWatch("Adpative_toProgram2");
		String subProg = space.toProgram();

		stopWatch.stop();
		if (subProg == null || subProg.indexOf("null") != -1) {
			return false;
		}

		ProgramParser xParser = new ProgramParser();
		ParseTreeNode nroot = xParser.parse(subProg);
		ArrayList<ParseTreeNode> children = nroot.getChildren();
		pat.replaceNodes.clear();
		pat.replaceNodes.addAll(children);
		return true;
	}

	// use the new patch to update the space and return modified program spaces
	public GrammarTreeNode updateNewSpace(Patcher pat) {
		GrammarTreeNode res = null;
		Vector<GrammarTreeNode> space = pat.patchSpace;
		Vector<GrammarTreeNode> groundTruth = pat.groundTruth;
		if(space.size() == groundTruth.size())
		{
			if(space.size() == 1)
			{
				GrammarTreeNode node = space.get(0).mergewith(groundTruth.get(0));
				res = node;
			}
			else
			{
				Template x = new Template(space);
				Template y = new Template(groundTruth);
				Template r = (Template)x.mergewith(y);
				res = r;
			}
		}
		return res;
	}
	public ArrayList<ArrayList<String>> transpose(ArrayList<ArrayList<String>> vals,ArrayList<String> tarStrings)
	{
		ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>>();
		for(int i = 0; i < vals.get(0).size(); i++)
		{
			ArrayList<String> line = new ArrayList<String>();
			String total = "";
			for( int j = 0; j < vals.size(); j++)
			{
				total += vals.get(j).get(i);
				line.add(vals.get(j).get(i));
			}
			res.add(line);
			tarStrings.add(total);
		}
		return res;
	}
	public ArrayList<Patcher> align(ArrayList<Patcher> tTree, Traces wSpace,
			ArrayList<String[]> preExps,HashMap<String, ArrayList<String>> prog2Eval, HashMap<String, String> prog2New) {
		ArrayList<Patcher> res = new ArrayList<Patcher>();
		/*Vector<String> orgs = new Vector<String>();
		//get all original values
		for(GrammarTreeNode node: wSpace.traceline.values().iterator().next().body)
		{
			Segment xn = (Segment)node;
			if(!xn.isConstSegment())
			{
				System.out.println(""+xn.section.toString());
				orgs = xn.section.get(0).orgStrings;
			}
		}*/
		for (Patcher pat : tTree) {
			//prepare the raw data
			ArrayList<String> tarStrings = new ArrayList<String>();
			ArrayList<ArrayList<String>> tarSegs = new ArrayList<ArrayList<String>>();
			ArrayList<String> evalNewExp = new ArrayList<String>();
			ArrayList<ArrayList<String>> tmp = new ArrayList<ArrayList<String>>();
			for(ParseTreeNode node: pat.programNodes)
			{
				ArrayList<String> lvals = prog2Eval.get(node.value);
				evalNewExp.add(prog2New.get(node.value));
				tmp.add(lvals);
			}
			tarSegs = transpose(tmp, tarStrings);
			// extract the corresponding program spaces
			HashMap<Integer, Template> tmpts = wSpace.traceline;
			Vector<GrammarTreeNode> groundTruth = pat.groundTruth;
			ArrayList<Path> allPaths = new ArrayList<Path>();
			for (Integer len : tmpts.keySet()) {
				Vector<GrammarTreeNode> tBody = tmpts.get(len).body;
				ArrayList<Path> paths = extractPath(tBody, tarStrings, tarSegs, evalNewExp);
				for(Path p: paths)
				{
					if(p.sections.size() == groundTruth.size())
					{
						allPaths.add(p);
					}
				}
			}
			ArrayList<Patcher> temPats = refinePather(pat, groundTruth, allPaths);
			if(temPats.size() == 0)
				return null;
			res.addAll(temPats);
		}
		return res;
	}

	public Vector<GrammarTreeNode> convertPathtoSpace(ArrayList<Path> paths) {
		Vector<Vector<GrammarTreeNode>> res = new Vector<Vector<GrammarTreeNode>>();
		for (Path p : paths) {
			Vector<GrammarTreeNode> line = new Vector<GrammarTreeNode>();
			ArrayList<Section> secs = p.sections;
			for (Section sec : secs) {
				Vector<Section> st = new Vector<Section>();
				st.add(sec);
				Segment seg = new Segment(st, false);
				line.add(seg);
			}
			res.add(line);
		}
		Traces uTraces = new Traces();
		Vector<GrammarTreeNode> tempres = new Vector<GrammarTreeNode>(); 
		if(res.size() > 0)
		{
			tempres = uTraces.consolidate_tool(res);
		}
		return tempres;
	}

	public ArrayList<Path> extractPath(Vector<GrammarTreeNode> tBody,
			ArrayList<String> tarStrings,ArrayList<ArrayList<String>> tarSegs, ArrayList<String> evalNewExp) {
		ArrayList<Path> res = new ArrayList<Path>();
		ArrayList<Path> seeds = new ArrayList<Path>();
		// initialize the seeds
		for (int i = 0; i < tBody.size(); i++) {
			Segment seg = (Segment) tBody.get(i);
			for (Section sec : seg.section) {
				ArrayList<Integer> markers = new ArrayList<Integer>();
				for (int j = 0; j < sec.tarStrings.size(); j++) {
					markers.add(0);
				}
				Vector<String> subs = sec.tarStrings;
				ArrayList<Integer> x = updateMarker(subs, tarStrings, markers);
				if (x != null) {
					ArrayList<Section> sections = new ArrayList<Section>();
					sections.add(sec);
					Path nP = new Path(sections, x, tarStrings, i + 1, tarSegs, evalNewExp);
					seeds.add(nP);
				}
			}
		}
		// seeking a sequence of sections that can form the target strings
		while (seeds.size() > 0) {
			Path path = seeds.remove(0);
			// check if it succeeds already
			boolean finished = true;
			boolean nofit = false;
			for (int i = 0; i < path.markers.size(); i++) {
				int strlen = path.tarStrings.get(i).length();
				int mk = path.markers.get(i);
				if (mk > strlen) {
					nofit = true;
				}
				if (mk < strlen) {
					finished = false;
					break;
				}
				
			}
			if (finished)
			{
				res.add(path);
				continue;
			}
			if (nofit)
				continue;
			ArrayList<Path> cands = updatePath(path, tBody);
			seeds.addAll(cands);
		}
		return res;
	}

	// -1 failed, 0 succeed, 1 finished
	public ArrayList<Path> updatePath(Path np, Vector<GrammarTreeNode> tBody) {
		ArrayList<Path> res = new ArrayList<Path>();
		if(np.tmplateMarker >= tBody.size())
		{
			return res;
		}
		Segment seg = (Segment) tBody.get(np.tmplateMarker);
		for (Section as : seg.section) {
			Vector<String> subs = as.tarStrings;
			ArrayList<Integer> x = updateMarker(subs, np.tarStrings, np.markers);
			if (x != null) {
				Path xp = np.updatePath(as, x);
				res.add(xp);
			}
		}
		return res;
	}

	// if anyone is not a prefix of the other return null
	public ArrayList<Integer> updateMarker(Vector<String> subs,
			ArrayList<String> target, ArrayList<Integer> markers) {
		ArrayList<Integer> res = new ArrayList<Integer>();
		for (int i = 0; i < subs.size(); i++) {
			String sub = subs.get(i);
			String tar = target.get(i).substring(markers.get(i));
			if (tar.indexOf(sub) != 0) {
				return null;
			} else {
				res.add(markers.get(i) + sub.length());
			}
		}
		return res;
	}

	// identify the correspondence between the subprograms and the concrete
	// instance tree
	public ArrayList<ArrayList<Patcher>> createCandidates(ParseTreeNode tree,
			Traces concrete) {
		ArrayList<ArrayList<Patcher>> res = new ArrayList<ArrayList<Patcher>>();
		// get all the parseTree segments
		ArrayList<ParseTreeNode> segments = tree.getChildren();
		// get all the different segmentations of the example
		Vector<Vector<GrammarTreeNode>> segs = concrete.rawlines;
		for (Vector<GrammarTreeNode> tBody : segs) {
			if (tBody.size() < segments.size()) {
				continue;
			}
			int len = tBody.size();
			ArrayList<Patcher> list = new ArrayList<Patcher>();
			if (len == segments.size()) {
				// one on one match
				for (int i = 0; i < len; i++) {
					String truVal = ((Segment) tBody.get(i)).tarString;
					if (segments.get(i).tmpEval.compareTo(truVal) == 0) {
						continue;
					} else {
						Patcher pa = new Patcher();
						Vector<GrammarTreeNode> grt = new Vector<GrammarTreeNode>();
						grt.add(tBody.get(i));
						Vector<ParseTreeNode> progs = new Vector<ParseTreeNode>();
						progs.add(segments.get(i));
						pa.groundTruth = grt;
						pa.programNodes = progs;
						list.add(pa);
					}
				}
			} else {
				// one prog to multiple trace Nodes
				multipleAlign(list, tBody, segments);
			}
			if (list.size() > 0) {
				res.add(list);
			}
		}
		return res;
	}

	public void multipleAlign(ArrayList<Patcher> list,
			Vector<GrammarTreeNode> tbody, ArrayList<ParseTreeNode> segments) {
		// find anchor point, from the start point and the end
		int progS = 0;
		int tS = 0;
		int endSpan = 0;
		for (int i = 0; i < tbody.size() && i < segments.size(); i++) {
			String gTruth = ((Segment) tbody.get(i)).tarString;
			if (segments.get(progS).tmpEval.compareTo(gTruth) == 0) {
				progS++;
				tS++;
			} else {
				break;
			}
		}
		for (int i = 0; i < tbody.size() && i < segments.size()&& i>progS && i> tS; i++) {
			String gTruth = ((Segment) tbody.get(tbody.size()-1-i)).tarString;
			if (segments.get(segments.size()-1-i).tmpEval.compareTo(gTruth) == 0) {
				endSpan = i+1;
			} else {
				break;
			}
		}
		Vector<GrammarTreeNode> grt = new Vector<GrammarTreeNode>();
		for (int i = tS; i <= tbody.size()-1-endSpan; i++) {
			grt.add(tbody.get(i));
		}
		Vector<ParseTreeNode> progs = new Vector<ParseTreeNode>();
		for (int i = progS; i <= segments.size()-1-endSpan; i++) {
			progs.add(segments.get(i));
		}
		if (grt.size() > 0 && progs.size() > 0) {
			Patcher pa = new Patcher();
			pa.groundTruth = grt;
			pa.programNodes = progs;
			list.add(pa);
		}
	}

	public Traces createSingleTrace(String[] exp) {
		ExampleTraces eTraces = new ExampleTraces();
		return eTraces.createTrace(exp);
	}

	public ArrayList<Integer> chooseLargestSubset(
			HashMap<String, String> exp2program, ArrayList<String[]> examples) {
		ArrayList<Integer> res = new ArrayList<Integer>();
		int maxsize = -1;
		for (String key : exp2program.keySet()) {
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			for (int i = 0; i < examples.size(); i++) {
				String line = UtilTools.formatExp(examples.get(i));
				if (key.indexOf(line) != -1)
					tmp.add(i);
			}
			if (tmp.size() > maxsize && formKey(examples, tmp, true).compareTo(key)==0) {
				res = tmp;
				maxsize = tmp.size();
			}
		}
		return res;
	}
	public String getProgram(String subkey, HashMap<String, String> exp2Prog)
	{
		String prog = "";
		//most compatible program
		int msize = -1;
		for(String key: exp2Prog.keySet())
		{
			int sz = key.split("\\*").length;
			if(sz > msize &&UtilTools.iscovered(key, subkey))
			{
				msize = sz;
				prog = exp2Prog.get(key);
			}
		}
		return prog;
	}
	public Traces getProgramSpace(String subkey,
			HashMap<String, Traces> exp2space) {
		return exp2space.get(subkey);
	}

	public ArrayList<String[]> getRestExamples(ArrayList<String[]> examples,
			ArrayList<Integer> keys) {
		ArrayList<String[]> restExp = new ArrayList<String[]>();
		for (int i = 0; i < examples.size(); i++) {
			if (!keys.contains(i)) {
				restExp.add(examples.get(i));
			}
		}
		return restExp;
	}

	public String formKey(ArrayList<String[]> examples,
			ArrayList<Integer> keys, boolean include) {
		ArrayList<String[]> resList = new ArrayList<String[]>();
		if (include) {
			for (int i = 0; i < examples.size(); i++) {
				if (keys.contains(i)) {
					resList.add(examples.get(i));
				}
			}
		} else {
			for (int i = 0; i < examples.size(); i++) {
				if (!keys.contains(i)) {
					resList.add(examples.get(i));
				}
			}
		}
		String key = UtilTools.createkey(resList);
		return key;
	}
}


class Path {
	ArrayList<Section> sections = new ArrayList<Section>();
	ArrayList<int[]> progCrepLandMarkers = new ArrayList<int[]>();
	//the individual evaluation results for the new example
	ArrayList<String> evalNewExp = new ArrayList<String>();
	// record the progress for this path on all examples
	ArrayList<Integer> markers = new ArrayList<Integer>();
	// record the target substring that should be covered
	ArrayList<String> tarStrings = new ArrayList<String>();
	// tmplate position marker
	int tmplateMarker = -1;
	//each evaled segments
	ArrayList<ArrayList<String>> tarSegs = new ArrayList<ArrayList<String>>();
	ArrayList<ArrayList<Integer>> landMarkers = new ArrayList<ArrayList<Integer>>();
	//point to the start position of the segments
	//0 unexplored , positive number matched, -2 passed without match
	int[] indicator = null;

	public Path(ArrayList<Section> sec, ArrayList<Integer> markers,
			ArrayList<String> tarStrings, int tMarker,ArrayList<ArrayList<String>> tarSegs,ArrayList<String> evalNewExp) 
	{
		this.sections = sec;
		this.markers = markers;
		this.tarStrings = tarStrings;
		this.tmplateMarker = tMarker;
		this.tarSegs = tarSegs;
		this.evalNewExp = evalNewExp;
		indicator = new int[evalNewExp.size()+1];
		for(int i = 0; i < indicator.length; i++)
		{
			indicator[i] = 0;
		}
		processData();
	}
	
	public void processData()
	{
		ArrayList<Integer> xArrayList = new ArrayList<Integer>();
		for(int i = 0; i < tarSegs.size(); i++)
		{
			xArrayList.add(0);
		}
		landMarkers.add(xArrayList);
		for(int i = 0; i < tarSegs.get(0).size(); i++)
		{
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			for(int j = 0; j < tarSegs.size(); j ++)
			{
				int curLen = landMarkers.get(i).get(j)+tarSegs.get(j).get(i).length();
				tmp.add(curLen);
			}
			landMarkers.add(tmp);
		}
	}

	// 0 equal 1 x partially pass y, -1 x less than y
	public int compareMarker(ArrayList<Integer> x, ArrayList<Integer> y)
	{
		if(x.size() != y.size())
		{
			System.out.println("The Landmarker size should match");
		}
		boolean equal = true;
		for(int i = 0; i < x.size(); i++)
		{
			if(x.get(i) > y.get(i))
			{
				return 1;
			}
			if(x.get(i)< y.get(i))
			{
				equal = false;
			}
		}
		if(equal)
			return 0;
		else
			return -1;
	}
	public void setIndicator(int[] indic)
	{
		this.indicator = indic;
	}
	public Path updatePath(Section sec, ArrayList<Integer> markers) {
		//this.sections.add(sec);
		ArrayList<Section> xsections = new ArrayList<Section>();
		xsections.addAll(this.sections);
		xsections.add(sec);
		ArrayList<Integer> nmarkers = new ArrayList<Integer>();
		nmarkers = markers;
		int mark = this.tmplateMarker+1;
		//update the mapping info
		int ptr = 0;
		while(ptr < indicator.length-1 && indicator[ptr]!= -1)
		{
			ptr++;
		}
		ArrayList<Integer> nextMarker = landMarkers.get(ptr);
		int cres = this.compareMarker(markers, nextMarker);
		//check whether it reaches a landmark
		if(cres == 0)
		{
			indicator[ptr] = xsections.size(); 
		}
		else if(cres == 1)
		{
			indicator[ptr] = -2;
		}
		//update the progCrepLandmarker with the new portion
		Path np = new Path(xsections, nmarkers, tarStrings, mark,this.tarSegs,evalNewExp);
		np.setIndicator(this.indicator);
		return np;
	}
}
