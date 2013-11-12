/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.isi.karma.kr2rml;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.alignment.GenerateR2RMLModelCommand;
import edu.isi.karma.controller.command.publish.PublishRDFCommand;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.controller.history.WorksheetCommandHistoryReader;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

/**
 *
 * @author mielvandersande
 */
public class KR2RMLModelGenerator {

    public KR2RMLMappingGenerator generateModel(Workspace workspace, Worksheet worksheet) throws JSONException {
        // Get the alignment for this Worksheet
        Alignment alignment = AlignmentManager.Instance().getAlignment(AlignmentManager.
                Instance().constructAlignmentId(workspace.getId(), worksheet.getId()));

        if (alignment == null) {
            throw new NullPointerException("Please align the worksheet before generating R2RML Model!");
        }

        // Get the namespace and prefix from the preferences
        String namespace = "";
        String prefix = "";
        JSONObject prefObject = workspace.getCommandPreferences().getCommandPreferencesJSONObject(
                PublishRDFCommand.class.getSimpleName() + "Preferences");
        if (prefObject != null) {
            namespace = prefObject.getString(GenerateR2RMLModelCommand.PreferencesKeys.rdfNamespace.name());
            prefix = prefObject.getString(GenerateR2RMLModelCommand.PreferencesKeys.rdfPrefix.name());
            namespace = ((namespace == null) || (namespace.equals("")))
                    ? Namespaces.KARMA_DEV : namespace;
            prefix = ((prefix == null) || (prefix.equals("")))
                    ? Prefixes.KARMA_DEV : prefix;
        } else {
            namespace = Namespaces.KARMA_DEV;
            prefix = Prefixes.KARMA_DEV;
        }

        // Generate the KR2RML data structures for the RDF generation
        final ErrorReport errorReport = new ErrorReport();
        OntologyManager ontMgr = workspace.getOntologyManager();

        return new KR2RMLMappingGenerator(ontMgr, alignment,
                worksheet.getSemanticTypes(), prefix, namespace, true, errorReport);


    }

    public void writeModel(Workspace workspace,
            KR2RMLMappingGenerator mappingGen, Worksheet worksheet, PrintWriter writer)
            throws RepositoryException, FileNotFoundException,
            UnsupportedEncodingException, JSONException {

        OntologyManager ontMgr = workspace.getOntologyManager();
        WorksheetModelWriter modelWriter = new WorksheetModelWriter(writer,
                workspace.getFactory(), ontMgr, worksheet.getTitle());

        // Writer worksheet properties such as Service URL
        modelWriter.writeWorksheetProperties(worksheet);

        // Write the transformation commands if any
        WorksheetCommandHistoryReader histReader = new WorksheetCommandHistoryReader(worksheet.getId(),
                workspace);
        List<String> commandsJSON = histReader.getJSONForCommands(Command.CommandTag.Transformation);
        if (!commandsJSON.isEmpty()) {
            modelWriter.writeTransformationHistory(commandsJSON);
        }
        // Write the worksheet history
        String historyFilePath = HistoryJsonUtil.constructWorksheetHistoryJsonFilePath(
                worksheet.getTitle(), workspace.getCommandPreferencesId());
        modelWriter.writeCompleteWorksheetHistory(historyFilePath);

        // Write the R2RML mapping
        modelWriter.writeR2RMLMapping(ontMgr, mappingGen);
        modelWriter.close();
        writer.flush();
        writer.close();
    }

    public String extractHistoryFromModel(InputStream r2rmlModelStream)
            throws RepositoryException, RDFParseException, IOException {
        Repository myRepository = new SailRepository(new MemoryStore());
        myRepository.initialize();
        RepositoryConnection con = myRepository.getConnection();
        ValueFactory f = con.getValueFactory();
        con.add(r2rmlModelStream, "", RDFFormat.TURTLE);


        URI wkHistUri = f.createURI(Uris.KM_HAS_WORKSHEET_HISTORY_URI);

        RepositoryResult<Statement> wkHistStmts = con.getStatements(null, wkHistUri,
                null, false);
        while (wkHistStmts.hasNext()) {
            // Return the object value of the first history statement
            Statement st = wkHistStmts.next();
            Value histVal = st.getObject();
            return histVal.stringValue();
        }
        return "";
    }
}
