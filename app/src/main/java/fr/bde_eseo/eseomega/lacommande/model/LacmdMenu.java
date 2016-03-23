package fr.bde_eseo.eseomega.lacommande.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Rascafr on 17/08/2015.
 */
public class LacmdMenu extends LacmdRoot {
    private String mainElemStr;
    private int maxMainElem, maxSecoElem;
    public final static String ID_CAT_MENU = "lacmd-menus";
/*
    public LacmdMenu(String name, String idstr, double price) {
        super(name, idstr);
        this.price = price;
    }
*//*
    public LacmdMenu(JSONObject obj) throws JSONException {
        super(obj.getString("name"), obj.getString("idstr"), 0, 1, obj.getDouble("price"), ID_CAT_MENU); // no ingredients, but elements yes
        JSONArray items = obj.getJSONArray("items");
        mixeds = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            mixeds.add(new Mixed(items.getJSONObject(i)));
        }
    }*/

    public LacmdMenu(JSONObject obj) throws JSONException {
        super(obj.getString("name"), obj.getString("idstr"), 0, 1, obj.getDouble("price"), ID_CAT_MENU); // no ingredients, but elements yes
        // expressions
        mainElemStr = obj.getString("mainElemStr");
        maxMainElem = obj.getInt("nbMainElem");
        maxSecoElem = obj.getInt("nbSecoElem");
    }

    public String getMainElemStr() {
        return mainElemStr;
    }

    public int getMaxMainElem() {
        return maxMainElem;
    }

    public int getMaxSecoElem() {
        return maxSecoElem;
    }

    public LacmdMenu(LacmdMenu obj) {
        super(obj.getName(), obj.getIdstr(), obj.hasIngredients(), obj.hasElements(), obj.getPrice(), ID_CAT_MENU);
        mainElemStr = obj.getMainElemStr();
        maxMainElem = obj.getMaxMainElem();
        maxSecoElem = obj.getMaxSecoElem();
    }

    /*

    @Override
    public String toString() {
        return "LacmdMenu{" +
                "name='" + name + '\'' +
                ", idstr='" + idstr + '\'' +
                ", price=" + price +
                '}';
    }*/

    @Override
    public String toString() {
        return "LacmdMenu{" +
                "name='" + getName() + '\'' +
                ", idstr='" + idstr + '\'' +
                ", price=" + price +
                '}';
    }
}
