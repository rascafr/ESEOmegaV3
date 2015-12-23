package fr.bde_eseo.eseomega.ingenews;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.events.EventItem;

/**
 * Created by Rascafr on 11/08/2015.
 */
public class MyIngeNewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<IngenewsItem> ingenewsItems;
    private Context ctx;

    public ArrayList<IngenewsItem> getIngenewsItems() {
        return ingenewsItems;
    }

    public void setIngenewsItems(ArrayList<IngenewsItem> ingenewsItems) {
        this.ingenewsItems = ingenewsItems;
    }

    public MyIngeNewsAdapter(Context ctx, ArrayList<IngenewsItem> ingenewsItems) {
        this.ingenewsItems = ingenewsItems;
        this.ctx = ctx;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new IngeNewsItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_ingenews, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        IngenewsItem ii = ingenewsItems.get(position);
        IngeNewsItemViewHolder inivh = (IngeNewsItemViewHolder) holder;
        inivh.name.setText(ii.getName());
        inivh.details.setText(ii.getDetails());

        // On click listener → open PDF file (intent)
        //Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(ingenewsItems.get(position).getFile()));
        inivh.cardInge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Old code ...
                /*Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(ingenewsItems.get(position).getFile()));
                ctx.startActivity(i);*/

                // Ask to view directly
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(ingenewsItems.get(position).getFile()));
                if (intent.resolveActivity(ctx.getPackageManager()) != null) {
                    ctx.startActivity(intent);
                } else {
                    Toast.makeText(ctx, "Pas d'application installée pour ça !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // On long click listener → share link to a friend
        inivh.cardInge.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                new MaterialDialog.Builder(ctx)
                        .title(ingenewsItems.get(position).getName())
                        .content("Que souhaitez vous faire ?")
                        .positiveText("Partager")
                        .negativeText("Fermer")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);

                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_SEND);
                                intent.setType("text/plain"); // texte brut (lien de la news en pdf ...)
                                intent.putExtra(Intent.EXTRA_TEXT, "\"" + ingenewsItems.get(position).getName() + "\"\n" + ingenewsItems.get(position).getFile());
                                ctx.startActivity(Intent.createChooser(intent, "Partager ..."));
                            }
                        })
                        .show();

                // Consume callback
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return ingenewsItems.size();
    }

    // Classic View Holder for Ingénews item
    public class IngeNewsItemViewHolder extends RecyclerView.ViewHolder {

        protected TextView name, details;
        protected CardView cardInge;

        public IngeNewsItemViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.tvNameIngeNews);
            details = (TextView) v.findViewById(R.id.tvDetailsIngeNews);
            cardInge = (CardView) v.findViewById(R.id.cardInge); // For local-adapter on click listener
        }
    }
}
