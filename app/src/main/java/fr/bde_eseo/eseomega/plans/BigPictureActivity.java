package fr.bde_eseo.eseomega.plans;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import fr.bde_eseo.eseomega.R;

/**
 * Created by root on 23/03/16.
 */
public class BigPictureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_image);

        SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) findViewById(R.id.bigImg);
        imageView.setImage(ImageSource.asset("plan.jpg"));
    }
}
