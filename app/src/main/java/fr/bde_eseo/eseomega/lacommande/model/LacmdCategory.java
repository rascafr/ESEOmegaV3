package fr.bde_eseo.eseomega.lacommande.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import fr.bde_eseo.eseomega.Constants;

/**
 * Created by Rascafr on 21/07/2015.
 */
public class LacmdCategory {

    private String name;
    private String imgUrl;
    private double beginPrice;
    private String smallText;
    private String catname;

    public LacmdCategory(String name, String imgUrl, double beginPrice, String smallText, String catname) {
        this.name = name;
        this.imgUrl = imgUrl;
        this.beginPrice = beginPrice;
        this.smallText = smallText;
        this.catname = catname;
    }

    public LacmdCategory(JSONObject obj) throws JSONException {
        this.name = obj.getString("name");
        this.imgUrl = obj.getString("imgUrl");
        this.beginPrice = obj.getDouble("firstPrice");
        this.catname = obj.getString("catname");
        this.smallText = obj.getString("briefText");
    }

    public String getName() {
        return name;
    }

    public String getImgUrl() {
        return Constants.URL_ASSETS + imgUrl;
    }

    public double getBeginPrice() {
        return beginPrice;
    }

    public String getBeginPriceAsStr() {
        return "À partir de " + new DecimalFormat("0.00").format(beginPrice) + "€";
    }

    public String getCatname() {
        return catname;
    }

    public String getSmallText() {
        return smallText;
    }

    @Override
    public String toString() {
        return "LacmdCategory{" +
                "name='" + name + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", beginPrice=" + beginPrice +
                ", smallText='" + smallText + '\'' +
                ", catname='" + catname + '\'' +
                '}';
    }
}
