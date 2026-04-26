package com.jdreamer.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VerbPanel extends JPanel {
    private JTable verbsTable;
    private DefaultTableModel verbsModel;

    public VerbPanel() {
        super(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        verbsModel = new DefaultTableModel(new Object[]{"Bab", "English", "Command", "Present", "Past", "BookID", "PageID"}, 0);
        verbsTable = new JTable(verbsModel);
        verbsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        verbsTable.setRowHeight(24);
        verbsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JButton addVerbBtn = new JButton("Add Verb");
        //addVerbBtn.addActionListener(e -> verbsModel.addRow(new Object[]{"", "", "", "", "", 0, 0}));

        add(new JScrollPane(verbsTable), BorderLayout.CENTER);
        add(addVerbBtn, BorderLayout.SOUTH);
    }
}
