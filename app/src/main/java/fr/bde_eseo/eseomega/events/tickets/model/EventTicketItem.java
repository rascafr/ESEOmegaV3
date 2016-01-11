package fr.bde_eseo.eseomega.events.tickets.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rascafr on 11/01/2016.
 * Décrit un achat de place
 */
public class EventTicketItem {

    private String name, datetime;
    private int idcmd;
    private double price;

    public EventTicketItem(JSONObject obj) throws JSONException {
        name = obj.getString("name");
        datetime = obj.getString("datetime");
        idcmd = obj.getInt("idcmd");
        price = obj.getDouble("price");
    }

    public String getName() {
        return name;
    }

    public String getDatetime() {
        return datetime;
    }

    public int getIdcmd() {
        return idcmd;
    }

    public double getPrice() {
        return price;
    }
}
