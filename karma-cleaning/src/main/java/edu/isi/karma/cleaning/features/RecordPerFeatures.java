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

package edu.isi.karma.cleaning.features;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecordPerFeatures implements Feature {

	public String name = "";
	public double score = 0.0;
	public String value = "";
	public String tar = "";

	public RecordPerFeatures(String name, String value, String tar) {
		this.name = "attr_" + name;
		this.value = value;
		this.tar = tar;
		this.score = this.computeScore();
	}

	public double computeScore() {
		int cnt1 = 0;
		Pattern p = Pattern.compile(tar);
		Matcher m = p.matcher(this.value);
		while (m.find()) {
			cnt1 += m.group().length();
		}
		if (value.length() != 0) {
			return cnt1 * 1.0 / value.length();
		} else {
			return 10000;
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public double getScore() {
		return score;
	}

}
