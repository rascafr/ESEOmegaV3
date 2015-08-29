package fr.bde_eseo.eseomega.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rascafr.test.matdesignfragment.R;

import fr.bde_eseo.eseomega.listeners.RecyclerItemClickListener;
import fr.bde_eseo.eseomega.model.Sponsor;
import fr.bde_eseo.eseomega.adapter.MySponsorAdapter;

import java.util.ArrayList;

/**
 * Created by François on 13/04/2015.
 */
public class SponsorsFragment extends Fragment {

    public SponsorsFragment() {}

    // Valeurs de la V1.0
    // A modifier si les sponsors changent
    static String sponsorsNames[] = {
            "Alinéa Atoll",
            "AviaSim",
            "BNP Paribas",
            "Camaloon",
            "Dalkia",
            "L'Entrepôt Café",
            "Groupe ESEO",
            "Française des Jeux",
            "Les Belles Années",
            "Le Guillou",
            "Magic Form",
            "Maine Optique",
            "Monsieur Store"};


    static String sponsorsWebsites[] = {
            "alinea.fr",
            "www.aviasim.fr/fr",
            "bnpparibas.com",
            "www.camaloon.com",
            "www.dalkia.fr",
            "www.facebook.com/pages/LEntrepot-Café/107851909791",
            "www.eseo.fr",
            "www.fdj.fr",
            "lesbellesannees.com",
            "www.leguillou.fr",
            "www.magic-form.fr",
            "www.pagesjaunes.fr/pros/55587998",
            "www.monsieurstore.com/angers/"};

    static String sponsorsAddress[] = {
            "Centre commercial Atoll Écoparc du Buisson 49070 BEAUCOUZÉ",
            "",
            "41, Boulevard du Maréchal Foch 49000 ANGERS",
            "",
            "",
            "23, rue Boisnet 49100 ANGERS",
            "10, Boulevard Jean Jeanneteau 49100 ANGERS",
            "58, rue Georges Charpak 44115 HAUTE GOULAINE",
            "Rue Georgette-Boulestreau 49100 ANGERS",
            "BISCUITERIE LE GUILLOU ZA du Restou 29140 Tourch",
            "12, Boulevard Gaston Birgé 49100 ANGERS",
            "16bis, rue Haut Rocher 49100 ANGERS",
            "Espace Brenon, 45 bvd de la Romanerie 49124 ST-BARTHÉLEMY-D'ANJOU"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_sponsors, container, false);
        RecyclerView recList = (RecyclerView) rootView.findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        ArrayList<Sponsor> spList = new ArrayList<>();
        for (int i=0;i<sponsorsNames.length;i++) {
            String web = sponsorsWebsites[i];
            if (web.length() > 38)
                web = web.substring(0, 38) + "...";
            spList.add(new Sponsor(sponsorsNames[i], sponsorsAddress[i], web, i + ""));
        }
        MySponsorAdapter msa = new MySponsorAdapter(spList, getActivity());
        recList.setAdapter(msa);

        // On click custom listener
        recList.addOnItemTouchListener(
            new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                @Override public void onItemClick(View view, int position) {
                    // Go to website
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + sponsorsWebsites[position]));
                    startActivity(browserIntent);
                }
            })
        );


        return rootView;
    }
}
