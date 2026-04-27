package com.jdreamer.ui.model;

import com.jdreamer.model.Verb;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class VerbTableModel extends AbstractTableModel {

    private final String[] columns = {"ID", "Bab", "Meaning", "Masdar", "Command", "Present", "Past", "Word", "BookID", "PageID"};
    private List<Verb> verbs;          // Word is a POJO

    public VerbTableModel(List<Verb> verbs) {
        this.verbs = verbs;
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
        return true;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        Verb v = verbs.get(row);

        switch (col) {
            case 0 -> v.setId((Integer) value);
            case 1 -> v.setBab((String) value);
            case 2 -> v.setMeaning((String) value);
            case 3 -> v.setMasdar((String) value);
            case 4 -> v.setCommand((String) value);
            case 5 -> v.setFuture((String) value);
            case 6 -> v.setPast((String) value);
            case 7 -> v.setWord((String) value);
            case 8 -> v.setBookId((Integer) value);
            case 9 -> v.setPageId((Integer) value);
        }
        fireTableCellUpdated(row, col);
    }

    /* --------------------- Utility --------------------- */
    public void updateData(List<Verb> verbs) {
        this.verbs = verbs;

        fireTableDataChanged();
    }

    public void addWord(Verb v) {
        int idx = verbs.size();
        verbs.add(v);

        fireTableRowsInserted(idx, idx);
    }
}
