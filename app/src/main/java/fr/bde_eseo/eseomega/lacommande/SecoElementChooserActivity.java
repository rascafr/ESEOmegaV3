package fr.bde_eseo.eseomega.lacommande;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
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

import com.rascafr.test.matdesignfragment.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.lacommande.model.LacmdElement;
import fr.bde_eseo.eseomega.lacommande.model.LacmdIngredient;
import fr.bde_eseo.eseomega.lacommande.model.LacmdMenu;
import fr.bde_eseo.eseomega.lacommande.model.LacmdRoot;

/**
 * Created by Rascafr on 24/08/2015.
 * Displays a list of items associated with bundled data
 * Used to display checkboxes with elements inside
 */
public class SecoElementChooserActivity extends AppCompatActivity {

    private RecyclerView recList;
    private CheckboxListAdapter mAdapter;
    private TextView tvIngredients, tvAdd, tvStackMorePrice, tvStackMoreText;
    private ArrayList<CheckboxItem> checkboxItems;
    private Toolbar toolbar;
    private LacmdMenu menu;
    private String menuID;
    private int maxElements, currentElements, elemPos;
    private double supplMore;
    private final static String TITLE_BASE = "Choisissez vos éléments";

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
        checkboxItems = new ArrayList<>();
        mAdapter = new CheckboxListAdapter();
        recList = (RecyclerView) findViewById(R.id.recyList);
        recList.setAdapter(mAdapter);
        recList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        currentElements = 0;

        // Add elemnts without ingredients to list's dataset
        for (int i=0;i<DataManager.getInstance().getElements().size();i++) {
            if (DataManager.getInstance().getElements().get(i).hasIngredients() == 0 &&
                    DataManager.getInstance().getElements().get(i).getOutofmenu() == 0)
                checkboxItems.add(
                    new CheckboxItem(
                        DataManager.getInstance().getElements().get(i).getName(),
                        DataManager.getInstance().getElements().get(i).getIdstr(),
                        DataManager.getInstance().getElements().get(i).getPrice()
                    )
                );
        }

        for (int i=0;i<DataManager.getInstance().getMenu().getItems().size();i++) {
            LacmdRoot element = DataManager.getInstance().getMenu().getItems().get(i);
            if (element.hasIngredients() == 0) {
                if (element.getName().length() > 0) {
                    setArrayCheck(element.getIdstr());
                    currentElements++;
                }
            }
        }

        supplMore = 0;
        mAdapter.notifyDataSetChanged();
        tvStackMoreText.setVisibility(View.INVISIBLE);
        tvStackMorePrice.setVisibility(View.INVISIBLE);

        // Get parameters
        /*
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                Toast.makeText(SecoElementChooserActivity.this, "Erreur de l'application (c'est pas normal)", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                menuID = extras.getString(Constants.KEY_MENU_ID);
                menu = DataManager.getInstance().getMenuFromID(menuID);
                //elemPos = extras.getInt(Constants.KEY_ELEMENT_POSITION);
                //element = new LacmdElement(DataManager.getInstance().getElementFromID(elementID));
                getSupportActionBar().setTitle(TITLE_BASE);
                Log.d("INTENT", menuID);
            }
        } else {
            //elementID = (String) savedInstanceState.getSerializable(Constants.KEY_ELEMENT_ID);
            //elemPos = (int) savedInstanceState.getSerializable(Constants.KEY_ELEMENT_POSITION);
            //element = new LacmdElement(DataManager.getInstance().getElementFromID(elementID));
            getSupportActionBar().setTitle(TITLE_BASE);
        }*/

        getSupportActionBar().setTitle(TITLE_BASE);
        menu = DataManager.getInstance().getMenu();
        maxElements = menu.getMaxSecoElem();
        tvIngredients.setText("Vous devez choisir " + maxElements +
                " élément" + (maxElements>0?"s":"") +
                " dans la liste ci-dessous.");

        // Validation
        tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tvAdd.setBackgroundColor(0x2fffffff);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvAdd.setBackgroundColor(0x00ffffff);
                    }
                }, 500);

                if (currentElements == maxElements) {

                    // Remove old items
                    for (int i=0;i<DataManager.getInstance().getMenu().getItems().size();i++) {
                        LacmdRoot element = DataManager.getInstance().getMenu().getItems().get(i);
                        if (element.hasIngredients() == 0) {
                            DataManager.getInstance().getMenu().getItems().remove(i);
                            i--;
                        }
                    }

                    // Add all checked items
                    for (int i = 0; i < checkboxItems.size(); i++) {
                        if (checkboxItems.get(i).isChecked()) {
                            DataManager.getInstance().getMenu().getItems().add(
                                    new LacmdElement(DataManager.getInstance().getElementFromID(checkboxItems.get(i).getIdstr())));
                        }
                    }
                    //element.setItems(items);

                /*if (elemPos == -1) {
                    Toast.makeText(SecoElementChooserActivity.this, "\"" + element.getName() + "\" a été ajouté au panier", Toast.LENGTH_SHORT).show();
                    DataManager.getInstance().addCartItem(element);
                } else {
                    DataManager.getInstance().getMenu().getItems().set(elemPos, element);
                }*/
                    SecoElementChooserActivity.this.finish();
                } else {
                    Toast.makeText(SecoElementChooserActivity.this, "Vous devez sélectionner tous vos éléments.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private class CheckboxListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            return new CheckBoxHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_check, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (checkboxItems != null) {
                final CheckBoxHolder cbh = (CheckBoxHolder) holder;
                cbh.checkBox.setChecked(checkboxItems.get(position).isChecked());
                cbh.checkBox.setText(checkboxItems.get(position).getName());
                cbh.checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        currentElements += cb.isChecked()?1:-1;
                        if (currentElements > maxElements) {
                            cb.setChecked(false);
                            currentElements --;
                            Toast.makeText(SecoElementChooserActivity.this, "Vous avez attend le nombre max d'éléments", Toast.LENGTH_SHORT).show();
                        }
                        checkboxItems.get(position).setChecked(cb.isChecked());
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return checkboxItems.size();
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        // Holder for checkbox item
        public class CheckBoxHolder extends RecyclerView.ViewHolder {

            protected CheckBox checkBox;
            protected TextView tvMore;

            public CheckBoxHolder(View itemView) {
                super(itemView);
                checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
                tvMore = (TextView) itemView.findViewById(R.id.tvSuppl);
            }
        }
    }

    private void setArrayCheck (String idstr) {
        for (int i=0;i<checkboxItems.size();i++) {
            if (checkboxItems.get(i).getIdstr().equals(idstr)) {
                checkboxItems.get(i).setChecked(true);
            }
        }
    }

    private class CheckboxItem extends LacmdRoot {
        private boolean checked;
        private String more;
        private boolean visible;

        public CheckboxItem (String name, String id, double price) {
            super(name, id, 0, 0, price, LacmdIngredient.ID_CAT_INGREDIENT);
            checked = false;
            this.visible = false;
            this.more = this.price == 0.0?"":"+" + new DecimalFormat("0.00").format(this.price) + "€";
        }

        public boolean isVisible() {
            return visible;
        }

        public String getMore() {
            return more;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public boolean isChecked() {

            return checked;
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
}
