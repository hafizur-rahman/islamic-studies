package com.jdreamer.ui;

import com.jdreamer.model.Noun;
import com.jdreamer.service.BookService;
import com.jdreamer.ui.model.NounTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class NounPanel extends JPanel {
    private BookService bookService;
    private NounTableModel nounsModel;

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

        nounsTable.setRowHeight(24);
        nounsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));

        add(new JScrollPane(nounsTable), BorderLayout.CENTER);

        setBorder(BorderFactory.createTitledBorder("Nouns"));
    }

    public void loadData(List<Noun> nouns, int bookId, int pageId) {
        nounsModel.updateData(nouns);

        nounsModel.addEmptyRowAtEnd(bookId, pageId);
    }

    public void saveData() {
        List<Noun> nouns = nounsModel.getNouns();

        bookService.saveNouns(nouns);
    }
}
