package fr.bde_eseo.eseomega.events.tickets;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

/**
 * Sends the email adress to server
 */
public class AsyncEventEmail extends AsyncTask<String, String, String> {

    private Context context;
    private MaterialDialog md;
    private String email, b64email;
    private UserProfile userProfile;
    private AppCompatActivity backActivity;
    private int idcmd;

    public AsyncEventEmail(Context context, String email, AppCompatActivity backActivity, UserProfile userProfile, int idcmd) {
        this.context = context;
        this.email = email;
        this.backActivity = backActivity;
        this.userProfile = userProfile;
        this.idcmd = idcmd;
        this.b64email = ""; // pre init, prevents from null.getBytes
        try {
            this.b64email = Base64.encodeToString(email.getBytes("UTF-8"), Base64.NO_WRAP);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        md = new MaterialDialog.Builder(context)
                .title("Envoi de l'email")
                .content("Veuillez patienter ...")
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .cancelable(false)
                .show();
    }

    @Override
    protected void onPostExecute(String data) {

        md.hide();
        int err = 0;
        String errMsg = "Erreur réseau";

        if (Utilities.isNetworkDataValid(data)) {
            try {
                JSONObject obj = new JSONObject(data);
                err = obj.getInt("status");
                errMsg = obj.getString("cause");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Email sent with success !
        if (err == 1) {
            Toast.makeText(context, "Votre place a été envoyée à " + email, Toast.LENGTH_SHORT).show();
            if (backActivity != null) backActivity.finish();
        } else {
            // Error, show message
            new MaterialDialog.Builder(context)
                    .title("Erreur")
                    .content(errMsg + (err == 0 ? "" : " (code : " + err + ")"))
                    .negativeText(R.string.dialog_close)
                    .positiveText(R.string.dialog_retry)
                    .cancelable(false)
                    .callback(new MaterialDialog.ButtonCallback() {

                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            AsyncEventEmail asyncEmail = new AsyncEventEmail(context, email, backActivity, userProfile, idcmd);
                            asyncEmail.execute();
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                            if (backActivity != null) backActivity.finish();
                        }
                    })
                    .show();
        }
    }

    @Override
    protected String doInBackground(String... sData) {
        HashMap<String, String> params = new HashMap<>();
        params.put(context.getResources().getString(R.string.client), userProfile.getId());
        params.put(context.getResources().getString(R.string.password), userProfile.getPassword());
        params.put(context.getResources().getString(R.string.idcmd), String.valueOf(idcmd));
        params.put(context.getResources().getString(R.string.email), b64email);
        params.put(context.getResources().getString(R.string.hash), EncryptUtils.sha256(context.getResources().getString(R.string.MESSAGE_SEND_EMAIL_EVENT) + userProfile.getId() + userProfile.getPassword() + String.valueOf(idcmd) + b64email));
        return ConnexionUtils.postServerData(Constants.URL_API_EVENT_MAIL, params, context);
    }
}