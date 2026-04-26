package com.jdreamer.ui;

import javax.swing.*;
import java.awt.*;

public class NotesPanel extends JPanel {
    private JTextArea translationArea;

    public NotesPanel() {
        super(new BorderLayout());
        
        buildUI();
    }

    private void buildUI() {
        translationArea = new JTextArea(8, 40);
        translationArea.setLineWrap(true);
        translationArea.setWrapStyleWord(true);
        JScrollPane translationScroll = new JScrollPane(translationArea);
        translationScroll.setBorder(BorderFactory.createTitledBorder("Translation (Editable)"));

        JPanel tablesPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        tablesPanel.add(new VerbPanel());
        tablesPanel.add(new NounPanel());
        tablesPanel.setBorder(BorderFactory.createTitledBorder("Language Data (Optional Editable)"));

        JButton saveDataBtn = new JButton("Save Data to DB");
        //saveDataBtn.addActionListener(e -> saveDataToDB());

        JPanel topLeft = new JPanel(new BorderLayout(4, 4));
        topLeft.add(translationScroll, BorderLayout.NORTH);
        topLeft.add(tablesPanel, BorderLayout.CENTER);
        topLeft.add(saveDataBtn, BorderLayout.SOUTH);

        add(topLeft, BorderLayout.CENTER);
    }
}
