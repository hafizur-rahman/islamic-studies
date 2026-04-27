package com.jdreamer.ui.model;

import com.jdreamer.model.Noun;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class NounTableModel extends AbstractTableModel {

    private final String[] columns = {"ID", "Meaning", "Plural", "Dual", "Singular", "BookID", "PageID"};
    private List<Noun> nouns;          // Word is a POJO

    public NounTableModel(List<Noun> nouns) {
        this.nouns = nouns;
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

    // Optional: allow editing
    @Override
    public boolean isCellEditable(int row, int col) {
        return true;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        Noun n = nouns.get(row);

        switch (col) {
            case 0 -> n.setId((Integer) value);
            case 1 -> n.setMeaning((String) value);
            case 2 -> n.setPlural((String) value);
            case 3 -> n.setDual((String) value);
            case 4 -> n.setSingular((String) value);
            case 5 -> n.setBookId((Integer) value);
            case 6 -> n.setPageId((Integer) value);
        }
        fireTableCellUpdated(row, col);
    }

    /* --------------------- Utility --------------------- */
    public void updateData(List<Noun> nouns) {
        this.nouns = nouns;

        fireTableDataChanged();
    }

    public void addWord(Noun n) {
        int idx = nouns.size();
        nouns.add(n);

        fireTableRowsInserted(idx, idx);
    }
}
