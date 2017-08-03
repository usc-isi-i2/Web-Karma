package edu.isi.karma.controller.history;

import edu.isi.karma.controller.command.ICommand;

import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import edu.isi.karma.controller.command.Command;

/**
 * Created by Frank on 9/14/15.
 */
public class WorksheetCommandHistory implements Cloneable {

    private class CommandTagListMap {
        private final Map<ICommand.CommandTag, List<ICommand> > commandTagListHashMap = new HashMap<>();
        private RedoCommandObject lastRedoCommand;
        private RedoCommandObject currentCommand;
        private boolean stale;
        public CommandTagListMap() {
            for (ICommand.CommandTag tag : ICommand.CommandTag.values()) {
                commandTagListHashMap.put(tag, new ArrayList<ICommand>());
            }
        }

        public List<ICommand> getCommands() {
            List<ICommand> commands = new ArrayList<>();
            commands.addAll(commandTagListHashMap.get(ICommand.CommandTag.Import));
            commands.addAll(commandTagListHashMap.get(ICommand.CommandTag.Transformation));
            commands.addAll(commandTagListHashMap.get(ICommand.CommandTag.Selection));
            commands.addAll(commandTagListHashMap.get(ICommand.CommandTag.SemanticType));
            commands.addAll(commandTagListHashMap.get(ICommand.CommandTag.Modeling));
            commands.addAll(commandTagListHashMap.get(ICommand.CommandTag.Other));
            return commands;
        }

        public List<ICommand> getCommands(ICommand.CommandTag commandTag) {
            List<ICommand> commands = new ArrayList<>();
            commands.addAll(commandTagListHashMap.get(commandTag));
            return commands;
        }

        public void addCommandToHistory(ICommand command) {
            commandTagListHashMap.get(command.getTagFromPriority()).add(command);
        }

        public void removeCommandFromHistory(List<ICommand> commands) {
            for (ICommand.CommandTag tag : ICommand.CommandTag.values()) {
                commandTagListHashMap.get(tag).removeAll(commands);
            }
        }
    }

    private final Map<String, CommandTagListMap> historyWorksheetMap = new TreeMap<>();
    private static final String IMPORT_COMMANDS = "ImportCommands";
    
    public WorksheetCommandHistory() {
        historyWorksheetMap.put(IMPORT_COMMANDS, new CommandTagListMap());
    }

    public void removeCommandFromHistory(List<ICommand> commands) {
        for(Map.Entry<String, CommandTagListMap> entry : historyWorksheetMap.entrySet()) {
            entry.getValue().removeCommandFromHistory(commands);
        }
    }

    public void replaceCommandFromHistory(ICommand oldCommand, ICommand newCommand) {
        String worksheetId = getWorksheetId(oldCommand);
        if (worksheetId == null) {
            worksheetId = IMPORT_COMMANDS;
        }
        CommandTagListMap commandTagListMap = historyWorksheetMap.get(worksheetId);
        if (commandTagListMap != null) {
            for (Map.Entry<ICommand.CommandTag, List<ICommand>> entry : commandTagListMap.commandTagListHashMap.entrySet()) {
                int index = entry.getValue().indexOf(oldCommand);
                if (index != -1) {
                    entry.getValue().set(index, newCommand);
                }
            }
        }
    }

    public void insertCommandToHistory(ICommand command) {
        String worksheetId = getWorksheetId(command);
        if (worksheetId == null) {
            worksheetId = IMPORT_COMMANDS;
        }
        CommandTagListMap commandTagListMap = historyWorksheetMap.get(worksheetId);
        if (commandTagListMap == null) {
            commandTagListMap = new CommandTagListMap();
            historyWorksheetMap.put(worksheetId, commandTagListMap);
        }
        commandTagListMap.addCommandToHistory(command);
    }

    public void insertCommandToHistoryAfterCommand(ICommand newCommand, ICommand afterCommand) {
        String worksheetId = getWorksheetId(afterCommand);
        if (worksheetId == null) {
            worksheetId = IMPORT_COMMANDS;
        }
        CommandTagListMap commandTagListMap = historyWorksheetMap.get(worksheetId);
        if (commandTagListMap != null) {
            for (Map.Entry<ICommand.CommandTag, List<ICommand>> entry : commandTagListMap.commandTagListHashMap.entrySet()) {
                int index = entry.getValue().indexOf(afterCommand);
                if (index != -1) {
                    entry.getValue().add(index+1, newCommand);
                }
            }
        }
    }
    
    public void setLastRedoCommandObject(RedoCommandObject command) {

        String worksheetId = getWorksheetId(command.getCommand());
        if (worksheetId == null) {
            worksheetId = IMPORT_COMMANDS;
        }
        CommandTagListMap commandTagListMap = historyWorksheetMap.get(worksheetId);
        if (commandTagListMap == null) {
            commandTagListMap = new CommandTagListMap();
            historyWorksheetMap.put(worksheetId, commandTagListMap);
        }
        if (commandTagListMap.lastRedoCommand == null) {
            commandTagListMap.lastRedoCommand = command;
        }
    }

    public void setCurrentCommand(ICommand command, Pair<ICommand,Object> consolidatedCommand) {
        String worksheetId = getWorksheetId(command);
        if (worksheetId == null) {
            worksheetId = IMPORT_COMMANDS;
        }
        CommandTagListMap commandTagListMap = historyWorksheetMap.get(worksheetId);
        if (commandTagListMap == null) {
            commandTagListMap = new CommandTagListMap();
            historyWorksheetMap.put(worksheetId, commandTagListMap);
        }
        commandTagListMap.currentCommand = new RedoCommandObject(command, consolidatedCommand);
    }

    public void setStale(String worksheetId, boolean stale) {
        if (worksheetId == null) {
            worksheetId = IMPORT_COMMANDS;
        }
        CommandTagListMap commandTagListMap = historyWorksheetMap.get(worksheetId);
        if (commandTagListMap != null) {
            commandTagListMap.stale = stale;
        }
    }

    public void clearRedoCommand(String worksheetId) {
        if (worksheetId == null) {
            worksheetId = IMPORT_COMMANDS;
        }
        CommandTagListMap commandTagListMap = historyWorksheetMap.get(worksheetId);
        if (commandTagListMap != null) {
            commandTagListMap.lastRedoCommand = null;
        }
    }

    public void clearCurrentCommand(String worksheetId) {
        if (worksheetId == null) {
            worksheetId = IMPORT_COMMANDS;
        }
        CommandTagListMap commandTagListMap = historyWorksheetMap.get(worksheetId);
        if (commandTagListMap != null) {
            commandTagListMap.currentCommand = null;
        }
    }

    public void removeWorksheet(String worksheetId) {
        historyWorksheetMap.remove(worksheetId);
    }

    public String getWorksheetId(ICommand c) {
        try {
            Method getWorksheetIdMethod = c.getClass().getMethod("getWorksheetId");
            String worksheetId = (String)getWorksheetIdMethod.invoke(c, (Object[])null);
            return worksheetId;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {

        }
        return null;
    }

    public List<ICommand> getAllCommands() {
        List<ICommand> commands = new ArrayList<>();
        for (Map.Entry<String, CommandTagListMap> entry : historyWorksheetMap.entrySet()) {
            commands.addAll(entry.getValue().getCommands());
        }
        return commands;
    }

    public List<ICommand> getCommandsFromWorksheetId(String worksheetId) {
        List<ICommand> commands = new ArrayList<>();
        if (worksheetId == null) {
            worksheetId = IMPORT_COMMANDS;
        }
        CommandTagListMap map = historyWorksheetMap.get(worksheetId);
        if (map != null) {
            commands.addAll(map.getCommands());
        }
        return commands;
    }

    public List<ICommand> getCommandsFromWorksheetIdAndCommandTag(String worksheetId, ICommand.CommandTag commandTag) {
        List<ICommand> commands = new ArrayList<>();
        if (worksheetId == null) {
            worksheetId = IMPORT_COMMANDS;
        }
        CommandTagListMap map = historyWorksheetMap.get(worksheetId);
        if (map != null) {
            commands.addAll(map.getCommands(commandTag));
        }
        return commands;
    }

    public List<ICommand> getCommandsAfterCommand(ICommand command, ICommand.CommandTag commandTag) {
    	List<ICommand> commands = new ArrayList<>();
    	String worksheetId = getWorksheetId(command);
    	if (worksheetId == null) {
            worksheetId = IMPORT_COMMANDS;
        }
    	CommandTagListMap map = historyWorksheetMap.get(worksheetId);
        if (map != null) {
        	List<ICommand> tagCommands = map.getCommands(commandTag);
        	boolean start = false;
        	for(ICommand cmd : tagCommands) {
        		if(cmd.equals(command)) {
        			start = true;
        			continue;
        		}
        		if(start)
        			commands.add(cmd);
        	}
            commands.addAll(map.getCommands(commandTag));
        }
        return commands;
    }
    
    public RedoCommandObject getLastRedoCommandObject(String worksheetId) {
        if (worksheetId == null) {
            worksheetId = IMPORT_COMMANDS;
        }
        CommandTagListMap map = historyWorksheetMap.get(worksheetId);
        if (map == null) {
            return null;
        }
        return map.lastRedoCommand;
    }

    public RedoCommandObject getCurrentRedoCommandObject(String worksheetId) {
        if (worksheetId == null) {
            worksheetId = IMPORT_COMMANDS;
        }
        CommandTagListMap map = historyWorksheetMap.get(worksheetId);
        if (map == null) {
            return null;
        }
        return map.currentCommand;
    }

    public boolean isStale(String worksheetId) {
        if (worksheetId == null) {
            worksheetId = IMPORT_COMMANDS;
        }
        CommandTagListMap commandTagListMap = historyWorksheetMap.get(worksheetId);
        if (commandTagListMap != null) {
            return commandTagListMap.stale;
        }
        return false;
    }

    public List<String> getAllWorksheetId() {
        return new ArrayList<>(historyWorksheetMap.keySet());
    }

    @Override
    public WorksheetCommandHistory clone() {
        WorksheetCommandHistory worksheetCommandHistory = new WorksheetCommandHistory();
        for (String worksheetId : getAllWorksheetId()) {
            worksheetCommandHistory.historyWorksheetMap.put(worksheetId, new CommandTagListMap());
        }
        for (ICommand command : getAllCommands()) {
            worksheetCommandHistory.insertCommandToHistory(command);
        }
        for (String worksheetId : getAllWorksheetId()) {
            CommandTagListMap newMap = worksheetCommandHistory.historyWorksheetMap.get(worksheetId);
            CommandTagListMap oldMap = this.historyWorksheetMap.get(worksheetId);
            if (oldMap != null && newMap != null) {
                if (oldMap.currentCommand != null) {
                    newMap.currentCommand = new RedoCommandObject(oldMap.currentCommand.getCommand(), oldMap.currentCommand.getConsolidatedCommand());
                }
                if (oldMap.lastRedoCommand != null) {
                    newMap.lastRedoCommand = new RedoCommandObject(oldMap.lastRedoCommand.getCommand(), oldMap.lastRedoCommand.getConsolidatedCommand());
                }
                newMap.stale = oldMap.stale;
            }
        }
        return worksheetCommandHistory;
    }
}
