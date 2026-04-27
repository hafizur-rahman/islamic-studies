package com.jdreamer.ui;

import com.jdreamer.model.Noun;
import com.jdreamer.ui.model.NounTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class NounPanel extends JPanel {
    private NounTableModel nounsModel;

    public NounPanel() {
        super(new BorderLayout());

        buildUI();
    }

    public void buildUI() {
        nounsModel = new NounTableModel(Collections.emptyList());

        JTable nounsTable = new JTable(nounsModel);
        nounsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        nounsTable.setRowHeight(24);
        nounsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        add(new JScrollPane(nounsTable), BorderLayout.CENTER);

        JButton addNounBtn = new JButton("Add Noun");
        //addNounBtn.addActionListener(e -> nounsModel.addRow(new Object[]{"", "", "", "", 0, 0}));

        add(addNounBtn, BorderLayout.SOUTH);
    }

    public void loadData(List<Noun> nouns) {
        nounsModel.updateData(nouns);
    }
}
