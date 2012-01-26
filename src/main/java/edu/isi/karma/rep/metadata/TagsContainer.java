package edu.isi.karma.rep.metadata;

import java.util.HashSet;
import java.util.Set;

public class TagsContainer {
	public static enum TagName {
		Outlier
	}

	public static enum Color {
		Blue, Red, Green
	}

	private Set<Tag> tags = new HashSet<Tag>();

	public Set<Tag> getTags() {
		return tags;
	}

	public void addTag(Tag tag) {
		tags.add(tag);
	}

	public void removeTag(Tag tag) {
		tags.remove(tag);
	}

	public Tag getTag(TagName tagName) {
		for (Tag tag : tags) {
			if(tag.getLabel().name().equals(tagName.name()))
				return tag;
		}
		return null;
	}
}
