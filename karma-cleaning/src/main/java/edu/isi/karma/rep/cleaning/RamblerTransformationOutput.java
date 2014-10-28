/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.rep.cleaning;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.cleaning.ProgSynthesis;
import edu.isi.karma.cleaning.ProgramRule;

public class RamblerTransformationOutput implements TransformationOutput {
	private static Logger logger = LoggerFactory.getLogger(RamblerTransformationOutput.class);
	private RamblerTransformationInputs input;
	private HashMap<String, Transformation> transformations;
	public boolean nullRule = false;

	public RamblerTransformationOutput(RamblerTransformationInputs input) {
		this.input = input;
		transformations = new HashMap<String, Transformation>();
		ExecutorService executor = Executors.newFixedThreadPool(1);
		final Future<?> worker = executor.submit(new Runnable() {
			public void run() {
				try {
					learnTransformation();
				} catch (Exception ex) {
					logger.error(ex.toString());
				}
			}
		});
		try {
			worker.get(3000, TimeUnit.SECONDS);
		} catch (Exception e) {
			nullRule = true;
			transformations.clear();
		}
		
	}

	private void learnTransformation() throws Exception {
		Collection<TransformationExample> exs = input.getExamples();
		Vector<String[]> exps = new Vector<String[]>();
		Iterator<TransformationExample> iter = exs.iterator();
		while (iter.hasNext()) {
			TransformationExample t = iter.next();
			String[] tmp = { t.getBefore(), t.getAfter() };
			exps.add(tmp);
		}
		ProgSynthesis psProgSynthesis = new ProgSynthesis();
		psProgSynthesis.inite(exps, input.dpp, input.msg);
		// add time out here
		Collection<ProgramRule> rules = null;
		rules = psProgSynthesis.adaptive_main();
		input.msg.updateCM_Constr(psProgSynthesis.partiCluster.getConstraints());
		input.msg.updateWeights(psProgSynthesis.partiCluster.weights);

		if (rules == null || rules.size() == 0) {
			ProgramRule r = new ProgramRule(ProgramRule.IDENTITY);
			r.nullRule = true;
			this.nullRule = true;
			rules = new Vector<ProgramRule>();
			rules.add(r);
			// return;+
		}
		Iterator<ProgramRule> iterator = rules.iterator();
		while (iterator.hasNext()) {
			RamblerTransformation r = new RamblerTransformation(iterator.next());
			if (!transformations.containsKey(r.signature)) {
				transformations.put(r.signature, r);
			}
		}
		// RamblerTransformation r = new
		// RamblerTransformation(psProgSynthesis.getBestRule());
		// transformations.put("BESTRULE",r);
	}

	public HashMap<String, Transformation> getTransformations() {
		// TODO Auto-generated method stub
		return transformations;
	}

	public ValueCollection getTransformedValues(String TransformatinId) {
		Transformation t = transformations.get(TransformatinId);
		ValueCollection v = input.getInputValues();
		ValueCollection vo = new RamblerValueCollection();
		Collection<String> keys = v.getNodeIDs();
		Iterator<String> iter = keys.iterator();
		while (iter.hasNext()) {
			String k = iter.next();
			String val = v.getValue(k);
			if (val.length() > 0)
				val = t.transform(val);
			else
				val = "";
			vo.setValue(k, val);
			// logger.debug(k+","+val);
		}
		return vo;
	}

	public Collection<String> getRecommandedNextExample() {
		return null;
	}

	@Override
	public ValueCollection getTransformedValues_debug(String TransformationId) {
		Transformation t = transformations.get(TransformationId);
		ValueCollection v = input.getInputValues();
		ValueCollection vo = new RamblerValueCollection();
		Collection<String> keys = v.getNodeIDs();
		Iterator<String> iter = keys.iterator();
		while (iter.hasNext()) {
			String k = iter.next();
			String orgval = v.getValue(k);
			String cLabel = "";
			String val = "";
			if (orgval.length() > 0) {
				val = t.transform_debug(orgval);
				cLabel = t.getClassLabel(orgval);
			} else {
				val = "";
				cLabel = t.getClassLabel(val);
			}
			vo.setValue(k, val);
			vo.setKeyClass(k, cLabel);
			// logger.debug(k+","+val);
		}
		return vo;
	}

}
