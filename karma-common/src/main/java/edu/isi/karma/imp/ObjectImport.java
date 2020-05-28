package edu.isi.karma.imp;

import java.io.IOException;
import java.util.Iterator;

import org.json.JSONException;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.rep.metadata.WorksheetProperties.SourceTypes;
import edu.isi.karma.webserver.KarmaException;

public class ObjectImport extends Import {

	private Iterator<ObjectImportEntity> entities;

	public ObjectImport(String worksheetName, Workspace workspace, String encoding,
			Iterator<ObjectImportEntity> entities) {
		super(worksheetName, workspace, encoding);
		this.entities = entities;
	}

	@Override
	public Worksheet generateWorksheet() throws IOException, KarmaException {
		Table dataTable = getWorksheet().getDataTable();
		HTable headers = getWorksheet().getHeaders();

		while (entities.hasNext()) {
			ObjectImportEntity entity = entities.next();
			if (headers.getHNodes().isEmpty()) {
				for (String column : entity.getColumns()) {
					headers.addHNode(column, HNodeType.Regular, getWorksheet(), getFactory());
				}
			}
			addRow(getWorksheet(), getFactory(), entity, dataTable);
		}
		getWorksheet().getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.sourceType,
				SourceTypes.OBJECT.toString());
		return getWorksheet();
	}

	private boolean addRow(Worksheet worksheet, RepFactory fac, ObjectImportEntity entity, Table dataTable)
			throws IOException {

		if (entity == null || entity.getLiteralValues().isEmpty()) {
			return false;
		}

		Row row = dataTable.addRow(fac);
		Iterator<String> columnsItr = entity.getColumns().iterator();
		Iterator<String> literalValuesItr = entity.getLiteralValues().iterator();
		HTable headers = worksheet.getHeaders();

		while (columnsItr.hasNext() && literalValuesItr.hasNext()) {
			String column = columnsItr.next();
			String literalValue = literalValuesItr.next();
			HNode hNode = headers.getHNodeFromColumnName(column);
			row.setValue(hNode.getId(), literalValue, fac);
		}
		return true;
	}

	@Override
	public Import duplicate() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
