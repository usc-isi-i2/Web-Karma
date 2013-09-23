/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.isi.karma.mvs;

import java.io.File;

/**
 *
 * @author mielvandersande
 */
public abstract class ImportFileCommand extends ImportCommand {
    
    private File file;

    public ImportFileCommand(String id, File file) {
        super(id);
        this.file = file;
    }

    public File getFile() {
        return file;
    }
    
    

    
    
}
