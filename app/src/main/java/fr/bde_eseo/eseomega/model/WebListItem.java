package fr.bde_eseo.eseomega.model;

import android.widget.ImageView;

/**
 * Created by Rascafr on 10/08/2015.
 * Custom item to define a listView (4 types : Header, Text, Image, Url)
 */
public class WebListItem {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TEXT = 1;
    private static final int TYPE_IMAGE = 2;
    private static final int TYPE_URL = 3;

    /**
     * Yeah recyclables values
     */
    // Header : 2 text + image
    // Normal text : textA only
    // Link : textA only
    // Image : textA only
    String textA, textB, imageLink;

    // Type
    int type;

    // Header constructor
    public WebListItem (String author, String title, String imageLink) {
        this.textA = author;
        this.textB = title;
        this.imageLink = imageLink;
    }

    // Text / Link / Image constructor
    public WebListItem (String data, int type) {
        if (type == TYPE_IMAGE)
            this.imageLink = data;
        else
            this.textA = data;
        this.type = type;
    }


}
