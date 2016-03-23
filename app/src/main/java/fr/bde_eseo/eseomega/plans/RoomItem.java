package fr.bde_eseo.eseomega.plans;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by root on 23/03/16.
 */
public class RoomItem {

    private String name, number, batiment, info, details;
    private int floor;
    private boolean isHeader;

    /*"nom":"Local Animâ€™",
        "num":"CS001",
        "bat":"C",
        "etage":-1,
        "info"*/

    public RoomItem (String headerName) {
        this.name = headerName;
        this.isHeader = true;
    }

    public RoomItem (JSONObject obj) throws JSONException {
        this.name = obj.getString("nom");
        this.number = obj.getString("num");
        this.batiment = obj.getString("bat");
        this.floor = obj.getInt("etage");
        this.info = obj.getString("info");
        this.isHeader = false;

        ArrayList<String> arrayDetails = new ArrayList<>();
        if (number.length() > 0) arrayDetails.add(number);
        if (batiment.length() > 0) arrayDetails.add("Bâtiment " + batiment);
        arrayDetails.add("Étage " + floor);
        if (info.length() > 0) arrayDetails.add(info);

        this.details = "";

        for (int i=0;i<arrayDetails.size();i++) {
            details += arrayDetails.get(i);
            if (i != arrayDetails.size() - 1) {
                details += " • ";
            }
        }
}

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getBatiment() {
        return batiment;
    }

    public String getInfo() {
        return info;
    }

    public int getFloor() {
        return floor;
    }

    public String getDetails() {
        return details;
    }

    public boolean isHeader() {
        return isHeader;
    }
}
