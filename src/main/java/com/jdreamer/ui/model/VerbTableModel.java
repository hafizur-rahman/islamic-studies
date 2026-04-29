package com.jdreamer.ui.model;

import com.jdreamer.model.Verb;
import lombok.Getter;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class VerbTableModel extends AbstractTableModel {

    private final String[] columns = {"ID", "Bab", "Meaning", "Masdar", "Command", "Present", "Past", "Word", "BookID", "PageID"};

    @Getter
    private final List<Verb> verbs;          // Word is a POJO

    public VerbTableModel(List<Verb> verbs) {
        this.verbs = (verbs != null) ? new ArrayList<>(verbs) : new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return verbs.size();
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
        Verb v = verbs.get(row);

        return switch (col) {
            case 0 -> v.getId();
            case 1 -> v.getBab();
            case 2 -> v.getMeaning();
            case 3 -> v.getMasdar();
            case 4 -> v.getCommand();
            case 5 -> v.getFuture();
            case 6 -> v.getPast();
            case 7 -> v.getWord();
            case 8 -> v.getBookId();
            case 9 -> v.getPageId();

            default -> null;
        };
    }

    // Optional: allow editing
    @Override
    public boolean isCellEditable(int row, int col) {
        if (col == 0) {
            return false;
        }

        return true;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        Verb v = verbs.get(row);

        try {
            switch (col) {
                case 0 -> v.setId(tryParseInt(value));
                case 1 -> v.setBab(value != null ? value.toString() : "");
                case 2 -> v.setMeaning(value != null ? value.toString() : "");
                case 3 -> v.setMasdar(value != null ? value.toString() : "");
                case 4 -> v.setCommand(value != null ? value.toString() : "");
                case 5 -> v.setFuture(value != null ? value.toString() : "");
                case 6 -> v.setPast(value != null ? value.toString() : "");
                case 7 -> v.setWord(value != null ? value.toString() : "");
                case 8 -> v.setBookId(tryParseInt(value));
                case 9 -> v.setPageId(tryParseInt(value));
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
    public void updateData(List<Verb> newVerbs) {
        this.verbs.clear();
        if (newVerbs != null) {
            this.verbs.addAll(newVerbs);
        }

        fireTableDataChanged();
    }

    /**
     * Adds a new row at the end of the existing non-empty rows.
     * Creates a blank Noun object to act as a template for the user.
     */
    public void addEmptyRowAtEnd(int bookId, int pageId) {
        if (verbs.isEmpty() || verbs.get(verbs.size()-1).getWord() != null) {
            int newIndex = verbs.size();

            Verb newVerb = new Verb(); // Assumes a default constructor exists

            newVerb.setBookId(bookId);
            newVerb.setPageId(pageId);

            verbs.add(newVerb);

            fireTableRowsInserted(newIndex, newIndex);

            // Optional: You could add logic here to automatically
            // select the new row in the JTable via the UI controller.
        }
    }

}
