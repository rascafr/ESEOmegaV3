/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.bde_eseo.eseomega.gcmpush;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONException;
import org.json.JSONObject;

import fr.bde_eseo.eseomega.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    private SharedPreferences sharedPreferences;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d("DBG", "onHandleIntent");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            //Log.i(TAG, "Sender ID : " + getString(R.string.play_api_push));
            String token = instanceID.getToken(getString(R.string.play_api_push), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            //Log.i(TAG, "GCM Registration Token: " + token);

            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer(token, this);

            // Subscribe to topic channels
            subscribeTopics(token);

            Log.d("DBG", "try succeed");

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            //sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            // [END register_for_gcm]
        } catch (Exception e) {
            //Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            Log.d("DBG", "catch fail " + e);
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }

    }

    /**
     * Persist registration to third-party servers.
     *
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token, Context ctx) {
        // Add custom implementation, as needed.
        GcmPushToken asyncPushToken = new GcmPushToken(ctx, token);
        asyncPushToken.execute();
    }

    private class AsyncPushToken extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            return null;
        }
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS) {
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]

    /**
     * Send push token to server
     */
    private class GcmPushToken extends AsyncTask <String,String,String> {

        private Context ctx;
        private UserProfile profile;
        private String token;

        public GcmPushToken (Context ctx, String token) {
            this.ctx = ctx;
            this.token = token;
        }

        @Override
        protected void onPostExecute(String data) {

            Log.d("DBG", "onPostExecute : " + data);

            String err = "";
            int retCode = -1;

            if (Utilities.isNetworkDataValid(data)) {

                try {
                    JSONObject obj = new JSONObject(data);
                    retCode = obj.getInt("status");
                    err = obj.getString("cause");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (retCode == 1) {

                // Save token into profile
                profile.setPushToken(token);
                profile.registerProfileInPrefs(ctx);

                // Notify UI that registration has completed, so the progress indicator can be hidden.
                sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
            } else {
                sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
            }

            Intent registrationStatus = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
            LocalBroadcastManager.getInstance(RegistrationIntentService.this).sendBroadcast(registrationStatus);
        }

        @Override
        protected String doInBackground(String... params) {

            String pushResult = null;
            profile = new UserProfile();
            profile.readProfilePromPrefs(RegistrationIntentService.this);

            Log.d("DBG", "doInBackground, " + profile.isCreated() + ", " + token);

            if (profile.isCreated()) {
                HashMap<String, String> pairs = new HashMap<>();
                pairs.put(RegistrationIntentService.this.getResources().getString(R.string.client), profile.getId());
                pairs.put(RegistrationIntentService.this.getResources().getString(R.string.password), profile.getPassword());
                pairs.put(RegistrationIntentService.this.getResources().getString(R.string.os), Constants.APP_ID);
                pairs.put(RegistrationIntentService.this.getResources().getString(R.string.token), token);
                pairs.put(RegistrationIntentService.this.getResources().getString(R.string.hash), EncryptUtils.sha256(
                        getResources().getString(R.string.MESSAGE_SYNC_PUSH) +
                                profile.getId() +
                                profile.getPassword() +
                                Constants.APP_ID +
                                token));

                pushResult = ConnexionUtils.postServerData(Constants.URL_API_PUSH_REGISTER, pairs, RegistrationIntentService.this);
            }

            return pushResult;
        }
    }

}
