package fr.bde_eseo.eseomega.events.tickets.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rascafr on 13/01/2016.
 * Décris une navette
 */
public class CheckShuttleItem {

    private boolean isCheck;
    private ShuttleItem shuttleItem;

    public CheckShuttleItem(ShuttleItem shuttleItem) {
        this.shuttleItem = shuttleItem;
        isCheck = false;
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

    public void setShuttleItem(ShuttleItem shuttleItem) {
        this.shuttleItem = shuttleItem;
    }
}
