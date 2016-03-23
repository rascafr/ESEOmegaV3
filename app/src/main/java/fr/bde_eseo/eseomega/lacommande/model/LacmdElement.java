package fr.bde_eseo.eseomega.lacommande.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rascafr on 17/08/2015.
 */
public class LacmdElement extends LacmdRoot {
    private String idcat;
    private double pricemore;
    private int stock, outofmenu;
    public final static String ID_CAT_ELEMENT = "lacmd-elements";

    public LacmdElement(String name, String idstr, double priceuni, double pricemore, int stock, int outofmenu, int hasingredients) {
        super(name, idstr, hasingredients, 0, priceuni, ID_CAT_ELEMENT);
        this.pricemore = pricemore;
        this.stock = stock;
        this.outofmenu = outofmenu;
    }

    public LacmdElement (JSONObject obj) throws JSONException {
        super(obj.getString("name"), obj.getString("idstr"), obj.getInt("hasingredients"), 0, obj.getDouble("priceuni"), ID_CAT_ELEMENT);
        this.pricemore = obj.getDouble("pricemore");
        this.stock = obj.getInt("stock");
        this.outofmenu = obj.getInt("outofmenu");
        this.idcat = obj.getString("idcat");
    }

    public LacmdElement (LacmdElement obj) {
        super(obj.getName(), obj.getIdstr(), obj.hasIngredients(), obj.hasElements(), obj.getPrice(), ID_CAT_ELEMENT);
        this.pricemore = obj.getPricemore();
        this.stock = obj.stock;
        this.outofmenu = obj.outofmenu;
        this.idcat = getIdcat();
    }

    public String getIdcat() {
        return idcat;
    }

    public double getPricemore() {
        return pricemore;
    }

    public int getOutofmenu() {
        return outofmenu;
    }

    @Override
    public String toString() {
        return "LacmdElement{" +
                "name='" + getName() + '\'' +
                ", idstr='" + idstr + '\'' +
                ", priceuni=" + price +
                ", pricemore=" + pricemore +
                ", stock=" + stock +
                ", outofmenu=" + outofmenu +
                ", hasingredients=" + hasIngredients +
                '}';
    }
}
