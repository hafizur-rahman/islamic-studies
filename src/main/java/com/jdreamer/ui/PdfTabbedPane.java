package com.jdreamer.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class PdfTabbedPane extends JTabbedPane {
    private final int sideId;
    private PdfTabbedPane peer;

    private final List<String> openedFiles = new ArrayList<>();

    public PdfTabbedPane(int sideId) {
        this.sideId = sideId;

        initContextMenu();
    }

    public void setPeer(PdfTabbedPane peer) {
        this.peer = peer;
    }

    private void initContextMenu() {
        final JPopupMenu popup = new JPopupMenu();

        JMenuItem moveItem = new JMenuItem("Move to other side");
        moveItem.addActionListener(e -> {
            if (peer != null) moveSelectedTab();
        });
        popup.add(moveItem);

        // mouse listener on the tabbed pane itself
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) showPopupIfOnTab(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {  // some OS fire it here
                if (e.isPopupTrigger()) showPopupIfOnTab(e);
            }

            private void showPopupIfOnTab(MouseEvent e) {
                int tabIndex = indexAtLocation(e.getX(), e.getY());
                if (tabIndex != -1) {
                    setSelectedIndex(tabIndex);      // make the clicked tab the active one
                    popup.show(PdfTabbedPane.this, e.getX(), e.getY());
                }
            }
        });
    }

    private void moveSelectedTab() {
        int idx = getSelectedIndex();
        if (idx == -1) return;                 // nothing selected

        Component c = getComponentAt(idx);

        if (c instanceof PdfViewerPanel) {
            PdfViewerPanel viewerPanel = (PdfViewerPanel) c;
            String title = getTitleAt(idx);

            remove(idx);
            notifyBookClosing(title);
            peer.addPdfFile(title, viewerPanel); // add to the opposite side
        }
    }

    public void addPdfFile(String title, PdfViewerPanel panel) {
        if (!openedFiles.contains(panel.getBookId())) {
            openedFiles.add(title);
            panel.setSideId(sideId);

            addTab(title, panel);
            setSelectedComponent(panel);   // make it visible immediately
        }
    }

    public boolean isBookOpen(String title) {
        return openedFiles.contains(title);
    }

    public void notifyBookClosing(String title) {
        if (openedFiles.contains(title)) {
            openedFiles.remove(title);
        }
    }

    public void selectBook(int bookId) {
        for (int i = 0; i < getTabCount(); i++) {
            String tabTitle = getTitleAt(i);
            int idStartPos = tabTitle.lastIndexOf("ID: ");

            if (idStartPos != -1) {
                String idStr = tabTitle.substring(idStartPos + 4, tabTitle.length() - 1);
                try {
                    int tabBookId = Integer.parseInt(idStr);
                    if (tabBookId == bookId) {
                        setSelectedIndex(i);
                        break;
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
