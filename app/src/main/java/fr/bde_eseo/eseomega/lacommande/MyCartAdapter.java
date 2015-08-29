package fr.bde_eseo.eseomega.lacommande;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rascafr.test.matdesignfragment.R;

import fr.bde_eseo.eseomega.lacommande.model.LacmdRoot;

/**
 * Created by Rascafr on 19/08/2015.
 * Simple adapter for cart's items easy view
 */
public class MyCartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CartItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_cart, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        LacmdRoot root = DataManager.getInstance().getCartArray().get(position);

        CartItemHolder cih = (CartItemHolder) holder;
        cih.tvName.setText(root.getName());
        String desc = root.getFriendlyString(false);
        if (desc.length() == 0) cih.tvDetails.setVisibility(View.GONE);
        else {
            cih.tvDetails.setVisibility(View.VISIBLE);
            cih.tvDetails.setText(desc);
        }
        cih.tvPrice.setText(root.getFormattedPrice());
    }

    @Override
    public int getItemCount() {
        return DataManager.getInstance().getNbCartItems();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    // Holder for cart item
    public class CartItemHolder extends RecyclerView.ViewHolder {

        protected TextView tvPrice, tvName, tvDetails;

        public CartItemHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tvNameCartItem);
            tvPrice = (TextView) itemView.findViewById(R.id.tvPriceCartItem);
            tvDetails = (TextView) itemView.findViewById(R.id.tvDetailsCartItem);
        }
    }
}
