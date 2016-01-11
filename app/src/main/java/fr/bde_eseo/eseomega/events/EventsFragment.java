package fr.bde_eseo.eseomega.events;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import fr.bde_eseo.eseomega.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.events.tickets.EventItem;
import fr.bde_eseo.eseomega.events.tickets.model.TicketStore;
import fr.bde_eseo.eseomega.listeners.RecyclerItemClickListener;
import fr.bde_eseo.eseomega.utils.JSONUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

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
    private final static int LATENCY_REFRESH = 8; // 8 sec min between 2 refreshs

    private String cachePath;
    private File cacheFileEseo;

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
        progCircle.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.md_grey_500), PorterDuff.Mode.SRC_IN);
        tv1.setVisibility(View.GONE);
        tv2.setVisibility(View.GONE);
        img.setVisibility(View.GONE);
        disabler = new RecyclerViewDisabler();

        // I/O cache data
        cachePath = getActivity().getCacheDir() + "/";
        cacheFileEseo = new File(cachePath + "events.json");

        // Init static model
        TicketStore.getInstance().reset();

        // Model / objects
        eventItems = TicketStore.getInstance().getEventItems();
        mAdapter = new MyEventsAdapter(getActivity(), eventItems);
        recList = (RecyclerView) rootView.findViewById(R.id.recyList);
        recList.setAdapter(mAdapter);
        recList.setHasFixedSize(false);
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
                final EventItem ei = eventItems.get(position);
                if (!ei.isHeader()) {
                    boolean hasUrl = ei.getUrl() != null && ei.getUrl().length() != 0;
                    boolean hasPlace = ei.getLieu() != null && ei.getLieu().length() != 0;
                    boolean isAllDay = false;
                    MaterialDialog.Builder mdb = new MaterialDialog.Builder(getActivity())
                            .customView(R.layout.dialog_event, false)
                            .positiveText("Ajouter au calendrier");

                    if (hasUrl)
                        mdb.neutralText("Consulter le site");
                    if (hasPlace)
                        mdb.negativeText("Y aller");

                    mdb.callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNegative(MaterialDialog dialog) { // Intent : go to address
                            super.onNegative(dialog);
                            try {
                                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(ei.getLieu()));
                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                mapIntent.setPackage("com.google.android.apps.maps");
                                startActivity(mapIntent);
                            } catch (ActivityNotFoundException ex) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps"));
                                startActivity(intent);
                            }
                        }

                        @Override
                        public void onPositive(MaterialDialog dialog) { // Intent : add to calendar
                            super.onPositive(dialog);
                            startActivity(ei.toCalendarIntent());
                        }

                        @Override
                        public void onNeutral(MaterialDialog dialog) { // Intent : go to website
                            super.onNeutral(dialog);
                            String url = ei.getUrl();
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        }
                    });

                    MaterialDialog md = mdb.show();

                    View mdView = md.getView();
                    ((TextView)mdView.findViewById(R.id.tvEventName)).setText(ei.getName());
                    (mdView.findViewById(R.id.rlBackDialogEvent)).setBackgroundColor(ei.getColor());
                    if (ei.getDetails() != null && ei.getDetails().length() > 0) {
                        ((TextView)mdView.findViewById(R.id.tvEventDetails)).setText(ei.getDetails());
                    } else {
                        ((TextView)mdView.findViewById(R.id.tvEventDetails)).setText("Que souhaitez vous faire ?");
                    }
                    if (ei.getLieu() != null && ei.getLieu().length() > 0) {
                        ((TextView)mdView.findViewById(R.id.tvEventPlace)).setText("Lieu : " + ei.getLieu());
                        ((TextView)mdView.findViewById(R.id.tvEventPlace)).setVisibility(View.VISIBLE);
                    } else {
                        ((TextView)mdView.findViewById(R.id.tvEventPlace)).setVisibility(View.GONE);
                    }

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
                    int scrollTo = -1, counter = 0;

                    for (int i = 0; i < array.length(); i++) {

                        EventItem ei = new EventItem(array.getJSONObject(i));

                        if (ei.getDate().after(new Date()) && scrollTo == -1) {
                            scrollTo = counter;
                        }

                        if (ei.getMonthHeader().equals(lastHeader)) { // same month, no header
                            eventItems.add(ei);
                            counter++;
                        } else { // another month : add header then event
                            eventItems.add(new EventItem(ei.getMonthHeader()));
                            eventItems.add(ei);
                            counter+=2;
                            lastHeader = ei.getMonthHeader();
                        }
                    }
                    if (displayCircle) progCircle.setVisibility(View.GONE);
                    mAdapter.setEventItems(eventItems);
                    mAdapter.notifyDataSetChanged();
                    if (scrollTo >= 2 && recList.getAdapter().getItemViewType(scrollTo-2) == MyEventsAdapter.TYPE_HEADER) {
                        scrollTo-=2;

                    }
                    if (scrollTo == -1) {
                        scrollTo = eventItems.size()-1;
                    }
                    recList.scrollToPosition(scrollTo);

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

            JSONObject obj = JSONUtils.getJSONFromUrl(params[0], getActivity());

            if (obj == null) {
                if (cacheFileEseo.exists()) {
                    try {
                        obj = new JSONObject(Utilities.getStringFromFile(cacheFileEseo));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Utilities.writeStringToFile(cacheFileEseo, obj.toString());
            }
            return obj;
        }
    }
}

