package fr.bde_eseo.eseomega.ingenews;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rascafr on 22/12/2015.
 * Sur la bonne idée de Mr Naudet
 */
public class IngenewsItem {

    private int id;
    private long size;
    private String name, date, file, details;

    private final static long MBYTE = 1048576;
    private final static String MBYTE_STR = " Mio";
    private final static long KBYTE = 1024;
    private final static String KBYTE_STR = " Kio";
    private final static long BYTE = 1;
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
        this.details = date + " · " + getFormattedSize();
        Log.d("INGE", name + " , " + details);
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
            return (size / MBYTE) + MBYTE_STR;
        } else if (size >= KBYTE && size < MBYTE) {
            return (size / KBYTE) + KBYTE_STR;
        } else {
            return (size / BYTE) + BYTE_STR;
        }
    }

    public String getDetails() {
        return details;
    }
}
