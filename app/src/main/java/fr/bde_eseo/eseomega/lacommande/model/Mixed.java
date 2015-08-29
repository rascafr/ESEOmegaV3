package fr.bde_eseo.eseomega.lacommande.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rascafr on 17/08/2015.
 * Simple object for String + Int array
 */
public class Mixed {
    String id;
    int value;

    public Mixed(String id, int value) {
        this.id = id;
        this.value = value;
    }

    public Mixed(JSONObject obj) throws JSONException {
        this.id = obj.getString("id");
        this.value = obj.getInt("quantity");
    }

    @Override
    public String toString() {
        return "Mixed{" +
                "id='" + id + '\'' +
                ", value=" + value +
                '}';
    }
}
