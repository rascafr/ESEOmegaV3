package fr.bde_eseo.eseomega.ingenews;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.events.EventItem;

/**
 * Created by Rascafr on 11/08/2015.
 */
public class MyIngeNewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<IngenewsItem> ingenewsItems;
    private Context ctx;

    public ArrayList<IngenewsItem> getIngenewsItems() {
        return ingenewsItems;
    }

    public void setIngenewsItems(ArrayList<IngenewsItem> ingenewsItems) {
        this.ingenewsItems = ingenewsItems;
    }

    public MyIngeNewsAdapter(Context ctx, ArrayList<IngenewsItem> ingenewsItems) {
        this.ingenewsItems = ingenewsItems;
        this.ctx = ctx;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new IngeNewsItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_ingenews, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        IngenewsItem ii = ingenewsItems.get(position);
        IngeNewsItemViewHolder inivh = (IngeNewsItemViewHolder) holder;
        inivh.name.setText(ii.getName());
        inivh.details.setText(ii.getDetails());
    }

    @Override
    public int getItemCount() {
        return ingenewsItems.size();
    }

    // Classic View Holder for Ingénews item
    public class IngeNewsItemViewHolder extends RecyclerView.ViewHolder {

        protected TextView name, details;

        public IngeNewsItemViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.tvNameIngeNews);
            details = (TextView) v.findViewById(R.id.tvDetailsIngeNews);
        }
    }
}
