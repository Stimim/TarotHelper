package com.stimim.tarothelper.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.stimim.tarothelper.card.Card;

public class PlayGroundView extends RelativeLayout {
  private HashMap<Card, CardAttribute> map;
  private HashMap<ImageView, Card> viewToCard;

  private List<Card> undrawedCards;
  private Card baseCard;
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
          if (x < 0) {
            x = 0;
          }
          if (y < 0) {
            y = 0;
          }

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

    baseCard = undrawedCards.get(undrawedCards.size() - 1);

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
      Toast toast = Toast.makeText(getContext(), "The deck is empty!", Toast.LENGTH_SHORT);
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

    public void showInDialog() {
      AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

      ImageView largeImage = new ImageView(getContext());

      builder.setView(largeImage);

      largeImage.setImageResource(card.imageNormal);

      if (reversed) {
        Bitmap normal = BitmapFactory.decodeResource(getResources(), card.imageNormal);
        Matrix matrix = new Matrix();
        matrix.setRotate(180, normal.getWidth() / 2, normal.getHeight() / 2);
        normal =
            Bitmap.createBitmap(normal, 0, 0, normal.getWidth(), normal.getHeight(), matrix, true);
        largeImage.setImageBitmap(normal);
      }

      builder.show();

    }

    public void onClick() {
      if (!revealed) {
        reveal();
      } else {
        showInDialog();
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
          if (selectedCard != null) {
            v.bringToFront();
          }
      }
      return false;
    }
  };

  private void takeScreenshotPhase2(final ScreenshotBundle bundle, Activity invoker) {
    Paint paint = new Paint();
    paint.setDither(true);
    paint.setFilterBitmap(true);

    ArrayList<Entry<Card, CardAttribute>> list = new ArrayList<Entry<Card, CardAttribute>>();
    for (Entry<Card, CardAttribute> e : map.entrySet()) {
      CardAttribute attr = e.getValue();
      if (attr.getImageView() != null) {
        list.add(e);
      }
    }

    Collections.sort(list, new Comparator<Entry<Card, CardAttribute>>() {
      @Override
      public int compare(Entry<Card, CardAttribute> lhs, Entry<Card, CardAttribute> rhs) {
        ImageView a = lhs.getValue().getImageView();
        ImageView b = rhs.getValue().getImageView();
        return PlayGroundView.this.indexOfChild(a) - PlayGroundView.this.indexOfChild(b);
      }
    });

    for (Entry<Card, CardAttribute> e : list) {
      Card card = e.getKey();
      CardAttribute attr = e.getValue();

      if (attr.getImageView() == null) {
        continue;
      }

      ImageView image = attr.getImageView();
      float left = image.getLeft() - bundle.left;
      float top = image.getTop() - bundle.top;
      left = left * bundle.scaleSmall2Normal * bundle.scaleNormal2Real;
      top = top * bundle.scaleSmall2Normal * bundle.scaleNormal2Real;

      Bitmap normal = BitmapFactory.decodeResource(getResources(), card.imageNormal);

      Rect src = new Rect(0, 0, normal.getWidth(), normal.getHeight());
      Rect dst = new Rect(0, 0,
          (int) (normal.getWidth() * bundle.scaleNormal2Real),
          (int) (normal.getHeight() * bundle.scaleNormal2Real));

      if (attr.isReversed()) {
        Matrix matrix = new Matrix();
        matrix.setRotate(180, normal.getWidth() / 2, normal.getHeight() / 2);
        normal =
            Bitmap.createBitmap(normal, 0, 0, normal.getWidth(), normal.getHeight(), matrix, true);
      }

      int saveCount = bundle.canvas.getSaveCount();
      bundle.canvas.save();
      bundle.canvas.translate(left, top);
      bundle.canvas.drawBitmap(normal, src, dst, paint);
      bundle.canvas.restoreToCount(saveCount);
    }

    if (!bundle.bitmap.compress(CompressFormat.PNG, 95, bundle.output)) {
      Toast.makeText(getContext(), "Failed to save file", Toast.LENGTH_SHORT).show();
    }

    invoker.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(getContext(), "Screenshot saved", Toast.LENGTH_SHORT).show();

        getContext().sendBroadcast(
            new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.parse("file://" + bundle.filepath)));
      }
    });
  }

  public void showBaseCard() {
    CardAttribute attribute = map.get(baseCard);

    if (attribute != null) {
      attribute.showInDialog();
    }
  }

  private void takeScreenshotPhase1(final Activity invoker) {
    /* Check if we can write to SD card */
    String state = Environment.getExternalStorageState();
    if (!Environment.MEDIA_MOUNTED.equals(state)) {
      Toast.makeText(getContext(), "Can't find any writable external storages",
          Toast.LENGTH_SHORT).show();
      return;
    }

    /* compute size */
    int bLeft = -1;
    int bTop = -1;
    int bRight = -1;
    int bBottom = -1;
    float bScaleSmall2Normal = -1;
    float bScaleNormal2Real = 1;

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

      if (bScaleSmall2Normal < 0) {
        int sH = BitmapFactory.decodeResource(getResources(), card.imageSmall).getHeight();
        int nH = BitmapFactory.decodeResource(getResources(), card.imageNormal).getHeight();

        bScaleSmall2Normal = (float) nH / (float) sH;
      }
    }

    if (bLeft == -1 || bTop == -1 || bRight == -1 || bBottom == -1) {
      return;
    }

    int width = (int) ((bRight - bLeft) * bScaleSmall2Normal);
    int height = (int) ((bBottom - bTop) * bScaleSmall2Normal);

    if (width * height > 786432) {
      bScaleNormal2Real = (float) Math.sqrt(786432.0 / (width * height));

      width = (int) (width * bScaleNormal2Real);
      height = (int) (height * bScaleNormal2Real);
    }

    final int left = bLeft;
    final int top = bTop;
    final float scaleSmall2Normal = bScaleSmall2Normal;
    final float scaleNormal2Real = bScaleNormal2Real;
    final Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
    final Canvas canvas = new Canvas(bitmap);

    /* Ask for a file name */
    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    final EditText editText = new EditText(getContext());
    final String defaultName = "NoName";
    editText.setText(defaultName);

    builder.setView(editText)
        .setTitle("Save as...")
        .setMessage("Save current spread in SD card")
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            String filename = editText.getText().toString().trim();

            if (filename.isEmpty()) {
              filename = defaultName;
            }

            File file;
            FileOutputStream output;
            try {
              file = new File(
                  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                  filename + ".png");
              output = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
              Toast.makeText(getContext(),
                  "Can't open a writable file.", Toast.LENGTH_SHORT).show();
              return;
            }

            final String msg = "Saving to " + filename + ".png";
            final ScreenshotBundle bundle =
                new ScreenshotBundle(output, file.getAbsolutePath(),
                    canvas, bitmap, left, top, scaleSmall2Normal,
                    scaleNormal2Real);

            dialog.dismiss();

            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();

            Thread thread = new Thread(new Runnable() {
              @Override
              public void run() {
                takeScreenshotPhase2(bundle, invoker);
              }
            });
            thread.start();
          }
        });
    builder.create().show();
  }

  public void takeScreenshot(final Activity invoker) {
    synchronized(invoker) {
      takeScreenshotPhase1(invoker);
    }
  }

  private static ImageView makeReversed(ImageView imageView) {
    imageView.setScaleType(ScaleType.MATRIX);
    Matrix matrix = new Matrix(imageView.getImageMatrix());
    matrix.postRotate(180, imageView.getWidth() / 2, imageView.getHeight() / 2);
    imageView.setImageMatrix(matrix);
    imageView.requestLayout();
    return imageView;
  }

  /**
   * Everything you need to take a screenshot
   */
  private class ScreenshotBundle {
    FileOutputStream output;
    String filepath;
    Canvas canvas;
    Bitmap bitmap;
    int left;
    int top;
    float scaleSmall2Normal;
    float scaleNormal2Real;

    ScreenshotBundle(FileOutputStream output, String filepath,
        Canvas canvas, Bitmap bitmap, int left, int top, float scaleS2N, float scaleN2R) {
      this.filepath = filepath;
      this.output = output;
      this.canvas = canvas;
      this.bitmap = bitmap;
      this.left = left;
      this.top = top;
      this.scaleSmall2Normal = scaleS2N;
      this.scaleNormal2Real = scaleN2R;
    }
  }
}
