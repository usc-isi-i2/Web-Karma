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

package edu.isi.karma.modeling.alignment.learner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.research.Params;

public class ModelReader {

	private ModelReader() {
	}

	public static void main(String[] args) throws Exception {
		
		List<SemanticModel> semanticModels = null;

		try {

			semanticModels = importSemanticModelsFromJsonFiles(Params.MODEL_DIR, Params.MODEL_MAIN_FILE_EXT);
			if (semanticModels != null) {
				for (SemanticModel sm : semanticModels) {
					sm.print();
					sm.writeGraphviz(Params.GRAPHVIS_DIR + sm.getName() + Params.GRAPHVIS_MAIN_FILE_EXT, true, true);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static List<SemanticModel> importSemanticModelsFromJsonFiles(String path, String fileExtension) throws Exception {

		File ff = new File(path);
		File[] files = ff.listFiles();
		
		List<SemanticModel> semanticModels = new ArrayList<>();
		
		for (File f : files) {
			if (f.getName().endsWith(fileExtension)) {
				SemanticModel model = SemanticModel.readJson(f.getAbsolutePath());
				semanticModels.add(model);
			}
		}
		
		return semanticModels;

	}


}