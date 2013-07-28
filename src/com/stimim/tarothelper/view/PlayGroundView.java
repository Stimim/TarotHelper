package com.stimim.tarothelper.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
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
    if (this.canReverseCards == canReverseCards) {
      return;
    }
    this.canReverseCards = canReverseCards;
    reset();
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
            break;
          }

          distance = (float) Math.sqrt(
              Math.pow(startX - event.getX(), 2) + Math.pow(startY - event.getY(), 2));

          if (distance < 20) {
            break;
          }
          int x = (int) (event.getX());
          int y = (int) (event.getY());
          CardAttribute attr = map.get(selectedCard);
          if (attr == null) {
            return false;
          }

          int w = PlayGroundView.this.getWidth();
          int h = PlayGroundView.this.getHeight();

          w -= attr.getImageView().getWidth();
          h -= attr.getImageView().getHeight();
          x -= attr.getImageView().getWidth() / 2;
          y -= attr.getImageView().getHeight() / 2;

          attr.placeCard(Math.min(x, w), Math.min(y, h));

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
    drawCard(0, 0);
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

        RelativeLayout layout = new RelativeLayout(getContext());
        ImageView largeImage = new ImageView(getContext());
        largeImage.setImageResource(card.imageNormal);

        RelativeLayout.LayoutParams params =
            new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layout.addView(largeImage, params);

        // if (reversed) {
        //  makeReversed(largeImage);
        // }

        layout.requestLayout();

        builder.setView(layout);
        builder.show();
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
        makeReversed(imageView);
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
      }
      return false;
    }
  };

  public void takeScreenshot() {
    String state = Environment.getExternalStorageState();
    if (!Environment.MEDIA_MOUNTED.equals(state)) {
      Toast.makeText(getContext(), "Can't find any writable external storages", Toast.LENGTH_LONG)
          .show();
      return;
    }

    int bLeft = -1;
    int bTop = -1;
    int bRight = -1;
    int bBottom = -1;
    float scale = -1;

    for (Entry<Card, CardAttribute> e : map.entrySet()) {
      CardAttribute attr = e.getValue();
      Card card = e.getKey();
      if (attr.getImageView() == null) {
        continue;
      }

      ImageView image = attr.getImageView();
      if (bLeft == -1 || bLeft > image.getLeft()) {
        bLeft = image.getLeft();
      }

      if (bTop == -1 || bTop > image.getTop()) {
        bTop = image.getTop();
      }

      if (bRight == -1 || bRight < image.getRight()) {
        bRight = image.getRight();
      }

      if (bBottom == -1 || bBottom < image.getBottom()) {
        bBottom = image.getBottom();
      }

      if (scale < 0) {
        int sH = BitmapFactory.decodeResource(getResources(), card.imageSmall).getHeight();
        int nH = BitmapFactory.decodeResource(getResources(), card.imageNormal).getHeight();

        scale = (float) nH / (float) sH;
      }
    }

    if (bLeft == -1 || bTop == -1 || bRight == -1 || bBottom == -1) {
      return;
    }

    FileOutputStream output;
    try {
      File file =
          new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
              "TarotHelperScreenshot.png");
      output = new FileOutputStream(file);
    } catch (FileNotFoundException e) {
      Toast.makeText(getContext(), "Can't open a writable file.", Toast.LENGTH_SHORT).show();
      return;
    }


    int width = (int) ((bRight - bLeft) * scale);
    int height = (int) ((bBottom - bTop) * scale);

    Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);

    Paint paint = new Paint();
    paint.setDither(true);
    paint.setFilterBitmap(true);

    for (Entry<Card, CardAttribute> e : map.entrySet()) {
      Card card = e.getKey();
      CardAttribute attr = e.getValue();

      if (attr.getImageView() == null) {
        continue;
      }

      ImageView image = attr.getImageView();
      float left = image.getLeft() - bLeft;
      float top = image.getTop() - bTop;
      left = left * scale;
      top = top * scale;

      Bitmap normal = BitmapFactory.decodeResource(getResources(), card.imageNormal);

      Rect src = new Rect(0, 0, normal.getWidth(), normal.getHeight());

      if (attr.isReversed()) {
        Matrix matrix = new Matrix();
        if (attr.isReversed()) {
          matrix.setRotate(180, normal.getWidth() / 2, normal.getHeight() / 2);
        }
        normal =
            Bitmap.createBitmap(normal, 0, 0, normal.getWidth(), normal.getHeight(), matrix, true);
      }

      int saveCount = canvas.getSaveCount();
      canvas.save();
      canvas.translate(left, top);
      canvas.drawBitmap(normal, src, src, paint);
      canvas.restoreToCount(saveCount);
    }

    if (!bitmap.compress(CompressFormat.PNG, 95, output)) {
      Toast.makeText(getContext(), "Failed to save file", Toast.LENGTH_SHORT).show();
    }

    Toast.makeText(getContext(), "Screenshot saved", Toast.LENGTH_SHORT).show();
    return;
  }

  private static ImageView makeReversed(ImageView imageView) {
    imageView.setScaleType(ScaleType.MATRIX);
    Matrix matrix = new Matrix(imageView.getImageMatrix());
    matrix.setRotate(180, imageView.getWidth() / 2, imageView.getHeight() / 2);
    imageView.setImageMatrix(matrix);
    imageView.requestLayout();
    return imageView;
  }
}
