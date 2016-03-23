package fr.bde_eseo.eseomega.news;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import fr.bde_eseo.eseomega.Constants;

/**
 * Created by Rascafr on 25/08/2015.
 */
public class NewsItem {
    private String name, strDate, author, link, data, shData, frenchStr, headerImg;
    private boolean isFooter;
    private boolean isHeader;
    private Date date;

    public boolean isFooter() {
        return isFooter;
    }

    private ArrayList<String> imgLinks;

    public NewsItem() {
        isFooter = true;
        isHeader = false;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public NewsItem(String text) {
        isFooter = false;
        isHeader = true;
        name = text;
    }

    public NewsItem(String name, String strDate, String author, String link, String shData, String data, String headerImg) {
        this.isFooter = false;
        this.isHeader = false;
        this.name = name;
        this.strDate = strDate;
        this.author = author;
        this.link = link;
        this.data = data;
        this.shData = shData;
        this.date = getParsedDate(strDate);
        this.frenchStr = getFrenchDate(strDate);
        this.headerImg = headerImg;
        // TODO parse data and fill imgLinks
        this.imgLinks = new ArrayList<>();
        imgLinks.add(headerImg);
        /*

        // All img are like <img ... >
        if (this.data != null) {
            int stPos = 0;
            int endPos = 0, ipos = 0;
            while (stPos != -1) {
                stPos = this.data.indexOf("<img ");
                if (stPos != -1) {
                    endPos = this.data.indexOf(">", stPos);
                    if (endPos != -1 && stPos < endPos) {

                        // We found an image link : remove st...end
                        int fst = data.indexOf("src=\"", stPos)+5;
                        String imgLink = data.substring(fst, data.indexOf("\"", fst+1));
                        imgLinks.add(imgLink);
                        ipos++;
                        this.data = this.data.substring(0, stPos) + this.data.substring(endPos+1);
                        stPos = 0; // restart from zero -> we remove string in length
                    }
                }
            }
        }*/
    }

    public NewsItem (String name, String author, String html, String link, ArrayList<String> imgLinks) {
        this.name = name;
        this.author = author;
        this.data = html;
        this.link = link;
        this.imgLinks = imgLinks;
    }

    public String toJSONstr () {
        JSONObject obj = new JSONObject();
        try {
            obj.put("titre",name);
            obj.put("auteur",author);
            obj.put("date",getFrenchStr());
            JSONArray array = new JSONArray();
            obj.put("imgarray", imgLinks);
            obj.put("html",data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj.toString();
    }

    @Override
    public String toString() {
        return "NewsItem{" +
                "name='" + name + '\'' +
                ", strDate='" + strDate + '\'' +
                ", author='" + author + '\'' +
                ", link='" + link + '\'' +
                ", data='" + data + '\'' +
                ", shData='" + shData + '\'' +
                ", frenchStr='" + frenchStr + '\'' +
                ", headerImg='" + headerImg + '\'' +
                ", date=" + date +
                ", imgLinks=" + imgLinks +
                '}';
    }

    public NewsItem (JSONObject obj) throws JSONException {
        this(obj.getString("titre"), obj.getString("date"), obj.getString("auteur"),
                obj.getString("lien"), obj.getString("resume"),
                obj.getString("content"), obj.getString("img"));

        Log.d("NEWS", "Add : " + obj.getString("titre"));
    }

    private Date getParsedDate (String d) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE);
        Date date = null;
        try {
            date = format.parse(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String getName() {
        return name;
    }

    public String getStrDate() {
        return strDate;
    }

    public String getAuthor() {
        return author;
    }

    public String getLink() {
        return link;
    }

    public String getData() {
        return data;
    }

    public String getShData() {
        return shData;
    }

    public String getFrenchStr() {
        return frenchStr;
    }

    public String getHeaderImg() {
        return headerImg;
    }

    public Date getDate() {
        return date;
    }

    public ArrayList<String> getImgLinks() {
        return imgLinks;
    }

    private String getFrenchDate (String dt) {
        Date d = getParsedDate(dt);
        SimpleDateFormat sdf = new SimpleDateFormat("E dd MMMM yyyy, 'à' HH:mm", Locale.FRANCE);
        return sdf.format(d);
    }
}
