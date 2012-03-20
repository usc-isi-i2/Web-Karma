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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Vector;

import au.com.bytecode.opencsv.CSVWriter;

public class ResultViewer {
	public Vector<Vector<String>> table;
	private boolean init = false;
	public ResultViewer()
	{
		table = new Vector<Vector<String>>();
	}
	public void addRow(Vector<String> row)
	{
		table.add(row);
	}
	public void addColumn(Vector<String> column)
	{
		int rows = column.size();
		if(!init)
		{
			for(int i=0;i<rows;i++)
			{
				Vector<String> elem = new Vector<String>();
				table.add(elem);
			}
		}
		for(int j=0;j<rows;j++)
		{
			table.get(j).add(column.get(j));
		}
	}
	public void publishHTML(String fname)
	{
		try
		{
			String htmlheader = "<html><title></title><body><table border='1'>";
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fname)));
			bw.write(htmlheader);
			for(int i = 0;i<table.size();i++)
			{
				String h = "<tr>";
				for(int j = 0; j<table.get(i).size();j++)
				{
					h += "<td>"+table.get(i).get(j)+"</td>";
				}
				h += "</tr>";
				bw.write(h);
			}
			String htmltail = "</table></body></html>";
			bw.write(htmltail);
			bw.close();
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
		}
	}
	public void print(String fname)
	{
		try
		{
			CSVWriter writer = new CSVWriter(new FileWriter(fname), '\t');
			for(int i = 0;i<table.size();i++)
			{
				 // feed in your array (or convert your data to an array)
				 writer.writeNext(table.get(i).toArray(new String[table.get(i).size()]));
			}
			writer.close();
		}
		catch(Exception ex)
		{
			System.out.println(""+ex.toString());
		}
		
	}
	public static void main(String[] args)
	{
		
	}
	
}
