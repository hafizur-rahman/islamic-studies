package com.jdreamer.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class NounPanel extends JPanel {
    private JTable nounsTable;
    private DefaultTableModel nounsModel;

    public NounPanel() {
        super(new BorderLayout());

        buildUI();
    }

    public void buildUI() {
        nounsModel = new DefaultTableModel(new Object[]{"English", "Plural", "Dual", "Singular", "BookID", "PageID"}, 0);
        nounsTable = new JTable(nounsModel);
        nounsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        nounsTable.setRowHeight(24);
        nounsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        add(new JScrollPane(nounsTable), BorderLayout.CENTER);

        JButton addNounBtn = new JButton("Add Noun");
        //addNounBtn.addActionListener(e -> nounsModel.addRow(new Object[]{"", "", "", "", 0, 0}));

        add(addNounBtn, BorderLayout.SOUTH);
    }
}
