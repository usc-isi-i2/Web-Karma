package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.rep.metadata.Tag;
import edu.isi.karma.view.VWorkspace;

public class TagsUpdate extends AbstractUpdate {

	private static Logger logger = LoggerFactory.getLogger(TagsUpdate.class);

	private enum JsonKeys {
		Tags, Label, Color, Nodes
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		Set<Tag> tags = vWorkspace.getWorkspace().getTagsContainer().getTags();
		try {
			JSONObject topObj = new JSONObject();
			topObj.put(GenericJsonKeys.updateType.name(),
					TagsUpdate.class.getSimpleName());

			JSONArray arr = new JSONArray();
			for (Tag tag : tags) {
				JSONObject tagObj = new JSONObject();
				tagObj.put(JsonKeys.Label.name(), tag.getLabel().name());
				tagObj.put(JsonKeys.Color.name(), tag.getColor().name());

				JSONArray nodeArr = new JSONArray(tag.getNodeIdList());
				tagObj.put(JsonKeys.Nodes.name(), nodeArr);

				arr.put(tagObj);
			}
			topObj.put(JsonKeys.Tags.name(), arr);

			pw.write(topObj.toString(4));
		} catch (JSONException e) {
			logger.error("Error occured while writing to JSON!", e);
		}

	}

}
