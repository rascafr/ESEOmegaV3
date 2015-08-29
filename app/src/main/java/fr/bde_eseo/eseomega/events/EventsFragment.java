package fr.bde_eseo.eseomega.events;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.rascafr.test.matdesignfragment.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.hintsntips.DividerItemDecoration;
import fr.bde_eseo.eseomega.hintsntips.MyTipsAdapter;
import fr.bde_eseo.eseomega.hintsntips.SponsorItem;
import fr.bde_eseo.eseomega.listeners.RecyclerItemClickListener;
import fr.bde_eseo.eseomega.utils.JSONUtils;

/**
 * Created by Rascafr on 14/08/2015.
 * Displays Events
 */
public class EventsFragment extends Fragment {

    // UI
    private ProgressBar progCircle;
    private ImageView img;
    private TextView tv1, tv2;
    private RecyclerView recList;
    private MyEventsAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private long timestamp;
    private RecyclerView.OnItemTouchListener disabler;

    // Model
    private ArrayList<EventItem> eventItems;

    // Constants
    private final static String JSON_KEY_ARRAY = "events";
    private final static String JSON_KEY_ITEM_NAME = "titre";
    private final static String JSON_KEY_ITEM_DETAIL = "detail";
    private final static String JSON_KEY_ITEM_DATE = "date";
    private final static String JSON_KEY_ITEM_CLUB = "club";
    private final static String JSON_KEY_ITEM_URL = "url";
    private final static String JSON_KEY_ITEM_LIEU = "lieu";
    private final static String JSON_KEY_ARRAY_COLOR = "color";
    private final static int LATENCY_REFRESH = 8; // 8 sec min between 2 refreshs

    public EventsFragment () {}

    @Override
    public View onCreateView(LayoutInflater rootInfl, ViewGroup container, Bundle savedInstanceState) {

        // UI
        View rootView = rootInfl.inflate(R.layout.fragment_event_list, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.events_refresh);
        swipeRefreshLayout.setColorSchemeColors(R.color.md_blue_800);
        progCircle = (ProgressBar) rootView.findViewById(R.id.progressEvent);
        tv1 = (TextView) rootView.findViewById(R.id.tvListNothing);
        tv2 = (TextView) rootView.findViewById(R.id.tvListNothing2);
        img = (ImageView) rootView.findViewById(R.id.imgNoEvent);
        progCircle.setVisibility(View.GONE);
        tv1.setVisibility(View.GONE);
        tv2.setVisibility(View.GONE);
        img.setVisibility(View.GONE);
        disabler = new RecyclerViewDisabler();

        // Model / objects
        eventItems = new ArrayList<>();
        mAdapter = new MyEventsAdapter(getActivity(), eventItems);
        recList = (RecyclerView) rootView.findViewById(R.id.recyList);
        recList.setAdapter(mAdapter);
        recList.setHasFixedSize(false);
        recList.addItemDecoration(new DividerItemDecoration(getActivity(), R.drawable.drawer_divider));
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        mAdapter.setEventItems(eventItems);
        mAdapter.notifyDataSetChanged();

        // Start download of data
        AsyncJSON asyncJSON = new AsyncJSON(true); // circle needed for first call
        asyncJSON.execute(Constants.URL_JSON_EVENTS);

        // On click listener
        recList.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                EventItem ei = eventItems.get(position);
                if (!ei.isHeader()) {
                    boolean hasUrl = ei.getUrl() != null && ei.getUrl().length() != 0;
                    boolean hasPlace = ei.getLieu() != null && ei.getLieu().length() != 0;
                    MaterialDialog md = new MaterialDialog.Builder(getActivity())
                            .customView(R.layout.dialog_event, false)
                            .positiveText("Ajouter au calendrier")
                            .neutralText("Consulter le site")
                            .negativeText("Y aller")
                            .show();

                    View mdView = md.getView();
                    ((TextView)mdView.findViewById(R.id.tvEventName)).setText(ei.getName());
                    (mdView.findViewById(R.id.rlBackDialogEvent)).setBackgroundColor(ei.getColor());


                        /*
                        .title(ei.getName())
                        .theme(Theme.DARK)
                        //.contentColor(ei.getColor())
                        .backgroundColor(ei.getColor())
                        .negativeColor(0xffffffff)
                        .neutralColor(0xffffffff)
                        .positiveColor(0xffffffff)
                        .content("Que souhaitez vous faire ?")

                        .show();*/
                }

            }
        }));

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
                    asyncJSON.execute(Constants.URL_JSON_EVENTS);
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

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
    public class AsyncJSON extends AsyncTask<String, String, JSONObject> {

        boolean displayCircle;

        public AsyncJSON (boolean displayCircle) {
            this.displayCircle = displayCircle;
        }

        @Override
        protected void onPreExecute() {
            recList.addOnItemTouchListener(disabler);
            if (eventItems != null) {
                eventItems.clear();
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

                    String lastHeader = "---"; // undefined for the first iteration (no event before)

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        ArrayList<String> colors = new ArrayList<>();
                        JSONArray colorsJSON = obj.getJSONArray(JSON_KEY_ARRAY_COLOR);
                        for (int a = 0; a < colorsJSON.length(); a++) {
                            colors.add(colorsJSON.getInt(a)+""); // TODO pass integer directly without using string
                        }

                        EventItem ei = new EventItem(
                                obj.getString(JSON_KEY_ITEM_NAME),
                                obj.getString(JSON_KEY_ITEM_DETAIL),
                                obj.getString(JSON_KEY_ITEM_DATE),
                                colors);
                        ei.setAdditionnal(
                                obj.getString(JSON_KEY_ITEM_CLUB),
                                obj.getString(JSON_KEY_ITEM_URL),
                                obj.getString(JSON_KEY_ITEM_LIEU));
                        ei.performShortedDetails();

                        if (ei.getMonthHeader().equals(lastHeader)) // same month, no header
                            eventItems.add(ei);
                        else { // another month : add header then event
                            eventItems.add(new EventItem(ei.getMonthHeader()));
                            eventItems.add(ei);
                            lastHeader = ei.getMonthHeader();
                        }
                    }
                    if (displayCircle) progCircle.setVisibility(View.GONE);
                    mAdapter.setEventItems(eventItems);
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
            return JSONUtils.getJSONFromUrl(params[0], getActivity());
        }
    }
}

