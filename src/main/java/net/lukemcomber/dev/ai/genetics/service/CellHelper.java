package net.lukemcomber.dev.ai.genetics.service;

import net.lukemcomber.dev.ai.genetics.biology.Cell;

import java.util.LinkedList;
import java.util.List;

public class CellHelper {

    public static List<Cell> getAllOrganismsCells(final Cell rootCell ){
        final List<Cell> retVal = new LinkedList<>();
        final List<Cell> stack = new LinkedList<>();
        stack.add(rootCell);
        do {
            final Cell currentCell = stack.remove(0);
            retVal.add(currentCell);
            stack.addAll(currentCell.getChildren());

        } while( 0 < stack.size());
        return retVal;
    }
}
