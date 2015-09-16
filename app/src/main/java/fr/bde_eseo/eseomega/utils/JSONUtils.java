package fr.bde_eseo.eseomega.utils;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Rascafr on 11/08/2015.
 */
public class JSONUtils {

    public static JSONObject getJSONFromUrl(String url, Context ctx) {

        String result = null;
        JSONObject obj = null;

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        if (Utilities.isPingOnline(ctx)) {

            try {
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                InputStream is = response.getEntity().getContent();

                InputStreamReader isr = new InputStreamReader(is, "utf8");
                BufferedReader reader = new BufferedReader(isr);
                //Log.d("InputStreamReader", "Charset encoding : " + isr.getEncoding());
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                is.close();
                result = sb.toString();

                //result = EntityUtils.toString(response.getEntity());
                if (result.contains("<html><body><h1>503"))
                    obj = null;
                else
                    obj = new JSONObject(result);

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        return obj;
    }

    public static JSONArray getJSONArrayFromUrl(String url, Context ctx) {

        String result = null;
        JSONArray array = null;

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        if (Utilities.isPingOnline(ctx)) {

            try {
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                InputStream is = response.getEntity().getContent();

                InputStreamReader isr = new InputStreamReader(is, "utf8");
                BufferedReader reader = new BufferedReader(isr);
                //Log.d("InputStreamReader", "Charset encoding : " + isr.getEncoding());
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                is.close();
                result = sb.toString();

                //result = EntityUtils.toString(response.getEntity());
                array = new JSONArray(result);

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

        return array;
    }
}
