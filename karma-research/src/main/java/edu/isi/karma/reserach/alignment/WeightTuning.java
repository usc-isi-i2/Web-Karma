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

package edu.isi.karma.reserach.alignment;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.learner.SortableSemanticModel_Old;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeightTuning {

	private static Logger logger = LoggerFactory.getLogger(WeightTuning.class);

	private static WeightTuning instance = null;

	private double coherenceFactor;
	private double sizeFactor;
	private double confidenceFactor;
	
	public static WeightTuning getInstance() {
		if (instance == null)
			instance = new WeightTuning();
		return instance;
	}
	
	private WeightTuning() {
		setDefaults(1.0, 1.0, 1.0);
	}
	
	public double getCoherenceFactor() {
		return coherenceFactor;
	}

	public double getSizeFactor() {
		return sizeFactor;
	}

	public double getConfidenceFactor() {
		return confidenceFactor;
	}

	public void setDefaults(double a, double b, double c) {
		this.coherenceFactor = a;
		this.sizeFactor = b;
		this.confidenceFactor = c;
	}
	
	public void updateWeights(List<SortableSemanticModel_Old> rankedSemanticModels, SemanticModel correctModel) {
		
		if (rankedSemanticModels == null || 
				rankedSemanticModels.isEmpty())
			return;
		
		for (SortableSemanticModel_Old sm: rankedSemanticModels)
			System.out.println(sm.getId());
		
		HashMap<SortableSemanticModel_Old, Integer> modelRanking = 
				new HashMap<SortableSemanticModel_Old, Integer>();
		
		for (int i = 0; i < rankedSemanticModels.size(); i++) {
			modelRanking.put(rankedSemanticModels.get(i), i + 1);
		}

		HashMap<SortableSemanticModel_Old, Double> modelDistance = 
				new HashMap<SortableSemanticModel_Old, Double>();
		

		double distance = 0.0;
		for (SortableSemanticModel_Old sm : rankedSemanticModels) {
			distance = correctModel.evaluate(sm).getDistance();
			modelDistance.put(sm, distance);
		}
		
//		Ordering<SortableSemanticModel> orderingByDistance = Ordering.natural().nullsLast().onResultOf(Functions.forMap(modelDistance));
		
		// To prevent duplicate keys - having two entries equal according to comparator function
		Ordering<SortableSemanticModel_Old> orderingByDistance = Ordering.natural().nullsLast().onResultOf(Functions.forMap(modelDistance))
				.compound(Ordering.natural().nullsLast().onResultOf(Functions.forMap(modelRanking)))
				.compound(Ordering.natural());;
		
//		Ordering<Map.Entry<SortableSemanticModel, Double>> orderingByDistance = Ordering.natural().nullsLast().onResultOf(
//				new Function<Map.Entry<SortableSemanticModel, Double>, Double>() {
//					public Double apply(Entry<SortableSemanticModel, Double> entry) {
//						return entry.getValue();
//		    }
		    
		Map<SortableSemanticModel_Old, Double> map = ImmutableSortedMap.copyOf(modelDistance, orderingByDistance);
		
		SortableSemanticModel_Old closestModel = map.entrySet().iterator().next().getKey();
		if (modelRanking.get(closestModel) == 1) {
			logger.info("best model is already the first suggested one!");
			return;
		} 
		
		// update the weights
		logger.info("updating the weights ...");
		
	}
}
