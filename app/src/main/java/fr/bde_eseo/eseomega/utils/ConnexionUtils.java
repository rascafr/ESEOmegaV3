package fr.bde_eseo.eseomega.utils;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rascafr on 31/07/2015.
 * Use the class to connect to network (in AsyncTask)
 */
public class ConnexionUtils {

    private static final String LOG_KEY_ERROR = "ConnexionUtils.ERROR";

    // Send data with POST method, and returns the server's response
    // V2.0 : no more '\n' char
    public static String postServerData(String url, List<NameValuePair> nameValuePairs) {

        String result = null;

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        try {
            // Add your data
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

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
            result = sb.toString().replace("\n", ""); // Correction buffer \n;

        } catch (IOException e) {
            Log.i(LOG_KEY_ERROR, e.getMessage());
        }

        return result;
    }
}
