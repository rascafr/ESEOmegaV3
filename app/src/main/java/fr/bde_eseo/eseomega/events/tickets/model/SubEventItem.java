package fr.bde_eseo.eseomega.events.tickets.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * Created by Rascafr on 11/01/2016.
 */
public class SubEventItem {

    private String titre, id;
    private double price;

    public SubEventItem(JSONObject obj) throws JSONException {

        Log.d("DBG", "JSON : " + obj.toString());

        titre = obj.getString("nom");
        id = obj.getString("id");
        price = obj.getDouble("prix");
    }

    public String getTitre() {
        return titre;
    }

    public String getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public String getEventPriceAsString(){
        return new DecimalFormat("0.00").format(price) + "€";
    }
}
