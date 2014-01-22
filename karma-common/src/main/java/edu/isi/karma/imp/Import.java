/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.isi.karma.imp;

import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import org.json.JSONException;

import java.io.IOException;

/**
 * 
 * This abstract class in an interface to all classes with import functionality
 *
 * @author mielvandersande
 */
public abstract class Import {

    private final RepFactory factory;
    private final Worksheet worksheet;
    
    public Import(String worksheetName, Workspace workspace, String encoding) {
        this.factory = workspace.getFactory();
        this.worksheet = factory.createWorksheet(worksheetName, workspace, encoding);
    }

    public Import(RepFactory factory, Worksheet worksheet) {
        this.factory = factory;
        this.worksheet = worksheet;
    }

    public RepFactory getFactory() {
        return factory;
    }

    public Worksheet getWorksheet() {
        return worksheet;
    }
    
    /*
     * Generate worksheet from data
     */
    public abstract Worksheet generateWorksheet() throws JSONException, IOException, KarmaException, ClassNotFoundException;
    
}
