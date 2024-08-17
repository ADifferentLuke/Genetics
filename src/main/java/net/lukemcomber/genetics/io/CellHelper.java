package net.lukemcomber.genetics.io;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.biology.Cell;

import java.util.LinkedList;
import java.util.List;

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
        final List<Cell> retVal = new LinkedList<>();
        final List<Cell> stack = new LinkedList<>();
        stack.add(rootCell);
        do {
            final Cell currentCell = stack.remove(0);
            retVal.add(currentCell);
            stack.addAll(currentCell.getChildren());

        } while (0 < stack.size());
        return retVal;
    }
}
