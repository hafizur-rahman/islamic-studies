package com.jdreamer.ui;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.io.File;

/** Uses the OS icon and name for each node. */
public class FileSystemTreeCellRenderer extends DefaultTreeCellRenderer {

    private final FileSystemView fsv = FileSystemView.getFileSystemView();

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if (value instanceof FileTreeNode) {
            File f = ((FileTreeNode) value).getFile();
            setText(fsv.getSystemDisplayName(f));
            setIcon(fsv.getSystemIcon(f));
        }
        return this;
    }
}
