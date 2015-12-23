package fr.bde_eseo.eseomega.news;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Created by Rascafr on 15/11/2015.
 * Replace Sonasi's span style by simple HTML tags
 * color: rgb(20, 24, 35); → <font color="rgb(20, 24, 35)"></font>
 * text-align: center; → <center></center>
 * font-weight: bold; → <b></b>
 * text-decoration: underline; → <u></u>
 * font-style: italic; → <i></i>
 */
public class SimpleHTML {

    private String rawHTML, simpleHTML;
    private static final String TAG_SPAN = "span";

    // Correspondances css ↔ html
    private String[] equivCSS = {"color", "font-weight", "text-decoration", "font-style"};
    private String[] equivHTML = {"<font color"};

    public SimpleHTML (String rawHTML) {
        this.rawHTML = rawHTML;
        this.simpleHTML = "";
    }

    public String getSimpleHTML() {
        return simpleHTML;
    }

    // Simplify the HTML code
    public void simplify () {

        int roll = 0;

        while (roll != -1) {

            // Search for <span
            roll = getTagPosition(TAG_SPAN, roll);

            // If <span has been found
            if (roll != -1) {

                String startTag = "", endTag = "";

                String log = "--- SPAN BEGIN ---\n";

                // Search for style="
                roll = rawHTML.indexOf("\"", roll)+1;

                // Search for span style end
                int end = getEndTagPosition(roll + 1); // +1 prevents redundancy

                // Get the elements inside span
                ArrayList<String> items = spanSplitter(rawHTML.substring(roll, end));

                // Convert each of the elements
                for (int i=0;i<items.size();i++) {

                    // Get header and value
                    ArrayList<String> style = styleSplitter(items.get(i).replace(" ", ""));

                    log += "Style item : " + items.get(i) + " " + style.get(0) + "\n";

                    // Style is correct ?
                    if (style.size() == 2) {

                        // Get header
                        String header = style.get(0);

                        // And associated value
                        String value = style.get(1);

                        log += header + " ↔ " + value + "\n";

                        // Convert it to HTML code
                        ArrayList<String> convertedTags = cssToHtml(header, value);

                        // If tag is valid, add it to result string
                        if (convertedTags.size() == 2) {

                            startTag += convertedTags.get(0);
                            endTag += convertedTags.get(1);

                        }
                    }
                }

                // Search to closing tag
                int close = rawHTML.indexOf("</span>", end);

                // Get text substring
                String text = rawHTML.substring(end+1, close);

                // Add start end close html tag to it
                text = startTag + text + endTag + "<br>";

                // Add converted string and html tag to final result
                simpleHTML += text;

                log += text + "\n--- SPAN END ---\n";

                Log.d("SimpleHTML", log);

                roll = close + 7;
            }
        }
    }

    // Returns a tag position
    // for example : span → return the position of <span, or < "manyspaces" span
    // returns -1 if not found
    private int getTagPosition (String tag, int offset) {
        int st = rawHTML.indexOf("<", offset);
        int pos = -1;
        if (st != -1) {
            pos = rawHTML.indexOf("span", st+1);
        }
        return pos;
    }

    // Return the next > close item
    private int getEndTagPosition (int offset) {
        return rawHTML.indexOf(">", offset);
    }

    // Split a span item into style elements
    private ArrayList<String> spanSplitter (String spanItem) {

        ArrayList<String> items = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(spanItem, ";");

        for (int i=0;i<tokenizer.countTokens();i++) {
            items.add(tokenizer.nextToken());
        }

        return items;
    }

    // Split style item into header and value
    private ArrayList<String> styleSplitter (String styleItem) {

        ArrayList<String> items = new ArrayList<>();
        Collections.addAll(items, styleItem.split(":"));
        return items;
    }

    // Convert header and value into html code (start + end)
    private ArrayList<String> cssToHtml (String header, String value) {

        ArrayList<String> tags = new ArrayList<>();

        if (header.equals("font-weight")) {
            tags.add("<b>"); tags.add("</b>");
        } else if (header.equals("text-decoration") && header.equals("underline")) {
            tags.add("<u>"); tags.add("</u>");
        } else if (header.equals("font-style") && header.equals("italic")) {
            tags.add("<i>"); tags.add("</i>");
        } else if (header.equals("color")) {
            tags.add("<font color=\"" + value + "\">"); tags.add("</font>");
        }

        return tags;
    }

}
