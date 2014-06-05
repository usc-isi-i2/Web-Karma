package edu.isi.karma.controller.command.alignment;

import java.util.Set;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.CloneTableUtils;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.*;
import java.util.*;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkKeyInfo;

public class AugmentDataCommand extends Command{
	private String worksheetId;
	private String predicate;
	private String triplesMap;
	private String columnUri;
	private String alignmentId;
	
	public AugmentDataCommand(String id, String worksheetId, String columnUri, String alignmentId, String predicate, String triplesMap) {
		super(id);
		this.worksheetId = worksheetId;
		this.predicate = predicate;
		this.triplesMap = triplesMap;
		this.columnUri = columnUri;
		this.alignmentId = alignmentId;
	}

	@Override
	public String getCommandName() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Augment Data";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommandType getCommandType() {
		// TODO Auto-generated method stub
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		// TODO Auto-generated method stub
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		RepFactory factory = workspace.getFactory();
		Worksheet worksheet = factory.getWorksheet(worksheetId);
		Set<LabeledLink> tmp = alignment.getCurrentOutgoingLinksToNode(columnUri);
		String hNodeId = null;
		for (LabeledLink link : tmp) {
			if (link.getKeyType() == LinkKeyInfo.UriOfInstance) {
				hNodeId = link.getTarget().getId();
			}
		}
		HNode hnode = factory.getHNode(hNodeId);
		List<Table> dataTables = new ArrayList<Table>();
		CloneTableUtils.getDatatable(worksheet.getDataTable(), factory.getHTable(hnode.getHTableId()), dataTables);
		System.out.println(predicate);
		System.out.println(hNodeId);
		System.out.println(triplesMap);
		System.out.println(hnode);
		return new UpdateContainer();
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
