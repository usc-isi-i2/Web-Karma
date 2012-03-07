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
package edu.isi.karma.modeling.semantictypes.sl ;

import java.util.ArrayList;

import edu.isi.karma.modeling.semantictypes.myutils.Prnt;


public class RegexFeatureExtractor {
	
	public static ArrayList<String> getFieldFeatures(String field) {
		ArrayList<String> feature_list = new ArrayList<String>() ;
		
		if(field == null)
			return feature_list ;
		
		field.trim() ;
		if(field.equals(""))
			return feature_list ;
		
		ArrayList<Part> parts = Lexer.tokenizeField(field) ;
		
		if(parts.size() == 1) {
			feature_list.add(Feature.single_token_field) ;
			return feature_list ;
		}
		else {
			feature_list.add(Feature.starts_with_token_ + parts.get(0).string) ;
			feature_list.add(Feature.ends_with_token_ + parts.get(parts.size()-1).string) ;
			return feature_list ;
		}
	}
	
	public static ArrayList<String> getTokenFeatures(Part part) {
		
		ArrayList<String> feature_list = new ArrayList<String>() ;
		String token = part.string ;		
		
		if(part.type == Type.pure_alpha) {
			int len = token.length() ;
			feature_list.add(Feature.alpha_length_ + len) ;   
			
			String first_character  = token.substring(0,1) ;
			feature_list.add(Feature.starts_with_char_ + first_character) ;
			
			boolean all_caps = true ;
			for(int i=0; i<token.length() ; i++) {
				if(part.string.charAt(i) >= 'A' && part.string.charAt(i) <= 'Z')
					continue ;
				else {
					all_caps = false ;
					break ;
				}
			}
			if(all_caps)
				feature_list.add(Feature.all_capitalized_token) ;
			else if(first_character.charAt(0) >= 'A' && first_character.charAt(0) <= 'Z')
				feature_list.add(Feature.capitalized_token) ;
			
			feature_list.add(Feature.alpha_id_ + token) ;
		}
		else if(part.type == Type.number) {
			
			if(token.substring(0,1) == "-") {
				token=token.substring(1) ;
				feature_list.add(Feature.neg_num) ;
			}
			
			String first_part = "" ;
			String decimal_part = "" ;
			
			int decimal_index = token.indexOf(".") ;
			if(decimal_index >= 0) {
				first_part = token.substring(0,decimal_index) ;
				decimal_part = token.substring(decimal_index+1) ;
			}
			else {
				first_part = token ;
				decimal_part = "" ;
			}
			
			feature_list.add(Feature.num_len_ + token.length()) ;
			
			feature_list.add(Feature.before_decimal_len_ + first_part.length()) ;
			
			feature_list.add(Feature.after_decimal_len_ + decimal_part.length()) ;
			
			if(!first_part.equals("")) 
				feature_list.add(Feature.starting_digit_ + first_part.substring(0,1)) ;
			
			if(!first_part.equals("")) 
				feature_list.add(Feature.unit_place_digit_ + first_part.substring(first_part.length()-1)) ;
			
			if(!decimal_part.equals(""))
				feature_list.add(Feature.tenth_place_digit_ + decimal_part.substring(0,1)) ;
			
		}
		else if(part.type == Type.symbol) {
			feature_list.add(Feature.symbol_ + token) ;
		}
		else {
			Prnt.endIt("RegexFeatureExtract.getTokenFeatures: type of part not found to be any of alpha, num, sym. \nEnding.") ;
		}
		
		return feature_list ;
	}
	
	
	
	
	
}
