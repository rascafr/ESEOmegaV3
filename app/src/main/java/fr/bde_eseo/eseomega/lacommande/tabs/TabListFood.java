package fr.bde_eseo.eseomega.lacommande.tabs;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rascafr.test.matdesignfragment.R;

import org.json.JSONArray;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.lacommande.ElementChooserActivity;
import fr.bde_eseo.eseomega.lacommande.IngredientsChooserActivity;
import fr.bde_eseo.eseomega.lacommande.MyFoodListAdapter;
import fr.bde_eseo.eseomega.interfaces.OnItemAddToCart;
import fr.bde_eseo.eseomega.lacommande.DataManager;
import fr.bde_eseo.eseomega.lacommande.model.LacmdRoot;
import fr.bde_eseo.eseomega.listeners.RecyclerItemClickListener;
import fr.bde_eseo.eseomega.utils.JSONUtils;
import fr.bde_eseo.eseomega.utils.Utilities;

/**
 * Created by Rascafr
 * Used to display categories to user : menus, drinks, sandwiches ...
 * Before display : Fill DataManager's database
 *
 * On item click -> new activity with element's list
 *
 * Get all content by parsing JSON over network
 * TODO : security
 */

public class TabListFood extends Fragment {

    private OnItemAddToCart mOnItemAddToCart;
    private RecyclerView recList;
    private MyFoodListAdapter mAdapter;
    private TextView tvNetStatus;
    private boolean activityStarted;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_listfood,container,false);

        // User GUI
        tvNetStatus = (TextView) rootView.findViewById(R.id.tvNetStatusFoodList);

        // Flags
        activityStarted = false;

        // Search for the listView, then set its adapter
        mAdapter = new MyFoodListAdapter(getActivity());
        recList = (RecyclerView) rootView.findViewById(R.id.cardListFood);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.setAdapter(mAdapter);

        // Check connectivity
        if (Utilities.isPingOnline(getActivity())) {

            // If online, download categories's data
            AsyncGetData asyncGetData = new AsyncGetData();
            asyncGetData.execute(Constants.URL_JSON_LACMD_DATA);

        } else {

            // Else set error message to user
            tvNetStatus.setBackgroundColor(getActivity().getResources().getColor(R.color.md_prim_dark_red));
            tvNetStatus.setText("Hors-ligne. Veuillez vérifier votre connexion.");

        }

        recList.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //Toast.makeText(getActivity(), "Link to JSON category : " + DataManager.getInstance().getCategories().get(position).toString(), Toast.LENGTH_LONG).show();

                final ArrayList<LacmdRoot> roots = DataManager.getInstance().arrayToCatArray(DataManager.getInstance().getCategories().get(position).getCatname());

                CharSequence items[] = new CharSequence[roots.size()];
                for (int i=0;i<roots.size();i++)
                    items[i] = roots.get(i).getName() + " ("+roots.get(i).getFormattedPrice()+")";

                // Material dialog to show list of items
                MaterialDialog md = new MaterialDialog.Builder(getActivity())
                        .items(items)
                        .title("Nos " + DataManager.getInstance().getCategories().get(position).getName())
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

                                /*  ON ITEM SELECTION
                                    - If item has ingredients : launch the IngredientChooserActivity
                                    - If item has elements : launch the ElementChooserActivity
                                    - If item is just a simple item : add it to cart directly
                                 */
                                LacmdRoot root = roots.get(which);

                                if (root.hasElements() > 0) { // ElementChooserActivity
                                    Intent myIntent = new Intent(getActivity(), ElementChooserActivity.class);
                                    myIntent.putExtra(Constants.KEY_MENU_ID, root.getIdstr());
                                    getActivity().startActivity(myIntent);
                                    activityStarted = true;
                                } else if (root.hasIngredients() > 0) { // IngredientChooserActivity
                                    Intent myIntent = new Intent(getActivity(), IngredientsChooserActivity.class);
                                    myIntent.putExtra(Constants.KEY_ELEMENT_ID, root.getIdstr());
                                    myIntent.putExtra(Constants.KEY_ELEMENT_POSITION, -1); // not in a menu
                                    getActivity().startActivity(myIntent);
                                    activityStarted = true;
                                } else { // Add it to cart !
                                    DataManager.getInstance().addCartItem(root);
                                    Toast.makeText(getActivity(), "\"" + text + "\" a été ajouté au panier", Toast.LENGTH_SHORT).show();
                                    mOnItemAddToCart.OnItemAddToCart();
                                }

                                Log.d("FOOD", "User chooses : " + roots.get(which).toString());

                                return true;
                            }
                        })
                        .show();
            }
        }));

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mOnItemAddToCart = (OnItemAddToCart) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (activityStarted && mOnItemAddToCart != null) {
            mOnItemAddToCart.OnItemAddToCart();
            activityStarted = false;
        }
    }

    /**
     * Asynctask to parse JSON data (categories, elements, sandwiches, all data)
     * We could display list of categories once everything is downloaded :)
     */
    class AsyncGetData extends AsyncTask<String, String, JSONArray> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            tvNetStatus.setBackgroundColor(getActivity().getResources().getColor(R.color.md_prim_dark_yellow));
            tvNetStatus.setText("Connexion au serveur ...");
        }

        @Override
        protected JSONArray doInBackground(String... params) {
            return JSONUtils.getJSONArrayFromUrl(params[0], getActivity());
        }

        @Override
        protected void onPostExecute(JSONArray array) {

            DataManager.getInstance().fillData(array);

            // Associate data with Adapter
            mAdapter.setFoodListArray(DataManager.getInstance().getCategories());
            mAdapter.notifyDataSetChanged();
            /*

            Log.d("JSON", "\n-- Categories --\n");
            for (int i=0;i<DataManager.getInstance().getCategories().size();i++) {
                Log.d("JSON", "  " + DataManager.getInstance().getCategories().get(i).toString() + "\n");
            }

            Log.d("JSON", "\n-- Menus --\n");
            for (int i=0;i<DataManager.getInstance().getMenus().size();i++) {
                Log.d("JSON", "  " + DataManager.getInstance().getMenus().get(i).toString() + "\n");
            }

            Log.d("JSON", "\n-- Elements --\n");
            for (int i=0;i<DataManager.getInstance().getElements().size();i++) {
                Log.d("JSON", "  " + DataManager.getInstance().getElements().get(i).toString() + "\n");
            }

            Log.d("JSON", "\n-- Ingredients --\n");
            for (int i=0;i<DataManager.getInstance().getIngredients().size();i++) {
                Log.d("JSON", "  " + DataManager.getInstance().getIngredients().get(i).toString() + "\n");
            }
            */

            // Set GUI data
            tvNetStatus.setText("Connexion effectuée !");
            tvNetStatus.setBackgroundColor(getActivity().getResources().getColor(R.color.md_prim_dark_blue));

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tvNetStatus.setVisibility(View.GONE);
                }
            }, 2000);

        }
    }
}
