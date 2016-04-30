package fr.bde_eseo.eseomega.news;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.R;

/**
 * Created by Rascafr on 30/08/2015.
 */
public class ImageViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        String imgUrl = "http://mabite";

        // Get parameters
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                Toast.makeText(ImageViewActivity.this, "Erreur de l'application (c'est pas normal)", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                imgUrl = extras.getString(Constants.KEY_IMG);
            }
        }

        TouchImageView touchImageView = (TouchImageView) findViewById(R.id.touchImg);
        Picasso.with(this).load(imgUrl).placeholder(R.drawable.solid_loading_background).error(R.drawable.solid_loading_background).into(touchImageView);

    }

}
