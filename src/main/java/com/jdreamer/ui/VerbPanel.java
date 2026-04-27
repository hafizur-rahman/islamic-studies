package com.jdreamer.ui;

import com.jdreamer.model.Noun;
import com.jdreamer.model.Verb;
import com.jdreamer.ui.model.VerbTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class VerbPanel extends JPanel {
    private VerbTableModel verbsModel;

    public VerbPanel() {
        super(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        verbsModel = new VerbTableModel(Collections.emptyList());

        JTable verbsTable = new JTable(verbsModel);
        verbsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        verbsTable.setRowHeight(24);
        verbsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JPanel buttonPanel = new JPanel();
        JButton addVerbBtn = new JButton("Add Verb");
        //addVerbBtn.addActionListener(e -> verbsModel.addRow(new Object[]{"", "", "", "", "", 0, 0}));
        buttonPanel.add(addVerbBtn);

        add(new JScrollPane(verbsTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setBorder(BorderFactory.createTitledBorder("Verbs"));
    }

    public void loadData(List<Verb> verbs) {
        verbsModel.updateData(verbs);
    }
}
