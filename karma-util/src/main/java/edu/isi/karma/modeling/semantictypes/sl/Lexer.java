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

import edu.isi.karma.modeling.semantictypes.myutils.Prnt;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class is used to split field into tokens.
 * 
 * @author amangoel
 *
 */
public class Lexer {
	
	// enumerated types used to remember what we the lexer has been looking/searching for while scanning the char stream 
	//static enum Searching {ALPHA, NUM, PUNC} ;
		
	// This func is the main lexer 
	// it makes a field as input and returns its tokens/components as a list
	// Basically it uses regex to find the patterns for alpha words, numbers and symbols
	// Assumption : Number can be 23, 23.45, .45, 0.45 , 23.0 , 2,345,678.350 but cannot be 23. (that is a decimal but no digits after it)
	// All of the above can have a negative sign in front
	public static ArrayList<Part> tokenizeField(String field) {
		ArrayList<Part> part_list = new ArrayList<Part>() ;
		String tmp_field = "" ;
		for(int i=0;i<field.length();i++) {
			char c = field.charAt(i) ;
			if((int) c == 160)
				c = ' '	;
			tmp_field+=c ;
		}
		field = tmp_field ;
		field = field.trim() ;
		field = field.replaceAll("^", "") ;
		
		if(field.equals("") || field == null) {
			// Prnt.prn("Lexer got empty string or null string in tokenizeField") ;
			return part_list ;
		}
			
		if(field.equals("NULLNULL")) {
			part_list.add(new Part("NULLNULL", Type.NULLNULL));
			return part_list ;
		}
		
		Pattern pure_alpha  = Pattern.compile("[a-z|A-Z]+") ;
		Pattern pure_symbol = Pattern.compile("\\W|\\_"); // For considering underscore as symbol
		Pattern number      = Pattern.compile("((\\-)?[0-9]{1,3}(,[0-9]{3})+(\\.[0-9]+)?)|((\\-)?[0-9]*\\.[0-9]+)|((\\-)?[0-9]+)") ;
		Matcher matcher ;
		int start_index=0, end_index=0 ;
	
		while(true) {
			//Util.prn(field) ;
			matcher = pure_alpha.matcher(field) ;      // for pure alpha
			if(matcher.find() && (start_index = matcher.start()) == 0) {
				end_index = matcher.end() ;
				part_list.add(new Part(field.substring(start_index, end_index), Type.pure_alpha)) ;
				field = field.substring(end_index).trim() ;
				if(field.equals(""))
					break ;
				else
					continue ;
			}
			matcher = number.matcher(field) ;          // for pure number 
			if(matcher.find() && (start_index = matcher.start()) == 0) {
				end_index = matcher.end() ;
				part_list.add(new Part(field.substring(start_index, end_index), Type.number)) ;
				field = field.substring(end_index).trim() ;
				if(field.equals(""))
					break ;
				else
					continue ;
			}
			matcher = pure_symbol.matcher(field) ;      // for symbol
			if(matcher.find() && (start_index = matcher.start()) == 0) {
				end_index = matcher.end() ;
				part_list.add(new Part(field.substring(start_index, end_index), Type.symbol)) ;
				field = field.substring(end_index).trim() ;
				if(field.equals(""))
					break ;
				else 
					continue ;
			}
			Prnt.endIt("Can't tokenize since field part not matching either alpha or numeric or symbol") ;
		}
		return part_list ;
	}
	
	/*
	// This func is the main lexer 
	// it makes a field as input and returns its tokens/components as a list
	// Basically it scans through the string separating out pure alphas, nums and other characters (continuous symbols are treated separate)
	// This function separates out pure alphabets, pure nums and pure symbols
	// 
	// 
	static ArrayList<String> tokenizeElement(String element) {
		ArrayList<String> tokenslist = new ArrayList<String>() ;
		String str = element.trim() ;
		
		if(str.equals("") || str == null) {
			Util.prn("Lexer got empty string or null string in tokenizeElement") ;
			System.exit(1) ;
		}
			
		if(str.equals("NULLNULL")) {
			tokenslist.add("NULLNULL");
			return tokenslist ;
		}
		
		Searching state = Searching.PUNC;
		String token = "" ;
		
		for(int j=0;j<str.length();j++) {
			char c = str.charAt(j)  ;
			if(state == Searching.ALPHA) {
				if(isAlpha(c))
					token+=c ;
				else {
					tokenslist.add(token) ;
					
					if(isNum(c)) {
						token = String.valueOf(c) ;
						state = Searching.NUM ;
					}
					else {
						if(!isSpace(c)) {
							tokenslist.add(String.valueOf(c)) ;
							
						}
						state = Searching.PUNC ;
					}
				}
			}
			else if(state == Searching.NUM) {
				if(isNum(c))
					token+=c ;
				else {
					tokenslist.add(token) ;
					
					if(isAlpha(c)) {
						token = String.valueOf(c);
						state = Searching.ALPHA ;
					}
					else {
						if(!isSpace(c)) {
							tokenslist.add(String.valueOf(c)) ;
							
						}
						state = Searching.PUNC ;
					}
				}
			}
			else {
				if(isAlpha(c)) {
					token=String.valueOf(c) ;
					state = Searching.ALPHA ;
				}
				else if(isNum(c)) {
					token = String.valueOf(c) ;
					state = Searching.NUM ;
				}
				else {
					if(!isSpace(c)) {
						tokenslist.add(String.valueOf(c)) ;
						
					}
					
				}
			}
		}
		if(state == Searching.ALPHA || state == Searching.NUM) { 
			tokenslist.add(token) ;
			
		}
		return tokenslist ;
	}
	*/
	
	// this func tells if the given char is an alphabet (caps or lowercase)
	static boolean isAlpha(char c) {
		if((c >= 'A' && c <= 'Z') || (c>='a' && c<='z'))
			return true ;
		else
			return false ;
	}
	
	// this func tells if the given char is numeric (0-9)
	static boolean isNum(char c) {
		if(c >='0' && c<='9')
			return true ;
		else 
			return false ;
	}

	// this func tells if the given char is a space
	static boolean isSpace(char c) {
		int i = (int) c ;
		if(i == 32 || i==160) // 160 is also a space. It is interpreted as hard space on web pages to force a space.
			return true ;
		else
			return false ;
	}
	
	
	/*
	 * old tokenize
	 * 
	 * 
	static void tokenize (ArrayList<String> listofelements, Table table, ArrayList<String> tokenslist, ArrayList<String> listoftypelabels) throws Exception {
		tokenslist.clear() ;
		listoftypelabels.clear() ;
		ArrayList<String> typelabelsoftable = null ;
		if(table != null && table.typelabels != null) {
			typelabelsoftable = table.typelabels ;
			if(typelabelsoftable.size() != listofelements.size()) {
				Util.prn("The size of typelabelsoftable doesn't match the size of listofelements in Lexer.tokenize") ;
				System.exit(1) ;
			}
		}
		
		for(int i=0;i<listofelements.size();i++) {
			
			
		}
	}
	*/
}
