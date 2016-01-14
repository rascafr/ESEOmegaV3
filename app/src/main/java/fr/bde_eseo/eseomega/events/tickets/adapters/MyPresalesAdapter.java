package fr.bde_eseo.eseomega.events.tickets.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.events.tickets.model.EventTicketItem;
import fr.bde_eseo.eseomega.events.tickets.model.TicketPictItem;

/**
 * Created by Rascafr on 11/01/2016.
 * Adapter pour l'historique des commandes évènements
 */
public class MyPresalesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<TicketPictItem> ticketPictItems;
    private Context context;

    public MyPresalesAdapter(Context context) {
        this.ticketPictItems = new ArrayList<>();
        this.context = context;
    }

    public void setPictItems(ArrayList<TicketPictItem> ticketPictItems) {
        if (ticketPictItems != null) {
            this.ticketPictItems = ticketPictItems;
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PresalesViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_ticket_pict, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TicketPictItem tpi = ticketPictItems.get(position);
        PresalesViewHolder pvh = (PresalesViewHolder) holder;
        pvh.vTitle.setText(tpi.getTitle());
        pvh.vDesc.setText(tpi.getDescription());
        pvh.vPrice.setText("À partir de " + tpi.getLowPrice());

        ImageLoader.getInstance().displayImage(tpi.getImgUrl(), pvh.vImg);
    }

    @Override
    public int getItemCount() {
        return ticketPictItems == null ? 0 : ticketPictItems.size();
    }

    // Classic View Holder for Ticket
    public static class PresalesViewHolder extends RecyclerView.ViewHolder {

        protected TextView vTitle;
        protected TextView vDesc;
        protected TextView vPrice;
        protected ImageView vImg;

        public PresalesViewHolder(View v) {
            super(v);
            vTitle =  (TextView) v.findViewById(R.id.tvTitleTicket);
            vDesc = (TextView)  v.findViewById(R.id.tvDescTicket);
            vPrice = (TextView)  v.findViewById(R.id.tvPriceTicket);
            vImg = (ImageView) v.findViewById(R.id.imgTicket);
        }
    }
}
