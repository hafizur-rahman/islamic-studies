package com.jdreamer.ui;

import com.jdreamer.model.MediaLink;
import com.jdreamer.service.BookService;
import com.jdreamer.ui.model.MediaLinkTableModel;
import com.jdreamer.ui.util.JTableUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MediaLinkPanel extends JPanel {
    private final BookService bookService;
    private MediaLinkTableModel linkTableModel;

    private VideoPlayer videoPlayer;

    private int bookId = -1;
    private int pageId = -1;

    public MediaLinkPanel(BookService bookService, VideoPlayer browserPanel) {
        super(new BorderLayout());

        this.bookService = bookService;
        this.videoPlayer = browserPanel;

        buildUI();
    }

    private void buildUI() {
        linkTableModel = new MediaLinkTableModel(Collections.emptyList());

        JTable mediaLinkTable = new JTable(linkTableModel);
        for (int columnId: new int[]{0, 1, 2}) {
            mediaLinkTable.getColumnModel().getColumn(columnId).setMinWidth(0);
            mediaLinkTable.getColumnModel().getColumn(columnId).setMaxWidth(0);
        }

        mediaLinkTable.setRowHeight(36);
        mediaLinkTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        add(new JScrollPane(mediaLinkTable), BorderLayout.CENTER);

        // Add key binding for Ctrl+S
        JTableUtil.addKeyBinding(mediaLinkTable, "saveAction", KeyStroke.getKeyStroke("control S"), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = mediaLinkTable.getSelectedRow();
                if (selectedRow != -1) {
                    MediaLink mediaLink = linkTableModel.getMediaLinks().get(selectedRow);
                    if (mediaLink != null && !mediaLink.getUrl().isBlank()) {
                        bookService.save(mediaLink);
                    }

                    if (selectedRow == linkTableModel.getRowCount()-1) {
                        linkTableModel.addEmptyRowAtEnd(bookId, pageId);
                    }
                }
            }
        });

        JTableUtil.addKeyBinding(mediaLinkTable, "playVideo", KeyStroke.getKeyStroke("control P"), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = mediaLinkTable.getSelectedRow();
                if (selectedRow != -1) {
                    MediaLink mediaLink = linkTableModel.getMediaLinks().get(selectedRow);
                    if (mediaLink != null && !mediaLink.getUrl().isBlank()) {
                        videoPlayer.showVideo(mediaLink.getUrl());
                    }
                }
            }
        });
    }

    public void changePage(int bookId, int pageId) {
        this.bookId = bookId;
        this.pageId = pageId;

        List<MediaLink> mediaLinks = bookService.findMediaLinksByBookIdAndPageId(bookId, pageId);
        linkTableModel.updateData(mediaLinks);

        linkTableModel.addEmptyRowAtEnd(bookId, pageId);
    }

    public void saveData() {
        List<MediaLink> mediaLinks = linkTableModel.getMediaLinks().stream()
                .filter(mediaLink -> !StringUtils.isBlank(mediaLink.getUrl()))
                .collect(Collectors.toList());

        bookService.saveMediaLinks(mediaLinks);
    }
}
