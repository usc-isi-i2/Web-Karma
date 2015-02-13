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

package edu.isi.karma.modeling.research;

public class Params {

	public static boolean RESEARCH_MODE = true;
	
//	private static String DATASET_NAME = "museum-saam-old";
//	private static String DATASET_NAME = "lod-bm";
//	private static String DATASET_NAME = "lod-bm-sample";
//	private static String DATASET_NAME = "lod-music";
//	private static String DATASET_NAME = "museum-saam";
	private static String DATASET_NAME = "museum-edm";
	private static String ROOT_DIR = "/Users/mohsen/Dropbox/Source Modeling/datasets/" + DATASET_NAME + "/";
	
	public static String ONTOLOGY_DIR = ROOT_DIR + "ontology/";
	
	public static String INPUT_DIR = ROOT_DIR + "input/";
	public static String OUTPUT_DIR = ROOT_DIR + "output/";

	public static String GRAPHS_DIR = ROOT_DIR + "graph/";
	public static String MODEL_DIR = ROOT_DIR + "model/";
	public static String GRAPHVIS_DIR = ROOT_DIR + "graphviz/";
	public static String RESULTS_DIR = ROOT_DIR + "result/";
	
	public static String GRAPH_FILE_EXT = ".graph.json";
	
	public static String MODEL_MAIN_FILE_EXT = ".model.json";
	public static String MODEL_KARMA_INITIAL_FILE_EXT = ".model.karma.initial.json";
	public static String MODEL_KARMA_FINAL_FILE_EXT = ".model.karma.final.json";
	public static String MODEL_RANK1_FILE_EXT = ".model.rank1.json";
	public static String MODEL_RANK2_FILE_EXT = ".model.rank2.json";
	public static String MODEL_RANK3_FILE_EXT = ".model.rank3.json";
	public static String MODEL_APP2_FILE_EXT = ".model.rank1.json";

	public static String GRAPHVIS_MAIN_FILE_EXT = ".model.dot";
	public static String GRAPHVIS_OUT_FILE_EXT = ".out.dot";
	public static String GRAPHVIS_OUT_DETAILS_FILE_EXT = ".out.details.dot";

	public static String LOD_DIR = ROOT_DIR + "lod/";
	public static String PATTERNS_DIR = ROOT_DIR + "patterns/";
	public static String LOD_OBJECT_PROPERIES_FILE = LOD_DIR + "objectproperties.csv";
	public static String LOD_DATA_PROPERIES_FILE = LOD_DIR + "dataproperties.csv";
	
}
