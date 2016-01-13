package fr.bde_eseo.eseomega.events.tickets;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import fr.bde_eseo.eseomega.BuildConfig;
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
import fr.bde_eseo.eseomega.lydia.LydiaActivity;
import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;
import fr.bde_eseo.eseomega.utils.StringUtils;
import fr.bde_eseo.eseomega.utils.Utilities;
import fr.bde_eseo.eseomega.version.AsyncCheckVersion;

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
                final String eventName = ticketPictItems.get(position).getExternalEventItem().getName();
                final String eventDate = ticketPictItems.get(position).getExternalEventItem().getDayAsString(ticketPictItems.get(position).getExternalEventItem().getDate());

                CharSequence items[] = new CharSequence[subTickets.size()];
                for (int i = 0; i < subTickets.size(); i++)
                    items[i] = subTickets.get(i).getTitre() + " • " + subTickets.get(i).getEventPriceAsString();

                // Material dialog to show list of items
                MaterialDialog md = new MaterialDialog.Builder(context)
                        .items(items)
                        .title("Tickets disponibles")
                        .cancelable(true) // faster for user
                        .positiveText(R.string.dialog_choose)
                        .negativeText(R.string.dialog_cancel)
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
                                    final String idevent = subTickets.get(which).getId();

                                    new MaterialDialog.Builder(context)
                                            .title("Confirmer l'achat")
                                            .content(eventName + "\n" + text + "\n" + eventDate + "\n\n" +
                                                    "Les places seront nominatives.\nVotre carte étudiante vous sera demandée à l'entrée.\nLes CGV s'appliquent.")
                                            .positiveText(R.string.dialog_pay)
                                            .negativeText(R.string.dialog_cancel)
                                            .callback(new MaterialDialog.ButtonCallback() {
                                                @Override
                                                public void onPositive(MaterialDialog dialog) {
                                                    super.onPositive(dialog);
                                                    AsyncSendTicket asyncSendTicket = new AsyncSendTicket(context);
                                                    asyncSendTicket.execute(idevent);
                                                }
                                            })
                                            .show();
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

    /**
     * Permet d'envoyer la commande sur les serveurs
     */
    private class AsyncSendTicket extends AsyncTask<String,String,String> {

        private MaterialDialog md;
        private Context context;

        public AsyncSendTicket(Context context) {
            this.context = context;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            md = new MaterialDialog.Builder(context)
                    .title("Réservation en cours")
                    .content("Veuillez patienter ...")
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            md.hide();
            int err = 0;
            int idcmd = -1;
            String errMsg = "Erreur réseau";

            Log.d("DBG", "Got : " + data);

            if (Utilities.isNetworkDataValid(data)) {
                try {
                    JSONObject obj = new JSONObject(data);
                    err = obj.getInt("status");
                    errMsg = obj.getString("cause");
                    JSONObject objData = obj.getJSONObject("data");
                    idcmd = objData.getInt("idcmd");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (err == 1) {
                // Ok ! Send order to Lydia
                Intent i = new Intent(context, LydiaActivity.class);
                i.putExtra(Constants.KEY_LYDIA_ORDER_ID, idcmd);
                i.putExtra(Constants.KEY_LYDIA_ORDER_TYPE, Constants.TYPE_LYDIA_EVENT);
                i.putExtra(Constants.KEY_LYDIA_ORDER_ASKED, false);
                startActivityForResult(i, Constants.RESULT_LYDIA_KEY);

            } else {
                // Error, show message
                new MaterialDialog.Builder(context)
                        .title("Erreur")
                        .content(errMsg + (err == 0 ? "":" (cause : " + err + ")"))
                        .negativeText(R.string.dialog_close)
                        .show();
            }
        }

        @Override
        protected String doInBackground(String... sData) {

            try {
                HashMap<String,String> params = new HashMap<>();
                params.put(context.getResources().getString(R.string.token), TicketStore.getInstance().getToken());
                params.put(context.getResources().getString(R.string.idevent), Base64.encodeToString(sData[0].getBytes("UTF-8"), Base64.NO_WRAP));
                if (sData.length > 1 && sData[1] != null && sData[1].length() > 0) {
                    params.put(context.getResources().getString(R.string.nav), Base64.encodeToString(sData[1].getBytes("UTF-8"), Base64.NO_WRAP));
                    Log.d("DBG", "Add POST data : " + sData[1]);
                }
                return ConnexionUtils.postServerData(Constants.URL_API_EVENT_SEND, params, context);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("DBG", "Primary Result : " + requestCode + " → " + resultCode + ", " + data);
        if (resultCode == RESULT_OK && requestCode == Constants.RESULT_LYDIA_KEY) {
            int statusLydia = data.getIntExtra(Constants.RESULT_LYDIA_VALUE, 0);
            Log.d("DBG", "Result : " + requestCode + " → " + resultCode);
            if (statusLydia == 2) { // Lydia OK !
                Toast.makeText(PresalesActivity.this, "Axel bande très dur", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PresalesActivity.this, "Axel bande", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
