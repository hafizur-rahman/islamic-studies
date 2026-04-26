package com.jdreamer.ui;

import com.jdreamer.model.Book;
import com.jdreamer.service.BookService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookPanel extends JPanel {
    private int currentBookId = -1;
    private JTabbedPane booksTabbedPane;
    private JLabel pdfImageLabel;
    private int currentPage = 0;
    private PDDocument currentDocument;
    private Map<Integer, PDDocument> loadedPdfs = new HashMap<>();
    private Map<Integer, JLabel> pdfLabels = new HashMap<>();
    private Map<Integer, Integer> currentPages = new HashMap<>();
    private Map<Integer, Float> zoomFactors = new HashMap<>();

    private BookService bookService;

    public BookPanel(BookService bookService) {
        super(new BorderLayout());

        this.bookService = bookService;

        buildUI();

        setupDragAndDrop();
    }

    private void buildUI() {
        JPanel toolbar = getToolbar();

        add(toolbar, BorderLayout.NORTH);

        createTabbedPane();

        add(booksTabbedPane, BorderLayout.CENTER);
    }

    private void createTabbedPane() {
        booksTabbedPane = new JTabbedPane();
        booksTabbedPane.addChangeListener(e -> {
            int selectedIndex = booksTabbedPane.getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < booksTabbedPane.getTabCount()) {
                String tabTitle = booksTabbedPane.getTitleAt(selectedIndex);
                // Extract book ID from tab title (format: "Title (ID: bookId)")
                int idStartPos = tabTitle.lastIndexOf("ID: ");
                if (idStartPos != -1) {
                    String idStr = tabTitle.substring(idStartPos + 4, tabTitle.length() - 1);
                    try {
                        currentBookId = Integer.parseInt(idStr);
                        switchToBook(currentBookId);
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private JPanel getToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton loadPdfBtn = new JButton("Load PDF");
        loadPdfBtn.addActionListener(e -> loadPdfFile());
        toolbar.add(loadPdfBtn);

        JButton prevPageBtn = new JButton("Previous");
        JButton nextPageBtn = new JButton("Next");
        JButton zoomInBtn = new JButton("Zoom In");
        JButton zoomOutBtn = new JButton("Zoom Out");
        JButton resetZoomBtn = new JButton("Reset Zoom");
        JButton closeBtn = new JButton("Close");

        prevPageBtn.addActionListener(e -> showPageForBook(currentBookId, currentPages.getOrDefault(currentBookId, 0) - 1));
        nextPageBtn.addActionListener(e -> showPageForBook(currentBookId, currentPages.getOrDefault(currentBookId, 0) + 1));
        zoomInBtn.addActionListener(e -> zoomInForBook(currentBookId));
        zoomOutBtn.addActionListener(e -> zoomOutForBook(currentBookId));
        resetZoomBtn.addActionListener(e -> resetZoomForBook(currentBookId));
        closeBtn.addActionListener(e -> closeBook(currentBookId));

        toolbar.add(prevPageBtn);
        toolbar.add(nextPageBtn);
        toolbar.add(zoomInBtn);
        toolbar.add(zoomOutBtn);
        toolbar.add(resetZoomBtn);
        toolbar.add(closeBtn);

        return toolbar;
    }

    private void loadPdfFile() {
        JFileChooser chooser = new JFileChooser();

        chooser.setDialogTitle("Select a PDF file");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Documents", "pdf"));
        chooser.setAcceptAllFileFilterUsed(false);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            openPDF(file);
        }
    }

    private void openPDF(File file) {
        try {
            currentDocument = Loader.loadPDF(file);

            // Get or create book record
            String filePath = file.getAbsolutePath();
            String fileName = file.getName();
            //currentBookId = getOrCreateBookId(filePath, fileName);

            // Store the PDF document
            loadedPdfs.put(currentBookId, currentDocument);

            // Get or create tab for this book
            createOrUpdateBookTab(currentBookId, fileName);

            // Initialize book-specific data
            int lastPage = 1; //getLastPageFromSession(currentBookId);
            currentPage = Math.min(lastPage, currentDocument.getNumberOfPages() - 1);
            currentPages.put(currentBookId, currentPage);
            zoomFactors.put(currentBookId, 1.0f);

            // Switch to the book
            switchToBook(currentBookId);
        } catch (IOException e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(this, "Failed to open PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupDragAndDrop() {
        new DropTarget(this, new DropTargetAdapter() {
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    java.util.List<File> files = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File f : files) {
                        if (f.getName().endsWith(".pdf")) openPDF(f);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showPageForBook(int bookId, int pageIndex) {
        if (!loadedPdfs.containsKey(bookId)) {
            return;
        }

        PDDocument doc = loadedPdfs.get(bookId);
        JLabel label = pdfLabels.get(bookId);

        if (doc == null || label == null) {
            return;
        }

        int pageCount = doc.getNumberOfPages();
        if (pageIndex < 0 || pageIndex >= pageCount) {
            return;
        }

        try {
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage img = renderer.renderImageWithDPI(pageIndex, 120);

            float bookZoom = zoomFactors.getOrDefault(bookId, 1.0f);
            if (bookZoom != 1.0f) {
                int newWidth = (int) (img.getWidth() * bookZoom);
                int newHeight = (int) (img.getHeight() * bookZoom);
                BufferedImage scaledImg = new BufferedImage(newWidth, newHeight, img.getType());
                Graphics2D g2d = scaledImg.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(img, 0, 0, newWidth, newHeight, null);
                g2d.dispose();
                img = scaledImg;
            }
            label.setIcon(new ImageIcon(img));
            label.setText(null);

            currentPages.put(bookId, pageIndex);
            // Save current page to session
//            saveSessionPage(bookId, pageIndex);
            loadDataByPage(pageIndex);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to render PDF page: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void switchToBook(int bookId) {
        if (!loadedPdfs.containsKey(bookId)) {
            return;
        }

        currentBookId = bookId;
        currentDocument = loadedPdfs.get(bookId);
        currentPage = currentPages.getOrDefault(bookId, 0);

        // Update tab selection
        for (int i = 0; i < booksTabbedPane.getTabCount(); i++) {
            String tabTitle = booksTabbedPane.getTitleAt(i);
            int idStartPos = tabTitle.lastIndexOf("ID: ");
            if (idStartPos != -1) {
                String idStr = tabTitle.substring(idStartPos + 4, tabTitle.length() - 1);
                try {
                    int tabBookId = Integer.parseInt(idStr);
                    if (tabBookId == bookId) {
                        booksTabbedPane.setSelectedIndex(i);
                        showPageForBook(bookId, currentPage);
                        break;
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void createOrUpdateBookTab(int bookId, String fileName) {
        Book bookById = bookService.findBookById(bookId);

        if (bookById != null) {

            String title = bookById.getTitle();
            String tabTitle = title + " (ID: " + bookId + ")";

            // Create panel for this book if not exists
            if (!pdfLabels.containsKey(bookId)) {
                JLabel pdfLabel = new JLabel("Loading...", SwingConstants.CENTER);
                pdfLabel.setVerticalAlignment(SwingConstants.CENTER);
                pdfLabel.setHorizontalAlignment(SwingConstants.CENTER);
                pdfLabels.put(bookId, pdfLabel);

                JScrollPane pdfScroll = new JScrollPane(pdfLabel);

                JPanel bookPanel = new JPanel(new BorderLayout());

                bookPanel.add(pdfScroll, BorderLayout.CENTER);

                booksTabbedPane.addTab(tabTitle, bookPanel);
            }
        }

    }

    private void zoomInForBook(int bookId) {
        float zoom = zoomFactors.getOrDefault(bookId, 1.0f);
        zoom *= 1.2f;
        if (zoom > 5.0f) zoom = 5.0f;
        zoomFactors.put(bookId, zoom);
        showPageForBook(bookId, currentPages.getOrDefault(bookId, 0));
    }

    private void zoomOutForBook(int bookId) {
        float zoom = zoomFactors.getOrDefault(bookId, 1.0f);
        zoom /= 1.2f;
        if (zoom < 0.1f) zoom = 0.1f;
        zoomFactors.put(bookId, zoom);
        showPageForBook(bookId, currentPages.getOrDefault(bookId, 0));
    }

    private void resetZoomForBook(int bookId) {
        zoomFactors.put(bookId, 1.0f);
        showPageForBook(bookId, currentPages.getOrDefault(bookId, 0));
    }

    private void closeBook(int bookId) {
        if (loadedPdfs.containsKey(bookId)) {
            PDDocument doc = loadedPdfs.get(bookId);
            try {
                doc.close();
            } catch (IOException ignore) {
            }

            loadedPdfs.remove(bookId);
            pdfLabels.remove(bookId);
            currentPages.remove(bookId);
            zoomFactors.remove(bookId);

            // Find and remove the tab
            for (int i = 0; i < booksTabbedPane.getTabCount(); i++) {
                String tabTitle = booksTabbedPane.getTitleAt(i);
                int idStartPos = tabTitle.lastIndexOf("ID: ");
                if (idStartPos != -1) {
                    String idStr = tabTitle.substring(idStartPos + 4, tabTitle.length() - 1);
                    try {
                        int tabBookId = Integer.parseInt(idStr);
                        if (tabBookId == bookId) {
                            booksTabbedPane.removeTabAt(i);
                            break;
                        }
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            // If this was the current book, switch to another one
            if (bookId == currentBookId) {
                if (booksTabbedPane.getTabCount() > 0) {
                    booksTabbedPane.setSelectedIndex(0);
                } else {
                    currentBookId = -1;
                    currentDocument = null;
                }
            }
        }
    }

    private void loadDataByPage(int page) {

    }
}
