package fr.bde_eseo.eseomega.fragment.tab;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rascafr.test.matdesignfragment.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

/**
 * Created by Rascafr on 31/07/2015.
 */
public class TabAnalyse extends Fragment {

    private JSONArray items;
    private String name;
    private TextView tvName, tvContent;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_analyse,container,false);
        tvName = (TextView) v.findViewById(R.id.tvDebugTitle);
        tvContent = (TextView) v.findViewById(R.id.tvDebugData);
        tvName.setText(name);
        tvContent.setText("");
        for (int i=0;i<items.length();i++) {
            try {
                JSONObject obj = items.getJSONObject(i);
                tvContent.setText(tvContent.getText()+" - " + obj.get("name")+"\n");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return v;
    }

    public void setItems(JSONArray items, String name) {
        this.items = items;
        this.name = name;

    }
}
