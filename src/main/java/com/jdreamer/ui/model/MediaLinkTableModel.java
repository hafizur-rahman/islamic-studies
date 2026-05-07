package com.jdreamer.ui.model;

import com.jdreamer.model.MediaLink;
import lombok.Getter;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class MediaLinkTableModel extends AbstractTableModel {

    private final String[] columns = {"ID", "BookID", "PageID"};
    // Use a private final list to ensure we control the data source
    @Getter
    private final List<MediaLink> mediaLinks;

    public MediaLinkTableModel(List<MediaLink> mediaLinks) {
        // We wrap the input list in a new ArrayList to ensure it is MUTABLE.
        // This prevents UnsupportedOperationException if List.of() or Arrays.asList() was passed.
        this.mediaLinks = (mediaLinks != null) ? new ArrayList<>(mediaLinks) : new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return mediaLinks.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int col) {
        return columns[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row >= mediaLinks.size()) return null;
        MediaLink n = mediaLinks.get(row);
        return switch (col) {
            case 0 -> n.getId();
            case 1 -> n.getBookId();
            case 2 -> n.getPageId();
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return true;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (row >= mediaLinks.size()) return;
        MediaLink n = mediaLinks.get(row);

        try {
            switch (col) {
                case 0 -> n.setId(value != null ? value.toString() : "");
                case 1 -> n.setBookId(tryParseInt(value));
                case 2 -> n.setPageId(tryParseInt(value));
            }
            fireTableCellUpdated(row, col);
        } catch (Exception e) {
            System.err.println("Error updating cell: " + e.getMessage());
        }
    }

    /**
     * Helper to prevent ClassCastException when the UI passes a String
     * (from a TextField) into an Integer field.
     */
    private Integer tryParseInt(Object value) {
        if (value instanceof Integer i) return i;
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return 0; // Default value on error
            }
        }
        return 0;
    }

    /* --------------------- Utility --------------------- */

    public void updateData(List<MediaLink> newMediaLinks) {
        this.mediaLinks.clear();

        if (newMediaLinks != null) {
            this.mediaLinks.addAll(newMediaLinks);
        }

        fireTableDataChanged();
    }

    /**
     * Adds a new row at the end of the existing non-empty rows.
     * Creates a blank Noun object to act as a template for the user.
     */
    public void addEmptyRowAtEnd(int bookId, int pageId) {
        if (mediaLinks.isEmpty() || mediaLinks.get(mediaLinks.size()-1).getId() != null) {
            int newIndex = mediaLinks.size();

            MediaLink newVideo = new MediaLink(); // Assumes a default constructor exists

            newVideo.setBookId(bookId);
            newVideo.setPageId(pageId);

            mediaLinks.add(newVideo);

            fireTableRowsInserted(newIndex, newIndex);

            // Optional: You could add logic here to automatically
            // select the new row in the JTable via the UI controller.
        }
    }
}
