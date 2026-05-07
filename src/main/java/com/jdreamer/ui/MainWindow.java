package com.jdreamer.ui;

import com.jdreamer.service.BookService;
import com.jdreamer.ui.model.BookOrPageChangeListener;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class MainWindow extends JFrame {
    private BookService bookService;

    public MainWindow(BookService bookService) {
        super("Islamic Studies");

        this.bookService = bookService;

        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        MediaPanel mediaPanel = new MediaPanel(bookService);
        tabbedPane.add("Video", mediaPanel);

        NotesPanel notesPanel = new NotesPanel(bookService);
        tabbedPane.add("Notes", notesPanel);

        ArrayList<BookOrPageChangeListener> listeners = new ArrayList<>();
        listeners.add(mediaPanel);
        listeners.add(notesPanel);

        BookPanel bookPanel = new BookPanel(bookService, listeners);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, bookPanel, tabbedPane);
        splitPane.setDividerLocation(1000);

        add(splitPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 850);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}

