package com.jdreamer.ui;

import com.jdreamer.ui.model.FileSystemTreeModel;

import javax.swing.*;
import javax.swing.tree.*;
import java.io.File;

/** Wraps a JTree with the File‑System model & renderer. */
public class FileSystemTree extends JTree {

    public FileSystemTree(File rootDir) {
        super(new FileSystemTreeModel(rootDir));
        setCellRenderer(new FileSystemTreeCellRenderer());
        setShowsRootHandles(true);      // nice + icon
        setRootVisible(true);
        setLargeModel(true);           // avoid JTree's internal node cache
    }

    /** Helper – returns the selected File (null if none or a directory). */
    public File getSelectedFile() {
        TreePath path = getSelectionPath();
        if (path == null) return null;
        Object node = path.getLastPathComponent();
        if (node instanceof FileTreeNode && ((FileTreeNode) node).getFile().isFile()) {
            return ((FileTreeNode) node).getFile();
        }
        return null;
    }
}
