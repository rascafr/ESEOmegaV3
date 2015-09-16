package fr.bde_eseo.eseomega.community;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Rascafr on 31/08/2015.
 */
public class ClubDataHolder {
    private static ClubDataHolder instance;

    private ClubDataHolder (){}

    public static ClubDataHolder getInstance() {
        if (instance == null)
            instance = new ClubDataHolder();
        return instance;
    }

    private ArrayList<ClubItem> clubs;

    public void reset() {
        if (clubs == null)
            clubs = new ArrayList<>();
        else
            clubs.clear();
    }

    public ArrayList<ClubItem> getClubs() {
        return clubs;
    }

    public void parseJSON (JSONObject obj) {
        try {
            JSONArray clubsarray = obj.getJSONArray("clubs");
            for (int i=0;i<clubsarray.length();i++)
                clubs.add(new ClubItem(clubsarray.getJSONObject(i)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
