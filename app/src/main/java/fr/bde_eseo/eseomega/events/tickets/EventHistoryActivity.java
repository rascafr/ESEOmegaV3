package fr.bde_eseo.eseomega.events.tickets;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import fr.bde_eseo.eseomega.BuildConfig;
import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.events.tickets.model.EventTicketItem;
import fr.bde_eseo.eseomega.events.tickets.model.TicketStore;
import fr.bde_eseo.eseomega.lacommande.DataManager;
import fr.bde_eseo.eseomega.lacommande.OrderTabsFragment;
import fr.bde_eseo.eseomega.lacommande.model.HistoryItem;
import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

/**
 * Created by Rascafr on 11/01/2016.
 */
public class EventHistoryActivity extends AppCompatActivity {

    // Android objects
    private Context context;

    // Model
    private ArrayList<EventTicketItem> eventTicketItems;

    // User profile
    private UserProfile userProfile;

    // Adapter / recycler
    private MyEventTicketAdapter mAdapter;
    private RecyclerView recList;

    // Layout
    private ProgressBar progressLoad, progressToken;
    private TextView tvNothing, tvNothing2;
    private ImageView imgNothing;
    private View viewToken;
    private FloatingActionButton fab;

    // Autoupdate
    private static Handler mHandler;
    private static final int RUN_UPDATE = 8000;
    private static final int RUN_START = 100;
    private static boolean run, backgrounded = false;
    private static boolean firstDisplay = true;

    // Cache
    private File cacheTicketsJSON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set view / call parent
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_history);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;

        // Get profile
        // Get user's data
        userProfile = new UserProfile();
        userProfile.readProfilePromPrefs(context);

        // Layout
        progressLoad = (ProgressBar) findViewById(R.id.progressTicketList);
        progressToken = (ProgressBar) findViewById(R.id.progressLoading);
        progressLoad.setVisibility(View.GONE);
        progressLoad.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.md_grey_500), PorterDuff.Mode.SRC_IN);
        progressToken.setVisibility(View.INVISIBLE);
        progressToken.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.md_white_1000), PorterDuff.Mode.SRC_IN);
        tvNothing = (TextView) findViewById(R.id.tvListNothing);
        tvNothing2 = (TextView) findViewById(R.id.tvListNothing2);
        imgNothing = (ImageView) findViewById(R.id.imgNoCommand);
        tvNothing.setVisibility(View.GONE);
        tvNothing2.setVisibility(View.GONE);
        imgNothing.setVisibility(View.GONE);
        viewToken = findViewById(R.id.viewCircle);
        viewToken.setVisibility(View.INVISIBLE);

        // Get file from cache directory
        String cachePath = getCacheDir() + "/";
        cacheTicketsJSON = new File(cachePath + "tickets.json");

        // Init model → get it from TicketStore
        eventTicketItems = TicketStore.getInstance().getEventTicketItems();

        // Init adapter / recycler view
        mAdapter = new MyEventTicketAdapter(context);
        recList = (RecyclerView) findViewById(R.id.cardTickets);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.setAdapter(mAdapter);
        recList.setVisibility(View.GONE);

        // Attach floating action button
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(recList);

        // On click listener for token
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimeZone tz = Calendar.getInstance().getTimeZone();
                String tzStr = tz.getID();

                if (!userProfile.isCreated()) {
                    new MaterialDialog.Builder(context)
                            .title("Vous n'êtes pas connecté")
                            .content("Nous avons besoin de savoir qui vous êtes avant de pouvoir vous laisser commander.")
                            .negativeText("D'accord")
                            .cancelable(false)
                            .show();
                } else if (!tzStr.equalsIgnoreCase(Constants.TZ_ID_PARIS)) {
                    new MaterialDialog.Builder(context)
                            .title("Erreur")
                            .content("L'accès aux réservations ne peut se faire depuis un autre pays que la France.\nEnvoyez nous une carte postale !")
                            .negativeText("D'accord")
                            .cancelable(false)
                            .show();
                } else {
                    SyncToken syncToken = new SyncToken();
                    syncToken.execute();
                }
            }
        });

        // Change message
        if (userProfile.isCreated()) {
            tvNothing.setText(getResources().getString(R.string.empty_header_order));
            tvNothing2.setText(getResources().getString(R.string.empty_desc_order));
        } else {
            tvNothing.setText(getResources().getString(R.string.empty_header_noorder));
            tvNothing2.setText(getResources().getString(R.string.empty_desc_noorder));
            tvNothing.setVisibility(View.VISIBLE);
            tvNothing2.setVisibility(View.VISIBLE);
            imgNothing.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);
        }

        // Set data
        mAdapter.setTicketsItems(eventTicketItems);

        // Start update
        if (mHandler == null) {
            mHandler = new android.os.Handler();
            mHandler.postDelayed(updateTimerThread, RUN_START);
        } else {
            mHandler.removeCallbacks(updateTimerThread);
            mHandler.postDelayed(updateTimerThread, RUN_START);
        }

        // Delay to update data
        run = true;

    }


    /**
     * Menu : back button + arrow in toolbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_empty, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        firstDisplay = true;
        // Delay to update data
        run = true;

        if (progressToken != null) progressToken.setVisibility(View.INVISIBLE);
        if (fab != null) fab.setVisibility(View.VISIBLE);
        if (viewToken != null) viewToken.setVisibility(View.INVISIBLE);

        if (mHandler == null) {
            mHandler = new android.os.Handler();
            mHandler.postDelayed(updateTimerThread, RUN_START);
        } else {
            mHandler.removeCallbacks(updateTimerThread);
            mHandler.postDelayed(updateTimerThread, RUN_START);
        }
    }


    @Override
    public void onPause() {
        if( mHandler != null) {
            mHandler.removeCallbacks(updateTimerThread);
        }
        run = false;
        super.onPause();
    }


    /**
     * Background task to fetch data periodically from server
     */
    private Runnable updateTimerThread = new Runnable() {
        public void run() {

            try {
                if (run && userProfile.isCreated()) {
                    run = false;
                    SyncTickets syncTickets = new SyncTickets();
                    syncTickets.execute();
                }
            } catch (NullPointerException e) { // Stop handler if activity disappears
                mHandler.removeCallbacks(updateTimerThread);
                run = false;
            }
        }
    };


    /**
     * Sync history, fetch data from server
     */
    // Async Task Class
    class SyncTickets extends AsyncTask<String, String, String> {

        private HashMap<String, String> syncParam;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            syncParam = new HashMap<>();
            run = false;
            if (firstDisplay) {
                progressLoad.setVisibility(View.VISIBLE);
                recList.setVisibility(View.INVISIBLE);
                firstDisplay = false;
            }

            // Prepare param array
            syncParam.clear(); // in case of ...
            syncParam.put(context.getResources().getString(R.string.client), userProfile.getId());
            syncParam.put(context.getResources().getString(R.string.password), userProfile.getPassword());
            syncParam.put(context.getResources().getString(R.string.hash), EncryptUtils.sha256(context.getResources().getString(R.string.MESSAGE_TICKETS_USER) + userProfile.getId() + userProfile.getPassword()));
        }

        @Override
        protected String doInBackground(String... args) {

            // Prepare JSON String
            String jsonStr;

            // Try to fetch data from server
            jsonStr = ConnexionUtils.postServerData(Constants.URL_API_EVENT_LIST, syncParam, context);

            // If data is empty
            if (!Utilities.isNetworkDataValid(jsonStr)) {

                // Fetch data from cache history
                if (cacheTicketsJSON.exists()) {
                    jsonStr = Utilities.getStringFromFile(cacheTicketsJSON);
                } else {
                    jsonStr = null; // force empty message
                }

            } else {

                // Else, there is a server response : phone is online
                try {
                    JSONObject servJson = new JSONObject(jsonStr);

                    // Check if there are no errors
                    if (servJson.getInt("status") == 1) {

                        jsonStr = servJson.getString("data");

                    } else {
                        // bad password (-2) : display nothing
                        jsonStr = null;
                    }

                } catch (JSONException e) {

                    e.printStackTrace();

                    // other error : display nothing
                    jsonStr = null;
                }
            }

            if (jsonStr != null) {

                // Check / fflush data
                if (eventTicketItems == null)
                    eventTicketItems = new ArrayList<>();
                else
                    eventTicketItems.clear();

                // 1 if ok, -2 if not
                try {
                    // Temporary array
                    ArrayList<EventTicketItem> tempNextArray = new ArrayList<>();
                    ArrayList<EventTicketItem> tempDoneArray = new ArrayList<>();

                    // Parse JSON
                    JSONObject json = new JSONObject(jsonStr);

                    // Get all history items
                    JSONArray jsData = new JSONArray(json.getString("tickets"));
                    for (int i = 0; i < jsData.length(); i++) {
                        eventTicketItems.add(new EventTicketItem(jsData.getJSONObject(i)));
                    }

                    // Set names to ID → not saved into cache !
                    TicketStore.getInstance().autoTicketAttributes();

                    // Parse data / set dates
                    for (int i = 0; i < eventTicketItems.size(); i++) {
                        EventTicketItem eti = eventTicketItems.get(i);

                        if (eti.getLinkedDatefin().after(new Date())) {
                            eti.setPassed(false);
                            tempNextArray.add(eti);
                        } else {
                            eti.setPassed(true);
                            tempDoneArray.add(eti);
                        }
                    }

                    // Clear everything
                    eventTicketItems.clear();

                    // Add tickets to view
                    if (tempNextArray.size() > 0) {
                        eventTicketItems.add(new EventTicketItem("Événements à venir"));
                    }
                    eventTicketItems.addAll(tempNextArray);

                    if (tempDoneArray.size() > 0) {
                        eventTicketItems.add(new EventTicketItem("Événements passés"));
                    }
                    eventTicketItems.addAll(tempDoneArray);

                    // save data
                    Utilities.writeStringToFile(cacheTicketsJSON, jsonStr);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return jsonStr;
        }

        // Once File is downloaded
        @Override
        protected void onPostExecute(String sJson) {

            progressLoad.setVisibility(View.GONE);
            if (sJson != null) {

                mAdapter.notifyDataSetChanged();

                // display "no ticket" or not
                if (eventTicketItems.size() == 0) {
                    tvNothing.setVisibility(View.VISIBLE);
                    tvNothing2.setVisibility(View.VISIBLE);
                    imgNothing.setVisibility(View.VISIBLE);
                    recList.setVisibility(View.GONE);
                } else {
                    tvNothing.setVisibility(View.GONE);
                    tvNothing2.setVisibility(View.GONE);
                    imgNothing.setVisibility(View.GONE);
                    recList.setVisibility(View.VISIBLE);
                }

            } else {
                tvNothing.setVisibility(View.VISIBLE);
                tvNothing2.setVisibility(View.VISIBLE);
                imgNothing.setVisibility(View.VISIBLE);
                recList.setVisibility(View.GONE);
            }

            mHandler.postDelayed(updateTimerThread, RUN_UPDATE);
            run = true;
        }
    }


    /**
     * Asynctask to get token from server
     */
    class SyncToken extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressToken.setVisibility(View.VISIBLE);
            fab.setVisibility(View.INVISIBLE);
            viewToken.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... sUrl) {

            HashMap<String,String> params = new HashMap<>();
            params.put(context.getResources().getString(R.string.client), userProfile.getId());
            params.put(context.getResources().getString(R.string.password), userProfile.getPassword());
            params.put(context.getResources().getString(R.string.os), Constants.APP_ID);
            params.put(context.getResources().getString(R.string.version), BuildConfig.VERSION_NAME);
            params.put(context.getResources().getString(R.string.hash), EncryptUtils.sha256(context.getResources().getString(R.string.MESSAGE_GET_TOKEN_EVENT) + userProfile.getId() + userProfile.getPassword() + Constants.APP_ID));

            return ConnexionUtils.postServerData(Constants.URL_API_EVENT_PREPARE, params, context);
        }

        @Override
        protected void onPostExecute(String data) {

            String err = "Impossible de se connecter au réseau";
            int retCode = 0;
            String jsonToken = "";

            /** Check if response is token, or an error **/
            if (Utilities.isNetworkDataValid(data)) { // 64 : nb chars for a SHA256 value
                try {
                    JSONObject obj = new JSONObject(data);
                    retCode = obj.getInt("status");
                    err = obj.getString("cause");

                    if (retCode == 1) {
                        jsonToken = obj.getJSONObject("data").getString("token");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // Check service answer
            if (retCode == 1) {
                // Success !
                run = false;
                TicketStore.getInstance().resetOrder(); // reset data before writing in it
                TicketStore.getInstance().setToken(jsonToken); // Sets the Token
            } else {
                progressToken.setVisibility(View.INVISIBLE);
                fab.setVisibility(View.VISIBLE);
                viewToken.setVisibility(View.INVISIBLE);

                new MaterialDialog.Builder(context)
                        .title("Erreur")
                        .content(err)
                        .cancelable(false)
                        .negativeText("Fermer")
                        .show();
            }
        }
    }
}
