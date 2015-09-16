package fr.bde_eseo.eseomega.hintsntips;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.rascafr.test.matdesignfragment.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Rascafr on 11/08/2015.
 */
public class MyTipsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<SponsorItem> sponsorItems;
    private Context ctx;
    private DisplayImageOptions options;
    private float px;
    private Drawable drawable;
    private MyGetter myGetter;


    public MyTipsAdapter (Context ctx, ArrayList<SponsorItem> sponsorItems) {
        this.sponsorItems = sponsorItems;
        this.ctx = ctx;

        // Dp to Pixels : 15 -> ?
        px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, ctx.getResources().getDisplayMetrics());

        drawable = ctx.getResources().getDrawable(R.drawable.ic_green_avantage);
        drawable.setBounds(0,0,(int)px,(int)px);
        this.options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_loading)
                .showImageForEmptyUri(R.drawable.ic_loading)
                .showImageOnFail(R.drawable.ic_loading)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .bitmapConfig(Bitmap.Config.ALPHA_8)
                .build();
        myGetter = new MyGetter();
    }

    public void setSponsorItems(ArrayList<SponsorItem> sponsorItems) {
        this.sponsorItems = sponsorItems;
        notifyDataSetChanged();
    }

    @Override
    public SponsorTipsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SponsorTipsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_tips, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SponsorItem si = sponsorItems.get(position);

        SponsorTipsViewHolder svh = (SponsorTipsViewHolder) holder;
        svh.name.setText(si.getName());
        svh.details.setText(si.getDetail());

        if (si.getUrl() == null) {
            svh.url.setVisibility(View.GONE);
        } else {
            svh.url.setVisibility(View.VISIBLE);
            svh.url.setText(si.getUrl().replace("http://", ""));
        }

        if (si.getAvantages().size() == 0) {
            svh.offers.setVisibility(View.GONE);
        } else {
            svh.offers.setVisibility(View.VISIBLE);
            String tv = "";
            ArrayList<String> av = si.getAvantages();
            for (int i=0;i<av.size();i++) {
                tv += "<img src=\"check.png\">&nbsp; " + av.get(i);
                if (i!=av.size()-1)
                    tv += "<br>";
            }

            svh.offers.setText(Html.fromHtml(tv, myGetter, null));
        }

        ImageLoader.getInstance().displayImage(si.getImg(), svh.imageView, options);
    }

    private class MyGetter implements Html.ImageGetter {

        public MyGetter () {

        }

        @Override
        public Drawable getDrawable(String source) {
            if (source.equals("check.png"))
                return drawable;
            else
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return sponsorItems.size();
    }

    // Classic View Holder for Sponsor
    public class SponsorTipsViewHolder extends RecyclerView.ViewHolder {

        protected CircleImageView imageView;
        protected TextView name, details, adr, url, offers;

        public SponsorTipsViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.tvNameSponsor);
            details = (TextView) v.findViewById(R.id.tvDescSponsor);
            imageView = (CircleImageView) v.findViewById(R.id.circleSponsor);
            url = (TextView) v.findViewById(R.id.tvUrl);
            offers = (TextView) v.findViewById(R.id.tvOffers);
        }
    }
}
