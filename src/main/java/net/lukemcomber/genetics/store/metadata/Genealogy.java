package net.lukemcomber.genetics.store.metadata;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

import net.lukemcomber.genetics.store.Metadata;

public class Genealogy implements Metadata {

    public static final String PROPERTY_GENEALOGY_ENABLE = "metadata.Genealogy.enabled";

    public String parentName = "";
    public String childName = "";
    public long birthTickDate = 0l;
}
