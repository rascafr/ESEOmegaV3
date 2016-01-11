package fr.bde_eseo.eseomega.lacommande.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import fr.bde_eseo.eseomega.Constants;

/**
 * Created by Rascafr on 20/07/2015.
 */
public class DetailedItem extends HistoryItem {

    private String instructions, imgUrl;
    private int idlydia;
    private boolean paidbefore, lydiaEnabled;

    public DetailedItem(JSONObject obj, int idcmd) throws JSONException {

        super(
                obj.getString("resume"),
                obj.getInt("status"),
                obj.getDouble("price"),
                obj.getString("datetime"),
                idcmd,
                obj.getInt("modcmd"),
                obj.getString("strcmd"),
                true
        );

        instructions = obj.getString("instructions");
        imgUrl = obj.getString("imgurl");
        idlydia = obj.getInt("idlydia");
        paidbefore = obj.getInt("paidbefore") == 1;
        lydiaEnabled = obj.getBoolean("lydia_enabled");

    }

    public String getInstructions() {
        return instructions;
    }

    public String getImgUrl() {
        return Constants.URL_ASSETS + imgUrl;
    }

    public int getIdlydia() {
        return idlydia;
    }

    public boolean isPaidbefore() {
        return paidbefore;
    }

    public boolean isLydiaEnabled() {
        return lydiaEnabled;
    }
}
