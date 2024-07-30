package net.lukemcomber.genetics.store;

/*
 * (c) 2023 Luke McOmber
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

@Searchable
public class TestMetadata implements Metadata {
    @Indexed
    public String str;

    public Long longNumber;
    @Indexed( name="int")
    public Integer intNumber;


}
