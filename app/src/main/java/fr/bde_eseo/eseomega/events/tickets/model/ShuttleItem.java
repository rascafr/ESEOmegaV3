package fr.bde_eseo.eseomega.events.tickets.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rascafr on 13/01/2016.
 * Décris une navette
 */
public class ShuttleItem {

    private int idshuttle, remainingSeats, totalSeats;
    private String departure, arrival, idevent, departPlace;

    public ShuttleItem(JSONObject obj) throws JSONException {
        idshuttle = obj.getInt("idshuttle");
        remainingSeats = obj.getInt("restseats");
        totalSeats = obj.getInt("totseats");
        departure = obj.getString("departure");
        arrival = obj.getString("arrival");
        idevent = obj.getString("idevent");
        departPlace = obj.getString("departplace");
    }

    public boolean correspondsToID(String id) {
        return idevent.equals(id);
    }

    public int getIdshuttle() {
        return idshuttle;
    }

    public int getRemainingSeats() {
        return remainingSeats;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public String getDeparture() {
        return departure;
    }

    public String getArrival() {
        return arrival;
    }

    public String getIdevent() {
        return idevent;
    }

    public String getDepartPlace() {
        return departPlace;
    }
}
