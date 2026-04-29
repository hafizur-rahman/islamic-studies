package com.jdreamer.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Collections;
import java.util.Set;

public class CustomCellRenderer extends DefaultTableCellRenderer {
    private Set<Integer> arabicCells = Collections.emptySet();

    public CustomCellRenderer(Set<Integer> arabicCells) {
        this.arabicCells = Collections.unmodifiableSet(arabicCells);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (arabicCells.contains(column)) {
            c.setFont(new Font("Arial", Font.PLAIN, 24));
        } else {
            c.setFont(new Font("Arial", Font.PLAIN, 14));
        }
        return c;
    }
}