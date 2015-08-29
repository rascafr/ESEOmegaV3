package fr.bde_eseo.eseomega.model;

/**
 * Created by François on 08/04/2015.
 */
public class NewsItem {

    public static final int MAX_DESC_LENGTH = 120;
    public static final String NEWS_AUTHOR_US = "ESEOmega";
    public static final String NEWS_AUTHOR_ELD = "BDEl'Dorado";

    private String name;
    private String content;
    private String description;
    private String shortDesc;
    private String imageLink;
    private String author;
    private String sDate;
    private String link;
    private boolean isESEOmega = false;
    private boolean isTitle;

    // News item with some data inside (not the date)
    public NewsItem (String name, String description, String content, String imageLink, String author) {
        this(name, description, content, imageLink, "", author, " --- ");
    }


    // News item with full data
    public NewsItem (String name, String description, String content, String imageLink, String link, String author, String sDate) {
        this.name = name;
        this.description = description;
        this.content = content;
        this.imageLink = imageLink;
        this.author = author;
        this.link = link;
        this.isTitle = false;
        this.sDate = sDate;

        // V2.0 -> less work into adapter
        if (this.description.length() > MAX_DESC_LENGTH) { // trim 120 char +/- 20 char if next space available
            int nextSpace = this.description.indexOf(" ", MAX_DESC_LENGTH);
            int relativeSpace = nextSpace - MAX_DESC_LENGTH;
            if (relativeSpace >= 0 && relativeSpace < 20)
                this.shortDesc = this.description.substring(0, nextSpace) + "...";
            else
                this.shortDesc = this.description.substring(0, MAX_DESC_LENGTH) + "...";
        } else {
            this.shortDesc = this.description;
        }
    }

    // Header
    public NewsItem (String name, boolean isESEOmega) {
        this.name = name;
        this.isESEOmega = isESEOmega;
        this.isTitle = true;
    }

    public String getName () {
        return this.name;
    }

    public String getContent () {
        return this.content;
    }

    public String getShortDesc () {
        return this.shortDesc;
    }

    public String getLink () {
        return this.link;
    }

    public String getDescription () { return this.description; }

    public boolean isESEOmega () {
        return this.isESEOmega;
    }

    public void setStrDAte (String sDate) {
        this.sDate = sDate;
    }

    public String getStrDate () {
        return this.sDate;
    }

    public boolean isTitle () {
        return this.isTitle;
    }

    public String getImageLink  () {
        return this.imageLink;
    }

    public String getAuthor () {
        return this.author;
    }

    public String toString () {
        return "News : " + name + " from " + author + " (img @ " + imageLink + ", short content length = " + ((shortDesc == null) ? "null":shortDesc.length()) + ")" + ", is ESEOmega : " + isESEOmega() + ", is title : " + isTitle;
    }
}
