package com.jdreamer.ui;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    public MainWindow() {
        super("Islamic Studies");

        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        BookPanel bookPanel = new BookPanel();
        NotesPanel notesPanel = new NotesPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, bookPanel, notesPanel);
        splitPane.setDividerLocation(700);

        add(splitPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 850);
        setLocationRelativeTo(null);
        setVisible(true);
    }

}

