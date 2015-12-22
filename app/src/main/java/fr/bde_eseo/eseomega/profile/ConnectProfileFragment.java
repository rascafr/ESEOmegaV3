package fr.bde_eseo.eseomega.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.system.ErrnoException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import fr.bde_eseo.eseomega.R;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.gcmpush.RegistrationIntentService;
import fr.bde_eseo.eseomega.interfaces.OnUserProfileChange;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
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
    private UserProfile profile;

    // Function to set the dialog visibity from activity
    public void setPushRegistration (boolean isSent) {
        if (isSent) {
            if (mdProgress != null) mdProgress.hide();
            //Toast.makeText(getActivity(), "Enregistrement notifs ok !", Toast.LENGTH_SHORT).show();

            MaterialDialog md = new MaterialDialog.Builder(getActivity())
                    .title("Bienvenue, " + profile.getFirstName() + " !")
                    .negativeText("Fermer")
                    .content("Votre profil a été synchronisé, et les notifications ont été activées, vous bénéficiez désormais de toutes les fonctionnalités.\nProfitez-en !")
                    .iconRes(R.drawable.ic_checked_user)
                    .show();

        } else {
            if (mdProgress != null) mdProgress.hide();

            MaterialDialog md = new MaterialDialog.Builder(getActivity())
                    .title("Oups ...")
                    .content("Impossible d'enregistrer votre appareil sur nos serveur\n" +
                            "Vous êtes connectés, mais vous ne pourrez pas recevoir les notifications news et cafétéria.\nTentez de vous reconnecter, ou contactez nous.")
                    .negativeText("Fermer")
                    .iconRes(R.drawable.ic_dizzy)
                    .show();
        }

        mOnUserProfileChange.OnUserProfileChange(profile);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.frame_container, new ViewProfileFragment(), "FRAG_VIEW_PROFILE")
                .commit();
    }

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
                    asyncLogin.execute();
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
            /*
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
            }*/


        }

        @Override
        protected void onProgressUpdate(String... progress) {
            //Log.d("PROGRESS", "Called");
            //mdProgress.incrementProgress(1);
        }

        @Override
        protected String doInBackground(String... urls) {

            HashMap<String, String> pairs = new HashMap<>();
            pairs.put(getActivity().getResources().getString(R.string.username), userID);
            pairs.put(getActivity().getResources().getString(R.string.password), enPass);
            pairs.put(getActivity().getResources().getString(R.string.hash), EncryptUtils.sha256(userID + enPass + getActivity().getResources().getString(R.string.MEMORY_SYNC_USER)));

            if (Utilities.isOnline(getActivity())) {
                return ConnexionUtils.postServerData(Constants.URL_LOGIN, pairs, getActivity());
            } else {
                return null;
            }
        }

        // Once connexion is done
        @Override
        protected void onPostExecute(String result) {

            String res;

            Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();

            if (Utilities.isNetworkDataValid(result)) {

                //Log.d("RES", result);

                if (result.length() > 2 && result.charAt(0) == '1') {

                    userName = result.substring(2);

                    profile = new UserProfile(ctx, userName, userID, userPassword);
                    profile.guessEmailAddress();
                    profile.registerProfileInPrefs(getActivity());

                    // Check Services, then start Registration class
                    if (Utilities.checkPlayServices(getActivity())) {
                        mdProgress.setContent("Enregistrement de l'appareil");
                        // Start IntentService to register this application with GCM.
                        Intent intent = new Intent(getActivity(), RegistrationIntentService.class);
                        getActivity().startService(intent);
                    }

                } else {
                    mdProgress.dismiss();
                    res = result.contains("-2")?"Mauvaise combinaison identifiant - mot de passe\n" +
                            "Veuillez vérifier vos informations, puis réessayer.":"Erreur inconnue, impossible de valider votre connexion sur nos serveurs.\n";

                    mdProgress = new MaterialDialog.Builder(ctx)
                            .title("Oups ...")
                            .content(res)
                            .negativeText("Fermer")
                            .iconRes(R.drawable.ic_dizzy)
                            .show();
                }
            } else {
                mdProgress.dismiss();
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
    }
}
