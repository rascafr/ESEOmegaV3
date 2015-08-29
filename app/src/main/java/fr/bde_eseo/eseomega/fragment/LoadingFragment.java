package fr.bde_eseo.eseomega.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rascafr.test.matdesignfragment.R;

/**
 * Created by François on 13/04/2015.
 */
public class LoadingFragment extends Fragment {

    public LoadingFragment() {}

    private static String newsURL = "http://intranet-wp.bdeldorado.fr";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_loading, container, false);

        final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fragmentManager.beginTransaction().replace(R.id.frame_container, new NewsFragment()).commit();
            }
        }, 400);

        return rootView;
    }
}
