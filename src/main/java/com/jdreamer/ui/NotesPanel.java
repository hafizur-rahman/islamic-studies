package com.jdreamer.ui;

import com.jdreamer.model.Noun;
import com.jdreamer.service.BookService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NotesPanel extends JPanel {
    private final BookService bookService;
    private JTextArea translationArea;
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

        nounPanel = new NounPanel();

        tablesPanel.add(new VerbPanel());
        tablesPanel.add(nounPanel);
        tablesPanel.setBorder(BorderFactory.createTitledBorder("Language Data (Optional Editable)"));

        JButton saveDataBtn = new JButton("Save Data to DB");
        //saveDataBtn.addActionListener(e -> saveDataToDB());

        JPanel topLeft = new JPanel(new BorderLayout(4, 4));
        topLeft.add(translationScroll, BorderLayout.NORTH);
        topLeft.add(tablesPanel, BorderLayout.CENTER);
        topLeft.add(saveDataBtn, BorderLayout.SOUTH);

        add(topLeft, BorderLayout.CENTER);
    }

    public void onBookOrPageChange(int bookId, int pageId) {
        List<Noun> nouns = this.bookService.findNounsByBookIdAndPageId(bookId, pageId);
        nounPanel.loadData(nouns);
    }
}
