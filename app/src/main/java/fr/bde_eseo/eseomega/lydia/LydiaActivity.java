package fr.bde_eseo.eseomega.lydia;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Set;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

/**
 * Created by Rascafr on 05/01/2016.
 * Activité permettant de vérifier si, lors du retour de l'app Lydia, la commande a bien été passée
 * + également passer une demande de paiement + redir Lydia
 * V1.2
 * <p/>
 * Sous la forme d'un dialogue ? tests
 */
public class LydiaActivity extends AppCompatActivity {

    // TODO
    private static final String CAT_ORDER = "CAFET";

    // Android lifecycle objects
    private Context context;

    // Our dialog
    private MaterialDialog md;
    private MaterialDialog.Builder mdb;

    // Our dialog's elements
    private EditText etPhone;
    private CheckBox checkRememberPhone;
    private View mdView;
    private TextView tvStatus;
    private ProgressBar progressStatus;

    // Divers
    private UserProfile userProfile;
    private String clientPhone;
    private boolean hasBeenPaused = false;

    // Intent-from
    private double orderPrice = 0.0;
    private int orderID = -1;

    // Types de requêtes qui ont lieu lors du onCreate (première ouverture de l'app)
    private enum INTENT_REQUEST {
        FROM_LYDIA, // Via URL Scheme
        FROM_APP,   // Via Intent interne
        ERROR       // Aucun des deux précédents
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set view / call parent
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lydia);
        context = this;

        // Init intent flag
        INTENT_REQUEST intent_request = INTENT_REQUEST.ERROR;

        // Get intent parameters
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            String action = intent.getAction();

            if (extras == null && action != null) {

                // Check if intent's action is correct (obviously yes, but prevents Manifest modifications)
                if (action.equals(Intent.ACTION_VIEW)) {

                    // Intent depuis Lydia
                    intent_request = INTENT_REQUEST.FROM_LYDIA;

                    // Get all parameters
                    Uri qUri = intent.getData();

                    // Order ID
                    String sOrderID = qUri.getQueryParameter("id");
                    if (sOrderID != null) {
                        orderID = Integer.parseInt(sOrderID);
                    }
                }

            } else if (extras != null) {

                // Intent interne
                intent_request = INTENT_REQUEST.FROM_APP;
                orderID = extras.getInt(Constants.KEY_LYDIA_ORDER_ID);
                orderPrice = extras.getDouble(Constants.KEY_LYDIA_ORDER_PRICE);
            }
        }

        // No intent received
        if (intent_request == INTENT_REQUEST.ERROR || orderID == -1) {
            Toast.makeText(context, "Erreur de l'application (c'est pas normal)", Toast.LENGTH_SHORT).show();
            close();
        }

        // Get objects
        userProfile = new UserProfile();
        userProfile.readProfilePromPrefs(context);

        // Init dialog
        dialogInit();

        // Get intent type
        if (intent_request == INTENT_REQUEST.FROM_APP) {

            /**
             * FROM_APP : On doit demander le numéro de téléphone du client, puis lorsqu'il clique sur "Payer", effectuer une demande de paiement auprès de Lydia.
             * Il faut ensuite lancer l'Intent vers Lydia avec le bon numéro de requête.
             *
             * A afficher :
             * - InputText téléphone
             * - Checkbox mémorisation numéro
             * - Texte "légal" sur le numéro de téléphone
             * - Boutons Annuler / Valider → AsyncTask ask.php
             */
            dialogFromApp();

        } else {

            /**
             * FROM_LYDIA : L'activité vient d'être ouverte après que le client ait cliqué sur "Accepter" depuis l'app Lydia.
             * Dans 100% des cas, le retour à notre activité se fait si il a eu un paiement validé.
             * Cependant, on vérifiera le statut de la commande auprès de notre serveur malgré tout.
             *
             * A afficher :
             * - Texte "État de votre commande" → titre dialogue
             * - Texte status : actualisé toutes les 3 secondes → asyncTask
             * - Bouton Fermer si status différent de "en cours"
             */
            dialogFromLydia();
        }
    }

    /**
     * Initialise un dialogue avec une entrée texte téléphone
     * Les boutons Annuler / Payer permettent de fermer l'activité et d'effectuer une demande de paiement auprès de Lydia
     * → passage direct à l'AsyncTask si téléphone déjà renseigné → ok done
     */
    void dialogFromApp () {

        // If phone number already present, skip renseignements
        if (userProfile.verifyPhoneNumber(userProfile.getPhoneNumber())) {

            // Send request to server
            AsyncRequestLydia asyncRequestLydia = new AsyncRequestLydia();
            asyncRequestLydia.execute();

        } else {

            // Current title
            mdb.title("Paiement par Lydia");

            // Current content
            mdb.customView(R.layout.dialog_lydia, false);

            // Customize
            mdb.positiveText("Payer");
            mdb.negativeText("Annuler");
            mdb.cancelable(false);
            mdb.autoDismiss(false);

            // Events
            mdb.callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);

                    clientPhone = etPhone.getText().toString();

                    // Check if phone is correct
                    if (userProfile.verifyPhoneNumber(clientPhone)) {

                        // Save it to preferences if checkbox checked
                        if (checkRememberPhone.isChecked()) {
                            userProfile.setPhoneNumber(clientPhone);
                            userProfile.registerProfileInPrefs(context);
                        }

                        // Send request to server
                        AsyncRequestLydia asyncRequestLydia = new AsyncRequestLydia();
                        asyncRequestLydia.execute();

                    } else {
                        // Phone syntax is bad
                        new MaterialDialog.Builder(context)
                                .title("Numéro incorrect")
                                .content("Vous pouvez écrire votre téléphone sous la forme +33 ou 06")
                                .negativeText("Fermer")
                                .show();
                    }
                }

                @Override
                public void onNegative(MaterialDialog dialog) {
                    super.onNegative(dialog);
                    close();
                }
            });

            // Dialog view
            md = mdb.build();
            mdView = md.getCustomView();

            // Dialog's elements
            etPhone = (EditText) mdView.findViewById(R.id.etLydiaPhone);
            checkRememberPhone = (CheckBox) mdView.findViewById(R.id.checkRememberPhone);

            // Set values
            etPhone.setText(userProfile.getPhoneNumber());

            // Show dialog
            md.show();
        }
    }

    /**
     * Initialise un dialogue avec un texte affichant le statut de la commande
     */
    void dialogFromLydia () {

        // Current title
        mdb.title("État du paiement Lydia");

        // Current content
        mdb.customView(R.layout.dialog_lydia_check, false);

        // Customize
        mdb.negativeText("Fermer");
        mdb.cancelable(false);

        // Events
        mdb.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onNegative(MaterialDialog dialog) {
                super.onNegative(dialog);
                close();
            }
        });

        // Dialog view
        md = mdb.build();
        mdView = md.getCustomView();

        // Dialog's elements
        tvStatus = (TextView) mdView.findViewById(R.id.tvStatusLydia);
        progressStatus = (ProgressBar) mdView.findViewById(R.id.progressCheckLydia);
        progressStatus.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.md_blue_800), PorterDuff.Mode.SRC_IN);

        // Set values
        progressStatus.setVisibility(View.GONE);

        // Start asyncTask
        AsyncStatusLydia asyncStatusLydia = new AsyncStatusLydia();
        asyncStatusLydia.execute();

        // Show dialog
        md.show();

    }

    /**
     * Closes the dialog / activity
     */
    void close() {
        LydiaActivity.this.finish();
    }

    /**
     * Class to create request to server (Lydia - ESEOmega)
     */
    private class AsyncRequestLydia extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String resp = null;
            try {
                String b64phone = Base64.encodeToString(userProfile.getPhoneNumber().getBytes("UTF-8"), Base64.NO_WRAP);
                String strOrder = String.valueOf(orderID);
                HashMap<String, String> pairs = new HashMap<>();
                pairs.put("username", userProfile.getId());
                pairs.put("password", userProfile.getPassword());
                pairs.put("phone", b64phone);
                pairs.put("idcmd", strOrder);
                pairs.put("hash", EncryptUtils.sha256(userProfile.getId() + userProfile.getPassword() + b64phone + strOrder + "Paiement effectué !"));
                resp = ConnexionUtils.postServerData(Constants.URL_API_LYDIA_ASK, pairs, context);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return resp;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (md != null) {
                md.dismiss();
            }
            md = new MaterialDialog.Builder(context)
                    .title("Demande de paiement")
                    .content("Veuillez patienter ...")
                    .cancelable(false)
                    .progressIndeterminateStyle(false)
                    .progress(true, 0, false)
                    .show();
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            md.hide();
            int result = 0;
            String msg = "Erreur réseau / serveur";

            // Check data
            if (Utilities.isNetworkDataValid(data)) {

                try {
                    // Get object
                    JSONObject obj = new JSONObject(data);
                    result = obj.getInt("status");
                    msg = obj.getString("cause");

                    // Get error-status
                    if (result == 1) {

                        // Get shared data
                        JSONObject sharedData = obj.getJSONObject("data");

                        // Get Lydia pay values
                        String mobileUrl = sharedData.getString("lydia_url"); // IMPORTANT : Utiliser si app Lydia non installée !
                        String requestID = sharedData.getString("request_id");

                        // Configure and make Lydia Intent
                        String intentUri;
                        boolean closeAfter = false;

                        // Package Lydia exists ?
                        if (Utilities.isPackageExisted(context, "com.lydia")) {
                            intentUri = "lydiahomologation://pendinglist?request_id=" + requestID;
                            closeAfter = true;
                        } else {
                            intentUri = mobileUrl; // Package doesn't exists : open URL
                        }

                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(intentUri));
                        startActivity(i);
                        if (closeAfter) close(); // prevent app-resume with null orderID
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (result != 1) {
                md = new MaterialDialog.Builder(context)
                        .title("Erreur")
                        .content("Cause : " + msg + "\n(code : " + result + ")")
                        .negativeText("Fermer")
                        .show();
            }
        }
    }

    /**
     * If the user come back to the Activity without Intent :
     * It means that the payment has failed / was cancelled
     * We have to show a Dialog which checks order status from server and then show status to user
     * OR : → just show it failed
     * NO : → check from server TODO
     */
    @Override
    public void onResume() {
        super.onResume();

        if (hasBeenPaused) {
            hasBeenPaused = false;

            dialogInit();
            dialogFromLydia();
        }
    }

    /**
     * On pause : just save the app has been paused
     */
    @Override
    protected void onPause() {
        super.onPause();
        hasBeenPaused = true;
    }

    /**
     * AsyncTask inside check sub dialog (Lydia callback intent)
     * TODO update frequently
     */
    private class AsyncStatusLydia extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String strOrder = String.valueOf(orderID);
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("username", userProfile.getId());
            pairs.put("password", userProfile.getPassword());
            pairs.put("idcmd", strOrder);
            pairs.put("cat_order", CAT_ORDER);
            pairs.put("hash", EncryptUtils.sha256(userProfile.getId() + userProfile.getPassword() + strOrder + CAT_ORDER + "Paiement refusé par votre banque"));
            return ConnexionUtils.postServerData(Constants.URL_API_LYDIA_CHECK, pairs, context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressStatus.setVisibility(View.VISIBLE);
            updateTextStatus("Vérification ...", 0, true);
        }

        @Override
        protected void onPostExecute(final String data) {
            super.onPostExecute(data);

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    progressStatus.setVisibility(View.GONE);

                    int result = 0;
                    String msg = "Erreur réseau / serveur";

                    // Check data
                    if (Utilities.isNetworkDataValid(data)) {

                        try {
                            // Get object
                            JSONObject obj = new JSONObject(data);
                            result = obj.getInt("status");
                            msg = obj.getString("cause");

                            // Get error-status
                            if (result == 1) {

                                // Get shared data
                                JSONObject sharedData = obj.getJSONObject("data");

                                // Get Lydia order status
                                int status = sharedData.getInt("status");
                                String info = sharedData.getString("info");

                                // Ok ?
                                updateTextStatus(info, status, false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    if (result != 1) {
                        updateTextStatus("Erreur : " + msg, -1, false);
                    }
                }
            }, 2000);
        }
    }

    /**
     * Set TextView status text and color
     */
    void updateTextStatus (String text, int status, boolean isLoading) {

        tvStatus.setText(text);

        if (isLoading) {
            tvStatus.setTextColor(context.getResources().getColor(R.color.md_blue_800));
        } else {
            if (status == 0) {
                tvStatus.setTextColor(context.getResources().getColor(R.color.md_prim_dark_yellow));
            } else if (status == 1) {
                tvStatus.setTextColor(context.getResources().getColor(R.color.md_blue_800));
            } else if (status == 2) {
                tvStatus.setTextColor(context.getResources().getColor(R.color.circle_ready));
            } else {
                tvStatus.setTextColor(context.getResources().getColor(R.color.md_prim_dark_red));
            }
        }
    }

    /**
     * Init the dialog for the current session
     */
    void dialogInit () {
        mdb = new MaterialDialog.Builder(context);
        mdb.theme(Theme.LIGHT);
        mdb.titleColor(getResources().getColor(R.color.md_blue_800));
    }
}
