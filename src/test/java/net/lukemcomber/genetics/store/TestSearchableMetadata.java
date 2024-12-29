package net.lukemcomber.genetics.store;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

public class TestSearchableMetadata implements Metadata {

    public static final String PROPERTY_ENABLED = "metadata.TestSearchableMetadata.enabled";
    @Indexed
    public String str;

    public Long longNumber;
    @Indexed( name="int")
    public Integer intNumber;


}
