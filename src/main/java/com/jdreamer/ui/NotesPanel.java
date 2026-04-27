package com.jdreamer.ui;

import com.jdreamer.model.Noun;
import com.jdreamer.model.Verb;
import com.jdreamer.service.BookService;
import com.jdreamer.ui.model.BookOrPageChangeListener;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NotesPanel extends JPanel implements BookOrPageChangeListener {
    private final BookService bookService;

    private JTextArea translationArea;
    private VerbPanel verbPanel;
    private NounPanel nounPanel;

    public NotesPanel(BookService bookService) {
        super(new BorderLayout());

        this.bookService = bookService;

        buildUI();
    }

    private void buildUI() {
        translationArea = new JTextArea(8, 40);
        translationArea.setLineWrap(true);
        translationArea.setWrapStyleWord(true);
        JScrollPane translationScroll = new JScrollPane(translationArea);
        translationScroll.setBorder(BorderFactory.createTitledBorder("Translation (Editable)"));

        JPanel tablesPanel = new JPanel(new GridLayout(2, 1, 8, 8));

        nounPanel = new NounPanel(bookService);
        verbPanel = new VerbPanel(bookService);
        tablesPanel.add(verbPanel);
        tablesPanel.add(nounPanel);

        JPanel topLeft = new JPanel(new BorderLayout(4, 4));
        topLeft.add(translationScroll, BorderLayout.NORTH);
        topLeft.add(tablesPanel, BorderLayout.CENTER);

        add(topLeft, BorderLayout.CENTER);
    }

    public void onBookOrPageChange(int bookId, int pageId) {
        verbPanel.saveData();
        nounPanel.saveData();

        List<Noun> nouns = bookService.findNounsByBookIdAndPageId(bookId, pageId);
        nounPanel.loadData(nouns, bookId, pageId);

        List<Verb> verbs = bookService.findVerbsByBookIdAndPageId(bookId, pageId);
        verbPanel.loadData(verbs, bookId, pageId);
    }
}
