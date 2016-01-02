package fr.bde_eseo.eseomega.lacommande;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import fr.bde_eseo.eseomega.R;

import fr.bde_eseo.eseomega.lacommande.tabs.TabCartView;
import fr.bde_eseo.eseomega.slidingtab.SlidingTabLayout;
import fr.bde_eseo.eseomega.slidingtab.ViewPagerAdapter;

/**
 * Created by Rascafr on 21/07/2015.
 */
public class OrderTabsFragment extends Fragment {

    public static final long MAX_DELAY_ORDER = 582*1000;
    private ViewPager mPager;
    private SlidingTabLayout mTabs;
    private ViewPagerAdapter mAdapter;
    private CharSequence titles[] = {"Carte", "Panier (0)"};
    private int nbTabs = 2;
    private Handler mHandler;
    private int count = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_order, container, false);

        mPager = (ViewPager) rootView.findViewById(R.id.home_fragment_pager);
        mAdapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager(), titles, nbTabs);
        mPager.setAdapter(mAdapter);
        mTabs = (SlidingTabLayout) rootView.findViewById(R.id.home_fragment_tabs);
        mTabs.setDistributeEvenly(true);
        mTabs.setViewPager(mPager);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // This method will be executed once the timer is over
                Context ctx = getActivity();
                if (ctx != null) {
                    new MaterialDialog.Builder(getActivity())
                            .title("Votre panier a expiré")
                            .content("Pour des raisons de sécurité, il n'est possible de passer commande que pendant 10 minutes sans la valider.\nMerci de bien vouloir recommencer ...")
                            .cancelable(false)
                            .negativeText("Ok")
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                    getFragmentManager().popBackStackImmediate();
                                }
                            })
                            .show();
                }

            }
        }, MAX_DELAY_ORDER);

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("FRAG", "Fragment detached");
    }

    // Used to refresh cart's item numbers
    public void refreshCart () {

        // Get number
        int countItem = DataManager.getInstance().getNbCartItems();

        // Update tabs
        mAdapter.setCartTitle("Panier (" + countItem + ")");
        mAdapter.notifyDataSetChanged();
        mTabs.setViewPager(mPager); // RESOLVES EVERYTHING YEAH :D

        // Update cart fragment
        ((TabCartView)mAdapter.getItem(1)).refreshCart();
    }
}
