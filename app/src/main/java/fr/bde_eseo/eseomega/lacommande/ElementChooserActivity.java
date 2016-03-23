package fr.bde_eseo.eseomega.lacommande;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import fr.bde_eseo.eseomega.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.lacommande.model.LacmdElement;
import fr.bde_eseo.eseomega.lacommande.model.LacmdMenu;
import fr.bde_eseo.eseomega.lacommande.model.LacmdRoot;

/**
 * Created by Rascafr on 24/08/2015.
 */
public class ElementChooserActivity extends AppCompatActivity {

    private RecyclerView recList;
    private MenuListAdapter mAdapter;
    private TextView tvIngredients, tvAdd, tvStackMorePrice, tvStackMoreText;
    private Toolbar toolbar;
    private String menuID;
    private LacmdMenu menuTemp, menu;
    private double supplMore;
    private int nbSandw, nbElems;
    private boolean activityStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredients);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        setSupportActionBar(toolbar);

        // Objets & UI
        tvAdd = (TextView) findViewById(R.id.tvValid);
        tvIngredients = (TextView) findViewById(R.id.tv_act_ingr_desc);
        tvStackMorePrice = (TextView) findViewById(R.id.tvStackMorePrice);
        tvStackMoreText = (TextView) findViewById(R.id.tvStackMoreText);
        mAdapter = new MenuListAdapter();
        recList = (RecyclerView) findViewById(R.id.recyList);
        recList.setAdapter(mAdapter);
        recList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        // Get parameters
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                Toast.makeText(ElementChooserActivity.this, "Erreur de l'application (c'est pas normal)", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                menuID = extras.getString(Constants.KEY_MENU_ID);
                menuTemp = DataManager.getInstance().getMenuFromID(menuID);
                getSupportActionBar().setTitle("Menu " + menuTemp.getName());
            }
            menuTemp.setItems(new ArrayList<LacmdRoot>());
            DataManager.getInstance().setMenu(menuTemp);
        } else {
            menuID = (String) savedInstanceState.getSerializable(Constants.KEY_MENU_ID);
            menuTemp = DataManager.getInstance().getMenuFromID(menuID);
            getSupportActionBar().setTitle("Menu " + menuTemp.getName());
            DataManager.getInstance().setMenu(menuTemp);
        }

        // copy menu
        menu = DataManager.getInstance().getMenu();

        nbSandw = menu.getMaxMainElem();
        nbElems = menu.getMaxSecoElem();
        menu.setItems(new ArrayList<LacmdRoot>());
        for (int i=0;i<nbSandw;i++)
            menu.getItems().add(new LacmdElement("", "", 0.0, 0.0, 0, 0, 1));
        if (nbElems > 0)
            menu.getItems().add(new LacmdElement("", "", 0.0, 0.0, 0, 0, 0));

        //menu.getItems().get(0).setName("Sandwich maxi");

        supplMore = 0;
        mAdapter.notifyDataSetChanged();
        tvStackMoreText.setVisibility(View.INVISIBLE);
        tvStackMorePrice.setVisibility(View.INVISIBLE);

        tvIngredients.setVisibility(View.GONE);

        // Validation
        tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvAdd.setBackgroundColor(0x2fffffff);

                // Everything is checked ?
                boolean ok = true;
                for (int i=0;i<menu.getItems().size();i++) {
                    if (menu.getItems().get(i).getFriendlyString(true).length() == 0) {
                        ok = false;
                    }
                }

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvAdd.setBackgroundColor(0x00ffffff);
                    }
                }, 500);

                if (ok) {
                    Toast.makeText(ElementChooserActivity.this, "Menu \"" + menu.getName() + "\" a été ajouté au panier", Toast.LENGTH_SHORT).show();
                    DataManager.getInstance().addCartItem(menu);
                    ElementChooserActivity.this.finish();
                } else {
                    new MaterialDialog.Builder(ElementChooserActivity.this)
                            .title("Hey !")
                            .content("Impossible de lancer cette commande : vous n'avez pas choisi tous vos éléments.")
                            .cancelable(false)
                            .negativeText("Retour")
                            .show();
                }
                // Add all checked items
                /*
                ArrayList<LacmdRoot> items = new ArrayList<LacmdRoot>();
                for (int i=0;i<checkboxItems.size();i++) {
                    if (checkboxItems.get(i).isChecked())
                        items.add(checkboxItems.get(i));
                }
                element.setItems(items);*/


            }
        });
    }

    /**
     * Custom adapter
     */
    public class MenuListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

        public ArrayList<CustomObjItem> adapterObjects;

        public ArrayList<CustomObjItem> getAdapterObjects() {
            return adapterObjects;
        }

        public MenuListAdapter () {
            adapterObjects = new ArrayList<>();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MenuItemHolder cbh = new MenuItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_menu, parent, false));

            return cbh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

            if (menu.getItems() != null) {
                final MenuItemHolder mih = (MenuItemHolder) holder;
            /*

                CustomObjItem obj = adapterObjects.get(position);

                if (obj.isMain()) {
                    mih.tvHeader.setText("Choisissez votre " + (position + 1) + (position == 0 ? "er" : "ème") + " élement principal");
                } else {
                    mih.tvHeader.setText("Choisissez vos " + nbElems + " éléments secondaires");
                }
                mih.tvName.setText(obj.getName());

                */

                final LacmdElement elem = (LacmdElement) menu.getItems().get(position);

                if (elem.hasIngredients() > 0) {
                    mih.tvHeader.setText("Choisissez votre " + (position + 1) + (position == 0 ? "er" : "ème") + " élement principal");
                    if (elem.getFriendlyString(true).length() == 0) {
                        mih.tvName.setText("-- Touchez ici --");
                    } else {
                        mih.tvName.setText(elem.getName());
                    }

                } else {
                    //mih.tvHeader.setText("Choisissez votre " + (position - nbSandw + 1) + (position - nbSandw == 0 ? "er" : "ème") + " élement secondaire");

                    if (position - nbSandw == 0) {
                        mih.tvHeader.setText("Choisissez vos " + nbElems + " éléments secondaires");
                        mih.tvHeader.setVisibility(View.VISIBLE);
                    } else {
                        mih.tvHeader.setVisibility(View.GONE);
                    }
                    mih.tvName.setText(elem.getName().length()==0?"-- Touchez ici --":elem.getName());
                }


                double price = elem.calcRealPrice(true);
                if (price > 0)
                    mih.tvMore.setText("+" + new DecimalFormat("0.00").format(elem.calcRealPrice(true)) + "€");
                else
                    mih.tvMore.setText("");

                mih.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // If is sandwich : call IngredientChooser
                        if (elem.hasIngredients() > 0) {
                            String elems = menu.getMainElemStr(); // sandw|panini etc
                            StringTokenizer st = new StringTokenizer(elems, "|");

                            final int nb = st.countTokens();
                            CharSequence items[] = new CharSequence[nb];
                            final ArrayList<String> strings = new ArrayList<String>();

                            for (int i = 0; i < nb; i++) {
                                String s = st.nextToken();
                                strings.add(s);
                                items[i] = DataManager.getInstance().getElementFromID(s).getName();
                            }

                            new MaterialDialog.Builder(ElementChooserActivity.this)
                                    .title("Element principal")
                                    .items(items)
                                    .cancelable(true) // faster for user
                                    .positiveText("Personnaliser")
                                    .negativeText("Annuler")
                                    .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                                        @Override
                                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                                            Intent myIntent = new Intent(ElementChooserActivity.this, IngredientsChooserActivity.class);
                                            myIntent.putExtra(Constants.KEY_ELEMENT_ID, strings.get(which));
                                            myIntent.putExtra(Constants.KEY_ELEMENT_POSITION, position);
                                            startActivity(myIntent);
                                            activityStarted = true;

                                            return true;
                                        }
                                    })
                                    .show();
                        } else { // call seco element chooser
                            Intent myIntent = new Intent(ElementChooserActivity.this, SecoElementChooserActivity.class);
                            startActivity(myIntent);
                            activityStarted = true;
                        }
                    }
                });
            }

            /*
            if (menuItems != null) {
                final MenuItemHolder mih = (MenuItemHolder) holder;
                mih.tvName.setText(menuItems.get(position).getName());
                mih.tvHeader.setText(menuItems.get(position).getHeader());
                mih.tvMore.setText("");

                mih.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // If is sandwich : call IngredientChooser


                        if (menuItems.get(position).isSandw()) {
                            String elems = menu.getMainElemStr(); // sandw|panini etc
                            StringTokenizer st = new StringTokenizer(elems, "|");

                            final int nb = st.countTokens();
                            CharSequence items[] = new CharSequence[nb];
                            final ArrayList<String> strings = new ArrayList<String>();

                            for (int i = 0; i < nb; i++) {
                                String s = st.nextToken();
                                strings.add(s);
                                Log.d("NEXT", s + "/" + nb);
                                items[i] = DataManager.getInstance().getElementFromID(s).getName();
                            }

                            new MaterialDialog.Builder(ElementChooserActivity.this)
                                    .title("Element principal")
                                    .items(items)
                                    .cancelable(true) // faster for user
                                    .positiveText("Choisir")
                                    .negativeText("Annuler")
                                    .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                                        @Override
                                        public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                                            Intent myIntent = new Intent(ElementChooserActivity.this, IngredientsChooserActivity.class);
                                            myIntent.putExtra(Constants.KEY_ELEMENT_ID, strings.get(which));
                                            myIntent.putExtra(Constants.KEY_ELEMENT_POSITION, position);
                                            startActivity(myIntent);
                                            Log.d("ELEM", "User chooses " + strings.get(which));

                                            return true;
                                        }
                                    })
                                    .show();
                        }
                        // If element : call SecondaryChooser
                    }
                });*/

                /*
                cbh.checkBox.setChecked(menuItems.get(position).isChecked());
                cbh.checkBox.setText(checkboxItems.get(position).getName());
                cbh.checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        checkboxItems.get(position).setChecked(cb.isChecked());
                        boolean isfree = checkboxItems.get(position).getPrice() == 0;
                        currentElements += cb.isChecked()?1:-1;
                        if (currentElements > maxElements) {
                            supplMore = checkboxItems.get(position).getPrice()*(currentElements - maxElements);
                            tvStackMorePrice.setVisibility(View.VISIBLE);
                            tvStackMorePrice.setText(new DecimalFormat("0.00").format(supplMore) + "€");
                            tvStackMoreText.setVisibility(View.VISIBLE);
                        } else {
                            tvStackMorePrice.setVisibility(View.INVISIBLE);
                            tvStackMoreText.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }*/
        }

        @Override
        public int getItemCount() {
            return menu.getItems().size();
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        // Holder for menu item
        public class MenuItemHolder extends RecyclerView.ViewHolder {

            protected TextView tvName, tvHeader, tvMore;
            protected CardView cardView;

            public MenuItemHolder(View itemView) {
                super(itemView);
                tvName = (TextView) itemView.findViewById(R.id.tvNameCartItem);
                tvMore = (TextView) itemView.findViewById(R.id.tvPriceCartItem);
                tvHeader = (TextView) itemView.findViewById(R.id.menuHeader);
                cardView = (CardView) itemView.findViewById(R.id.card);
            }
        }
    }

    /**
     * Mixed
     */
    private class CustomObjItem {
        private String name, idstr;
        private double priceMore;
        private boolean isMain;

        public CustomObjItem(String name, String idstr, double priceMore, boolean isMain) {
            this.name = name;
            this.idstr = idstr;
            this.priceMore = priceMore;
            this.isMain = isMain;
        }

        public String getName () {
            if (name == null || name.length() == 0)
                return "-- Touchez ici --";
            else
                return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setIdstr(String idstr) {
            this.idstr = idstr;
        }

        public void setPriceMore(double priceMore) {
            this.priceMore = priceMore;
        }

        public void setIsMain(boolean isMain) {
            this.isMain = isMain;
        }

        public String getIdstr() {
            return idstr;
        }

        public double getPriceMore() {
            return priceMore;
        }

        public boolean isMain() {
            return isMain;
        }
    }

    /**
     * Custom class to handle menu's objects
     */
    private class MenuItem {
        private String name, header, idStr;
        private double priceMore;
        private boolean isSandw; // true -> sandwich/panini etc, false -> other element
        private boolean isEmpty;

        public MenuItem(String name, double priceMore, String header, boolean isSandw) {
            this.name = name;
            this.priceMore = priceMore;
            this.header = header;
            this.isSandw = isSandw;
            this.isEmpty = true;
        }

        public String getName() {
            return name;
        }

        public double getPriceMore() {
            return priceMore;
        }

        public String getHeader() {
            return header;
        }

        public boolean isSandw() {
            return isSandw;
        }
    }

    // A method to find height of the status bar
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (activityStarted && mAdapter != null) {
            activityStarted = false;
            mAdapter.notifyDataSetChanged();
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (activityStarted && mAdapter != null && menu != null) {
            activityStarted = false;

            mAdapter.adapterObjects.clear();
            String str = "";
            for (int i=0;i<menu.getItems().size();i++) {

                if (menu.getItems().get(i).hasIngredients() > 0) {
                    mAdapter.adapterObjects.add(new CustomObjItem(menu.getItems().get(i).getName(),
                            menu.getItems().get(i).getIdstr(),
                            menu.getItems().get(i).getPrice(),
                            true));
                } else {
                    if (str.length() == 0)
                        str += "\n";
                    str += menu.getItems().get(i).getName();
                }
            }
            mAdapter.adapterObjects.add(new CustomObjItem(str, "", 0.0, false));

            /*

                if (menu.getItems().get(i).hasIngredients() == 0) {
                    if (mAdapter.adapterObjects.get(i) == null) { // no objects yet for this position
                        mAdapter.adapterObjects.add(new CustomObjItem(menu.getItems().get(i).getName(),
                                menu.getItems().get(i).getIdstr(),
                                menu.getItems().get(i).getPrice(),
                                false));
                    } else {

                    }
                }

            }*/

                mAdapter.notifyDataSetChanged();
            double total = 0.0;
            for(int i=0;i<menu.getItems().size();i++) {
                total += menu.getItems().get(i).calcRealPrice(true);
            }

            if (total > 0) {
                tvStackMoreText.setVisibility(View.VISIBLE);
                tvStackMorePrice.setVisibility(View.VISIBLE);
                tvStackMorePrice.setText(new DecimalFormat("0.00").format(total) + "€");
            } else {
                tvStackMoreText.setVisibility(View.INVISIBLE);
                tvStackMorePrice.setVisibility(View.INVISIBLE);
            }
        }
    }
}
