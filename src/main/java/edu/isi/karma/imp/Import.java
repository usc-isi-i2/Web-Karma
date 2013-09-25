/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.isi.karma.imp;

import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import java.io.IOException;
import java.sql.SQLException;
import org.json.JSONException;

/**
 *
 * @author mielvandersande
 */
public abstract class Import {

    private final RepFactory factory;
    private final Worksheet worksheet;

    public Import(String worksheetName, Workspace workspace) {
        this.factory = workspace.getFactory();
        this.worksheet = factory.createWorksheet(worksheetName, workspace);
    }

    public Import(RepFactory factory, Worksheet worksheet) {
        this.factory = factory;
        this.worksheet = worksheet;
    }
    
    public Import(String worksheetName, Workspace workspace, Worksheet revisedWorksheet) {
        this.factory = workspace.getFactory();
        this.worksheet = factory.createWorksheet(worksheetName, workspace, revisedWorksheet);
    }

    public RepFactory getFactory() {
        return factory;
    }

    public Worksheet getWorksheet() {
        return worksheet;
    }

    public abstract Worksheet generateWorksheet() throws JSONException, IOException, KarmaException, ClassNotFoundException, SQLException;
    
    public abstract void populateWorksheet() throws JSONException, IOException, KarmaException, ClassNotFoundException, SQLException;
}
