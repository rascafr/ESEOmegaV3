package fr.bde_eseo.eseomega.lacommande.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rascafr on 17/08/2015.
 */
public class LacmdIngredient extends LacmdRoot {
    private int stock;
    public final static String ID_CAT_INGREDIENT = "lacmd-ingredients";

    public LacmdIngredient(String name, String idstr, double price, int stock) {
        super(name, idstr, 0, 0, price, ID_CAT_INGREDIENT);
        this.stock = stock;
    }

    public LacmdIngredient (JSONObject obj) throws JSONException {
        super(obj.getString("name"), obj.getString("idstr"), 0, 0, obj.getDouble("priceuni"), ID_CAT_INGREDIENT);
        this.stock = obj.getInt("stock");
    }

    @Override
    public String toString() {
        return "LacmdIngredient{" +
                "name='" + name + '\'' +
                ", idstr='" + idstr + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                '}';
    }

    public String getName() {
        return name;
    }

    public String getIdstr() {
        return idstr;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }
}
