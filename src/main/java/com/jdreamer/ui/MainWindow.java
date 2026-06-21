package com.jdreamer.ui;

import com.jdreamer.cache.MediaUrlCache;
import com.jdreamer.model.Book;
import com.jdreamer.model.UserSession;
import com.jdreamer.service.BookService;
import com.jdreamer.ui.model.BookOrPageChangeListener;
import org.apache.pdfbox.Loader;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame {
    public static final Icon HIDE_ICON = IconLoader.loadIcon("icon/hide.png");
    public static final Icon LOAD_ICON = IconLoader.loadIcon("icon/show.png");

    private JButton showHideButton = new JButton(HIDE_ICON);

    private JScrollPane fileTreePane;
    private BookService bookService;

    private PdfTabbedPane leftPane = new PdfTabbedPane(0);
    private PdfTabbedPane rightPane = new PdfTabbedPane(1);

    private ArrayList<BookOrPageChangeListener> listeners = new ArrayList<>();

    public MainWindow(BookService bookService) {
        super("Islamic Studies");

        this.bookService = bookService;

        buildUI();
        setupDragAndDrop();

        loadBooksAtStartup();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        FileSystemTree tree = creteFileSystemTree();

        fileTreePane = new JScrollPane(tree);
        fileTreePane.setVisible(false);

        showHideButton.addActionListener(e -> {
            fileTreePane.setVisible(!fileTreePane.isVisible());

            showHideButton.setIcon(fileTreePane.isVisible() ? HIDE_ICON : LOAD_ICON);

            this.revalidate();
            this.repaint();
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(showHideButton, BorderLayout.NORTH);
        panel.add(fileTreePane, BorderLayout.CENTER);

        add(panel, BorderLayout.WEST);


        MediaPanel mediaPanel = new MediaPanel(bookService);
        rightPane.add("Video", mediaPanel);

        NotesPanel notesPanel = new NotesPanel(bookService);
        rightPane.add("Notes", notesPanel);


        listeners.add(mediaPanel);
        listeners.add(notesPanel);

        leftPane.setPeer(rightPane);
        rightPane.setPeer(leftPane);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, rightPane);
        //splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(940);

        add(splitPane, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 850);
        setLocationRelativeTo(null);
        setVisible(true);
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
                            JOptionPane.showMessageDialog(MainWindow.this,
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

    private void loadBooksAtStartup() {
        java.util.List<UserSession> userSessions = bookService.findUserSessionsByOpenAtStartup();
        java.util.List<Integer> bookIds = userSessions.stream().map(UserSession::getBookId).toList();

        List<Book> books = bookService.findAllBooksByBookIds(bookIds);

        for (Book book: books) {
            String filePath = book.getFilePath();
            File file = new File(filePath);

            openPDF(file);
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

    private void openPDF(File file) {
        try {
            // Get or create book record
            String filePath = file.getAbsolutePath();
            String fileName = file.getName();

            int currentBookId = getOrCreateBookId(filePath, fileName);

            PdfViewerPanel viewerPanel = new PdfViewerPanel(currentBookId, file, bookService, listeners);

            if (viewerPanel.getSideId() == 0) {
                leftPane.addPdfFile(file.getName(), viewerPanel);
            } else {
                rightPane.addPdfFile(file.getName(), viewerPanel);
            }
        } catch (Exception e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(this, "Failed to open PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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
}

