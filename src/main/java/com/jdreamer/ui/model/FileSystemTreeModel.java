package com.jdreamer.ui.model;

import com.jdreamer.ui.FileTreeNode;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Lazy TreeModel that wraps a File system. */
public class FileSystemTreeModel implements TreeModel {

    private final FileTreeNode root;

    public FileSystemTreeModel(File rootDir) {
        this.root = new FileTreeNode(rootDir);
    }

    @Override public Object getRoot() { return root; }

    @Override public Object getChild(Object parent, int index) {
        FileTreeNode node = (FileTreeNode) parent;
        if (!node.isExplored()) explore(node);
        return node.getChildAt(index);
    }

    @Override public int getChildCount(Object parent) {
        FileTreeNode node = (FileTreeNode) parent;
        if (!node.isExplored()) explore(node);
        return node.getChildCount();
    }

    @Override public boolean isLeaf(Object node) {
        return !((FileTreeNode) node).getFile().isDirectory();
    }

    @Override public void valueForPathChanged(TreePath path, Object newValue) {
        // we don't allow renaming via the tree
    }

    @Override public int getIndexOfChild(Object parent, Object child) {
        return ((FileTreeNode) parent).getIndex((TreeNode) child);
    }

    @Override public void addTreeModelListener(TreeModelListener l) { /* unused */ }

    @Override public void removeTreeModelListener(TreeModelListener l) { /* unused */ }

    /** Lazily creates child nodes for a folder */
    private void explore(FileTreeNode node) {
        File file = node.getFile();
        File[] files = file.listFiles();
        if (files == null) return;               // permission denied, etc.

        // Sort: directories first, then alphabetically
        List<FileTreeNode> children = Arrays.stream(files)
                .sorted(Comparator.comparing(File::isFile)
                        .thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER))
                .map(f -> new FileTreeNode(f))
                .collect(Collectors.toList());

        children.forEach(node::add);
        node.setExplored(true);
    }
}
