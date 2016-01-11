package fr.bde_eseo.eseomega.events.tickets;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.events.tickets.model.EventTicketItem;

/**
 * Created by Rascafr on 11/01/2016.
 * Adapter pour l'historique des commandes évènements
 */
public class MyEventTicketAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<EventTicketItem> eventTicketItems;
    private Context context;

    public MyEventTicketAdapter(Context context) {
        this.eventTicketItems = new ArrayList<>();
        this.context = context;
    }

    public void setTicketsItems(ArrayList<EventTicketItem> eventTicketItems) {
        if (eventTicketItems != null) {
            this.eventTicketItems = eventTicketItems;
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TicketViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_tickets, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        EventTicketItem eti = eventTicketItems.get(position);
        TicketViewHolder tvh = (TicketViewHolder) holder;
        tvh.vName.setText(eti.getName());
        tvh.vNumero.setText(String.valueOf(eti.getIdcmd()));
        tvh.vPrice.setText(String.valueOf(eti.getPrice()));
        tvh.vDate.setText(eti.getDatetime());
    }

    @Override
    public int getItemCount() {
        return eventTicketItems == null ? 0 : eventTicketItems.size();
    }

    // Classic View Holder for Ticket
    public static class TicketViewHolder extends RecyclerView.ViewHolder {

        protected TextView vName;
        protected TextView vNumero;
        protected TextView vPrice;
        protected TextView vDate;

        public TicketViewHolder(View v) {
            super(v);
            vName =  (TextView) v.findViewById(R.id.ticketName);
            vNumero = (TextView)  v.findViewById(R.id.ticketNumber);
            vPrice = (TextView) v.findViewById(R.id.ticketPrice);
            vDate = (TextView) v.findViewById(R.id.ticketDate);
        }
    }
}
