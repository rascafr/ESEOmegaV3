package fr.bde_eseo.eseomega.lydia;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

/**
 * Created by Rascafr on 29/12/2015.
 */
public class LydiaTestFragment extends Fragment {

    private Context context;
    private EditText etPhone;
    private TextView tvConsole; // for logcat-less demo only
    private Button bpPay;
    private String clientPhone;

    private double orderPrice = 0.0;
    private int orderID = 0;
    private UserProfile userProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Find elements and attach listView / floating button
        View rootView = inflater.inflate(R.layout.fragment_lydia_test, container, false);

        // Get current fragment's context
        context = getActivity();

        // Get layout objects
        etPhone = (EditText) rootView.findViewById(R.id.etLydiaPhone);
        bpPay = (Button) rootView.findViewById(R.id.bpLydiaPay);
        tvConsole = (TextView) rootView.findViewById(R.id.tvConsole);

        // Set layout values
        bpPay.setText("Payer " + orderPrice + "€");
        tvConsole.setText("");

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

        return rootView;
    }

    // External accessors :
    // Set price
    public void setOrderPrice(double orderPrice) {
        this.orderPrice = orderPrice;
    }

    // Set idcmd
    public void setOrderID(int orderID) {
        this.orderID = orderID;
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
                resp = ConnexionUtils.postServerData(Constants.URL_API_LYDIA_ASK, pairs, getActivity());
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

                        // Configure and make Lydia Intent
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse("lydia://pendinglist?request_id=" + requestID));
                        startActivity(i);

                        Toast.makeText(context, "Lydia package : " + isPackageExisted(getActivity(), "com.lydia") + ", request : lydia://request_id=" + requestID, Toast.LENGTH_SHORT).show();
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
