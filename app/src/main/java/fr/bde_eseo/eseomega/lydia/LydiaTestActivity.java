package fr.bde_eseo.eseomega.lydia;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

/**
 * Created by Rascafr on 29/12/2015.
 */
public class LydiaTestActivity extends AppCompatActivity {

    private Context context;
    private EditText etPhone;
    private TextView tvConsole, tvOrder, tvRequest; // for logcat-less demo only
    private Button bpPay;
    private String clientPhone;
    private Toolbar toolbar;

    private double orderPrice = 0.0;
    private int orderID = -1;
    private UserProfile userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lydia_test);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setPadding(0, Utilities.getStatusBarHeight(this), 0, 0);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00263238")));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#550000ff")));

        // Get parameters
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            String action = intent.getAction();
            if (extras == null && action != null) {

                if (action.equals(Constants.EXTERNAL_INTENT_LYDIA_CAFET)) {
                    Log.d("Intent", "Got : " + intent.getData().getQuery());
                    String query = intent.getData().getQuery();
                    if (query.contains("id=")) {
                        orderID = Integer.parseInt(query.substring(query.indexOf("id=") + 3, query.length()));
                    }
                }

                if (orderID == -1) {
                    Toast.makeText(LydiaTestActivity.this, "Erreur de l'application (c'est pas normal)", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                orderID = extras.getInt(Constants.KEY_LYDIA_ORDER_ID);
                orderPrice = extras.getDouble(Constants.KEY_LYDIA_ORDER_PRICE);
                getSupportActionBar().setTitle("Lydia Test");
            }
        }

        // Get current fragment's context
        context = LydiaTestActivity.this;

        // Get layout objects
        etPhone = (EditText) findViewById(R.id.etLydiaPhone);
        bpPay = (Button) findViewById(R.id.bpLydiaPay);
        tvConsole = (TextView) findViewById(R.id.tvConsole);
        tvRequest = (TextView) findViewById(R.id.tvLydiaRequest);
        tvOrder = (TextView) findViewById(R.id.tvLydiaOrder);

        // Set layout values
        bpPay.setText("Payer " + orderPrice + "€");
        tvConsole.setText("");
        tvRequest.setText("request_id : ---");
        tvOrder.setText("order_id : " + orderID);

        // Get current profile
        userProfile = new UserProfile();
        userProfile.readProfilePromPrefs(context);

        // On button click listener
        bpPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clientPhone = etPhone.getText().toString();
                tvConsole.setText("");

                // Send request to server
                AsyncRequestLydia asyncRequestLydia = new AsyncRequestLydia();
                asyncRequestLydia.execute();
            }
        });
    }

    /**
     * Class to create request to server (Lydia - ESEOmega)
     */
    private class AsyncRequestLydia extends AsyncTask<String,String,String> {

        private MaterialDialog materialDialog;

        @Override
        protected String doInBackground(String... params) {
            String resp = null;
            try {
                String b64phone = Base64.encodeToString(clientPhone.getBytes("UTF-8"), Base64.NO_WRAP);
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
            materialDialog = new MaterialDialog.Builder(context)
                    .title("Opération en cours")
                    .content("Veuillez patienter ...")
                    .cancelable(false)
                    .progressIndeterminateStyle(false)
                    .progress(true, 0, false)
                    .show();
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            materialDialog.hide();
            Log.d("ODR", "Got from Lydia : " + data);
            tvConsole.setText("Got from Lydia : " + data);

            // Check data
            if (Utilities.isNetworkDataValid(data)) {

                try {
                    // Get object
                    JSONObject obj = new JSONObject(data);

                    // Get error-status
                    if (obj.getInt("status") == 1) {

                        // Get shared data
                        JSONObject sharedData = obj.getJSONObject("data");

                        // Get Lydia pay values
                        Toast.makeText(context, "Référence : " + sharedData.getString("order_ref"), Toast.LENGTH_SHORT).show();
                        String mobileUrl = sharedData.getString("lydia_url");
                        String requestID = sharedData.getString("request_id");

                        // Set layout element's values
                        tvRequest.setText("request_id : " + requestID);

                        // Configure and make Lydia Intent
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse("lydiahomologation://pendinglist?request_id=" + requestID));
                        startActivity(i);

                        Toast.makeText(context, "Lydia package : " + isPackageExisted(context, "com.lydia") + ", request : lydia://pendinglist?request_id=" + requestID, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    // Package exists ?
    public boolean isPackageExisted(Context context, String targetPackage){
        List<ApplicationInfo> packages;
        PackageManager pm;

        pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if(packageInfo.packageName.equals(targetPackage))
                return true;
        }
        return false;
    }
}
