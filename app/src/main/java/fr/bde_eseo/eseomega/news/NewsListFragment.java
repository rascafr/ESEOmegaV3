package fr.bde_eseo.eseomega.news;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.rascafr.test.matdesignfragment.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.hintsntips.DividerItemDecoration;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.JSONUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

/**
 * Created by Rascafr on 25/08/2015.
 */
public class NewsListFragment extends Fragment {

    private ArrayList<NewsItem> newsItems;
    private MyNewsAdapter mAdapter;
    private RecyclerView recList;
    private ProgressBar progCircle;
    private ImageView img;
    private TextView tv1, tv2;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int ptr;
    private long timestamp;
    private RecyclerView.OnItemTouchListener disabler;
    private final static int LATENCY_REFRESH = 8; // 8 sec min between 2 refreshs

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_news_list, container, false);

        // View
        progCircle = (ProgressBar) rootView.findViewById(R.id.progressNews);
        tv1 = (TextView) rootView.findViewById(R.id.tvListNothing);
        tv2 = (TextView) rootView.findViewById(R.id.tvListNothing2);
        img = (ImageView) rootView.findViewById(R.id.imgNoNews);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.news_refresh);
        swipeRefreshLayout.setColorSchemeColors(R.color.md_blue_800);
        progCircle.setVisibility(View.GONE);
        tv1.setVisibility(View.GONE);
        tv2.setVisibility(View.GONE);
        img.setVisibility(View.GONE);

        // Model
        ptr = 0;
        newsItems = new ArrayList<>();
        mAdapter = new MyNewsAdapter(getActivity());
        recList = (RecyclerView) rootView.findViewById(R.id.recyList);
        recList.setAdapter(mAdapter);
        recList.setHasFixedSize(false);
        recList.addItemDecoration(new DividerItemDecoration(getActivity(), R.drawable.drawer_divider));
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        mAdapter.notifyDataSetChanged();
        disabler = new RecyclerViewDisabler();

        // Start download of data
        AsyncJSONNews asyncJSONNews = new AsyncJSONNews(true, false); // circle needed for first call, and clear data
        asyncJSONNews.execute(Constants.URL_NEWS_ANDROID + "height=5&ptr=0");

        // Swipe-to-refresh implementations
        timestamp = 0;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("NEWS", "Is online ? " + Utilities.isPingOnline(getActivity()));
                //Toast.makeText(getActivity(), "Refreshing ...", Toast.LENGTH_SHORT).show();
                long t = System.currentTimeMillis() / 1000;
                if (t - timestamp > LATENCY_REFRESH) { // timestamp in seconds)
                    timestamp = t;
                    ptr = 0;
                    AsyncJSONNews asyncJSONNews = new AsyncJSONNews(false, false); // no circle here (already in SwipeLayout)
                    asyncJSONNews.execute(Constants.URL_NEWS_ANDROID + "height=5&ptr=0");
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
     * Async task to download news from JSON server's file
     */
    private class AsyncJSONNews extends AsyncTask<String,String,JSONObject> {

        private boolean displayCircle;
        private boolean addMore;

        private AsyncJSONNews(boolean displayCircle, boolean addMore) {
            this.displayCircle = displayCircle;
            this.addMore = addMore;
        }

        @Override
        protected void onPreExecute() {
            recList.addOnItemTouchListener(disabler);
            if (newsItems != null) {
                if (!addMore)
                    newsItems.clear();
                else
                    newsItems.remove(newsItems.size()-1);
            }
            img.setVisibility(View.GONE);
            tv1.setVisibility(View.GONE);
            tv2.setVisibility(View.GONE);
            if (displayCircle) progCircle.setVisibility(View.VISIBLE);

        }

        @Override
        protected void onPostExecute(JSONObject obj) {
            if (obj != null) {
                try {
                    JSONArray array = obj.getJSONArray("articles");
                    //newsItems.clear();

                    for (int i=0;i<array.length();i++) {
                        NewsItem ni = new NewsItem(array.getJSONObject(i));
                        newsItems.add(ni);
                    }

                    if (array.length()>0) {
                        newsItems.add(new NewsItem());
                    } else if (array.length() == 0 && ptr > 0) {
                        ptr--;
                    }

                    if (displayCircle) progCircle.setVisibility(View.GONE);
                    mAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
            return JSONUtils.getJSONFromUrl(params[0], getActivity());
        }
    }



    public class MyNewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private DisplayImageOptions options;
        private Typeface mFont;
        private Context ctx;

        private final static int TYPE_NORMAL = 0;
        private final static int TYPE_FOOTER = 1;

        public MyNewsAdapter (Context ctx) {
            this.ctx = ctx;
            this.options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.solid_loading_background)
                    .showImageForEmptyUri(R.drawable.solid_loading_background)
                    .showImageOnFail(R.drawable.solid_loading_background)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            mFont = Typeface.createFromAsset(ctx.getAssets(),"fonts/Biko_Regular.otf");
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_NORMAL)
                return new NewsHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_news_flat_very, parent, false));
            else
                return new NewsFooterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_loadmore, parent, false));
        }

        @Override
        public int getItemViewType(int position) {
            return newsItems!=null?newsItems.get(position).isFooter()?TYPE_FOOTER:TYPE_NORMAL:0;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            NewsItem ni = newsItems.get(position);

            if (!ni.isFooter()) {
                NewsHolder nh = (NewsHolder) holder;
                //nh.tvName.setTypeface(mFont);
                nh.tvName.setText(ni.getName());
                nh.tvContent.setText(Html.fromHtml(ni.getShData()));
                nh.tvDateAuthor.setText("Par " + ni.getAuthor() + " | " + ni.getFrenchStr());
                ImageLoader.getInstance().displayImage(ni.getHeaderImg(), nh.imgHeader, options);
            } else {
                NewsFooterHolder nfh = (NewsFooterHolder) holder;
                nfh.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AsyncJSONNews asyncJSONNews = new AsyncJSONNews(false, true);
                        ptr++;
                        asyncJSONNews.execute(Constants.URL_NEWS_ANDROID + "height=5&ptr=" + ptr);
                        Toast.makeText(ctx, "Loading ...", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return newsItems.size();
        }

        private class NewsHolder extends RecyclerView.ViewHolder {

            protected TextView tvName, tvContent, tvDateAuthor;
            protected ImageView imgHeader;

            public NewsHolder(View itemView) {
                super(itemView);
                tvName = (TextView) itemView.findViewById(R.id.newsTitle);
                tvContent = (TextView) itemView.findViewById(R.id.newsDesc);
                imgHeader = (ImageView) itemView.findViewById(R.id.newsPicture);
                tvDateAuthor = (TextView) itemView.findViewById(R.id.newsAuthorDate);
            }
        }

        private class NewsFooterHolder extends RecyclerView.ViewHolder {

            protected CardView cardView;

            public NewsFooterHolder (View itemView) {
                super(itemView);
                cardView = (CardView) itemView.findViewById(R.id.card_view);
            }

        }
    }
}
