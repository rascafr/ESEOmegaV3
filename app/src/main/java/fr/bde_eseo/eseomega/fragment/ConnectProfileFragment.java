package fr.bde_eseo.eseomega.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rascafr.test.matdesignfragment.R;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.interfaces.OnUserProfileChange;
import fr.bde_eseo.eseomega.model.UserProfile;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

import com.rengwuxian.materialedittext.MaterialEditText;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by François on 13/04/2015.
 */
public class ConnectProfileFragment extends Fragment {

    public ConnectProfileFragment(){}

    private MaterialEditText etUserID, etUserPassword;
    private Button btValid;
    private String userID, userName, userPassword;
    private OnUserProfileChange mOnUserProfileChange;
    private String[] bullshitHint;
    private Random rand;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mOnUserProfileChange = (OnUserProfileChange) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Find layout elements
        View rootView = inflater.inflate(R.layout.fragment_connect_profile, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        btValid = (Button) rootView.findViewById(R.id.button_disconnect);
        etUserID = (MaterialEditText) rootView.findViewById(R.id.etUserID);
        etUserPassword = (MaterialEditText) rootView.findViewById(R.id.etUserPassword);
        //Utilities.hideSoftKeyboard(getActivity()); // UI's better with that

        rand = new Random();

        // Bullshit
        bullshitHint = getActivity().getResources().getStringArray(R.array.bullshitHintUser);
        etUserID.setHint(bullshitHint[rand.nextInt(bullshitHint.length)]);

        // Listener on validation button
        btValid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etUserID.getText().toString().length() < 1) {
                    etUserID.setError("Vous ne savez pas quoi mettre ?");
                } else if (etUserPassword.getText().toString().length() < 1) {
                    etUserPassword.setError("Un trou de mémoire ? Mmh ...");
                } else {
                    AsyncLogin asyncLogin = new AsyncLogin(getActivity());
                    asyncLogin.execute(Constants.URL_CAMPUS_LOGIN);
                }
                etUserID.setHint(bullshitHint[rand.nextInt(bullshitHint.length)]);

            }
        });

        return rootView;
    }

    /**
     * Async task to login client
     */
    class AsyncLogin extends AsyncTask<String, String, String> {

        private Context ctx;
        private MaterialDialog mdProgress;
        private boolean errorsHasOccured = false, errorOmega = false, network = false;

        public AsyncLogin (Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected void onPreExecute() {
            userID = etUserID.getText().toString();
            userPassword = etUserPassword.getText().toString();

            if (Utilities.isPingOnline(ctx)) {
                network = true;
                mdProgress = new MaterialDialog.Builder(ctx)
                        .title("Veuillez patienter ...")
                        .content("Connexion au serveur ESEO")
                        .negativeText("Annuler")
                        .cancelable(false)
                        .progress(true, 4, false)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                AsyncLogin.this.cancel(true);
                            }
                        })
                        .show();
            } else {
                mdProgress = new MaterialDialog.Builder(ctx)
                        .title("Oups ...")
                        .content("Impossible d'accéder au réseau. Veuillez vérifier votre connexion, puis réessayer.")
                        .negativeText("Fermer")
                        .cancelable(false)
                        .iconRes(R.drawable.ic_facepalm)
                        .limitIconToDefaultSize()
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                AsyncLogin.this.cancel(true);
                            }
                        })
                        .show();
            }


        }

        @Override
        protected void onProgressUpdate(String... progress) {
            //Log.d("PROGRESS", "Called");
            //mdProgress.incrementProgress(1);
        }

        @Override
        protected String doInBackground(String... urls) {

            String result = "";

            if (network) {

                // Create a local instance of cookie store
                CookieStore cookieStore = new BasicCookieStore();

                // Create local HTTP context
                HttpContext localContext = new BasicHttpContext();
                // Create our client
                HttpClient httpclient = new DefaultHttpClient();
                // Bind custom cookie store to the local context
                localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

                HttpPost httpPost = new HttpPost(urls[0]);

                // Request parameters and other properties.
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", userID));
                params.add(new BasicNameValuePair("password", userPassword));
                params.add(new BasicNameValuePair("rememberusername", "0"));
                publishProgress();
                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                    //Log.d("HTTPPOST", "executing request " + httpPost.getURI());

                    // Pass local context as a parameter
                    HttpResponse response = httpclient.execute(httpPost, localContext);
                    HttpEntity entity = response.getEntity();
                    publishProgress();

                    if (entity != null) {

                        //Log.d("HTTP", "Response [OK]");

                        // A Simple Response Read
                        InputStream instream = entity.getContent();
                        result = Utilities.convertStreamToString(instream);
                        // now you have the string representation of the HTML request
                        instream.close();

                        // now disconnect
                        String sCampus;
                        int id = result.indexOf("http://campus.eseo.fr/login/logout.php?sesskey=");
                        int ed = result.indexOf("\"", id);

                        publishProgress();

                        if (id != -1 && ed != -1) {
                            sCampus = result.substring(id, ed);
                            //Log.d("HTTP", "Sessionkey : " + sCampus);
                            HttpGet httpGet = new HttpGet(sCampus);
                            httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
                            httpclient.execute(httpGet, localContext);
                            //Log.d("HTTP", "Disconnected [OK]");

                            // Campus's steps -> ok, connect to ESEOmega's server now
                            int ind = result.indexOf("title=\"Consulter le profil\">");
                            int end = result.indexOf("</a>", ind);
                            if (ind != -1 && end != -1 || end - ind > 15) { // leparofr, kadeltim, naudettho -> 8 char max ...
                                //Log.d("HTTP", "Username [OK]");
                                userName = result.substring(ind + 28, end);

                                // Server : POST data
                                String encoded = URLEncoder.encode(userName, "utf-8");
                                List<NameValuePair> pairs = new ArrayList<>();
                                pairs.add(new BasicNameValuePair("username", userID));
                                pairs.add(new BasicNameValuePair("fullname", userName));
                                pairs.add(new BasicNameValuePair("hash", EncryptUtils.sha256(getActivity().getResources().getString(R.string.SALT_SYNC_USER) + userID + userName)));
                                String omegaResp = ConnexionUtils.postServerData(Constants.URL_END_LOGIN, pairs);

                                Log.d("PROFILE", "Connect values : " + pairs.get(0) + ", " + pairs.get(1)+", " + pairs.get(2));
                                Log.d("PROFILE", "Connect response Omega : " + omegaResp);

                                if (omegaResp != null && omegaResp.equals("42")) {
                                    // ok ! subscription will be done in onPostExecute
                                } else {
                                    errorsHasOccured = true;
                                    errorOmega = true;
                                }

                            } else {
                                errorsHasOccured = true;
                            }

                        } else {
                            errorsHasOccured = true;
                            //Log.d("HTTP", "Disconnected [ERROR]");
                            //Log.d("HTTPPOST", result.length() + " bytes of data : " + result);
                        }
                        publishProgress();
                    } else {
                        errorsHasOccured = true;
                        //Log.d("HTTP", "Response [ERROR]");
                    }

                } catch (IOException e) {
                    errorsHasOccured = true;
                    e.printStackTrace();
                }
            }


            return result;
        }

        // Once connexion is done
        @Override
        protected void onPostExecute(String result) {

            String res;

            if (network) {
                mdProgress.hide();

                if (errorsHasOccured) {
                    res = errorOmega?
                            "Impossible de valider votre connexion sur nos serveur. Si le problème persiste, contactez-nous.":
                            "Erreur de connexion. Veuillez vérifier vos informations, puis réessayer.";

                    MaterialDialog md = new MaterialDialog.Builder(ctx)
                            .title("Oups ...")
                            .content(res)
                            .negativeText("Fermer")
                            .iconRes(R.drawable.ic_dizzy)
                            .show();
                } else {

                    UserProfile profile = new UserProfile(userName, userID, "");

                    MaterialDialog md = new MaterialDialog.Builder(getActivity())
                            .title("Bienvenue, " + profile.getFirstName() + " !")
                            .negativeText("Fermer")
                            .content("Votre profil a été synchronisé, vous bénéficiez désormais de toutes les fonctionnalités.\nProfitez-en !")
                            .iconRes(R.drawable.ic_checked_user)
                            .show();

                    //profile.reverseName();

                    profile.guessEmailAddress();
                    profile.registerProfileInPrefs(getActivity());

                    mOnUserProfileChange.OnUserProfileChange(profile);

                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                            .replace(R.id.frame_container, new ViewProfileFragment(), "FRAG_VIEW_PROFILE")
                            .commit();
                }
            }
        }
    }
}
