package com.jdreamer.ui;

import com.jdreamer.model.Verb;
import com.jdreamer.service.BookService;
import com.jdreamer.ui.model.VerbTableModel;
import com.jdreamer.ui.util.JTableUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class VerbPanel extends JPanel {
    private BookService bookService;
    private VerbTableModel verbsModel;

    private int bookId = -1;
    private int pageId = -1;

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

        final CustomCellRenderer renderer = new CustomCellRenderer(Set.of(1, 3, 4, 5, 6, 7, 8));
        for (int columnIndex = 0; columnIndex < verbsTable.getColumnCount(); columnIndex++) {
            verbsTable.getColumnModel().getColumn(columnIndex).setCellRenderer(renderer);
        }
        verbsTable.setRowHeight(28);
        verbsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        add(new JScrollPane(verbsTable), BorderLayout.CENTER);

        setBorder(BorderFactory.createTitledBorder("Verbs"));

        // Add key binding for Ctrl+S
        JTableUtil.addKeyBinding(verbsTable, "saveAction", KeyStroke.getKeyStroke("control S"), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = verbsTable.getSelectedRow();
                if (selectedRow != -1) {
                    Verb verb = verbsModel.getVerbs().get(selectedRow);
                    if (verb != null && !verb.getWord().isBlank() && !verb.getWord().isEmpty()) {
                        bookService.save(verb);
                    }

                    if (selectedRow == verbsModel.getRowCount()-1) {
                        verbsModel.addEmptyRowAtEnd(bookId, pageId);
                    }
                }
            }
        });
    }

    public void changePage(int bookId, int pageId) {
        this.bookId = bookId;
        this.pageId = pageId;

        List<Verb> nouns = bookService.findVerbsByBookIdAndPageId(bookId, pageId);
        verbsModel.updateData(nouns);

        verbsModel.addEmptyRowAtEnd(bookId, pageId);
    }

    public void saveData() {
        List<Verb> verbs = verbsModel.getVerbs();

        bookService.saveVerbs(verbs);
    }
}
