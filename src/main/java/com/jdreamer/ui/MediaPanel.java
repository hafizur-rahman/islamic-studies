package com.jdreamer.ui;

import com.jdreamer.service.BookService;
import com.jdreamer.ui.model.BookOrPageChangeListener;

import javax.swing.*;
import java.awt.*;

public class MediaPanel extends JPanel implements BookOrPageChangeListener  {
    private final BookService bookService;

    private MediaLinkPanel mediaLinkPanel;
    private BrowserPanel browserPanel;

    public MediaPanel(BookService bookService) {
        super(new BorderLayout());

        this.bookService = bookService;

        buildUI();
    }

    private void buildUI() {
        browserPanel = new BrowserPanel();
        mediaLinkPanel = new MediaLinkPanel(bookService, browserPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, browserPanel, mediaLinkPanel);
        splitPane.setDividerLocation(650);

        add(splitPane, BorderLayout.CENTER);
    }

    @Override
    public void onBookOrPageChange(int bookId, int pageId) {
        mediaLinkPanel.saveData();

        mediaLinkPanel.changePage(bookId, pageId);
    }
}
