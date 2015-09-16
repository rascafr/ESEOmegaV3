package fr.bde_eseo.eseomega.hintsntips;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rascafr.test.matdesignfragment.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.listeners.RecyclerItemClickListener;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.JSONUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

/**
 * Created by Rascafr on 11/08/2015.
 * Displays Hints And Tips from partners
 */
public class TipsFragment extends Fragment {

    // UI
    private ProgressBar progCircle;
    private ImageView img;
    private TextView tv1, tv2;
    private RecyclerView recList;
    private MyTipsAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private long timestamp;
    private RecyclerView.OnItemTouchListener disabler;

    // Model
    private ArrayList<SponsorItem> sponsorItems;

    // Constants
    private final static String JSON_KEY_ARRAY = "sponsors";
    private final static String JSON_KEY_ITEM_NAME = "nom";
    private final static String JSON_KEY_ITEM_DETAIL = "detail";
    private final static String JSON_KEY_ITEM_IMG = "img";
    private final static String JSON_KEY_ITEM_URL = "url";
    private final static String JSON_KEY_ITEM_ADR = "adr";
    private final static String JSON_KEY_ITEM_AVANTAGES = "avantages";
    private final static int LATENCY_REFRESH = 8; // 8 sec min between 2 refreshs

    private String cachePath;
    private File cacheFileEseo;

    public TipsFragment () {}

    @Override
    public View onCreateView(LayoutInflater rootInfl, ViewGroup container, Bundle savedInstanceState) {

        // UI
        View rootView = rootInfl.inflate(R.layout.fragment_tips_list, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.tips_refresh);
        swipeRefreshLayout.setColorSchemeColors(R.color.md_blue_800);
        progCircle = (ProgressBar) rootView.findViewById(R.id.progressList);
        tv1 = (TextView) rootView.findViewById(R.id.tvListNothing);
        tv2 = (TextView) rootView.findViewById(R.id.tvListNothing2);
        img = (ImageView) rootView.findViewById(R.id.imgNoSponsor);
        progCircle.setVisibility(View.GONE);
        tv1.setVisibility(View.GONE);
        tv2.setVisibility(View.GONE);
        img.setVisibility(View.GONE);
        disabler = new RecyclerViewDisabler();

        // I/O cache data
        cachePath = getActivity().getCacheDir() + "/";
        cacheFileEseo = new File(cachePath + "tips.json");

        // Model / objects
        sponsorItems = new ArrayList<>();
        mAdapter = new MyTipsAdapter(getActivity(), sponsorItems);
        recList = (RecyclerView) rootView.findViewById(R.id.recyList);
        recList.setAdapter(mAdapter);
        recList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        mAdapter.setSponsorItems(sponsorItems);

        //sponsorItems.add(new SponsorItem("A", "B", "C", "D", "E", null));
        mAdapter.notifyDataSetChanged();

        // Start download of data
        AsyncJSON asyncJSON = new AsyncJSON(true); // circle needed for first call
        asyncJSON.execute(Constants.URL_JSON_SPONSORS);

        // Swipe-to-refresh implementations
        timestamp = 0;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Toast.makeText(getActivity(), "Refreshing ...", Toast.LENGTH_SHORT).show();
                long t = System.currentTimeMillis() / 1000;
                if (t - timestamp > LATENCY_REFRESH) { // timestamp in seconds)
                    timestamp = t;
                    AsyncJSON asyncJSON = new AsyncJSON(false); // no circle here (already in SwipeLayout)
                    asyncJSON.execute(Constants.URL_JSON_SPONSORS);
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        // On click listener
        recList.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                SponsorItem si = sponsorItems.get(position);
                if (si.getUrl() != null && si.getUrl().length() > 0) {
                    String url = si.getUrl();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            }
        }));

        return rootView;
    }

    // Scroll listener to prevent issue 77846
    public class RecyclerViewDisabler implements RecyclerView.OnItemTouchListener {

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            return true;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    /**
     * Download JSON data
     */
    private class AsyncJSON extends AsyncTask<String, String, JSONObject> {

        private boolean displayCircle, onLine;

        public AsyncJSON (boolean displayCircle) {
            this.displayCircle = displayCircle;
        }

        @Override
        protected void onPreExecute() {
            recList.addOnItemTouchListener(disabler);
            if (sponsorItems != null) {
                sponsorItems.clear();
            }
            img.setVisibility(View.GONE);
            tv1.setVisibility(View.GONE);
            tv2.setVisibility(View.GONE);
            if (displayCircle) progCircle.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {

            if (jsonObject != null) {
                JSONArray array;
                try {
                    array = jsonObject.getJSONArray(JSON_KEY_ARRAY);

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        ArrayList<String> avantages = new ArrayList<>();
                        JSONArray avantagesJSON = obj.getJSONArray(JSON_KEY_ITEM_AVANTAGES);
                        for (int a = 0; a < avantagesJSON.length(); a++) {
                            avantages.add(avantagesJSON.getString(a));
                        }
                        sponsorItems.add(new SponsorItem(
                                obj.getString(JSON_KEY_ITEM_NAME),
                                obj.getString(JSON_KEY_ITEM_DETAIL),
                                obj.getString(JSON_KEY_ITEM_IMG),
                                obj.getString(JSON_KEY_ITEM_URL),
                                obj.getString(JSON_KEY_ITEM_ADR),
                                avantages
                        ));
                    }
                    if (displayCircle) progCircle.setVisibility(View.GONE);
                    mAdapter.setSponsorItems(sponsorItems);
                    mAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                if (displayCircle) progCircle.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
                img.setVisibility(View.VISIBLE);
                tv1.setVisibility(View.VISIBLE);
                tv2.setVisibility(View.VISIBLE);
            }
            swipeRefreshLayout.setRefreshing(false);
            recList.removeOnItemTouchListener(disabler);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject obj = null;
            onLine = Utilities.isPingOnline(getActivity());
            if (onLine) {
                obj = JSONUtils.getJSONFromUrl(params[0], getActivity());
                if (obj != null) {
                    Utilities.writeStringToFile(cacheFileEseo, obj.toString());
                }
            } else {
                if (cacheFileEseo.exists()) {
                    try {
                        obj = new JSONObject(Utilities.getStringFromFile(cacheFileEseo));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return obj;
        }
    }
}
