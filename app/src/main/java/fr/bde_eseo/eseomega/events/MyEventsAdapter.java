package fr.bde_eseo.eseomega.events;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import com.rascafr.test.matdesignfragment.R;

/**
 * Created by Rascafr on 11/08/2015.
 */
public class MyEventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final static int TYPE_HEADER = 0;
    public final static int TYPE_ITEM = 1;
    private ArrayList<EventItem> eventItems;
    private Context ctx;

    public MyEventsAdapter (Context ctx, ArrayList<EventItem> eventItems) {
        this.eventItems = eventItems;
        this.ctx = ctx;
    }

    public void setEventItems(ArrayList<EventItem> EventItems) {
        this.eventItems = EventItems;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER)
            return new EventHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_events_header, parent, false));
        else
            return new EventItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_event, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return eventItems.get(position).isHeader()?TYPE_HEADER:TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        EventItem ei = eventItems.get(position);
        int type = getItemViewType(position);

        if (type == TYPE_HEADER) {
            EventHeaderViewHolder ehvh = (EventHeaderViewHolder) holder;
            ehvh.name.setText(ei.getName());
        } else {
            EventItemViewHolder eivh = (EventItemViewHolder) holder;
            eivh.name.setText(ei.getName());
            eivh.details.setText(ei.getShortedDetails());
            eivh.dayNum.setText(ei.getDayNumero());
            eivh.dayName.setText(ei.getDayName());
            eivh.rlColor.setBackgroundColor(ei.getColor());
        }
    }

    @Override
    public int getItemCount() {
        return eventItems.size();
    }

    // Classic View Holder for Event item
    public class EventItemViewHolder extends RecyclerView.ViewHolder {

        protected TextView name, details, dayNum, dayName;
        protected RelativeLayout rlColor;

        public EventItemViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.tvNameEvent);
            details = (TextView) v.findViewById(R.id.tvDetailsEvent);
            dayName = (TextView) v.findViewById(R.id.tvDayEvent);
            dayNum = (TextView) v.findViewById(R.id.tvDateEvent);
            rlColor = (RelativeLayout) v.findViewById(R.id.rlEventColor);
        }
    }

    // Classic View Holder for Event header
    public class EventHeaderViewHolder extends RecyclerView.ViewHolder {

        protected TextView name;

        public EventHeaderViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.eventsHeader);
        }
    }
}
