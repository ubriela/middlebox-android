package middlebox.preferences;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import middlebox.R;
import middlebox.utils.CommonUtils;

public class SettingsPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Load the appropriate preferences
        getPreferenceManager().setSharedPreferencesName(CommonUtils.SHARED_PREFS);

        addPreferencesFromResource(R.xml.bs_preferences);

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        // Custom reset button
        final Preference reset = findPreference(getResources().getString(R.string.default_prefs));
        reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            public boolean onPreferenceClick(Preference p) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsPreferenceActivity.this);

                builder.setMessage("Are you sure you want to reset settings?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences sp = SettingsPreferenceActivity.this.getSharedPreferences(CommonUtils.SHARED_PREFS, MODE_PRIVATE);
                        sp.edit().clear().commit();
                        sp.edit().putInt(getResources().getString(R.string.default_prefs), 1).commit();
                        PreferenceManager.setDefaultValues(SettingsPreferenceActivity.this, R.xml.bs_preferences, true);
                        SettingsPreferenceActivity.this.finish();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the listener whenever a key changes
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences arg0, String key) {
    }

}