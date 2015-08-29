package fr.bde_eseo.eseomega;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.rascafr.test.matdesignfragment.R;

import fr.bde_eseo.eseomega.model.UserProfile;

/**
 * Created by François on 20/04/2015.
 */
public class SplashActivity extends Activity {

    private final static int SPLASH_TIME_OUT = 1900;
    private final static int MIN_TRICK = 1;
    private int trick = 0;
    private UserProfile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ImageView vLogo = (ImageView) findViewById(R.id.imgLogo);
        profile = new UserProfile();
        profile.readProfilePromPrefs(this);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.idLoad);


        // Special trick
        vLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trick++;
            }
        });

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 1, 500);
                animation.setDuration(1500); //in milliseconds
                animation.setInterpolator(new AccelerateDecelerateInterpolator());
                animation.start();

            }
        }, 400);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // This method will be executed once the timer is over
                Intent i;
                if (trick >= MIN_TRICK && profile.isCreated()) {
                    i = new Intent(SplashActivity.this, GPGame.class);
                } else {
                    i = new Intent(SplashActivity.this, MainActivity.class);
                }
                startActivity(i);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();

            }
        }, SPLASH_TIME_OUT);
    }

}
