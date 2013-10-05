/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.isi.karma.controller.command.importdata;

import edu.isi.karma.controller.command.importdata.ImportCommand;
import java.io.File;

/**
 * This abstract class in an interface to all Commands that import data from files
 * 
 * @author mielvandersande
 */
public abstract class ImportFileCommand extends ImportCommand {

    private File file;

    public ImportFileCommand(String id, File file) {
        super(id);
        this.file = file;
    }

    public ImportFileCommand(String id, String revisionId, File file) {
        super(id, revisionId);
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
