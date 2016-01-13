package fr.bde_eseo.eseomega.events.tickets;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.events.tickets.model.EventTicketItem;
import fr.bde_eseo.eseomega.events.tickets.model.ShuttleItem;
import fr.bde_eseo.eseomega.events.tickets.model.SubEventItem;
import fr.bde_eseo.eseomega.events.tickets.model.TicketPictItem;
import fr.bde_eseo.eseomega.events.tickets.model.TicketStore;
import fr.bde_eseo.eseomega.lacommande.DataManager;
import fr.bde_eseo.eseomega.lacommande.ElementChooserActivity;
import fr.bde_eseo.eseomega.lacommande.IngredientsChooserActivity;
import fr.bde_eseo.eseomega.lacommande.model.LacmdRoot;
import fr.bde_eseo.eseomega.listeners.RecyclerItemClickListener;
import fr.bde_eseo.eseomega.profile.UserProfile;

/**
 * Created by Rascafr on 11/01/2016.
 * Liste les évenements que l'utilisateur peut acheter
 * <p/>
 * Événements (liste) → type de place (dialogue) → Activité de choix de la navette (si il y a navette) → Paiement Lydia
 * <p/>
 * Événements possibles :
 * - date < date_debut
 * - au moins un type de place de dispo
 * - prix >= 0.5
 */
public class PresalesActivity extends AppCompatActivity {

    // Model
    private ArrayList<TicketPictItem> ticketPictItems;

    // Android objects
    private Context context;

    // User profile
    private UserProfile userProfile;

    // Adapter / recycler
    private MyPresalesAdapter mAdapter;
    private RecyclerView recList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set view / call parent
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presales);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(android.R.drawable.ic_delete);
        context = this;

        // Get current events / tickets
        fillArray();

        // Init adapter / recycler view
        mAdapter = new MyPresalesAdapter(context);
        recList = (RecyclerView) findViewById(R.id.recyList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.setAdapter(mAdapter);

        recList.addOnItemTouchListener(new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final ArrayList<SubEventItem> subTickets = ticketPictItems.get(position).getExternalEventItem().getSubEventItems();

                CharSequence items[] = new CharSequence[subTickets.size()];
                for (int i = 0; i < subTickets.size(); i++)
                    items[i] = subTickets.get(i).getTitre() + " • " + subTickets.get(i).getEventPriceAsString() + " - " + subTickets.get(i).getId();

                // Material dialog to show list of items
                MaterialDialog md = new MaterialDialog.Builder(context)
                        .items(items)
                        .title("Tickets disponibles")
                        .cancelable(true) // faster for user
                        .positiveText("Choisir")
                        .negativeText("Annuler")
                        .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                /**
                                 * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                                 * returning false here won't allow the newly selected radio button to actually be selected.
                                 **/

                                /**
                                 * Check si navette dispo pour ce ticket :
                                 * - si navette : choix navette
                                 * - sinon : payer
                                 */
                                if (subTickets.get(which).hasShuttles()) {
                                    Intent i = new Intent(context, ShuttleActivity.class);
                                    TicketStore.getInstance().setSelectedTicket(subTickets.get(which));
                                    startActivity(i);
                                } else {
                                    // Payer directement
                                }

                                return true;
                            }
                        })
                        .show();

            }
        }));

        // Set data
        mAdapter.setPictItems(ticketPictItems);

    }

    /**
     * Permet d'ajouter les événements visibles dans un dataset utilisable par la RecyclerView
     */
    private void fillArray() {

        if (ticketPictItems == null)
            ticketPictItems = new ArrayList<>();
        ticketPictItems.clear();

        ArrayList<EventItem> eventItems = TicketStore.getInstance().getEventItems();

        for (int i = 0; i < eventItems.size(); i++) {
            EventItem ei = eventItems.get(i);
            if (!ei.isHeader() && !ei.isPassed() && ei.hasSubEventChildEnabled() && ei.hasSubEventChildPriced()) {
                ticketPictItems.add(new TicketPictItem(ei));
                Log.d("DBG", "Add : " + ei.getName());
            }
        }
    }
}
