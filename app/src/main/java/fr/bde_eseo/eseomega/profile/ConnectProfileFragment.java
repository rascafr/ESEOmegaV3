package fr.bde_eseo.eseomega.profile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.rascafr.test.matdesignfragment.R;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.gcmpush.QuickstartPreferences;
import fr.bde_eseo.eseomega.gcmpush.RegistrationIntentService;
import fr.bde_eseo.eseomega.interfaces.OnUserProfileChange;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

import com.rengwuxian.materialedittext.MaterialEditText;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Created by François on 13/04/2015.
 */
public class ConnectProfileFragment extends Fragment {

    public ConnectProfileFragment(){}

    private MaterialEditText etUserID, etUserPassword;
    private MaterialDialog mdProgress;
    private Button btValid;
    private String userID, userName, userPassword;
    private OnUserProfileChange mOnUserProfileChange;
    private String[] bullshitHint;
    private Random rand;

    // GCM
    private BroadcastReceiver mRegistrationBroadcastReceiver;

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

        // GCM Receiver
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    //mInformationTextView.setText(getString(R.string.gcm_send_message));
                    mdProgress.hide();
                    Toast.makeText(getActivity(), "OK TOKEN SENT !", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "ERROR TOKEN !", Toast.LENGTH_SHORT).show();
                }
            }
        };


        return rootView;
    }

    /**
     * Async task to login client
     */
    class AsyncLogin extends AsyncTask<String, String, String> {

        private Context ctx;
        private boolean errorsHasOccured = false, errorOmega = false, network = false;
        private String enPass;

        public AsyncLogin (Context ctx) {
            this.ctx = ctx;
        }

        @Override
        protected void onPreExecute() {
            userID = etUserID.getText().toString().toLowerCase(Locale.FRANCE).trim();
            etUserID.setText(userID);
            userPassword = etUserPassword.getText().toString();
            enPass = EncryptUtils.passBase64(userPassword);

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

            if (network) {
                String enPass = EncryptUtils.passBase64(userPassword);
                List<NameValuePair> pairs = new ArrayList<>();
                pairs.add(new BasicNameValuePair("username", userID));
                pairs.add(new BasicNameValuePair("password", enPass));
                pairs.add(new BasicNameValuePair("hash", EncryptUtils.sha256(userID + enPass + getActivity().getResources().getString(R.string.SALT_SYNC_USER))));

                return ConnexionUtils.postServerData(Constants.URL_LOGIN, pairs);
            } else {
                return "";
            }
        }

        // Once connexion is done
        @Override
        protected void onPostExecute(String result) {

            String res;

            if (network) {

                mdProgress.hide();

                if (result.length() > 2 && result.charAt(0) == '1') {

                    userName = result.substring(2);

                    UserProfile profile = new UserProfile(ctx, userName, userID, userPassword);

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

                    // Check Services, then start Registration class
                    if (Utilities.checkPlayServices(getActivity())) {
                        // Start IntentService to register this application with GCM.
                        Intent intent = new Intent(getActivity(), RegistrationIntentService.class);
                        getActivity().startService(intent);
                    }

                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                            .replace(R.id.frame_container, new ViewProfileFragment(), "FRAG_VIEW_PROFILE")
                            .commit();
                } else {
                    mdProgress.hide();
                    res = result.contains("-2")?"Mauvaise combinaison identifiant - mot de passe\n" +
                            "Veuillez vérifier vos informations, puis réessayer.":"Erreur inconnue : " + result + "\nImpossible de valider votre connexion sur nos serveur.\n";

                    MaterialDialog md = new MaterialDialog.Builder(ctx)
                            .title("Oups ...")
                            .content(res)
                            .negativeText("Fermer")
                            .iconRes(R.drawable.ic_dizzy)
                            .show();
                }
            }
        }
    }
}
