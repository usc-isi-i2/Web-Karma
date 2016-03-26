package edu.isi.karma.kr2rml.template;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.rep.Row;

public class ConstantTemplateTermSetPopulatorPlan extends
TemplateTermSetPopulatorPlan {

	public ConstantTemplateTermSetPopulatorPlan(SuperSelection sel) {
		super(sel);
	}

	public Map<PopulatedTemplateTermSet, List<PartiallyPopulatedTermSet>> execute(Row topRow, List<PopulatedTemplateTermSet> anchors)
	{
		Map<PopulatedTemplateTermSet, List<PartiallyPopulatedTermSet>> results = new HashMap<>();
		
			for(PopulatedTemplateTermSet anchor: anchors)
			{
				List<PartiallyPopulatedTermSet> references = new LinkedList<>();
				references.add(new PartiallyPopulatedTermSet());
				results.put(anchor, references);
			}
			return results;
		
	}
}
