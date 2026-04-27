package com.jdreamer.ui;

import com.jdreamer.model.Noun;
import com.jdreamer.service.BookService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class NounPanel extends JPanel {
    private JTable nounsTable;
    private DefaultTableModel nounsModel;
    private BookService bookService;

    public NounPanel(BookService bookService) {
        super(new BorderLayout());

        this.bookService = bookService;

        buildUI();
    }

    public void buildUI() {
        String[] columns = new String[]{"English", "Plural", "Dual", "Singular", "BookID", "PageID"};
        nounsModel = new DefaultTableModel(columns, 0);
        nounsTable = new EditableTable(columns, new Object[][]{}, null);
        nounsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        nounsTable.setRowHeight(24);
        nounsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        add(new JScrollPane(nounsTable), BorderLayout.CENTER);

        JButton addNounBtn = new JButton("Add Noun");
        //addNounBtn.addActionListener(e -> nounsModel.addRow(new Object[]{"", "", "", "", 0, 0}));

        add(addNounBtn, BorderLayout.SOUTH);
    }

    public void loadData(int bookId, int pageId) {
        List<Noun> nouns = this.bookService.findNounsByBookIdAndPageId(bookId, pageId);
    }
}
