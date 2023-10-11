package net.lukemcomber.dev.ai.genetics.model;

import net.lukemcomber.dev.ai.genetics.biology.Cell;

import java.util.function.Function;

public class QuadTreeSearch {

    public static void foreach (Cell rootNode, Class<? extends Cell> klass, Function<Cell,Boolean> function) {
        /*
        if (rootNode.getClass().equals(klass)) {
            function.apply(rootNode);
        }
        if (null != rootNode.getLeft()) {
            foreach(rootNode.getLeft(),klass,function);
        }
        if( null != rootNode.getRight()){
            foreach(rootNode.getRight(),klass,function);
        }
        if( null != rootNode.getUp() ){
            foreach(rootNode.getUp(),klass,function);
        }
        if( null != rootNode.getDown() ){
            foreach(rootNode.getDown(),klass,function);
        }
         */
    }

}
