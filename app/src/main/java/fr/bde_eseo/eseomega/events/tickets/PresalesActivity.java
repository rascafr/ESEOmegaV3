package fr.bde_eseo.eseomega.events.tickets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.events.EventItem;
import fr.bde_eseo.eseomega.events.tickets.adapters.MyPresalesAdapter;
import fr.bde_eseo.eseomega.events.tickets.model.SubEventItem;
import fr.bde_eseo.eseomega.events.tickets.model.TicketPictItem;
import fr.bde_eseo.eseomega.events.tickets.model.TicketStore;
import fr.bde_eseo.eseomega.listeners.RecyclerItemClickListener;
import fr.bde_eseo.eseomega.lydia.LydiaActivity;
import fr.bde_eseo.eseomega.profile.UserProfile;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

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

    // Data
    private int idcmd = -1;
    private String eventName, eventDate, eventID, ticketName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set view / call parent
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presales);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = this;

        // Get user profile
        userProfile = new UserProfile();
        userProfile.readProfilePromPrefs(context);

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
                eventName = ticketPictItems.get(position).getExternalEventItem().getName();
                eventDate = ticketPictItems.get(position).getExternalEventItem().getDayAsString(ticketPictItems.get(position).getExternalEventItem().getDate());

                CharSequence items[] = new CharSequence[subTickets.size()];
                for (int i = 0; i < subTickets.size(); i++)
                    items[i] = subTickets.get(i).getTitre() + " • " + subTickets.get(i).getEventPriceAsString();

                // Material dialog to show list of items
                new MaterialDialog.Builder(context)
                        .items(items)
                        .title("Tickets disponibles")
                        .cancelable(true) // faster for user
                        .positiveText(R.string.dialog_choose)
                        .negativeText(R.string.dialog_cancel)
                        .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                                // Conservation des paramètres
                                eventID = subTickets.get(which).getId();
                                ticketName = "" + text;

                                /**
                                 * Check si navette dispo pour ce ticket :
                                 * - si navette : choix navette
                                 * - sinon : payer
                                 */
                                if (subTickets.get(which).hasShuttles()) {
                                    Intent i = new Intent(context, ShuttleActivity.class);
                                    TicketStore.getInstance().setSelectedTicket(subTickets.get(which));
                                    startActivityForResult(i, Constants.RESULT_SHUTTLES_KEY);
                                } else {
                                    // Payer directement
                                    new MaterialDialog.Builder(context)
                                            .title("Confirmer l'achat")
                                            .content(eventName + "\n" + ticketName + "\n" + eventDate + "\n\n" +
                                                    "Les places seront nominatives.\nVotre carte étudiante vous sera demandée à l'entrée.\nLes CGV s'appliquent.")
                                            .positiveText(R.string.dialog_pay)
                                            .negativeText(R.string.dialog_cancel)
                                            .callback(new MaterialDialog.ButtonCallback() {
                                                @Override
                                                public void onPositive(MaterialDialog dialog) {
                                                    super.onPositive(dialog);
                                                    AsyncSendTicket asyncSendTicket = new AsyncSendTicket(context);
                                                    asyncSendTicket.execute(eventID);
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
            }
        }
    }

    /**
     * Permet d'envoyer la commande sur les serveurs
     */
    private class AsyncSendTicket extends AsyncTask<String, String, String> {

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
            String errMsg = "Erreur réseau";

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
                        .content(errMsg + (err == 0 ? "" : " (code : " + err + ")"))
                        .negativeText(R.string.dialog_close)
                        .show();
            }
        }

        @Override
        protected String doInBackground(String... sData) {

            try {
                HashMap<String, String> params = new HashMap<>();
                params.put(context.getResources().getString(R.string.token), TicketStore.getInstance().getToken());
                params.put(context.getResources().getString(R.string.idevent), Base64.encodeToString(sData[0].getBytes("UTF-8"), Base64.NO_WRAP));
                if (sData.length > 1 && sData[1] != null && sData[1].length() > 0) {
                    params.put(context.getResources().getString(R.string.nav), Base64.encodeToString(sData[1].getBytes("UTF-8"), Base64.NO_WRAP));
                }
                return ConnexionUtils.postServerData(Constants.URL_API_EVENT_SEND, params, context);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Called when called Activities (child) finished
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // From Lydia-Activity
        if (requestCode == Constants.RESULT_LYDIA_KEY) {

            if (LydiaActivity.LAST_STATUS() == 2) {
                new MaterialDialog.Builder(context)
                        .title("Félicitations !")
                        .content("Votre réservation a été payée.\nEntrez ci-dessous votre email afin de recevoir votre place au format PDF.\n\nNote : vous pouvez également accéder à cette fenêtre depuis l'historique des réservations)")
                        .cancelable(false)
                        .inputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                        .input("sterling@archer.fr", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                AsyncEventEmail asyncEmail = new AsyncEventEmail(context, "" + input, PresalesActivity.this, userProfile, idcmd); // convert charSequence into String object
                                asyncEmail.execute();
                            }
                        }).show();
            } else {
                new MaterialDialog.Builder(context)
                        .title("Échec de la réservation")
                        .content("Le paiement n'a pas abouti.\nImpossible de valider la transaction.\n\nRéessayez plus tard ou contactez un membre du BDE.")
                        .cancelable(false)
                        .negativeText(R.string.dialog_close)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                                PresalesActivity.this.finish();
                            }
                        })
                        .show();
            }

            // From Shuttle-Activity
        } else if (requestCode == Constants.RESULT_SHUTTLES_KEY && resultCode == Activity.RESULT_OK) {

            // Get shuttle ID
            final String shuttleID = String.valueOf(data.getExtras().getInt(Constants.RESULT_SHUTTLES_VALUE));
            final String shuttleName = data.getExtras().getString(Constants.RESULT_SHUTTLES_NAME);

            // Ask for user confirmation
            new MaterialDialog.Builder(context)
                    .title("Confirmer l'achat")
                    .content(eventName + "\n" + ticketName + "\n" + shuttleName + "\n\n" +
                            "Les places seront nominatives.\nVotre carte étudiante vous sera demandée à l'entrée.\nLes CGV s'appliquent.")
                    .positiveText(R.string.dialog_pay)
                    .negativeText(R.string.dialog_cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);

                            // Send network request
                            AsyncSendTicket asyncSendTicket = new AsyncSendTicket(context);
                            asyncSendTicket.execute(eventID, shuttleID);
                        }
                    })
                    .show();
        }
    }

    /**
     * Menu : back button + arrow in toolbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_empty, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
