package fr.bde_eseo.eseomega.lacommande.tabs;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.rascafr.test.matdesignfragment.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import de.greenrobot.event.EventBus;
import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.lacommande.DataManager;
import fr.bde_eseo.eseomega.lacommande.MyCartAdapter;
import fr.bde_eseo.eseomega.lacommande.OrderTabsFragment;
import fr.bde_eseo.eseomega.lacommande.model.LacmdMenu;
import fr.bde_eseo.eseomega.lacommande.model.LacmdRoot;
import fr.bde_eseo.eseomega.listeners.RecyclerItemClickListener;
import fr.bde_eseo.eseomega.utils.ConnexionUtils;
import fr.bde_eseo.eseomega.utils.EncryptUtils;


public class TabCartView extends Fragment {

    private FloatingActionButton floatingShop;
    private TextView tvPrice;
    private ImageView img;
    private TextView tv1, tv2;
    private RecyclerView recList;
    private MyCartAdapter mAdapter;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_cartview_2,container,false);

        // UI objets
        floatingShop = (FloatingActionButton) v.findViewById(R.id.fabCommand);
        tvPrice = (TextView) v.findViewById(R.id.tvCartPrice);
        tv1 = (TextView) v.findViewById(R.id.tvListNothing);
        tv2 = (TextView) v.findViewById(R.id.tvListNothing2);
        img = (ImageView) v.findViewById(R.id.imgNoCart);
        tv1.setVisibility(View.VISIBLE);
        tv2.setVisibility(View.VISIBLE);
        img.setVisibility(View.VISIBLE);

        // Database model and view
        mAdapter = new MyCartAdapter(getActivity());
        recList = (RecyclerView) v.findViewById(R.id.recyList);
        recList.setAdapter(mAdapter);
        recList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        // event bus
        //EventBus.getDefault().register(this);
        // remove on clic ?
        recList.addOnItemTouchListener(
            new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, final int position) {
                    new MaterialDialog.Builder(getActivity())
                            .title(DataManager.getInstance().getCartArray().get(position).getName())
                            .content("Quelle action effectuer ?")
                            .positiveText("Supprimer")
                            .negativeText("Annuler")
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                }

                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    DataManager.getInstance().getCartArray().remove(position);
                                    refreshCart();
                                    OrderTabsFragment mFragment = (OrderTabsFragment) getActivity().getSupportFragmentManager().findFragmentByTag(Constants.TAG_FRAGMENT_ORDER_TABS);
                                    mFragment.refreshCart();
                                    Toast.makeText(getActivity(), "Élément retiré du panier", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .show();
                }
            })
        );

        // GO GO GO Post the order data to server !
        floatingShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int size = DataManager.getInstance().getCartArray().size();

                int nbMenu = 0;
                for (int i=0;i<size;i++)
                    if (DataManager.getInstance().getCartArray().get(i).getObjectType().equals(LacmdMenu.ID_CAT_MENU))
                        nbMenu++;

                if (size > 0 && size <= 10 && nbMenu <= 2) {

                    new MaterialDialog.Builder(getActivity())
                            .title("Valider la commande ?")
                            .content("En validant, vous vous engagez à venir payer et récupérer votre repas au comptoir de la cafet aujourd'hui entre 12h et 13h.\n\n" +
                                    "Si ce n'est pas le cas, il vous sera impossible de passer une nouvelle commande dès demain.")
                            .positiveText("Je confirme, j'ai faim !")
                            .negativeText("Annuler")
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    AsyncPostCart asyncPostCart = new AsyncPostCart();
                                    asyncPostCart.execute();
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                }
                            })
                            .show();

                } else if (size == 0){
                    new MaterialDialog.Builder(getActivity())
                            .title("Panier vide")
                            .content("Une commande doit comporter au moins un élément !")
                            .negativeText("En effet")
                            .show();
                } else if (size == 10){
                    new MaterialDialog.Builder(getActivity())
                            .title("Panier rempli")
                            .content("Une commande est limité à 10 éléments !")
                            .negativeText("Je vais en retirer")
                            .show();
                } else if (nbMenu > 2) {
                    new MaterialDialog.Builder(getActivity())
                            .title("Trop gourmand")
                            .content("Une commande est limité à 2 menus !")
                            .negativeText("Dommage")
                            .show();
                }
            }
        });

        return v;
    }

    public void refreshCart () {

        // Cart empty, or not ?
        if (DataManager.getInstance().getNbCartItems() == 0) {
            tv1.setVisibility(View.VISIBLE);
            tv2.setVisibility(View.VISIBLE);
            img.setVisibility(View.VISIBLE);
        } else {
            tv1.setVisibility(View.GONE);
            tv2.setVisibility(View.GONE);
            img.setVisibility(View.GONE);
        }

        // Total price
        tvPrice.setText(new DecimalFormat("0.00").format(DataManager.getInstance().getCartPrice()) + "€");

        // Notify the adapter
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Async task to post token & JSON data (in base64 format) to server
     */
    private class AsyncPostCart extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {

            String JSONstr = DataManager.getInstance().outputJSON();
            String resp = null;

            try {
                List<NameValuePair> pairs = new ArrayList<>();
                pairs.add(new BasicNameValuePair("token", DataManager.getInstance().getToken()));
                pairs.add(new BasicNameValuePair("data", Base64.encodeToString(JSONstr.getBytes("UTF-8"), Base64.NO_WRAP)));
                resp = ConnexionUtils.postServerData(Constants.URL_POST_CART, pairs);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return resp;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s != null) {
                if (s.equals("1")) {

                    Calendar cal = Calendar.getInstance(); //Create Calendar-Object
                    int hour = cal.get(Calendar.HOUR_OF_DAY);

                    new MaterialDialog.Builder(getActivity())
                            .title("Commande validée !")
                            .content("Celle-ci " + (hour<12?"va être traitée après 12h":"est en cours de préparation") + " et sera disponible après avoir payé au comptoir.\n\nBon appétit !")
                            .negativeText("Merci")
                            .cancelable(false)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                    getFragmentManager().popBackStackImmediate();
                                }
                            })
                            .show();
                } else {
                    new MaterialDialog.Builder(getActivity())
                            .title("Oups")
                            .content("Erreur avec les données envoyées / mauvaise réponse du serveur (" + s + ")")
                            .negativeText("Fermer")
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                }
                            })
                            .show();
                }
            }
        }
    }
}