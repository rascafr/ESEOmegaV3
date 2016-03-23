package fr.bde_eseo.eseomega.lacommande;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import fr.bde_eseo.eseomega.R;

import java.util.ArrayList;

import fr.bde_eseo.eseomega.lacommande.model.LacmdCategory;

/**
 * Created by François on 24/04/2015.
 */

public class MyFoodListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<LacmdCategory> foodCategoryArrayList;
    private Context context;
    private DisplayImageOptions options;

    public static final int TYPE_FOOD_ITEM = 0;
    private static final int TYPE_NUMBER = TYPE_FOOD_ITEM + 1;

    public MyFoodListAdapter(Context context) {
        this.foodCategoryArrayList = new ArrayList<>();
        this.context = context;
        this.options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.solid_loading_background)
                .showImageForEmptyUri(R.drawable.solid_loading_background)
                .showImageOnFail(R.drawable.solid_loading_background)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    public void setFoodListArray(ArrayList<LacmdCategory> foodCategoryArrayList) {
        if (foodCategoryArrayList != null) {
            this.foodCategoryArrayList = foodCategoryArrayList;
            notifyDataSetChanged();
        }
    }

    public LacmdCategory getItem(int position) {
        return foodCategoryArrayList.get(position);
    }

    @Override
    public int getItemCount() {
        return foodCategoryArrayList.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        LacmdCategory fc = foodCategoryArrayList.get(position);

        if (getItemViewType(position) == TYPE_FOOD_ITEM) { // Food Item
            FoodListViewHolder fvh = (FoodListViewHolder) viewHolder;

            fvh.vName.setText(fc.getName());
            fvh.vPrice.setText(fc.getBeginPriceAsStr());
            fvh.vSmallText.setText(fc.getSmallText());
            ImageLoader.getInstance().displayImage(fc.getImgUrl(), fvh.vImg, this.options);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                fvh.cardView.setPreventCornerOverlap(false);
            } else {
                fvh.cardView.setPreventCornerOverlap(true); // Only supported if Android Version is >= Lollipop
            }
        }

    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_FOOD_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        switch (viewType) {
            case TYPE_FOOD_ITEM:
                return new FoodListViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_food_image, viewGroup, false));
        }

        return null;
    }

    // Classic View Holder for Food
    public static class FoodListViewHolder extends RecyclerView.ViewHolder {

        protected TextView vName;
        protected TextView vSmallText;
        protected ImageView vImg;
        protected TextView vPrice;
        protected CardView cardView;

        public FoodListViewHolder(View v) {
            super(v);
            vName =  (TextView) v.findViewById(R.id.foodTitle);
            vSmallText = (TextView)  v.findViewById(R.id.foodSmall);
            vImg = (ImageView) v.findViewById(R.id.foodPicture);
            vPrice = (TextView) v.findViewById(R.id.foodMore);
            cardView = (CardView) v.findViewById(R.id.card_food_list);
        }
    }
}