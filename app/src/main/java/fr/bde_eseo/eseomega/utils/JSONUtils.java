package fr.bde_eseo.eseomega.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by Rascafr on 11/08/2015.
 */
public class JSONUtils {

    public static JSONObject getJSONFromUrl(String url, Context ctx) {

        String result = null;
        JSONObject obj = null;

        result = ConnexionUtils.postServerData(url, new HashMap<String, String>(), ctx);
        if (Utilities.isNetworkDataValid(result)) {
            try {
                obj = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return obj;
    }

    public static JSONArray getJSONArrayFromUrl(String url, Context ctx) {

        String result = null;
        JSONArray array = null;

        result = ConnexionUtils.postServerData(url, new HashMap<String, String>(), ctx);
        if (Utilities.isNetworkDataValid(result)) {
            try {
                array = new JSONArray(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        return array;
    }
}
