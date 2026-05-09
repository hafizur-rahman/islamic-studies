package com.jdreamer.ui;

import com.jdreamer.service.BookService;
import com.jdreamer.ui.model.BookOrPageChangeListener;

import javax.swing.*;
import java.awt.*;

public class MediaPanel extends JPanel implements BookOrPageChangeListener  {
    private final BookService bookService;

    private MediaLinkPanel mediaLinkPanel;

    public MediaPanel(BookService bookService) {
        super(new BorderLayout());

        this.bookService = bookService;

        buildUI();
    }

    private void buildUI() {
        VideoPlayer videoPlayer = new VideoPlayer();

        mediaLinkPanel = new MediaLinkPanel(bookService, videoPlayer);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, videoPlayer, mediaLinkPanel);
        splitPane.setDividerLocation(650);

        add(splitPane, BorderLayout.CENTER);
    }

    @Override
    public void onBookOrPageChange(int bookId, int pageId) {
        mediaLinkPanel.saveData();

        mediaLinkPanel.changePage(bookId, pageId);
    }
}
