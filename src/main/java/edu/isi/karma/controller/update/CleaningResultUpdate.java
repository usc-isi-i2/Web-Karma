package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Vector;

import org.json.JSONArray;

import edu.isi.karma.rep.cleaning.ValueCollection;
import edu.isi.karma.view.VWorkspace;

public class CleaningResultUpdate extends AbstractUpdate {

	private Vector<ValueCollection> cvc;
	private String id = "";
	private String worksheetId = "";
	private String hNodeId = "";
	public CleaningResultUpdate(String id, String worksheetId, String hNodeId)
	{
		cvc = new Vector<ValueCollection>();
		this.id = id;
		this.worksheetId = worksheetId;
		this.hNodeId = hNodeId;
	}
	public void addValueCollection(ValueCollection vc)
	{
		cvc.add(vc);
	}
	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		// TODO Auto-generated method stub
		JSONArray jsa = new JSONArray();
		for(ValueCollection vc:cvc)
		{
			jsa.put(vc.getJson());
		}
		pw.print(jsa.toString());
	}

}
