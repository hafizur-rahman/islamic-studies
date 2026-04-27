package com.jdreamer.ui;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.util.function.Consumer;

/**
 *  A JTable that
 *   1) lets every cell be edited,
 *   2) automatically appends a blank row when the user starts editing the last row,
 *   3) notifies a consumer whenever a cell value changes.
 *
 *  Usage:
 *      EditableTable table = new EditableTable(new String[]{"Word","Definition","Book","Page"},
 *                                             rowData, changedRow -> {
 *                          // changedRow contains the 0‑based row index
 *                          // Update DB or whatever you need
 *                      });
 *      table.addRow();          // optional – start with a blank row
 */
public class EditableTable extends JTable {

    /** The underlying table model */
    private final DefaultTableModel model;

    /** Called whenever any cell is committed.  Parameter = row index that changed. */
    private final Consumer<Integer> onCellUpdated;

    public EditableTable(String[] columnNames,
                         Object[][] rowData,
                         Consumer<Integer> onCellUpdated) {

        this.onCellUpdated = onCellUpdated;
        this.model = new DefaultTableModel(rowData, columnNames) {
            // 1️⃣ every cell is editable
            @Override public boolean isCellEditable(int row, int column) { return true; }

            // 2️⃣ keep a blank row at the end – see addBlankRow()
        };
        setModel(model);

        // 3️⃣ Commit edits when focus leaves the cell
//        setSurrendersFocusOnKeystroke(true);
//        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
//                .put(KeyStroke.getKeyStroke("ENTER"), "stopCellEditing");
//        getActionMap().put("stopCellEditing", e -> stopCellEditing());

        // 4️⃣ Listen for data changes
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                // We only care about value updates (not insert/delete)
                if (e.getType() == TableModelEvent.UPDATE && e.getFirstRow() == e.getLastRow()) {
                    int row = e.getFirstRow();
                    // If the edited row is the last row, append a new blank one
                    if (row == model.getRowCount() - 1 && rowDataNotEmpty(row)) {
                        addBlankRow();
                    }
                    if (onCellUpdated != null) {
                        onCellUpdated.accept(row);
                    }
                }
            }
        });

        setFillsViewportHeight(true);
        setAutoCreateRowSorter(true);
    }

    /** Call this once (or whenever you want to add a new empty row) */
    public void addBlankRow() {
        model.addRow(new Object[model.getColumnCount()]);
    }

    /** Helper: checks if the row already contains any non‑empty value */
    private boolean rowDataNotEmpty(int row) {
        for (int col = 0; col < model.getColumnCount(); col++) {
            Object val = model.getValueAt(row, col);
            if (val != null && !val.toString().trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** Convenience: returns the model so you can query it later */
    public DefaultTableModel getTableModel() { return model; }
}

