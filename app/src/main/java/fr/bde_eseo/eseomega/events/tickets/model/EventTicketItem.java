package fr.bde_eseo.eseomega.events.tickets.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * Created by Rascafr on 11/01/2016.
 * Décrit un achat de place
 */
public class EventTicketItem {

    private String name, datetime, idevent, strcmd;
    private int idcmd, modcmd;
    private double price;

    public EventTicketItem(JSONObject obj) throws JSONException {
        idevent = obj.getString("idevent");
        datetime = obj.getString("datetime");
        idcmd = obj.getInt("idcmd");
        modcmd = obj.getInt("modcmd");
        strcmd = obj.getString("strcmd");
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

    public String getIdevent() {
        return idevent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTicketNumberAsString() {
        return strcmd + new DecimalFormat("000").format(modcmd);
    }
}
