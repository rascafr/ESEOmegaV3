package fr.bde_eseo.eseomega.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rascafr.test.matdesignfragment.R;

/**
 * Created by François on 13/04/2015.
 */
public class ExampleFragment extends Fragment {

    public ExampleFragment() {}

    private static String newsURL = "http://intranet-wp.bdeldorado.fr";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        String titles[] = getResources().getStringArray(R.array.nav_drawer_items);

        View rootView = inflater.inflate(R.layout.fragment_example, container, false);
        TextView tv = (TextView) rootView.findViewById(R.id.section_label);
        tv.setText("Please keep in mind this is not the final version of this app.\n\n@fragment [...]");

        return rootView;
    }
}
