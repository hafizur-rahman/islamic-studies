package com.jdreamer.ui;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;

/** Holds a File.  The node may be a folder or a file. */
public class FileTreeNode extends DefaultMutableTreeNode {

    private boolean isExplored = false;   // are children already created?

    public FileTreeNode(File file) {
        super(file, true);                // can have children
    }

    public File getFile() {
        return (File) getUserObject();
    }

    public boolean isExplored() { return isExplored; }
    public void setExplored(boolean b) { isExplored = b; }
}
