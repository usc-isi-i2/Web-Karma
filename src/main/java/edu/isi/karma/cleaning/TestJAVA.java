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

package edu.isi.karma.cleaning;

import java.util.HashMap;
import java.util.StringTokenizer;

import org.json.JSONException;
import org.json.JSONObject;


public class TestJAVA {
	public void StringColorCode(String org,String res,HashMap<String, String> dict)
	{
		int segmentCnt = 0;
		String pat = "((?<=\\{_L\\})|(?=\\{_L\\}))";
		String pat1 = "((?<=\\{_S\\})|(?=\\{_S\\}))";
		String orgdis = "";
		String tardis = "";
		String tar = "";
		String[] st = res.split(pat);
		int pre = 0;
		boolean inloop = false;
		for(String token:st)
		{
			if(token.compareTo("{_L}")==0 && !inloop)
			{
				inloop = true;
				continue;
			}
			if(token.compareTo("{_L}")==0 && inloop)
			{
				inloop = false;
				continue;
			}
			String[] st1 = token.split(pat1);
			for(String str:st1)
			{
				if(str.compareTo("{_S}")==0||str.compareTo("{_S}")==0)
				{
					continue;
				}
				if(str.indexOf(",")!=-1 && str.length() > 1)
				{
					String[] pos = str.split(",");
					if(Integer.valueOf(pos[0]) >pre && pre<org.length())
					{
						orgdis += org.substring(pre,Integer.valueOf(pos[0]));
						pre = Integer.valueOf(pos[1]);
					}
					String tarseg = org.substring(Integer.valueOf(pos[0]),Integer.valueOf(pos[1]));
					if(inloop)
					{
						
						tardis += String.format("<span class=\"a%d\">%s</span>", segmentCnt,tarseg);
						orgdis += String.format("<span class=\"a%d\">%s</span>", segmentCnt,tarseg);
						tar += tarseg;
					}
					else
					{
						tardis += String.format("<span class=\"a%d\">%s</span>", segmentCnt,tarseg);
						orgdis += String.format("<span class=\"a%d\">%s</span>", segmentCnt,tarseg);
						segmentCnt ++;
						tar += tarseg;
					}
					
				}
				else
				{
					tardis += String.format("<span class=\"ins\">%s</span>",str);
					tar += str;
					if(!inloop)
						segmentCnt ++;
				}
			}
		}
		if(pre<org.length())
			orgdis += org.substring(pre);
		dict.put("Org", org);
		dict.put("Tar",tar );
		dict.put("Orgdis", orgdis);
		dict.put("Tardis", tardis);
	}
	public static void main(String[] args)
	{
		TestJAVA j = new TestJAVA();
		//String s = "$";
		//System.out.println(s.replaceAll("\\$", "\\\\\\$"));
		//System.out.println(UtilTools.escape("$"));
		String key = "xx";
		String value = "41.9cm x 36.8cm (16 1/2\" x 14 1/2\")";
		value = value.replaceAll("\"", "\\\\\"");
		try {
			JSONObject jsonObject = new JSONObject(String.format("{\"rowID\":\"%s\",\"value\":\"%s\"}", key,value));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
