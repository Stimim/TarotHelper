package com.stimim.tarothelper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.stimim.tarothelper.util.SystemUiHider;
import com.stimim.tarothelper.view.PlayGroundView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e. status bar and
 * navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class NewSpreadActivity extends Activity {
  private PlayGroundView mPlayGroundView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_new_spread);

    mPlayGroundView = (PlayGroundView) findViewById(R.id.play_ground);

    mPlayGroundView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        NewSpreadActivity.this.openOptionsMenu();
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();

    SharedPreferences sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(getApplication());
    boolean useReversedCards =
        sharedPreferences.getBoolean(getString(R.string.pref_use_reversed_card_key), false);
    mPlayGroundView.setCanReverseCards(useReversedCards);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.new_spread, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_draw_a_card:
        mPlayGroundView.drawCard();
        return true;
      case R.id.action_clear_play_ground:
        mPlayGroundView.reset();
        return true;
      case R.id.action_take_screenshot:
        mPlayGroundView.takeScreenshot(this);
        return true;
      case R.id.action_show_base_card:
        mPlayGroundView.showBaseCard();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
