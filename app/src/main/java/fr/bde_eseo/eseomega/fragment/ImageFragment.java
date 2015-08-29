package fr.bde_eseo.eseomega.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rascafr.test.matdesignfragment.R;

/**
 * Created by François on 13/04/2015.
 */
public class ImageFragment extends Fragment {

    public ImageFragment() {}

    private static String newsURL = "http://intranet-wp.bdeldorado.fr";
    private final static String imgURL = "http://www.joomlaworks.net/images/demos/galleries/abstract/7.jpg";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_image, container, false);
        TextView tv = (TextView) rootView.findViewById(R.id.section_label);
        tv.setText("[TEST CACHE] L'image va s'afficher bientôt ...");

        ImageView img = (ImageView) rootView.findViewById(R.id.img);

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_autorenew)
                .showImageForEmptyUri(R.drawable.ic_warning)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        ImageLoader.getInstance().displayImage(imgURL, img, options);

        return rootView;
    }
}
