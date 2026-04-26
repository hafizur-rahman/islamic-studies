package com.jdreamer;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainWindow extends JFrame {
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

    public MainWindow() {
        super("Islamic Studies");

        buildUI();
        setupDragAndDrop();
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
        //saveDataBtn.addActionListener(e -> saveDataToDB());

        JPanel topLeft = new JPanel(new BorderLayout(4, 4));
        topLeft.add(translationScroll, BorderLayout.NORTH);
        topLeft.add(tablesPanel, BorderLayout.CENTER);
        topLeft.add(saveDataBtn, BorderLayout.SOUTH);

        leftPanel.add(topLeft, BorderLayout.CENTER);

        // Right panel (PDF viewer with tabs)
        JPanel rightPanel = new JPanel(new BorderLayout());

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

//        prevPageBtn.addActionListener(e -> showPageForBook(currentBookId, currentPages.getOrDefault(currentBookId, 0) - 1));
//        nextPageBtn.addActionListener(e -> showPageForBook(currentBookId, currentPages.getOrDefault(currentBookId, 0) + 1));
//        zoomInBtn.addActionListener(e -> zoomInForBook(currentBookId));
//        zoomOutBtn.addActionListener(e -> zoomOutForBook(currentBookId));
//        resetZoomBtn.addActionListener(e -> resetZoomForBook(currentBookId));
//        closeBtn.addActionListener(e -> closeBook(currentBookId));

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
                        //switchToBook(currentBookId);
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
            //createOrUpdateBookTab(currentBookId, fileName);

            // Initialize book-specific data
//            int lastPage = getLastPageFromSession(currentBookId);
//            currentPage = Math.min(lastPage, currentDocument.getNumberOfPages() - 1);
//            currentPages.put(currentBookId, currentPage);
//            zoomFactors.put(currentBookId, 1.0f);
//
//            // Switch to the book
//            switchToBook(currentBookId);
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
}

