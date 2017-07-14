package com.advancetech.digitalsignage;

/**
 * Created by rahul on 06-Jun-17.
 * <p>
 * Data Model of fireBase dataBase items.
 */

public class DataModel {
    /**
     * file name
     */
    public String name;

    /**
     * file download URL
     */
    public String url;

    /**
     * Constructor
     */
    public DataModel() {

    }

    /**
     * Constructor
     *
     * @param name file name
     * @param url  file download URL
     */
    public DataModel(String name, String url) {
        this.name = name;
        this.url = url;
    }
}
