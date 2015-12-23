package fr.bde_eseo.eseomega.ingenews;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Locale;

import fr.bde_eseo.eseomega.utils.SimplyDate;

/**
 * Created by Rascafr on 22/12/2015.
 * Sur la bonne idée de Mr Naudet
 */
public class IngenewsItem {

    private int id;
    private long size;
    private String name, date, file, details;

    private final static double MBYTE = 1048576.0;
    private final static String MBYTE_STR = " Mio";
    private final static double KBYTE = 1024.0;
    private final static String KBYTE_STR = " Kio";
    private final static double BYTE = 1.0;
    private final static String BYTE_STR = " octets";


    /**
     * Constructeur depuis un objet JSON (depuis le serveur / cache)
     */
    public IngenewsItem(JSONObject obj) throws JSONException {
        this.id = obj.getInt("id");
        this.name = obj.getString("name");
        this.date = obj.getString("date");
        this.file = obj.getString("file");
        this.size = obj.getLong("size");

        SimplyDate simplyDate = new SimplyDate(date);
        this.details = simplyDate.simplify() + " · " + getFormattedSize();
        //Log.d("INGE", name + " , " + details);
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getFile() {
        return file;
    }

    public String getFormattedSize() {
        if (size >= MBYTE) {
            return new DecimalFormat("0.0").format(size / MBYTE) + MBYTE_STR;
        } else if (size >= KBYTE && size < MBYTE) {
            return new DecimalFormat("0.0").format(size / KBYTE) + KBYTE_STR;
        } else {
            return new DecimalFormat("0.0").format(size / BYTE) + BYTE_STR;
        }
    }

    public String getDetails() {
        return details;
    }
}
