package fr.bde_eseo.eseomega.events.tickets.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Rascafr on 11/01/2016.
 */
public class SubEventItem {

    private String titre, id;
    private double price;
    private boolean available;
    private ArrayList<ShuttleItem> shuttleItems;

    public SubEventItem(JSONObject obj) throws JSONException {

        titre = obj.getString("nom");
        id = obj.getString("id");
        price = obj.getDouble("prix");
        available = obj.getInt("dispo") == 1;
        shuttleItems = new ArrayList<>();
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

    public boolean isAvailable() {
        return available;
    }

    public void searchShuttles (ArrayList<ShuttleItem> allShuttles) {
        for (int i=0;i<allShuttles.size();i++) {
            ShuttleItem si = allShuttles.get(i);
            if (si.correspondsToID(id)) {
                shuttleItems.add(si);
                Log.d("DBG", "Shuttle added : " + si.getIdshuttle());
            }
        }
    }

    public boolean hasShuttles () {
        return shuttleItems.size() > 0;
    }

    public ArrayList<ShuttleItem> getShuttleItems() {
        return shuttleItems;
    }
}
