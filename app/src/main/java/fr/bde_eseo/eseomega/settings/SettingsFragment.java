package fr.bde_eseo.eseomega.settings;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import fr.bde_eseo.eseomega.R;

/**
 * Created by Rascafr on 23/12/2015.
 * Manage app's settings :
 * - First window to display
 * - App Theme
 * - Update check ?
 * - Ingenews icon visibility
 * - Lydia account management
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }
}
