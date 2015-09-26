package fr.bde_eseo.eseomega.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import fr.bde_eseo.eseomega.MainActivity;

/**
 * Created by François on 18/04/2015.
 */
public class Utilities {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "Utilities";

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    // V2.0 : checks if network is reachable
    public static boolean isPingOnline(Context context) {

        if (context != null) {

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                Runtime runtime = Runtime.getRuntime();
                try {

                    Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
                    int exitValue = ipProcess.waitFor();
                    return (exitValue == 0);

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

                return false;
            } else
                return false;
        } return false;
    }

    public static String convertStreamToString(InputStream is) {
    /*
     * To convert the InputStream to String we use the BufferedReader.readLine()
     * method. We iterate until the BufferedReader return null which means
     * there's no more data to read. Each line will appended to a StringBuilder
     * and returned as String.
     */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static String getStringFromFile (File filePath)  {
        FileInputStream fin = null;
        String ret = "";
        try {
            fin = new FileInputStream(filePath);
            ret = convertStreamToString(fin);
            //Make sure you close all streams.
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static void writeStringToFile (File filePath, String data) {
        FileOutputStream stream = null;
        try {
            try {
                stream = new FileOutputStream(filePath);
                try {
                    try {
                        stream.write(data.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } finally {
                    stream.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Do not use : prefer isPingOnline which checks a real connexion
    @Deprecated
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static String getCalendarAsString () {
        Calendar c  = Calendar.getInstance(TimeZone.getTimeZone("Europe/Paris"), Locale.FRANCE);
        int day = c.get(Calendar.DATE), month = c.get(Calendar.MONTH) + 1, year = c.get(Calendar.YEAR);

        return ((day < 10) ? "0":"") + day + "/" + ((month < 10) ? "0":"") + month + "/" + year;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public static boolean checkPlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                activity.finish();
            }
            return false;
        }
        return true;
    }

}
