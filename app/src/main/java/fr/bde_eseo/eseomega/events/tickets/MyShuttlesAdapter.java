package fr.bde_eseo.eseomega.events.tickets;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.events.tickets.model.CheckShuttleItem;
import fr.bde_eseo.eseomega.events.tickets.model.ShuttleItem;
import fr.bde_eseo.eseomega.events.tickets.model.TicketPictItem;

/**
 * Created by Rascafr on 11/01/2016.
 * Adapter pour l'historique des commandes évènements
 */
public class MyShuttlesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<CheckShuttleItem> checkShuttleItems;
    private Context context;

    public MyShuttlesAdapter(Context context) {
        this.checkShuttleItems = new ArrayList<>();
        this.context = context;
    }

    public void setShuttleItems(ArrayList<CheckShuttleItem> checkShuttleItems) {
        if (checkShuttleItems != null) {
            this.checkShuttleItems = checkShuttleItems;
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ShuttleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_shuttle, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final CheckShuttleItem csi = checkShuttleItems.get(position);
        final ShuttleViewHolder svh = (ShuttleViewHolder) holder;
        svh.vPlace.setText(csi.getShuttleItem().getDepartPlace());
        svh.vDeparture.setText(csi.getShuttleItem().getDepartureStr());
        svh.vSeats.setText(csi.getShuttleItem().getRemainingSeats() + "/" + csi.getShuttleItem().getTotalSeats() + " places disponibles");
        svh.checkShuttle.setChecked(csi.isCheck());
        svh.checkShuttle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uncheckAll();
                csi.setIsCheck(svh.checkShuttle.isChecked());
                notifyDataSetChanged();
            }
        });

        svh.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uncheckAll();
                svh.checkShuttle.setChecked(!svh.checkShuttle.isChecked());
                csi.setIsCheck(svh.checkShuttle.isChecked());
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return checkShuttleItems == null ? 0 : checkShuttleItems.size();
    }

    // Classic View Holder for Ticket
    public static class ShuttleViewHolder extends RecyclerView.ViewHolder {

        protected TextView vPlace, vDeparture, vSeats;
        protected CheckBox checkShuttle;
        protected CardView cardView;

        public ShuttleViewHolder(View v) {
            super(v);
            vPlace =  (TextView) v.findViewById(R.id.shuttlePlace);
            vDeparture = (TextView)  v.findViewById(R.id.shuttleDeparture);
            vSeats = (TextView) v.findViewById(R.id.shuttleSeats);
            checkShuttle = (CheckBox) v.findViewById(R.id.checkShuttle);
            cardView = (CardView) v.findViewById(R.id.card_view);
        }
    }

    /**
     * Uncheck all items
     */
    void uncheckAll () {
        for (int i=0;i<checkShuttleItems.size();i++) {
            checkShuttleItems.get(i).setIsCheck(false);
        }
    }
}
