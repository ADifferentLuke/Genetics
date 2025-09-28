package net.lukemcomber.genetics.io;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.ArrayDeque;

/**
 * Helper class for cell operations
 */
public class CellHelper {

    /**
     * Flattens a cell tree and return as a list
     *
     * @param rootCell root tree node to flatten
     * @return list of cells in the tree
     */
    public static List<Cell> getAllOrganismsCells(final Cell rootCell) {
        // Breadth-first snapshot of the cell tree. Safe against concurrent edits to child lists.
        final List<Cell> retVal = new LinkedList<>();
        final ArrayDeque<Cell> stack = new ArrayDeque<>();

        if (rootCell == null) return retVal;
        stack.add(rootCell);

        while (!stack.isEmpty()) {
            final Cell currentCell = stack.removeFirst();
            retVal.add(currentCell);

            // Snapshot children defensively in case another thread mutates the list while we traverse.
            final List<Cell> children = currentCell.getChildren();
            if (children == null || children.isEmpty()) continue;

            // Synchronize on the list object to prevent races during the snapshot.
            List<Cell> snapshot;
            synchronized (children) {
                snapshot = new ArrayList<>(children); // iterate a stable copy
            }
            for (Cell c : snapshot) {
                if (c != null) stack.addLast(c);
            }
        }
        return retVal;
    }
}
