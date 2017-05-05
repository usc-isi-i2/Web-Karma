/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.isi.karma.imp;

import java.io.IOException;

import org.json.JSONException;

import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

/**
 * 
 * This abstract class in an interface to all classes with import functionality
 *
 * @author mielvandersande
 */
public abstract class Import {

    private final RepFactory factory;
    private Worksheet worksheet;
    protected Workspace workspace;
    
    public static int MAX_WORKSHEET_NAME_LEN = 100;
    
    public Import(String worksheetName, Workspace workspace, String encoding) {
        this.factory = workspace.getFactory();
        if(worksheetName.length() > MAX_WORKSHEET_NAME_LEN)
        	worksheetName = worksheetName.substring(0, MAX_WORKSHEET_NAME_LEN) + "...";
        this.worksheet = factory.createWorksheet(worksheetName, workspace, encoding);
        this.workspace = workspace;
        this.worksheet.setImportMethod(this);
    }

    public Import(RepFactory factory, Worksheet worksheet) {
        this.factory = factory;
        this.worksheet = worksheet;
        this.worksheet.setImportMethod(this);
    }

    public RepFactory getFactory() {
        return factory;
    }

    public Worksheet getWorksheet() {
        return worksheet;
    }
    
    public void createWorksheet(String worksheetName, Workspace workspace, String encoding) {
    	this.worksheet = factory.createWorksheet(worksheetName, workspace, encoding);
        this.worksheet.setImportMethod(this);
    }
    
    /*
     * Generate worksheet from data
     */
    public abstract Worksheet generateWorksheet() throws JSONException, IOException, KarmaException, ClassNotFoundException;
    
    public abstract Import duplicate() throws IOException;
    
}
