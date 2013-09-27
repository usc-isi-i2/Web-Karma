/**
 * *****************************************************************************
 * Copyright 2012 University of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * This code was developed by the Information Integration Group as part of the
 * Karma project at the Information Sciences Institute of the University of
 * Southern California. For more information, publications, and related
 * projects, please see: http://www.isi.edu/integration
 *****************************************************************************
 */
package edu.isi.karma.rep;

import java.io.PrintWriter;

import edu.isi.karma.modeling.semantictypes.FullCRFModel;
import edu.isi.karma.rep.alignment.SemanticTypes;
import edu.isi.karma.rep.metadata.MetadataContainer;

/**
 * @author szekely
 *
 */
public class Worksheet extends RepEntity {

    //MVS: add revision worksheet
    private Worksheet revisedWorksheet = null;
    private HTable headers;
    private Table dataTable;
    private SemanticTypes semanticTypes = new SemanticTypes();
    private FullCRFModel crfModel = new FullCRFModel();
    private MetadataContainer metadataContainer;

    @Override
    public void prettyPrint(String prefix, PrintWriter pw, RepFactory factory) {
        pw.print(prefix);
        pw.println("Worksheet/" + id);
        headers.prettyPrint(prefix + "  ", pw, factory);
        dataTable.prettyPrint(prefix + "  ", pw, factory);
    }

    Worksheet(String id, HTable headers, Table dataTable) {
        super(id);
        this.headers = headers;
        this.dataTable = dataTable;
    }
    
    /*
     * Method for accessing the revised file
     */
    public Worksheet getRevised() {
        return revisedWorksheet;
    }
    
    /*
     * 
     */
    public void setRevisedWorksheet(Worksheet revisedWorksheet) {
        this.revisedWorksheet = revisedWorksheet;
    }

    /*
     * Check if this worksheet is revising another worksheet
     */
    public boolean hasRevision(){
        return getRevised() != null;
    }

    public HTable getHeaders() {
        return headers;
    }

    public Table getDataTable() {
        return dataTable;
    }

    public String getTitle() {
        return headers.getTableName();
    }

    public SemanticTypes getSemanticTypes() {
        return semanticTypes;
    }

    public void clearSemanticTypes() {
        semanticTypes = new SemanticTypes();
    }

    public void setSemanticTypes(SemanticTypes t) {
        semanticTypes = t;
    }

    public FullCRFModel getCrfModel() {
        return crfModel;
    }

    public MetadataContainer getMetadataContainer() {
        if (metadataContainer == null) {
            metadataContainer = new MetadataContainer();
        }
        return metadataContainer;
    }

    public void setMetadataContainer(MetadataContainer metadataContainer) {
        this.metadataContainer = metadataContainer;
    }

    /**
     * When a new HNode is added to a table or one of the nested tables, we need
     * to go through and add place holders in the data table to hold the values
     * for the new HNode.
     *
     * @param newHNode
     */
    void addNodeToDataTable(HNode newHNode, RepFactory factory) {
        dataTable.addNodeToDataTable(newHNode, factory);
    }

    //mariam
    /**
     * @param newHNode
     * @param factory
     */
    void removeNodeFromDataTable(String hNodeId) {
        dataTable.removeNodeFromDataTable(hNodeId);
    }

    /**
     * Convenience method to add rows to the top table.
     *
     * @param factory
     * @return the added row.
     */
    public Row addRow(RepFactory factory) {
        return dataTable.addRow(factory);
    }

    /**
     * Convenience method to add HNodes to the top table.
     *
     * @param columnName
     * @param factory
     * @return The added HNode.
     */
    public HNode addHNode(String columnName, RepFactory factory) {
        return headers.addHNode(columnName, this, factory);
    }

    /**
     * This HNode received a new nested HTable. We need to go through the data
     * table and make sure we have placeholders to hold values for this table.
     *
     * @param hNode
     * @param factory
     */
    public void addNestedTableToDataTable(HNode hNode, RepFactory factory) {
        dataTable.addNestedTableToDataTable(hNode, factory);
    }

    public boolean containService() {
        if (this.getMetadataContainer() == null) {
            return false;
        }

        if (this.getMetadataContainer().getService() == null) {
            return false;
        }

        return true;
    }

    public boolean containSource() {
        if (this.getMetadataContainer() == null) {
            return false;
        }

        if (this.getMetadataContainer().getSource() == null) {
            return false;
        }

        return true;
    }

}
