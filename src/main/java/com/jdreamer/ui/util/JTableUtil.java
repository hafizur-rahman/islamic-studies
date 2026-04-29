package com.jdreamer.ui.util;

import javax.swing.*;

public class JTableUtil {

    /**
     * Adds a key binding to a JTable without interfering with cell editing.
     */
    public static void addKeyBinding(JTable table, String actionName, KeyStroke keyStroke, Action action) {
        // Use WHEN_ANCESTOR_OF_FOCUSED_COMPONENT so it works even if a cell is being edited
        InputMap inputMap = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = table.getActionMap();

        inputMap.put(keyStroke, actionName);
        actionMap.put(actionName, action);
    }
}
