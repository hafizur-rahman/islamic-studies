package com.jdreamer.ui;

import com.jdreamer.service.BookService;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private BookService bookService;

    public MainWindow(BookService bookService) {
        super("Islamic Studies");

        this.bookService = bookService;

        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        NotesPanel notesPanel = new NotesPanel(bookService);
        BookPanel bookPanel = new BookPanel(bookService, notesPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, bookPanel, notesPanel);
        splitPane.setDividerLocation(1200);

        add(splitPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 850);
        setLocationRelativeTo(null);
        setVisible(true);
    }

}

