package fr.bde_eseo.eseomega.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rascafr.test.matdesignfragment.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by François on 13/04/2015.
 */
public class HomeFragment extends Fragment {

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        TextView tv = (TextView) rootView.findViewById(R.id.section_text);

        String mContent = "", str;
        // Chargement du fichier programme.html du dossier "assets"
        try {
            // get input stream for text
            //BufferedReader is = new BufferedReader(new InputStreamReader(getActivity().getAssets().open("filename.txt"), "UTF-8"));
            InputStream is = getActivity().getAssets().open("homepage/programme.html");

            String UTF8 = "utf8";
            int BUFFER_SIZE = 8192;
            BufferedReader br = new BufferedReader(new InputStreamReader(is, UTF8), BUFFER_SIZE);

            while ((str = br.readLine()) != null) {
                mContent += str;
            }
        }
        catch (IOException ex) {
            mContent = "<b><font color=\"red\">Erreur de l'application</font></b><br>Le contenu n'a pas pu être chargé.<br>Cette erreur n'est pas normale du tout, merci de nous contacter pour nous en informer.";
        }

        tv.setText(Html.fromHtml(mContent));

        return rootView;
    }
}
