package edu.isi.karma.controller.history;

import edu.isi.karma.controller.command.ICommand;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by Frank on 9/14/15.
 */
public class WorksheetCommandHistory {

    private class CommandTagListMap {
        private final Map<ICommand.CommandTag, List<ICommand> > commandTagListHashMap = new HashMap<>();
        private final List<RedoCommandObject> redoStack = new ArrayList<>();

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

        public List<RedoCommandObject> getRedoStackCommands() {
            List<RedoCommandObject> commands = new ArrayList<>();
            commands.addAll(redoStack);
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

        public void insertCommandToRedoStack(RedoCommandObject command) {
            redoStack.add(command);
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

    public void removeCommandFromRedoStack(String worksheetId, List<RedoCommandObject> commands) {
        if (worksheetId == null) {
            worksheetId = IMPORT_COMMANDS;
        }
        CommandTagListMap commandTagListMap = historyWorksheetMap.get(worksheetId);
        if (commandTagListMap != null) {
            commandTagListMap.redoStack.removeAll(commands);
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

    public void insertCommandToRedoStack(List<RedoCommandObject> commands) {
        for (RedoCommandObject command : commands) {
            String worksheetId = getWorksheetId(command.getCommand());
            if (worksheetId == null) {
                worksheetId = IMPORT_COMMANDS;
            }
            CommandTagListMap commandTagListMap = historyWorksheetMap.get(worksheetId);
            if (commandTagListMap == null) {
                commandTagListMap = new CommandTagListMap();
                historyWorksheetMap.put(worksheetId, commandTagListMap);
            }
            commandTagListMap.insertCommandToRedoStack(command);
        }

    }

    public void clearRedoStack(String worksheetId) {
        if (worksheetId == null) {
            worksheetId = IMPORT_COMMANDS;
        }
        CommandTagListMap commandTagListMap = historyWorksheetMap.get(worksheetId);
        if (commandTagListMap != null) {
            commandTagListMap.redoStack.clear();
        }
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

    public List<RedoCommandObject> getAllRedoStackCommands() {
        List<RedoCommandObject> commands = new ArrayList<>();
        for (Map.Entry<String, CommandTagListMap> entry : historyWorksheetMap.entrySet()) {
            commands.addAll(entry.getValue().getRedoStackCommands());
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

    public List<RedoCommandObject> getRedoStack(String worksheetId) {
        if (worksheetId == null) {
            worksheetId = IMPORT_COMMANDS;
        }
        CommandTagListMap map = historyWorksheetMap.get(worksheetId);
        if (map == null) {
            return Collections.emptyList();
        }
        return map.redoStack;
    }

    public List<String> getAllWorksheetId() {
        return new ArrayList<>(historyWorksheetMap.keySet());
    }
}
