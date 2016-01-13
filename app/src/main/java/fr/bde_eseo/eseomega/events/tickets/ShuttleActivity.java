package fr.bde_eseo.eseomega.events.tickets;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.events.tickets.model.CheckShuttleItem;
import fr.bde_eseo.eseomega.events.tickets.model.SubEventItem;
import fr.bde_eseo.eseomega.events.tickets.model.TicketStore;
import fr.bde_eseo.eseomega.profile.UserProfile;

/**
 * Created by Rascafr on 13/01/2016.
 * Affiche les navettes disponibles pour un ticket précisé
 */
public class ShuttleActivity extends AppCompatActivity {

    // Model
    private ArrayList<CheckShuttleItem> checkShuttleItems;

    // Android objects
    private Context context;

    // User profile
    private UserProfile userProfile;

    // Adapter / recycler
    private MyShuttlesAdapter mAdapter;
    private RecyclerView recList;

    // Ticket ID
    private SubEventItem subEventItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set view / call parent
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shuttles);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(android.R.drawable.ic_delete);
        context = this;

        // Get intent
        subEventItem = TicketStore.getInstance().getSelectedTicket();

        // Init model
        fillCheckables();

        // Init adapter / recycler view
        mAdapter = new MyShuttlesAdapter(context);
        recList = (RecyclerView) findViewById(R.id.recyList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.setAdapter(mAdapter);
        mAdapter.setShuttleItems(checkShuttleItems);
    }

    /**
     * Permet d'ajouter les navettes en tant qu'objets checkables
     */
    private void fillCheckables() {
        if (checkShuttleItems == null)
            checkShuttleItems = new ArrayList<>();
        checkShuttleItems.clear();

        for (int i=0;i<subEventItem.getShuttleItems().size();i++) {
            checkShuttleItems.add(new CheckShuttleItem(subEventItem.getShuttleItems().get(i)));
        }
    }
}
