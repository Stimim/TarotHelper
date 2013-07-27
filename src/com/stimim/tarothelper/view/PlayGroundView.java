package com.stimim.tarothelper.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.stimim.tarothelper.card.Card;

public class PlayGroundView extends RelativeLayout {
  HashMap<Card, CardAttribute> map;
  HashMap<ImageView, Card> viewToCard;

  List<Card> undrawedCards;
  private boolean canReverseCards;
  private final Random random;
  private int nextIndex;

  private Card selectedCard;

  public PlayGroundView(Context context) {
    super(context);

    random = new Random();
    reset();
  }

  public PlayGroundView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PlayGroundView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    random = new Random();
    reset();
  }

  public void setCanReverseCards(boolean canReverseCards) {
    this.canReverseCards = canReverseCards;
  }

  private final OnTouchListener myOnTouchListener = new OnTouchListener() {
    float startX;
    float startY;
    @Override
    public boolean onTouch(View view, MotionEvent event) {
      float distance;
      switch (event.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
          startX = event.getX();
          startY = event.getY();
          break;
        case MotionEvent.ACTION_UP:
          distance = (float) Math.sqrt(
              Math.pow(startX - event.getX(), 2) + Math.pow(startY - event.getY(), 2));
          if (distance < 10) {
            if (selectedCard == null) {
              PlayGroundView.this.performClick();
              break;
            }
            CardAttribute a = map.get(selectedCard);
            if (a != null) {
              if (event.getEventTime() - event.getDownTime() < 1000) {
                a.onClick();
              }
            }
          }

          selectedCard = null;
          break;
        case MotionEvent.ACTION_CANCEL:
          selectedCard = null;
          break;
        case MotionEvent.ACTION_MOVE:
          if (selectedCard == null) {
            Log.d("MOVING", "No card selected");
            break;
          }
          Log.d("MOVING", selectedCard.toString());

          distance = (float) Math.sqrt(
              Math.pow(startX - event.getX(), 2) + Math.pow(startY - event.getY(), 2));

          if (distance < 20) {
            break;
          }
          int dx = (int) (event.getX());
          int dy = (int) (event.getY());
          CardAttribute attr = map.get(selectedCard);
          if (attr == null) {
            Log.d("CAN'T FIND ATTR", "Can't find attr for " + selectedCard);
            return false;
          }

          int w = PlayGroundView.this.getWidth();
          int h = PlayGroundView.this.getHeight();

          w -= attr.getImageView().getWidth();
          h -= attr.getImageView().getHeight();

          attr.placeCard(Math.min(dx, w), Math.min(dy, h));

          PlayGroundView.this.requestLayout();
          break;
      }
      return true;
    }
  };

  public void reset() {
    removeAllViews();
    if (map == null) {
      map = new HashMap<Card, CardAttribute>();
    }
    if (viewToCard == null) {
      viewToCard = new HashMap<ImageView, Card>();
    }

    undrawedCards = Arrays.asList(Card.tarotCards());
    for (Card card : undrawedCards) {
      map.put(card, new CardAttribute(card));
    }
    viewToCard.clear();

    Collections.shuffle(undrawedCards);
    nextIndex = 0;

    setOnTouchListener(myOnTouchListener);
  }

  public void drawCard() {
    int x = getWidth() / 2;
    int y = getHeight() / 2;

    drawCard(x, y);
  }

  public void drawCard(int x, int y) {
    if (nextIndex < undrawedCards.size()) {
      Card card = undrawedCards.get(nextIndex++);
      CardAttribute attribute = map.get(card);
      attribute.placeCard(x, y);
    } else {
      Toast toast = Toast.makeText(getContext(), "The deck is empty!", Toast.LENGTH_LONG);
      toast.show();
    }
  }

  class CardAttribute {
    private boolean revealed;
    private final boolean reversed;
    private ImageView imageView;
    private final Card card;
    private final RelativeLayout.LayoutParams layoutParams;

    CardAttribute(Card card) {
      reversed = canReverseCards ? random.nextBoolean() : false;
      revealed = false;
      this.card = card;

      layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
          LayoutParams.WRAP_CONTENT);
      layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
      layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
    }

    public void onClick() {
      if (!revealed) {
        reveal();
      } else {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

//        Dialog dialog = new Dialog(getContext());

        ImageView largeImage = new ImageView(getContext());
        largeImage.setImageResource(card.imageNormal);

//        ViewGroup.LayoutParams params =
//            new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT);

//        dialog.addContentView(largeImage, params);

        builder.setView(largeImage);
        builder.show();
//        dialog.show();
      }
    }

    public void reveal() {
      // already revealed
      if (revealed) {
        return;
      }

      revealed = true;
      setCardImage();
    }

    private void setCardImage() {
      imageView.setImageResource(card.imageSmall);

      if (reversed) {
        imageView.setRotation(180);
      }
    }

    public boolean isRevealed() {
      return revealed;
    }

    public boolean isReversed() {
      return reversed;
    }

    private void createImageView() {
      imageView = new ImageView(getContext());
      imageView.setOnTouchListener(cardOnTouchListener);
      if (revealed) {
        setCardImage();
      } else {
        imageView.setImageResource(Card.BACK.imageSmall);
      }

      PlayGroundView.this.addView(imageView, layoutParams);
      PlayGroundView.this.viewToCard.put(imageView, card);
    }

    public void placeCard(int x, int y) {
      Log.d("PLACE CARD", String.format("place card @ (%d, %d)", x, y));
      if (imageView == null) {
        // this card is not on the table
        createImageView();
      }

      layoutParams.setMargins(x, y, 0, 0);
      imageView.setLayoutParams(layoutParams);
      imageView.requestLayout();
    }

    public ImageView getImageView() {
      return imageView;
    }
  }

  private final OnTouchListener cardOnTouchListener = new OnTouchListener() {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
      switch (event.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
          selectedCard = viewToCard.get(v);

          if (selectedCard != null) {
            Log.d("SELECT CARD", selectedCard.toString());
          } else {
            Log.d("SELECT CARD", "null");
          }
      }
      return false;
    }
  };
}
