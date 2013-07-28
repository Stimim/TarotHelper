package com.stimim.tarothelper;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    setupSimplePreferencesScreen();
  }

  /**
   * Shows the simplified settings UI if the device configuration if the device configuration
   * dictates that a simplified, single-pane UI should be shown.
   */
  @SuppressWarnings("deprecation")
  private void setupSimplePreferencesScreen() {
    addPreferencesFromResource(R.xml.preferences);
  }
}
