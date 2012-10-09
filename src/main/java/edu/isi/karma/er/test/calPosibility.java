package edu.isi.karma.er.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.isi.karma.er.helper.Constants;

 
public class calPosibility {
	private HashMap<String,Integer> birthYear=new HashMap<String,Integer>();
	private HashMap<String,Integer> deathYear=new HashMap<String,Integer>();
	private HashMap<String,Integer> fullName=new HashMap<String,Integer>();
	private HashMap<String,Integer> state=new HashMap<String,Integer>();
	private HashMap<String,Integer> country=new HashMap<String,Integer>();
	private HashMap<String,Integer> city=new HashMap<String,Integer>();
	private HashMap<String,Integer> associatedYear=new HashMap<String,Integer>();
	private ArrayList<Double> totalNumber=new ArrayList<Double>();
	public ArrayList<Double> possibility=new ArrayList<Double>();
	public ArrayList<String> propertyList=new ArrayList<String>();
	public Resource v;
	treeNode head=new treeNode();
	Queue<treeNode> q=new LinkedList<treeNode>();
	double thredhold;
	public calPosibility()
	{
		propertyList.add("fullName");
		propertyList.add("birthYear");
		propertyList.add("deathYear");
		propertyList.add("associatedYear");
		propertyList.add("country");
		propertyList.add("city");
		propertyList.add("state");
		
		
		
		readNumberDistribution("birthYear");
		readNumberDistribution("deathYear");
		readNumberDistribution("associatedYear");
		readStringDistribution("fullName");
		readStringDistribution("country");
		readStringDistribution("city");
		readStringDistribution("state");
		
		totalNumber.add((double)fullName.get("__total__"));
		totalNumber.add((double)birthYear.get("__total__"));
		totalNumber.add((double)deathYear.get("__total__"));
		totalNumber.add((double)associatedYear.get("__total__"));
		totalNumber.add((double)country.get("__total__"));
		totalNumber.add((double)city.get("__total__"));
		totalNumber.add((double)state.get("__total__"));
	}
	public static void main(String argv[])
	{
		
		
		calPosibility c=new calPosibility();
		c.propertyList.add("fullName");
		c.propertyList.add("birthYear");
		c.propertyList.add("deathYear");
		c.propertyList.add("associatedYear");
		c.propertyList.add("country");
		c.propertyList.add("city");
		c.propertyList.add("state");
		
		c.readNumberDistribution("birthYear");
		c.readNumberDistribution("deathYear");
		c.readNumberDistribution("associatedYear");
		c.readStringDistribution("fullName");
		c.readStringDistribution("country");
		c.readStringDistribution("city");
		c.readStringDistribution("state");
		c.creatTree();
		/*
		c.createPo();
		c.head.possbility=-1;
		c.head.left=new treeNode();
		c.head.right=new treeNode();
		c.cal(c.head.left, 0, -1, 0);
		c.cal(c.head.right, 0, -1, 1);
		ArrayList<String> list=new ArrayList<String>();
		list.add("H");
		c.DFS(list, c.head);
		c.show();
		*/
	}
	public double run(Resource v,Double threldhold)
	{
		this.v=v;
		this.thredhold=threldhold;

		return creatTree();
	}
	public double creatTree()
	{
		head.possbility=-1;
		double possbility=head.possbility;
		treeNode temp=head;
		String path="H";
		for(int i=0;i<propertyList.size();i++)
		{
			String predicate = "http://americanart.si.edu/saam/" + propertyList.get(i);
			Property prop = ResourceFactory.createProperty(predicate);
			RDFNode nodeV = null;
			StmtIterator stmt = v.listProperties(prop);
			if (stmt.hasNext())
				nodeV = stmt.next().getObject();
			if(nodeV==null)
			{
				temp.left=new treeNode();
				temp=temp.left;
				if(possbility==-1)
					temp.possbility=possbility;
				else
					temp.possbility=1-possbility;
				path+="l";
			}
			else
			{
				double temppo=-1;;
				switch(i)
				{
				case 0: if(fullName.containsKey(nodeV.asLiteral().getString())) temppo=fullName.get(nodeV.asLiteral().getString()); else temppo=-1;break;
				case 1:if(birthYear.containsKey(nodeV.asLiteral().getInt())) temppo=birthYear.get(nodeV.asLiteral().getInt());else temppo=-1;break;
				case 2:if(deathYear.containsKey(nodeV.asLiteral().getInt())) temppo=deathYear.get(nodeV.asLiteral().getInt());else temppo=-1;break;
				case 3:if(associatedYear.containsKey(nodeV.asLiteral().getInt())) temppo=associatedYear.get(nodeV.asLiteral().getInt());else temppo=-1;break;
				case 4: if(country.containsKey(nodeV.asLiteral().getString())) temppo=country.get(nodeV.asLiteral().getString()); else temppo=-1;break;
				case 5: if(city.containsKey(nodeV.asLiteral().getString())) temppo=city.get(nodeV.asLiteral().getString()); else temppo=-1;break;
				case 6: if(state.containsKey(nodeV.asLiteral().getString())) temppo=state.get(nodeV.asLiteral().getString()); else temppo=-1;break;
				
				}
				temppo=temppo/totalNumber.get(i);
				if(temppo<0)
				{
					path+="N";
					//System.out.println(path);
					break;
				}
				else
				{
					temp.right=new treeNode();
					temp=temp.right;
					if(possbility==-1)
					{
						temp.possbility=1-temppo;
						possbility=temppo;
					}
					else
					{
						temp.possbility=1-temppo*possbility;
						possbility*=temppo;
					}
					path+="r";
				}
			}
			if(temp.possbility>=thredhold)
			{
				//System.out.println(temp.possbility);
				//System.out.println(path);
				return temp.possbility;
			}
		}
		//System.out.println(temp.possbility);
		//System.out.println(path);
		return temp.possbility;
	}
	public void readNumberDistribution(String filename)
	{
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Constants.PATH_RATIO_FILE + filename+".txt")));
			String input=new String();
			while((input=br.readLine())!=null)
			{
				if (input.trim().length() <= 0)
					continue;
				String []temps=input.split(":");
				String year=(temps[0].replace("\"", "").replace("-", ""));
				int count=Integer.parseInt(temps[2]);
				{
					if("birthYear".equals(filename)) {
						birthYear.put(year, count);
					} else if ("".equals(filename)) {
						deathYear.put(year, count);
					} else if ("".equals(filename)) {
					
						associatedYear.put(year, count);
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public  boolean isInteger(String str) {    
	    Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");    
	    return pattern.matcher(str).matches();    
	  }  
	public void readStringDistribution(String filename)
	{
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(Constants.PATH_RATIO_FILE + filename+".txt")));
			String input=new String();
			while((input=br.readLine())!=null)
			{
				if (input.trim().length() <= 0)
					continue;
				String []temps=input.split(":");
				String text=temps[0].replace("\"", "");
				if(!isInteger(temps[temps.length-1]))
					continue;
				int count=Integer.parseInt(temps[temps.length-1]);
				{
					if("fullName".equals(filename)) {
						fullName.put(text, count);
					} else if ("country".equals(filename)) {
						country.put(text, count);
					} else if ("state".equals(filename)) {
						state.put(text, count);
					} else if ("city".equals(filename)) {
						city.put(text, count);
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void createPo()
	{
		 Random rnd = new Random();
		 rnd.setSeed(new Date().getTime());
		for(int i=0;i<5;i++)
		{
			possibility.add(rnd.nextDouble());
		}
	}
	public void cal(treeNode head,int level,double po,int direction)
	{
		if(level>=5)
			return;
		if(level<4)
		{
			head.left=new treeNode();
			head.right=new treeNode();
		}
		
		if(direction==0)
		{
			if(po!=-1)
			{
				head.possbility=1-po;
				cal(head.left,level+1,po,0);
				cal(head.right,level+1,po,1);
			}
			else
			{
				head.possbility=po;
				cal(head.left,level+1,po,0);
				cal(head.right,level+1,po,1);
			}
		}
		else if(direction==1)
		{
			if(po!=-1)
			{
				head.possbility=1-po*possibility.get(level);
				cal(head.left,level+1,po*possibility.get(level),0);
				cal(head.right,level+1,po*possibility.get(level),1);
			}
			else
			{
				head.possbility=1-possibility.get(level);
				cal(head.left,level+1,possibility.get(level),0);
				cal(head.right,level+1,possibility.get(level),1);
			}
		}
	}
	public void DFS(ArrayList<String> list,treeNode node)
	{
		if(node==null)
			return;
		if(node.possbility-thredhold>0)
		{
			//for(String l:list)
				//System.out.print(l+" ");
			//System.out.println();
			return ;
		}
		else
		{
			ArrayList<String> ll=(ArrayList<String>)list.clone();
			ArrayList<String> rr=(ArrayList<String>)list.clone();
			ll.add("l");
			DFS(ll,node.left);
			rr.add("r");
			DFS(rr,node.right);
		}
	}
	public void show()
	{
		int count=0;
		int threhold=1;
		q.add(head);
		while(q.size()!=0)
		{
			treeNode temp=q.poll();
			//System.out.print(temp.possbility+" ");
			count++;
			if(count==threhold)
			{
				threhold*=2;
				count=0;
				//System.out.println();
			}
			if(temp.left!=null)
			q.add(temp.left);
			if(temp.right!=null)
			q.add(temp.right);
		}
	}
}
