package fr.bde_eseo.eseomega;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by Rascafr on 18/01/2016.
 * Activité qui montre les fonctionnalités de l'app au 1er lancement, puis redirection vers MainActivity
 */
public class TutorialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        // Preferences
        SharedPreferences prefs_Read;
        SharedPreferences.Editor prefs_Write;

        // Initialize preference objects
        prefs_Read = getSharedPreferences(Constants.PREFS_APP_WELCOME, 0);
        prefs_Write = prefs_Read.edit();

        // We won't reopen this Tutorial ever
        prefs_Write.putBoolean(Constants.PREFS_APP_TUTORIAL, false);
        prefs_Write.apply();
    }

    /**
     * On click menu action
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tuto, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_read:
                Intent i = new Intent(TutorialActivity.this, MainActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
