package com.jdreamer.ui;

import com.jdreamer.model.Book;
import com.jdreamer.model.UserSession;
import com.jdreamer.service.BookService;
import com.jdreamer.ui.model.BookOrPageChangeListener;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class BookPanel extends JPanel {
    private int currentBookId = -1;

    private JButton showHideButton = new JButton("Show Library");
    private JScrollPane fileTreePane;

    private JTabbedPane booksTabbedPane;
    private final JTextField pageIdField = new JTextField();
    private final JTextField pageCountField = new JTextField();


    private int currentPage = 0;
    private PDDocument currentDocument;
    private Map<Integer, PDDocument> loadedPdfs = new HashMap<>();
    private Map<Integer, JLabel> pdfLabels = new HashMap<>();

    private Map<Integer, UserSession> userSessions = new HashMap<>();

    private final BookService bookService;
    private final List<BookOrPageChangeListener> listeners;

    public BookPanel(BookService bookService, List<BookOrPageChangeListener> listeners) {
        super(new BorderLayout());

        this.bookService = bookService;
        this.listeners = listeners;

        buildUI();

        setupDragAndDrop();

        loadBooksAtStartup();
    }

    private void buildUI() {
        FileSystemTree tree = creteFileSystemTree();

        fileTreePane = new JScrollPane(tree);
        fileTreePane.setVisible(false);

        showHideButton.addActionListener(e -> {
            fileTreePane.setVisible(!fileTreePane.isVisible());

            showHideButton.setText(fileTreePane.isVisible() ? "Hide Library" : "Show Library");

            this.revalidate();
            this.repaint();
        });

        add(fileTreePane, BorderLayout.WEST);

        JPanel pdfView = new JPanel(new BorderLayout());
        pdfView.add(getToolbar(), BorderLayout.NORTH);

        createTabbedPane();

        pdfView.add(booksTabbedPane, BorderLayout.CENTER);

        add(pdfView, BorderLayout.CENTER);
    }

    private FileSystemTree creteFileSystemTree() {
        // Choose a root folder – here the user’s home directory
        File rootDir = new File("C:\\Users\\bibag\\work\\islamic-studies\\Library");
        FileSystemTree tree = new FileSystemTree(rootDir);

        // Optional: double‑click opens the file
        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {

                if (e.getClickCount() == 2 && isFileSelected(tree)) {
                    File f = ((FileTreeNode) tree.getSelectionPath().getLastPathComponent()).getFile();

                    if (f != null) {
                        try {
                           openPDF(f);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(BookPanel.this,
                                    "Could not open file:\n" + ex.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        return tree;
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

        JButton prevPageBtn = new JButton("Previous");

        pageIdField.setColumns(3);
        pageCountField.setEditable(false);


        JButton nextPageBtn = new JButton("Next");
        JButton zoomInBtn = new JButton("Zoom In");
        JButton zoomOutBtn = new JButton("Zoom Out");
        JButton resetZoomBtn = new JButton("Reset Zoom");
        JButton closeBtn = new JButton("Close");

        prevPageBtn.addActionListener(e -> {
            UserSession session = getOrCreateUserSession(currentBookId);
            if (session != null) {
                showPageForBook(currentBookId, session.getPageId() - 1);
            }
        });

        nextPageBtn.addActionListener(e -> {
            UserSession session = getOrCreateUserSession(currentBookId);
            if (session != null) {
                showPageForBook(currentBookId, session.getPageId() + 1);
            }
        });
        pageIdField.addActionListener(e -> {
            showPageForBook(currentBookId, Integer.parseInt(pageIdField.getText()));
        });

        zoomInBtn.addActionListener(e -> zoomInForBook(currentBookId));
        zoomOutBtn.addActionListener(e -> zoomOutForBook(currentBookId));
        resetZoomBtn.addActionListener(e -> resetZoomForBook(currentBookId));
        closeBtn.addActionListener(e -> closeBook(currentBookId));

        toolbar.add(showHideButton);
        toolbar.add(zoomInBtn);
        toolbar.add(zoomOutBtn);
        toolbar.add(resetZoomBtn);
        toolbar.add(closeBtn);

        toolbar.add(prevPageBtn);
        toolbar.add(pageIdField);
        toolbar.add(pageCountField);
        toolbar.add(nextPageBtn);

        return toolbar;
    }

    private void loadBooksAtStartup() {
        List<UserSession> userSessions = bookService.findUserSessionsByOpenAtStartup();
        List<Integer> bookIds = userSessions.stream().map(UserSession::getBookId).toList();

        List<Book> books = bookService.findAllBooksByBookIds(bookIds);

        for (Book book: books) {
            String filePath = book.getFilePath();
            File file = new File(filePath);

            openPDF(file);
        }
    }

    private void openPDF(File file) {
        try {
            currentDocument = Loader.loadPDF(file);

            // Get or create book record
            String filePath = file.getAbsolutePath();
            String fileName = file.getName();

            currentBookId = getOrCreateBookId(filePath, fileName);

            // Store the PDF document
            loadedPdfs.put(currentBookId, currentDocument);

            // Get or create tab for this book
            createOrUpdateBookTab(currentBookId, fileName);

            // Initialize book-specific data
            UserSession session = getOrCreateUserSession(currentBookId);
            session.setOpenAtStartup(true);

            bookService.saveSession(session);

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

    private boolean isFileSelected(JTree tree) {
        TreePath path = tree.getSelectionPath();
        if (path == null) return false;

        Object node = path.getLastPathComponent();
        if (!(node instanceof FileTreeNode)) return false;

        return ((FileTreeNode) node).getFile().isFile();
    }

    private int getOrCreateBookId(String filePath, String title) {
        Book book = bookService.findBookByFilePath(filePath);

        if (book == null) {
            book = new Book();
            book.setFilePath(filePath);
            book.setTitle(title);
            book.setLastAccessed(System.currentTimeMillis());

            bookService.save(book);
        }

        return book.getId();
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
        if (pageIndex < 0) {
            pageIndex = 0;
        } else if (pageIndex >= pageCount) {
            pageIndex = Math.min(pageIndex, pageCount - 1);
        }

        pageIdField.setText(String.valueOf(pageIndex));
        pageCountField.setText("/ " + (pageCount - 1));

        try {
            UserSession session = getOrCreateUserSession(bookId);

            float bookZoom = session.getZoomFactor();

            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage img = renderer.renderImageWithDPI(pageIndex, 95 * bookZoom);

            label.setIcon(new ImageIcon(img));
            label.setText(null);

            session.setPageId(pageIndex);
            session.setPageCount(pageCount);

            // Save current page to session
            session.setAccessedAt(System.currentTimeMillis());
            session.setOpenAtStartup(true);

            bookService.saveSession(session);

            for (BookOrPageChangeListener listener: listeners) {
                listener.onBookOrPageChange(bookId, pageIndex);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to render PDF page: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private UserSession getOrCreateUserSession(int bookId) {
        if (userSessions.containsKey(bookId)) {
            return userSessions.get(bookId);
        }

        UserSession session = bookService.findUserSessionByBookId(bookId);
        if (session == null) {
            session = new UserSession();
            session.setBookId(bookId);
            session.setAccessedAt(System.currentTimeMillis());
        }

        userSessions.put(bookId, session);

        return session;
    }

    private void switchToBook(int bookId) {
        if (!loadedPdfs.containsKey(bookId)) {
            return;
        }

        UserSession session = getOrCreateUserSession(bookId);

        currentBookId = bookId;
        currentDocument = loadedPdfs.get(bookId);
        currentPage = session != null ? session.getPageId() : 0;

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
        UserSession session = getOrCreateUserSession(bookId);

        if (session != null) {
            float zoom = session.getZoomFactor();
            zoom *= 1.2f;
            if (zoom > 5.0f) zoom = 5.0f;

            session.setZoomFactor(zoom);
            showPageForBook(bookId, session.getPageId());
        }
    }

    private void zoomOutForBook(int bookId) {
        UserSession session = getOrCreateUserSession(bookId);

        if (session != null) {
            float zoom = session.getZoomFactor();

            zoom /= 1.2f;

            if (zoom < 0.1f) zoom = 0.1f;

            session.setZoomFactor(zoom);
            showPageForBook(bookId, session.getPageId());
        }
    }

    private void resetZoomForBook(int bookId) {
        UserSession session = getOrCreateUserSession(bookId);

        if (session != null) {
            float zoom = 1.0f;

            session.setZoomFactor(zoom);
            showPageForBook(bookId, session.getPageId());
        }
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

            UserSession session = userSessions.get(bookId);
            if (session != null){
                session.setOpenAtStartup(false);
                bookService.saveSession(session);

                userSessions.remove(bookId);
            }

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
}
