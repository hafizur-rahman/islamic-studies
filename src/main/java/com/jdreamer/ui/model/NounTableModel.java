package com.jdreamer.ui.model;

import com.jdreamer.model.Noun;
import lombok.Getter;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class NounTableModel extends AbstractTableModel {

    private final String[] columns = {"ID", "معنى", "جمع", "تثنية", "واحد", "BookID", "PageID"};
    // Use a private final list to ensure we control the data source
    @Getter
    private final List<Noun> nouns;

    public NounTableModel(List<Noun> nouns) {
        // We wrap the input list in a new ArrayList to ensure it is MUTABLE.
        // This prevents UnsupportedOperationException if List.of() or Arrays.asList() was passed.
        this.nouns = (nouns != null) ? new ArrayList<>(nouns) : new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return nouns.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int col) {
        return columns[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row >= nouns.size()) return null;
        Noun n = nouns.get(row);
        return switch (col) {
            case 0 -> n.getId();
            case 1 -> n.getMeaning();
            case 2 -> n.getPlural();
            case 3 -> n.getDual();
            case 4 -> n.getSingular();
            case 5 -> n.getBookId();
            case 6 -> n.getPageId();
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (col == 0) {
            return false;
        }

        return true;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (row >= nouns.size()) return;
        Noun n = nouns.get(row);

        try {
            switch (col) {
                case 0 -> n.setId(tryParseInt(value));
                case 1 -> n.setMeaning(value != null ? value.toString() : "");
                case 2 -> n.setPlural(value != null ? value.toString() : "");
                case 3 -> n.setDual(value != null ? value.toString() : "");
                case 4 -> n.setSingular(value != null ? value.toString() : "");
                case 5 -> n.setBookId(tryParseInt(value));
                case 6 -> n.setPageId(tryParseInt(value));
            }
            fireTableCellUpdated(row, col);
        } catch (Exception e) {
            System.err.println("Error updating cell: " + e.getMessage());
        }
    }

    /**
     * Helper to prevent ClassCastException when the UI passes a String
     * (from a TextField) into an Integer field.
     */
    private Integer tryParseInt(Object value) {
        if (value instanceof Integer i) return i;
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return 0; // Default value on error
            }
        }
        return 0;
    }

    /* --------------------- Utility --------------------- */

    public void updateData(List<Noun> newNouns) {
        this.nouns.clear();
        if (newNouns != null) {
            this.nouns.addAll(newNouns);
        }

        fireTableDataChanged();
    }

    /**
     * Adds a new row at the end of the existing non-empty rows.
     * Creates a blank Noun object to act as a template for the user.
     */
    public void addEmptyRowAtEnd(int bookId, int pageId) {
        if (nouns.isEmpty() || nouns.get(nouns.size()-1).getSingular() != null) {
            int newIndex = nouns.size();

            Noun newNoun = new Noun(); // Assumes a default constructor exists

            newNoun.setBookId(bookId);
            newNoun.setPageId(pageId);

            nouns.add(newNoun);

            fireTableRowsInserted(newIndex, newIndex);

            // Optional: You could add logic here to automatically
            // select the new row in the JTable via the UI controller.
        }
    }
}
