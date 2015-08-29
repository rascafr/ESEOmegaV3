package fr.bde_eseo.eseomega.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rascafr.test.matdesignfragment.R;

import fr.bde_eseo.eseomega.model.Sponsor;

import java.io.IOException;
import java.util.List;

/**
 * Created by François on 24/04/2015.
 */

public class MySponsorAdapter extends RecyclerView.Adapter<MySponsorAdapter.SponsorViewHolder> {

    private List<Sponsor> sponsorList;
    private Context context;

    public MySponsorAdapter(List<Sponsor> sponsorList, Context context) {
        this.sponsorList = sponsorList;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return sponsorList.size();
    }

    @Override
    public void onBindViewHolder(SponsorViewHolder contactViewHolder, int i) {
        Sponsor sp = sponsorList.get(i);
        contactViewHolder.vName.setText(sp.getName());
        contactViewHolder.vName.setTypeface(Typeface.SERIF);

        if (sp.getAddress().length() < 1) {
            contactViewHolder.vAddr.setVisibility(View.GONE);
        } else {
            contactViewHolder.vAddr.setText(sp.getAddress());
            contactViewHolder.vAddr.setVisibility(View.VISIBLE);
        }
        contactViewHolder.vWebsite.setText(sp.getUrl());

        try {
            contactViewHolder.vImgPath.setImageDrawable(Drawable.createFromStream(context.getAssets().open("sponsors/" + sp.getImgPath() + ".jpg"), null));
        } catch (IOException e) {
            e.printStackTrace(); // debug
        }
    }

    @Override
    public SponsorViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.cardview_basic_image, viewGroup, false);

        return new SponsorViewHolder(itemView);
    }

    public static class SponsorViewHolder extends RecyclerView.ViewHolder {

        protected TextView vName;
        protected TextView vAddr;
        protected TextView vWebsite;
        protected ImageView vImgPath;

        public SponsorViewHolder(View v) {
            super(v);
            vName =  (TextView) v.findViewById(R.id.newsTitle);
            vAddr = (TextView)  v.findViewById(R.id.newsDesc);
            vWebsite = (TextView)  v.findViewById(R.id.more);
            vImgPath = (ImageView) v.findViewById(R.id.newsPicture);
        }
    }
}