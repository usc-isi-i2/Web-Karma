package edu.isi.karma.controller.command.alignment;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.rep.semantictypes.SynonymSemanticTypes;
import edu.isi.karma.view.VWorkspace;

public class SetSemanticTypeCommandFactory extends CommandFactory {

	private enum Arguments {
		vWorksheetId, hNodeId, isKey
	}

	private enum ClientJsonKeys {
		isPrimary
	}

	private final Logger logger = LoggerFactory.getLogger(this.getClass()
			.getSimpleName());

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {

		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String vWorksheetId = request.getParameter(Arguments.vWorksheetId
				.name());
		boolean isPartOfKey = Boolean.parseBoolean(request
				.getParameter(Arguments.isKey.name()));

		/*
		 * Parse the input JSON Array to get the sem types (including the
		 * synonym ones)
		 */
		List<SemanticType> typesList = new ArrayList<SemanticType>();
		SemanticType primaryType = null;
		String arrStr = request
				.getParameter(SemanticTypesUpdate.JsonKeys.SemanticTypesArray.name());
		JSONArray arr;
		try {
			arr = new JSONArray(arrStr);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject type = arr.getJSONObject(i);
				// Look for the primary semantic type
				if (type.getBoolean(ClientJsonKeys.isPrimary.name())) {
					primaryType = new SemanticType(hNodeId,
							type.getString(SemanticTypesUpdate.JsonKeys.FullType.name()),
							type.getString(SemanticTypesUpdate.JsonKeys.Domain.name()),
							SemanticType.Origin.User, 1.0, isPartOfKey);
				} else {		// Synonym semantic type
					SemanticType synType = new SemanticType(hNodeId,
							type.getString(SemanticTypesUpdate.JsonKeys.FullType.name()),
							type.getString(SemanticTypesUpdate.JsonKeys.Domain.name()),
							SemanticType.Origin.User, 1.0, isPartOfKey);
					typesList.add(synType);
				}
			}
		} catch (JSONException e) {
			logger.error("Bad JSON received from server!", e);
			return null;
		}
		
		SynonymSemanticTypes synTypes = new SynonymSemanticTypes(typesList);

		return new SetSemanticTypeCommand(getNewId(vWorkspace), vWorksheetId,
				hNodeId, isPartOfKey, primaryType, synTypes);
	}
}
