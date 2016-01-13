package fr.bde_eseo.eseomega.events.tickets.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rascafr on 13/01/2016.
 * Décris une navette
 */
public class CheckShuttleItem {

    private boolean isCheck, isHeader;
    private String name;
    private ShuttleItem shuttleItem;

    public CheckShuttleItem(ShuttleItem shuttleItem) {
        this.shuttleItem = shuttleItem;
        isHeader = false;
        isCheck = false;
    }

    public CheckShuttleItem(String name) {
        this.name = name;
        isHeader = true;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setIsCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }

    public ShuttleItem getShuttleItem() {
        return shuttleItem;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public String getName() {
        return name;
    }

    public void setShuttleItem(ShuttleItem shuttleItem) {
        this.shuttleItem = shuttleItem;
    }
}
