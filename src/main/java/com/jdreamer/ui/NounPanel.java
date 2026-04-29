package com.jdreamer.ui;

import com.jdreamer.model.Noun;
import com.jdreamer.model.Verb;
import com.jdreamer.service.BookService;
import com.jdreamer.ui.model.NounTableModel;
import com.jdreamer.ui.util.JTableUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class NounPanel extends JPanel {
    private BookService bookService;
    private NounTableModel nounsModel;

    private int bookId = -1;
    private int pageId = -1;

    public NounPanel(BookService bookService) {
        super(new BorderLayout());

        this.bookService = bookService;

        buildUI();
    }

    private void buildUI() {
        nounsModel = new NounTableModel(Collections.emptyList());

        JTable nounsTable = new JTable(nounsModel);
        for (int columnId: new int[]{0, 5, 6}) {
            nounsTable.getColumnModel().getColumn(columnId).setMinWidth(0);
            nounsTable.getColumnModel().getColumn(columnId).setMaxWidth(0);
        }

        final CustomCellRenderer renderer = new CustomCellRenderer(Set.of(2,3,4));
        for (int columnIndex = 0; columnIndex < nounsTable.getColumnCount(); columnIndex++) {
            nounsTable.getColumnModel().getColumn(columnIndex).setCellRenderer(renderer);
        }

        nounsTable.setRowHeight(28);
        nounsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));

        add(new JScrollPane(nounsTable), BorderLayout.CENTER);

        setBorder(BorderFactory.createTitledBorder("Nouns"));

        // Add key binding for Ctrl+S
        JTableUtil.addKeyBinding(nounsTable, "saveAction", KeyStroke.getKeyStroke("control S"), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = nounsTable.getSelectedRow();
                if (selectedRow != -1) {
                    List<Noun> nouns = nounsModel.getNouns();
                    Noun noun = nounsModel.getNouns().get(selectedRow);
                    if (noun != null && !noun.getSingular().isBlank() && !noun.getSingular().isEmpty()) {
                        bookService.save(noun);
                    }

                    if (selectedRow == nounsModel.getRowCount()-1) {
                        nounsModel.addEmptyRowAtEnd(bookId, pageId);
                    }
                }
            }
        });
    }

    public void changePage(int bookId, int pageId) {
        this.bookId = bookId;
        this.pageId = pageId;

        List<Noun> nouns = bookService.findNounsByBookIdAndPageId(bookId, pageId);
        nounsModel.updateData(nouns);

        nounsModel.addEmptyRowAtEnd(bookId, pageId);
    }

    public void saveData() {
        List<Noun> nouns = nounsModel.getNouns();

        bookService.saveNouns(nouns);
    }
}
