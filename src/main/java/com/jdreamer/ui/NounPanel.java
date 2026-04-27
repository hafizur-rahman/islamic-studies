package com.jdreamer.ui;

import com.jdreamer.model.Noun;
import com.jdreamer.service.BookService;
import com.jdreamer.ui.model.NounTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class NounPanel extends JPanel {
    private BookService bookService;
    private NounTableModel nounsModel;

    public NounPanel(BookService bookService) {
        super(new BorderLayout());

        this.bookService = bookService;

        buildUI();
    }

    private void buildUI() {
        nounsModel = new NounTableModel(Collections.emptyList(), bookService);

        JTable nounsTable = new JTable(nounsModel);
        nounsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        nounsTable.setRowHeight(24);
        nounsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        add(new JScrollPane(nounsTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton addNounBtn = new JButton("Add Noun");
        //addNounBtn.addActionListener(e -> nounsModel.addRow(new Object[]{"", "", "", "", 0, 0}));
        buttonPanel.add(addNounBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        setBorder(BorderFactory.createTitledBorder("Nouns"));
    }

    public void loadData(List<Noun> nouns, int bookId, int pageId) {
        nounsModel.updateData(nouns);

        nounsModel.addEmptyRowAtEnd(bookId, pageId);
    }

    public void saveData() {
        List<Noun> nouns = nounsModel.getNouns();

        bookService.saveAll(nouns);
    }
}
