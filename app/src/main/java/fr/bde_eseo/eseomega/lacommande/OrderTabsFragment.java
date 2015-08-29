package fr.bde_eseo.eseomega.lacommande;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rascafr.test.matdesignfragment.R;

import org.json.JSONArray;

import fr.bde_eseo.eseomega.lacommande.DataManager;
import fr.bde_eseo.eseomega.lacommande.tabs.TabCartView;
import fr.bde_eseo.eseomega.slidingtab.SlidingTabLayout;
import fr.bde_eseo.eseomega.slidingtab.ViewPagerAdapter;
import fr.bde_eseo.eseomega.utils.JSONUtils;

/**
 * Created by Rascafr on 21/07/2015.
 */
public class OrderTabsFragment extends Fragment {

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

        return rootView;
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
