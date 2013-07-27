package com.stimim.tarothelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {

  TextView buttonNewSpread;
  TextView buttonViewCards;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    buttonNewSpread = (TextView) findViewById(R.id.buttonNewSpread);
    buttonNewSpread.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent newIntent = new Intent(view.getContext(), NewSpreadActivity.class);
        startActivity(newIntent);
      }
    });

    buttonViewCards = (TextView) findViewById(R.id.buttonViewCards);
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

}
