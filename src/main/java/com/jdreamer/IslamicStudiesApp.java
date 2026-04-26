package com.jdreamer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.Loader;

public class IslamicStudiesApp extends JFrame {
    private static final String DB_URL = "jdbc:derby:islamic-studies;create=true";

    private JTextArea translationArea;
    private JTable verbsTable;
    private JTable nounsTable;
    private DefaultTableModel verbsModel;
    private DefaultTableModel nounsModel;

    private JLabel pdfImageLabel;
    private int currentPage = 0;
    private PDDocument currentDocument;

    private int currentBookId = -1;
    private JTabbedPane booksTabbedPane;
    private Map<Integer, PDDocument> loadedPdfs = new HashMap<>();
    private Map<Integer, JLabel> pdfLabels = new HashMap<>();
    private Map<Integer, Integer> currentPages = new HashMap<>();
    private Map<Integer, Float> zoomFactors = new HashMap<>();

    public IslamicStudiesApp() {
        super("Arabic Study PDF + Database GUI");
        initDatabase();
        buildUI();
        setupDragAndDrop();
        loadDataFromDB();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS translation(id INTEGER PRIMARY KEY, text TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS verbs(id INTEGER PRIMARY KEY, past TEXT, present TEXT, command TEXT, english TEXT, bab TEXT, book_id INTEGER, page_id INTEGER)");
            stmt.execute("CREATE TABLE IF NOT EXISTS nouns(id INTEGER PRIMARY KEY, singular TEXT, dual TEXT, plural TEXT, english TEXT, book_id INTEGER, page_id INTEGER)");
            stmt.execute("CREATE TABLE IF NOT EXISTS books(id INTEGER PRIMARY KEY, file_path TEXT UNIQUE NOT NULL, title TEXT, last_accessed TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS session(id INTEGER PRIMARY KEY, book_id INTEGER UNIQUE NOT NULL, last_page INTEGER, FOREIGN KEY(book_id) REFERENCES books(id))");

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM translation");
            if (rs.next() && rs.getInt(1) == 0) {
                PreparedStatement insert = conn.prepareStatement("INSERT INTO translation(text) VALUES(?)");
                insert.setString(1, "Add translation here...");
                insert.executeUpdate();
            }
            rs = stmt.executeQuery("SELECT COUNT(*) FROM verbs");
            if (rs.next() && rs.getInt(1) == 0) {
                PreparedStatement insert = conn.prepareStatement("INSERT INTO verbs(past, present, command, english, bab, book_id, page_id) VALUES(?, ?, ?, ?, ?, ?, ?)");
                insert.setString(1, "kataba");
                insert.setString(2, "yaktubu");
                insert.setString(3, "uktub");
                insert.setString(4, "to write");
                insert.setString(5, "1");
                insert.setInt(6, 0);
                insert.setInt(7, 0);
                insert.executeUpdate();
            }
            rs = stmt.executeQuery("SELECT COUNT(*) FROM nouns");
            if (rs.next() && rs.getInt(1) == 0) {
                PreparedStatement insert = conn.prepareStatement("INSERT INTO nouns(singular, dual, plural, english, book_id, page_id) VALUES(?, ?, ?, ?, ?, ?)");
                insert.setString(1, "كتاب");
                insert.setString(2, "كتابان");
                insert.setString(3, "كتب");
                insert.setString(4, "book");
                insert.setInt(5, 0);
                insert.setInt(6, 0);
                insert.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB init error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // Left panel (translation and tables)
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        translationArea = new JTextArea(8, 40);
        translationArea.setLineWrap(true);
        translationArea.setWrapStyleWord(true);
        JScrollPane translationScroll = new JScrollPane(translationArea);
        translationScroll.setBorder(BorderFactory.createTitledBorder("Translation (Editable)"));

        verbsModel = new DefaultTableModel(new Object[]{"Bab", "English", "Command", "Present", "Past", "BookID", "PageID"}, 0);
        verbsTable = new JTable(verbsModel);
        verbsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        verbsTable.setRowHeight(24);
        verbsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        nounsModel = new DefaultTableModel(new Object[]{"English", "Plural", "Dual", "Singular", "BookID", "PageID"}, 0);
        nounsTable = new JTable(nounsModel);
        nounsTable.setFont(new Font("Arial", Font.PLAIN, 14));
        nounsTable.setRowHeight(24);
        nounsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JPanel verbsPanel = new JPanel(new BorderLayout());
        verbsPanel.add(new JScrollPane(verbsTable), BorderLayout.CENTER);
        JButton addVerbBtn = new JButton("Add Verb");
        addVerbBtn.addActionListener(e -> verbsModel.addRow(new Object[]{"", "", "", "", "", 0, 0}));
        verbsPanel.add(addVerbBtn, BorderLayout.SOUTH);

        JPanel nounsPanel = new JPanel(new BorderLayout());
        nounsPanel.add(new JScrollPane(nounsTable), BorderLayout.CENTER);
        JButton addNounBtn = new JButton("Add Noun");
        addNounBtn.addActionListener(e -> nounsModel.addRow(new Object[]{"", "", "", "", 0, 0}));
        nounsPanel.add(addNounBtn, BorderLayout.SOUTH);

        JPanel tablesPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        tablesPanel.add(verbsPanel);
        tablesPanel.add(nounsPanel);
        tablesPanel.setBorder(BorderFactory.createTitledBorder("Language Data (Optional Editable)"));

        JButton saveDataBtn = new JButton("Save Data to DB");
        saveDataBtn.addActionListener(e -> saveDataToDB());

        JPanel topLeft = new JPanel(new BorderLayout(4, 4));
        topLeft.add(translationScroll, BorderLayout.NORTH);
        topLeft.add(tablesPanel, BorderLayout.CENTER);
        topLeft.add(saveDataBtn, BorderLayout.SOUTH);

        leftPanel.add(topLeft, BorderLayout.CENTER);

        // Right panel (PDF viewer with tabs)
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Add toolbar with Load PDF button
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

        rightPanel.add(toolbar, BorderLayout.NORTH);

        // Create a tabbed pane for loaded books with PDF viewers
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

        rightPanel.add(booksTabbedPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, rightPanel, leftPanel);
        splitPane.setDividerLocation(700);

        add(splitPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 850);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadDataFromDB() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT text FROM translation LIMIT 1")) {
                if (rs.next()) {
                    translationArea.setText(rs.getString("text"));
                }
            }
            loadDataByPage(0);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB load error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDataByPage(int page) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            verbsModel.setRowCount(0);
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT past, present, command, english, bab, book_id, page_id FROM verbs WHERE page_id = " + page)) {
                while (rs.next()) {
                    verbsModel.addRow(new Object[]{rs.getString("bab"), rs.getString("english"), rs.getString("command"), rs.getString("present"), rs.getString("past"), rs.getInt("book_id"), rs.getInt("page_id")});
                }
            }

            nounsModel.setRowCount(0);
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT singular, dual, plural, english, book_id, page_id FROM nouns WHERE page_id = " + page)) {
                while (rs.next()) {
                    nounsModel.addRow(new Object[]{rs.getString("english"), rs.getString("plural"), rs.getString("dual"), rs.getString("singular"), rs.getInt("book_id"), rs.getInt("page_id")});
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB load error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveDataToDB() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try (PreparedStatement updateTranslation = conn.prepareStatement("UPDATE translation SET text = ? WHERE id = 1")) {
                updateTranslation.setString(1, translationArea.getText());
                int updated = updateTranslation.executeUpdate();
                if (updated == 0) {
                    try (PreparedStatement insertTranslation = conn.prepareStatement("INSERT INTO translation(id, text) VALUES(1, ?)")) {
                        insertTranslation.setString(1, translationArea.getText());
                        insertTranslation.executeUpdate();
                    }
                }
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DELETE FROM verbs");
                stmt.execute("DELETE FROM nouns");
            }

            try (PreparedStatement insertVerbs = conn.prepareStatement("INSERT INTO verbs(past, present, command, english, bab, book_id, page_id) VALUES(?, ?, ?, ?, ?, ?, ?)");
                 PreparedStatement insertNouns = conn.prepareStatement("INSERT INTO nouns(singular, dual, plural, english, book_id, page_id) VALUES(?, ?, ?, ?, ?, ?)")) {
                for (int i = 0; i < verbsModel.getRowCount(); i++) {
                    String bab = (String) verbsModel.getValueAt(i, 0);
                    String english = (String) verbsModel.getValueAt(i, 1);
                    String command = (String) verbsModel.getValueAt(i, 2);
                    String present = (String) verbsModel.getValueAt(i, 3);
                    String past = (String) verbsModel.getValueAt(i, 4);
                    Object book_idObj = verbsModel.getValueAt(i, 5);
                    Object page_idObj = verbsModel.getValueAt(i, 6);
                    int book_id = (book_idObj instanceof Integer) ? (Integer) book_idObj : ((book_idObj != null && !book_idObj.toString().isEmpty()) ? Integer.parseInt(book_idObj.toString()) : 0);
                    int page_id = (page_idObj instanceof Integer) ? (Integer) page_idObj : ((page_idObj != null && !page_idObj.toString().isEmpty()) ? Integer.parseInt(page_idObj.toString()) : 0);
                    if (bab != null && !bab.trim().isEmpty()) {
                        insertVerbs.setString(1, past);
                        insertVerbs.setString(2, present != null ? present : "");
                        insertVerbs.setString(3, command != null ? command : "");
                        insertVerbs.setString(4, english != null ? english : "");
                        insertVerbs.setString(5, bab != null ? bab : "");
                        insertVerbs.setInt(6, book_id);
                        insertVerbs.setInt(7, page_id);
                        insertVerbs.addBatch();
                    }
                }
                insertVerbs.executeBatch();

                for (int i = 0; i < nounsModel.getRowCount(); i++) {
                    String english = (String) nounsModel.getValueAt(i, 0);
                    String plural = (String) nounsModel.getValueAt(i, 1);
                    String dual = (String) nounsModel.getValueAt(i, 2);
                    String singular = (String) nounsModel.getValueAt(i, 3);
                    Object book_idObj = nounsModel.getValueAt(i, 4);
                    Object page_idObj = nounsModel.getValueAt(i, 5);
                    int book_id = (book_idObj instanceof Integer) ? (Integer) book_idObj : ((book_idObj != null && !book_idObj.toString().isEmpty()) ? Integer.parseInt(book_idObj.toString()) : 0);
                    int page_id = (page_idObj instanceof Integer) ? (Integer) page_idObj : ((page_idObj != null && !page_idObj.toString().isEmpty()) ? Integer.parseInt(page_idObj.toString()) : 0);
                    if (english != null && !english.trim().isEmpty()) {
                        insertNouns.setString(1, singular);
                        insertNouns.setString(2, dual != null ? dual : "");
                        insertNouns.setString(3, plural != null ? plural : "");
                        insertNouns.setString(4, english != null ? english : "");
                        insertNouns.setInt(5, book_id);
                        insertNouns.setInt(6, page_id);
                        insertNouns.addBatch();
                    }
                }
                insertNouns.executeBatch();
            }
            conn.commit();
            JOptionPane.showMessageDialog(this, "Saved successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB save error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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
            currentBookId = getOrCreateBookId(filePath, fileName);

            // Store the PDF document
            loadedPdfs.put(currentBookId, currentDocument);

            // Get or create tab for this book
            createOrUpdateBookTab(currentBookId, fileName);

            // Initialize book-specific data
            int lastPage = getLastPageFromSession(currentBookId);
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
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }

    private void createOrUpdateBookTab(int bookId, String fileName) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT title FROM books WHERE id = ?")) {
                stmt.setInt(1, bookId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String title = rs.getString("title");
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
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private int getOrCreateBookId(String filePath, String title) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Check if book already exists
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM books WHERE file_path = ?")) {
                stmt.setString(1, filePath);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            }

            // Create new book record
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO books(file_path, title, last_accessed) VALUES(?, ?, datetime('now'))", java.sql.Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, filePath);
                stmt.setString(2, title);
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error managing book record: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return -1;
    }

    private int getLastPageFromSession(int bookId) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement stmt = conn.prepareStatement("SELECT last_page FROM session WHERE book_id = ?")) {
                stmt.setInt(1, bookId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("last_page");
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private void saveSessionPage(int bookId, int page) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // Try to update existing session
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE session SET last_page = ? WHERE book_id = ?")) {
                stmt.setInt(1, page);
                stmt.setInt(2, bookId);
                int updated = stmt.executeUpdate();

                if (updated == 0) {
                    // Insert new session record if not exists
                    try (PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO session(book_id, last_page) VALUES(?, ?)")) {
                        insertStmt.setInt(1, bookId);
                        insertStmt.setInt(2, page);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
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
            saveSessionPage(bookId, pageIndex);
            loadDataByPage(pageIndex);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to render PDF page: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new IslamicStudiesApp());
    }
}
