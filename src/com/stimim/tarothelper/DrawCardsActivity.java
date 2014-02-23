package com.stimim.tarothelper;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.stimim.tarothelper.card.Card;
import com.stimim.tarothelper.util.SystemUiHider;
import com.stimim.tarothelper.view.PlayGroundView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e. status bar and
 * navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class DrawCardsActivity extends Activity {
  private PlayGroundView mPlayGroundView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_draw_cards);

    mPlayGroundView = (PlayGroundView) findViewById(R.id.play_ground);

    mPlayGroundView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        DrawCardsActivity.this.openOptionsMenu();
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
    getMenuInflater().inflate(R.menu.draw_cards, menu);
    return true;
  }

  private boolean chooseACard() {
    final ArrayList<Card> cards = mPlayGroundView.getUndrawedCards();

    if (cards.isEmpty()) {
      onDeckEmpty();
      return false;
    }

    final Card[] card = new Card[1];
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.title_dialog_choose_card)
        .setSingleChoiceItems(Card.toString(cards), -1,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                card[0] = cards.get(which);
              }
        })
        .setPositiveButton("Not Reversed", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mPlayGroundView.chooseCard(card[0], false);
            dialog.dismiss();
          }
        })
        .setNeutralButton("Reversed", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mPlayGroundView.chooseCard(card[0], true);
            dialog.dismiss();
          }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        });

    builder.show();

    return false;
  }

  private void onDeckEmpty() {
    Toast toast = Toast.makeText(this, "The deck is empty!", Toast.LENGTH_SHORT);
    toast.show();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_draw_a_card:
        if (!mPlayGroundView.drawCard()) {
          onDeckEmpty();
        }
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
      case R.id.action_choose_a_card:
        chooseACard();
        return true;
      case R.id.action_shuffle_cards:
        mPlayGroundView.shuffleCards();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
