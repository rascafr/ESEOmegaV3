package fr.bde_eseo.eseomega.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.rascafr.test.matdesignfragment.R;
import fr.bde_eseo.eseomega.model.NewsItem;

import java.util.ArrayList;

/**
 * Created by François on 24/04/2015.
 */

public class MyNewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<NewsItem> newsList;
    private Context context;
    private DisplayImageOptions options;
    private Typeface mFont;

    public static final int TYPE_NEWS_ITEM = 0;
    public static final int TYPE_NEWS_HEADER = 1;
    private static final int TYPE_NUMBER = TYPE_NEWS_ITEM + TYPE_NEWS_HEADER + 1;
    private static final String TAG = "NewsAdapterDBG";

    public MyNewsAdapter(Context context) {
        this.newsList = new ArrayList<>();
        this.context = context;
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
        mFont = Typeface.createFromAsset(context.getAssets(),"fonts/ImperiumSerif.ttf");
    }

    public void addErrorMessage(String message) {
        newsList.add(new NewsItem(null, message, null, null, null));
        notifyDataSetChanged();
    }

    public void setNewsArray(ArrayList<NewsItem> newsList) {
        if (newsList != null) {
            this.newsList = newsList;
            notifyDataSetChanged();
        }
    }

    public NewsItem getItem(int position) {
        return newsList.get(position);
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        NewsItem ni = newsList.get(position);

        if (getItemViewType(position) == TYPE_NEWS_ITEM) { // News Item
            NewsViewHolder nvh = (NewsViewHolder) viewHolder;

            if (ni.getName() == null) { // Error message
                nvh.vTitle.setVisibility(View.GONE);
                nvh.vImg.setVisibility(View.GONE);
                nvh.vRl.setVisibility(View.GONE);
                nvh.vMore.setVisibility(View.GONE);
                nvh.vDesc.setText(ni.getDescription());
                Log.d(TAG, "Set error message : " + ni.getDescription());
            } else {
                nvh.vTitle.setVisibility(View.VISIBLE);
                nvh.vImg.setVisibility(View.VISIBLE);
                nvh.vRl.setVisibility(View.VISIBLE);
                nvh.vMore.setVisibility(View.VISIBLE);
                nvh.vTitle.setTypeface(Typeface.SERIF);
                nvh.vTitle.setText(ni.getName());
                nvh.vDesc.setText(Html.fromHtml(ni.getDescription())); // ok
                ImageLoader.getInstance().displayImage(ni.getImageLink(), nvh.vImg, this.options);
            }


        } else { // News Title / Category
            TitleViewHolder tvh = (TitleViewHolder) viewHolder;
            tvh.vTitle.setText(ni.getName());

            if (ni.isESEOmega())
                tvh.vCardView.setCardBackgroundColor(context.getResources().getColor(R.color.color_bde_eseomega));
            else
                tvh.vCardView.setCardBackgroundColor(context.getResources().getColor(R.color.color_bde_eldorado));
        }
    }

    @Override
    public int getItemViewType(int position) {
        NewsItem ni = newsList.get(position);
        return ni.isTitle() ? TYPE_NEWS_HEADER : TYPE_NEWS_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        switch (viewType) {
            case TYPE_NEWS_ITEM:
                return new NewsViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_basic_image, viewGroup, false));

            case TYPE_NEWS_HEADER:
                return new TitleViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_news_header, viewGroup, false));
        }

        return null;
    }

   // Classic View Holder for News
    public static class NewsViewHolder extends RecyclerView.ViewHolder {

        protected TextView vTitle;
        protected TextView vDesc;
        protected ImageView vImg;
        protected TextView vMore;
        protected RelativeLayout vRl;

        public NewsViewHolder(View v) {
            super(v);
            vTitle =  (TextView) v.findViewById(R.id.newsTitle);
            vDesc = (TextView)  v.findViewById(R.id.newsDesc);
            vImg = (ImageView) v.findViewById(R.id.newsPicture);
            vRl = (RelativeLayout) v.findViewById(R.id.rlNews);
            vMore = (TextView) v.findViewById(R.id.more);
        }
    }

    // Classic View Holder for News
    public static class TitleViewHolder extends RecyclerView.ViewHolder {

        protected TextView vTitle;
        protected CardView vCardView;

        public TitleViewHolder(View v) {
            super(v);
            vTitle =  (TextView) v.findViewById(R.id.newsHeader);
            vCardView = (CardView) v.findViewById(R.id.card_view);
        }
    }
}