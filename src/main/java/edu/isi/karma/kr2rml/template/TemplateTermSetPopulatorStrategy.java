package edu.isi.karma.kr2rml.template;

import java.util.Collection;

import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;

public interface TemplateTermSetPopulatorStrategy {


	public Collection<Node> getNodes(Row topRow, Row currentRow);
}
