package net.lukemcomber.genetics.store;


/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

public class TestMetadata implements Metadata{

    public static final String PROPERTY_ENABLED = "metadata.TestSearchableMetadata.enabled";
    @Primary
    public String str;

    public Long longNumber;
    public Integer intNumber;
}
