package com.jdreamer.ui;

import com.jdreamer.model.UserSession;
import com.jdreamer.service.BookService;
import com.jdreamer.ui.model.BookOrPageChangeListener;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * A panel that shows one PDF document.
 */
public class PdfViewerPanel extends JPanel {
    private final PDDocument doc;        // never null
    private final PDFRenderer renderer;

    private final JLabel imageLabel = new JLabel();
    private final JButton btnPrev = new JButton("◀");
    private final JButton btnNext = new JButton("▶");

    private final JTextField pageIdField = new JTextField(3);
    private final JLabel totalPageInfo = new JLabel();

    private final JButton btnZoomIn = new JButton(IconLoader.loadIcon("icon/zoom_in.png"));
    private final JButton btnZoomOut = new JButton(IconLoader.loadIcon("icon/zoom_out.png"));

    private final JButton btnClose = new JButton("✕");

    private static final int BASE_DPI = 150;      // reference DPI
    private float zoomFactor = 1.0f;     // multiplicative factor

    private int bookId;
    private int currentPage = 0;

    private BookService bookService;
    private UserSession session;

    private int sideId;

    private final List<BookOrPageChangeListener> listeners;

    public PdfViewerPanel(int bookId, File pdfFile, BookService bookService, List<BookOrPageChangeListener> listeners) throws Exception {
        this.bookId = bookId;
        this.doc = Loader.loadPDF(pdfFile);
        this.renderer = new PDFRenderer(doc);

        this.bookService = bookService;

        this.session = getOrCreateUserSession();
        this.session.setOpenAtStartup(true);
        this.sideId = this.session.getSide();

        this.currentPage = session.getPageId();
        this.zoomFactor = session.getZoomFactor();

        this.listeners = listeners;

        buildUI();

        bookService.saveSession(session);
    }

    private UserSession getOrCreateUserSession() {
        UserSession session = bookService.findUserSessionByBookId(bookId);
        if (session == null) {
            session = new UserSession();
            session.setBookId(bookId);
            session.setAccessedAt(System.currentTimeMillis());
        }

        return session;
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        btnClose.addActionListener(e -> closeThisPanel());

        btnPrev.addActionListener(e -> {
            session.setPageId(currentPage);
            goToPage(currentPage - 1);
            bookService.saveSession(session);
        });

        btnNext.addActionListener(e -> {
            session.setPageId(currentPage);
            goToPage(currentPage + 1);
            bookService.saveSession(session);
        });
        pageIdField.addActionListener(e -> {
            session.setPageId(currentPage);
            goToPage(Integer.parseInt(pageIdField.getText())-1);
            bookService.saveSession(session);
        });

        btnZoomIn.addActionListener(e -> {
            zoomFactor *= 1.1f;
            session.setZoomFactor(zoomFactor);
            renderCurrentPage();
            bookService.saveSession(session);
        });
        btnZoomOut.addActionListener(e -> {
            zoomFactor /= 1.1f;
            session.setZoomFactor(zoomFactor);
            renderCurrentPage();
            bookService.saveSession(session);
        });

        // Navigation bar at the bottom
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.CENTER));

        navBar.add(btnPrev);
        navBar.add(pageIdField);
        navBar.add(totalPageInfo);
        navBar.add(btnNext);

        navBar.add(Box.createHorizontalStrut(12));   // spacer
        navBar.add(btnZoomIn);
        navBar.add(btnZoomOut);
        navBar.add(Box.createHorizontalStrut(12));   // spacer
        navBar.add(btnClose);

        JScrollPane pdfScroll = new JScrollPane(imageLabel);
        pdfScroll.getVerticalScrollBar().setUnitIncrement(30);

        add(navBar, BorderLayout.NORTH);
        add(pdfScroll, BorderLayout.CENTER);

        // Show first page
        renderCurrentPage();
        bookService.saveSession(session);
    }

    /**
     * Navigate to the given 0‑based page number (clamped).
     */
    public void goToPage(int target) {
        if (target < 0 || target >= doc.getNumberOfPages()) return;

        currentPage = target;

        renderCurrentPage();
        bookService.saveSession(session);
    }

    /**
     * Render the currently selected page.
     */
    private void renderCurrentPage() {
        try {
            int dpi   = Math.max(1, (int)(BASE_DPI * zoomFactor));
            java.awt.image.BufferedImage image = renderer.renderImageWithDPI(currentPage, dpi);
            imageLabel.setIcon(new ImageIcon(image));

            pageIdField.setText(String.valueOf(currentPage+1));
            totalPageInfo.setText(String.format("/ %d", doc.getNumberOfPages()));

            btnPrev.setEnabled(currentPage > 0);
            btnNext.setEnabled(currentPage < doc.getNumberOfPages() - 1);

            for (BookOrPageChangeListener listener: listeners) {
                listener.onBookOrPageChange(bookId, currentPage);
            }
        } catch (Exception e) {
            imageLabel.setIcon(null);
            totalPageInfo.setText("Error rendering page");
            e.printStackTrace();
        }
    }

    /**
     * Call when you no longer need the component.
     */
    public void dispose() {
        try {
            doc.close();
        } catch (Exception ignored) {
        }
    }

    /* ----------------------------------------------------------- */

    /**
     * Close button action – removes this panel from whatever JTabbedPane
     * contains it and disposes of the underlying PDF.
     */
    private void closeThisPanel() {
        //session.setOpenAtStartup(false);
        bookService.saveSession(session);

        Component parent = SwingUtilities.getAncestorOfClass(JTabbedPane.class, this);

        if (!(parent instanceof JTabbedPane)) return;          // safety

        // 2. Locate our index in that tabbed pane
        int idx = ((JTabbedPane) parent).indexOfComponent(this);
        if (idx == -1) return;

        // 3. Dispose of PDF resources
        dispose();

        // 4. Remove the tab (this removes us from the UI)
        ((JTabbedPane) parent).remove(idx);
    }

    /**
     * The document that this panel is displaying.
     */
    public PDDocument getDocument() {
        return doc;
    }

    public int getSideId() {
        return sideId;
    }

    public void setSideId(int sideId) {
        this.sideId = sideId;
        this.session.setSide(sideId);

        bookService.saveSession(session);
    }
}
