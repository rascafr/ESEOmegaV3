package fr.bde_eseo.eseomega.lacommande;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.rascafr.test.matdesignfragment.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.adapter.MyHistoryAdapter;
import fr.bde_eseo.eseomega.listeners.RecyclerItemClickListener;
import fr.bde_eseo.eseomega.model.UserProfile;
import fr.bde_eseo.eseomega.lacommande.model.HistoryItem;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.JSONParser;
import fr.bde_eseo.eseomega.utils.Utilities;

/**
 * Created by François on 13/04/2015.
 * Permet d'afficher l'historique des commandes passées (synchronisée avec le serveur ESEOmega)
 * Implémente un FloatingActionButton (Lollipop Style) afin de permettre l'ajout d'une nouvelle commande.
 *
 * Si en ligne -> refresh des données depuis le serveur et enregistrement du JSON
 * Si hors ligne -> chargement des données depuis le JSON enregistré
 */
public class OrderListFragment extends Fragment {

    public OrderListFragment() {}

    private RecyclerView recList;
    private ProgressBar progressBar;
    private MyHistoryAdapter mAdapter;
    private ArrayList<HistoryItem> historyList;
    private UserProfile userProfile;
    private String userLogin;
    private Handler mHandler;
    private static final int RUN_UPDATE = 20000;
    private boolean run;
    private boolean firstDisplay = true;
    private File cacheHistoryJSON;
    private List<NameValuePair> params;

    private TextView tvNothing, tvNothing2;
    private ImageView imgNothing;

    @Override
    public void onResume() {
        Log.d("Hand", "Resuming update thread");
        run = true;
        mHandler.postDelayed(updateTimerThread, 0);
        progressBar.setVisibility(View.VISIBLE);
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d("Hand", "Stopping update thread");
        run = false;
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Find elements and attach listView / floating button
        View rootView = inflater.inflate(R.layout.fragment_cafet_history, container, false);

        // Get user's data
        userProfile = new UserProfile();
        userProfile.readProfilePromPrefs(getActivity());
        userLogin = userProfile.getId();

        // Search for the listView, then set its adapter
        mAdapter = new MyHistoryAdapter(getActivity());
        recList = (RecyclerView) rootView.findViewById(R.id.cardHistory);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressHistoryList);
        tvNothing = (TextView) rootView.findViewById(R.id.tvListNothing);
        tvNothing2 = (TextView) rootView.findViewById(R.id.tvListNothing2);
        imgNothing = (ImageView) rootView.findViewById(R.id.imgNoCommand);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.setAdapter(mAdapter);
        recList.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        tvNothing.setVisibility(View.GONE);
        tvNothing2.setVisibility(View.GONE);
        imgNothing.setVisibility(View.GONE);

        // Get file from cache directory
        String cachePath = getActivity().getCacheDir() + "/";
        cacheHistoryJSON = new File(cachePath + "history.json");

        // Create array and check online history
        historyList = new ArrayList<>();

        // Delay to update data
        run = true;
        mHandler = new android.os.Handler();
        //mHandler.postDelayed(updateTimerThread, 0);

        /*
        historyList.add(new HistoryItem("#041 Sandwich gros porc", HistoryItem.STATUS_NOPAID, 2.5));
        historyList.add(new HistoryItem("#014 Yaourt aux cannabis", HistoryItem.STATUS_PREPARING, 0.3));
        historyList.add(new HistoryItem("#074 Menu Maxi Best Of Deluxe", HistoryItem.STATUS_READY, 45.99));
        historyList.add(new HistoryItem("#085 Menu Délice Istambul", HistoryItem.STATUS_DONE, 5.50));
        historyList.add(new HistoryItem("#071 Côte de requin rouge", HistoryItem.STATUS_DONE, 9.50));
        historyList.add(new HistoryItem("#063 Menu très maigre (pain)", HistoryItem.STATUS_DONE, 3.50));
        historyList.add(new HistoryItem("#011 Poulet sauce basquaise", HistoryItem.STATUS_DONE, 7.50));
        historyList.add(new HistoryItem("#041 Menu Grosse Dalle\n - Sandwich Jambon Tomates Fromage\n - Yaourt Fraises\n - Verre Coca", HistoryItem.STATUS_DONE, 7.50));

        for (int i=0;i<15;i++) {
            historyList.add(new HistoryItem("Text hello " + i, i%3, 3.5+i*0.1));
        }*/
        mAdapter.setHistoryArray(historyList);

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.attachToRecyclerView(recList);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /** Prepare data **/
                long timestamp = System.currentTimeMillis() / 1000; // timestamp in seconds
                params = new ArrayList<>();
                params.add(new BasicNameValuePair("client", userLogin));
                params.add(new BasicNameValuePair("tstp", "" + timestamp));
                params.add(new BasicNameValuePair("os", "" + Constants.APP_ID));
                params.add(new BasicNameValuePair("hash", EncryptUtils.sha256(getActivity().getResources().getString(R.string.SALT_GET_TOKEN) + userLogin + timestamp + Constants.APP_ID)));

                /** Call async task **/
                SyncTimeToken syncTimeToken = new SyncTimeToken(getActivity());
                syncTimeToken.execute(Constants.URL_POST_TOKEN);

            }
        });

        recList.addOnItemTouchListener(
            new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {

                    if (Utilities.isPingOnline(getActivity())) {
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        OrderDetailsFragment fragment = new OrderDetailsFragment();
                        fragment.setIdcmd(historyList.get(position).getCommandNumber());
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.anim.slide_in, R.anim.slide_out, R.anim.slide_in, R.anim.slide_out)
                                .replace(R.id.frame_container, fragment, "FRAG_ORDER_DETAILS")
                                .addToBackStack("BACK")
                                .commit();
                    } else {
                        Toast.makeText(getActivity(), "Connexion au serveur impossible", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        ));

        return rootView;
    }


    /**
     * Asynctask to sync time and get token from server
     */
    class SyncTimeToken extends AsyncTask<String, String, String> {

        String content;
        Context context;

        public SyncTimeToken (Context context) {
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!Utilities.isPingOnline(getActivity())) {
                MaterialDialog progressDialog = new MaterialDialog.Builder(getActivity())
                        .title("Erreur réseau")
                        .content(Constants.ERROR_NETWORK)
                        .cancelable(false)
                        .negativeText("Fermer")
                        .show();
                SyncTimeToken.this.cancel(true);
            }
        }

        @Override
        protected String doInBackground(String... sUrl) {
            String s = ConnexionUtils.postServerData(Constants.URL_POST_TOKEN, params);
            return s;
        }

        @Override
        protected void onPostExecute(String data) {

            Log.d("DATA", "Receive " + data + " (" + data.length() + " bytes)");

            /** Check if response is token, or an error **/
            if (data == null || data.contains("-") || data.length() != 64) { // 64 : nb chars for a SHA256 value

                // Error
                int e = (data != null && data.contains("-")) ? data.charAt(1) - 0x30 : -1;
                String errorStr, errorCode;

                switch (e) {
                    case Constants.ERROR_TIMESTAMP:
                        errorStr = Constants.ERROR_TIMESTAMP_STR;
                        break;
                    case Constants.ERROR_SERVICE_OUT:
                        errorStr = Constants.ERROR_SERVICE_OUT_STR;
                        break;
                    case Constants.ERROR_USERREGISTER:
                        errorStr = Constants.ERROR_USERREGISTER_STR;
                        break;
                    case Constants.ERROR_UNPAID:
                        errorStr = Constants.ERROR_UNPAID_STR;
                        break;
                    case Constants.ERROR_APP_PB:
                        errorStr = Constants.ERROR_APP_PB_STR;
                        break;
                    case Constants.ERROR_USER_BAN:
                        String msg = "Inconnue", t;
                        if (data.contains("#")) {
                            t = data.substring(data.indexOf("#")+1);
                            if (t.length() > 0)
                                msg = t;
                        }
                        errorStr = Constants.ERROR_USER_BAN_STR + msg + ")";
                        break;
                    default:
                        errorStr = Constants.ERROR_UNKNOWN;
                        break;
                }

                MaterialDialog progressDialog = new MaterialDialog.Builder(context)
                                                .title("Erreur")
                                                .content(errorStr)
                                                .cancelable(false)
                                                .negativeText("Fermer")
                                                .show();

            } else {

                // Success !
                run = false;
                DataManager.getInstance().reset(); // reset data before writing in it
                DataManager.getInstance().setToken(data); // Sets the Token

                OrderTabsFragment fragment = new OrderTabsFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_in, R.anim.slide_out, R.anim.slide_in, R.anim.slide_out)
                        .replace(R.id.frame_container, fragment, Constants.TAG_FRAGMENT_ORDER_TABS)
                        .addToBackStack("BACK")
                        .commit();

            }
        }
    }

    /**
     * Background task to fetch data periodically from server
     */
    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            try {
                if (run) {
                    SyncHistory syncHistory = new SyncHistory(getActivity());
                    syncHistory.execute();
                }
            } catch (NullPointerException e) { // Stop handler if fragment disappears
                Log.d("Hand", "Handler stopped"); mHandler.removeCallbacks(updateTimerThread);
            }
        }
    };

    /**
     * Sync history, fetch data from server
     */
    // Async Task Class
    class SyncHistory extends AsyncTask<String, String, String> {

        private Context context;
        private boolean isOffline;
        private String sData;
        private List<NameValuePair> syncParam;

        public SyncHistory(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            syncParam = new ArrayList<>();
            run = false;
            if (firstDisplay) {
                progressBar.setVisibility(View.VISIBLE);
                firstDisplay = false;
            }

            if (Utilities.isPingOnline(getActivity())) {
                Log.d("Hand", "Updating data from server ...");
                syncParam.clear(); // in case of ...
                syncParam.add(new BasicNameValuePair("client", userLogin));
                sData = Constants.URL_SYNC_HISTORY;
                isOffline = false;
            } else {
                Log.d("Hand", "Updating data from cache file ...");
                sData = Utilities.getStringFromFile(cacheHistoryJSON);
                isOffline = true;
            }
        }

        @Override
        protected String doInBackground(String... args) {
            JSONParser jParser = new JSONParser();
            // Getting JSON from URL
            String json;

            if (isOffline) {
                json = sData; // data already contains JSON objects
            } else {
                json = ConnexionUtils.postServerData(sData, syncParam); // data contains only URL
            }
            return json;
        }

        @Override
        protected void onProgressUpdate(String... progress) {

        }

        // Once File is downloaded
        @Override
        protected void onPostExecute(String sJson) {

            if (sJson != null) {

                // Check / fflush data
                if (historyList == null)
                    historyList = new ArrayList<>();
                else
                    historyList.clear();

                try {

                    // Temporary's array
                    ArrayList<HistoryItem> tempErrorArray = new ArrayList<>();
                    ArrayList<HistoryItem> tempReadyArray = new ArrayList<>();
                    ArrayList<HistoryItem> tempPreparingArray = new ArrayList<>();
                    ArrayList<HistoryItem> tempDoneArray = new ArrayList<>();

                    // parse JSON
                    JSONObject json = new JSONObject(sJson);

                    // Get all history items
                    JSONArray jsData = new JSONArray(json.getString("history"));
                    for (int i = 0; i < jsData.length(); i++) {
                        JSONObject obj = jsData.getJSONObject(i);
                        String sDate = obj.getString("datetime");
                        HistoryItem hi = new HistoryItem(obj.getString("resume"), obj.getInt("status"),
                                obj.getDouble("price"), sDate, obj.getInt("idcmd"), obj.getInt("modcmd"), obj.getString("strcmd"));

                        switch (obj.getInt("status")) {
                            case HistoryItem.STATUS_NOPAID:
                                tempErrorArray.add(hi);
                                break;
                            case HistoryItem.STATUS_READY:
                                tempReadyArray.add(hi);
                                break;
                            case HistoryItem.STATUS_PREPARING:
                                tempPreparingArray.add(hi);
                                break;
                            case HistoryItem.STATUS_DONE:
                                tempDoneArray.add(hi);
                                break;
                        }

                        //tempArray.add(new HistoryItem(obj.getString("JSONdata"), obj.getInt("status"), obj.getDouble("price"), sDate.substring(0, sDate.indexOf(" "))));

                    }

                    // Add all data with headers if needed
                    int aSize = tempErrorArray.size();
                    if (aSize > 0) {
                        historyList.add(new HistoryItem("IMPAYÉE" + (aSize > 1 ? "S" : ""), false));
                    }
                    historyList.addAll(tempErrorArray);
                    if (aSize > 0) {
                        historyList.add(new HistoryItem("Merci de venir à la cafet' ou au BDE régler ce" + (aSize > 1 ? "s" : "tte") + " commande" + (aSize > 1 ? "s" : "") + " au plus vite, sinon contactez-nous.", true));
                    }

                    aSize = tempReadyArray.size();
                    if (aSize > 0) {
                        historyList.add(new HistoryItem("PRÊTE" + (aSize > 1 ? "S" : ""), false));
                    }
                    historyList.addAll(tempReadyArray);
                    if (aSize > 0) {
                        historyList.add(new HistoryItem("Merci de venir à la cafet' chercher votre repas.", true));
                    }

                    aSize = tempPreparingArray.size();
                    if (aSize > 0) {
                        historyList.add(new HistoryItem("EN PRÉPARATION", false));
                    }
                    historyList.addAll(tempPreparingArray);

                    aSize = tempDoneArray.size();
                    if (aSize > 0) {
                        historyList.add(new HistoryItem("TERMINÉE" + (aSize > 1 ? "S" : ""), false));
                    }
                    historyList.addAll(tempDoneArray);

                    if (progressBar.getVisibility() == View.VISIBLE)
                        progressBar.setVisibility(View.GONE);
                    if (recList.getVisibility() == View.GONE) recList.setVisibility(View.VISIBLE);

                    mAdapter.notifyDataSetChanged();

                    // display "no command" or not
                    if (jsData.length() == 0) {
                        tvNothing.setVisibility(View.VISIBLE);
                        tvNothing2.setVisibility(View.VISIBLE);
                        imgNothing.setVisibility(View.VISIBLE);
                        recList.setVisibility(View.GONE);
                    } else {
                        tvNothing.setVisibility(View.GONE);
                        tvNothing2.setVisibility(View.GONE);
                        imgNothing.setVisibility(View.GONE);
                    }

                    // save data
                    Utilities.writeStringToFile(cacheHistoryJSON, sJson);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            mHandler.postDelayed(updateTimerThread, RUN_UPDATE);
            run = true;
        }
    }
}
