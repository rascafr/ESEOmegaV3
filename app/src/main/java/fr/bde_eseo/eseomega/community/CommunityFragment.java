package fr.bde_eseo.eseomega.community;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.listeners.RecyclerItemClickListener;
import fr.bde_eseo.eseomega.utils.JSONUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

/**
 * Created by Rascafr on 31/08/2015.
 */
public class CommunityFragment extends Fragment {

    // UI
    private ProgressBar progCircle;
    private ImageView img;
    private TextView tv1, tv2;
    private RecyclerView recList;
    private MyCommunityAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private long timestamp;
    private RecyclerView.OnItemTouchListener disabler;
    private String cachePath;
    private File cacheFileEseo;

    // Model -> static

    // Constants
    private final static int LATENCY_REFRESH = 8; // 8 sec min between 2 refreshs

    public CommunityFragment () {}

    @Override
    public View onCreateView(LayoutInflater rootInfl, ViewGroup container, Bundle savedInstanceState) {

        // UI
        View rootView = rootInfl.inflate(R.layout.fragment_club_list, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.clubs_refresh);
        swipeRefreshLayout.setColorSchemeColors(R.color.md_blue_800);
        progCircle = (ProgressBar) rootView.findViewById(R.id.progressClubs);
        progCircle.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.md_grey_500), PorterDuff.Mode.SRC_IN);
        tv1 = (TextView) rootView.findViewById(R.id.tvListNothing);
        tv2 = (TextView) rootView.findViewById(R.id.tvListNothing2);
        img = (ImageView) rootView.findViewById(R.id.imgNoClub);
        progCircle.setVisibility(View.GONE);
        tv1.setVisibility(View.GONE);
        tv2.setVisibility(View.GONE);
        img.setVisibility(View.GONE);
        disabler = new RecyclerViewDisabler();

        // I/O cache data
        cachePath = getActivity().getCacheDir() + "/";
        cacheFileEseo = new File(cachePath + "community.json");

        // Model / objects
        mAdapter = new MyCommunityAdapter();
        recList = (RecyclerView) rootView.findViewById(R.id.recyList);
        recList.setAdapter(mAdapter);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        mAdapter.notifyDataSetChanged();

        // Start download of data
        AsyncJSON asyncJSON = new AsyncJSON(true); // circle needed for first call
        asyncJSON.execute(Constants.URL_JSON_CLUBS);

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
                    asyncJSON.execute(Constants.URL_JSON_CLUBS);
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        recList.addOnItemTouchListener(
            new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    // Start activity
                    Intent myIntent = new Intent(getActivity(), ClubViewActivity.class);
                    myIntent.putExtra(Constants.KEY_CLUB_VIEW, position);
                    startActivity(myIntent);
                }
            })
        );

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
            ClubDataHolder.getInstance().reset();
            img.setVisibility(View.GONE);
            tv1.setVisibility(View.GONE);
            tv2.setVisibility(View.GONE);
            if (displayCircle) progCircle.setVisibility(View.VISIBLE);

        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {

            if (jsonObject != null) {
                ClubDataHolder.getInstance().parseJSON(jsonObject);
                if (displayCircle) progCircle.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
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

    /**
     * Custom adapter to handle list item views
     */
    private class MyCommunityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private DisplayImageOptions options;

        public MyCommunityAdapter () {
            this.options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.solid_loading_background)
                    .showImageForEmptyUri(R.drawable.solid_loading_background)
                    .showImageOnFail(R.drawable.solid_loading_background)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .resetViewBeforeLoading(true)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ClubViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_community, parent, false));
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ClubItem ci = ClubDataHolder.getInstance().getClubs().get(position);
            ClubViewHolder cvh = (ClubViewHolder) holder;
            cvh.name.setText(ci.getName());
            cvh.desc.setText(ci.getDesc());
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                cvh.cardView.setPreventCornerOverlap(false);
            } else {
                cvh.cardView.setPreventCornerOverlap(true); // Only supported if Android Version is >= Lollipop
            }
            ImageLoader.getInstance().displayImage(ci.getImg(), cvh.img, options);
        }

        @Override
        public int getItemCount() {
            return ClubDataHolder.getInstance().getClubs().size();
        }

        // Classic View Holder for Club
        public class ClubViewHolder extends RecyclerView.ViewHolder {

            protected TextView name, desc;
            protected ImageView img;
            protected CardView cardView;

            public ClubViewHolder(View v) {
                super(v);
                name = (TextView) v.findViewById(R.id.tvClubName);
                desc = (TextView) v.findViewById(R.id.tvClubDetails);
                img = (ImageView) v.findViewById(R.id.imgClub);
                cardView = (CardView) v.findViewById(R.id.cardCommunity);
            }
        }

    }
}
