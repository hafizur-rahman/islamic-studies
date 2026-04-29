package com.jdreamer.ui;

import com.jdreamer.model.Noun;
import com.jdreamer.model.Verb;
import com.jdreamer.service.BookService;
import com.jdreamer.ui.model.VerbTableModel;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class VerbPanel extends JPanel {
    private BookService bookService;
    private VerbTableModel verbsModel;

    public VerbPanel(BookService bookService) {
        super(new BorderLayout());

        this.bookService = bookService;

        buildUI();
    }

    private void buildUI() {
        verbsModel = new VerbTableModel(Collections.emptyList());

        JTable verbsTable = new JTable(verbsModel);
        for (int columnId: new int[]{0, 8, 9}) {
            verbsTable.getColumnModel().getColumn(columnId).setMinWidth(0);
            verbsTable.getColumnModel().getColumn(columnId).setMaxWidth(0);
        }

        final CustomCellRenderer renderer = new CustomCellRenderer(Set.of(2,3,4,5, 6, 7));
        for (int columnIndex = 0; columnIndex < verbsTable.getColumnCount(); columnIndex++) {
            verbsTable.getColumnModel().getColumn(columnIndex).setCellRenderer(renderer);
        }
        verbsTable.setRowHeight(28);
        verbsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        add(new JScrollPane(verbsTable), BorderLayout.CENTER);

        setBorder(BorderFactory.createTitledBorder("Verbs"));
    }

    public void loadData(List<Verb> verbs, int bookId, int pageId) {
        verbsModel.updateData(verbs);

        verbsModel.addEmptyRowAtEnd(bookId, pageId);
    }

    public void saveData() {
        List<Verb> verbs = verbsModel.getVerbs();

        bookService.saveVerbs(verbs);
    }
}
