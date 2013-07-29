package com.stimim.tarothelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {

  TextView buttonDrawCards;
  TextView buttonViewCards;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    buttonDrawCards = (TextView) findViewById(R.id.button_draw_cards);
    buttonDrawCards.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent newIntent = new Intent(view.getContext(), DrawCardsActivity.class);
        startActivity(newIntent);
      }
    });

    buttonViewCards = (TextView) findViewById(R.id.button_view_cards);
    buttonViewCards.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent newIntent = new Intent(view.getContext(), ViewCardsActivity.class);
        startActivity(newIntent);
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_settings:
        Intent newIntent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(newIntent);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

}
