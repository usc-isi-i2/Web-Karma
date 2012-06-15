/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.modeling.alignment;

import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.controller.update.SVGAlignmentUpdate_ForceKarmaLayout;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.rep.semantictypes.SemanticTypes;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class AlignToOntology {
	private final String vWorksheetId;
	private final String alignmentId;
	private Worksheet worksheet;
	private VWorkspace vWorkspace;
	private Alignment alignment;
	
//	private static Logger logger = LoggerFactory.getLogger(AlignToOntology.class);
	
	public AlignToOntology(Worksheet worksheet, VWorkspace vWorkspace,
			String vWorksheetId) {
		super();
		this.worksheet = worksheet;
		this.vWorkspace = vWorkspace;
		this.vWorksheetId = vWorksheetId;
		this.alignmentId = AlignmentManager.Instance().constructAlignmentId(vWorkspace.getWorkspace().getId(), vWorksheetId);
	}
	
	public void align(boolean replaceExistingAlignment) {
		// Get the previous alignment
		alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		// If we need to use the previous alignment (if it exists)
		if (!replaceExistingAlignment) {
			// If the alignment does not exists, create a new one
			if (alignment == null) {
				alignment = getNewAlignment();
			}
		} else {
			// Save the previously added user links
			List<LabeledWeightedEdge> userLinks = null;
			if(alignment != null) {
				userLinks = alignment.getLinksForcedByUser();
			}
			
			alignment = getNewAlignment();
			// Add user links if any
			if (userLinks != null && userLinks.size() != 0) {
				for (LabeledWeightedEdge edge : userLinks)
					alignment.addUserLink(edge.getID());
			}
		}
		AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);
	}

	public void update(UpdateContainer c) {
		List<String> hNodeIdList = new ArrayList<String>();
		VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);
		List<HNodePath> columns = vw.getColumns();
		for(HNodePath path:columns)
			hNodeIdList.add(path.getLeaf().getId());

		SVGAlignmentUpdate_ForceKarmaLayout svgUpdate = new SVGAlignmentUpdate_ForceKarmaLayout(vWorksheetId, alignmentId, alignment, hNodeIdList);
		/*
		if (root != null) {
			// mariam
			WorksheetRDFGenerator.testRDFGeneration(vWorkspace.getWorkspace(), worksheet, alignment);
		}
		*/
		// Debug
		// GraphUtil.printGraph(tree);
		
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetId));
		c.add(svgUpdate);
	}
	
	public void alignAndUpdate(UpdateContainer c, boolean replaceExistingAlignment) {
		align(replaceExistingAlignment);
		update(c);
	}

	private Alignment getNewAlignment() {
		SemanticTypes semTypes = worksheet.getSemanticTypes();
		// Get the list of semantic types
		List<SemanticType> types = new ArrayList<SemanticType>();
		for (SemanticType type : semTypes.getTypes().values()) {
//		System.out.println("Type: " + type.getType().getLocalName() + " of " + type.getDomain().getLocalName() + "HNode ID: " + type.getHNodeId());
			types.add(type);
		}
		return new Alignment(vWorkspace.getWorkspace().getOntologyManager(), types);
	}
}
