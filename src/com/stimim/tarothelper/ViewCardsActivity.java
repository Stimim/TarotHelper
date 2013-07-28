package com.stimim.tarothelper;

import java.util.ArrayList;
import java.util.Arrays;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.stimim.tarothelper.card.Card;
import com.stimim.tarothelper.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class ViewCardsActivity extends Activity {
  /**
   * Whether or not the system UI should be auto-hidden after
   * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
   */
  private static final boolean AUTO_HIDE = true;

  /**
   * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after user
   * interaction before hiding the system UI.
   */
  private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

  /**
   * The flags to pass to {@link SystemUiHider#getInstance}.
   */
  private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

  /**
   * The instance of the {@link SystemUiHider} for this activity.
   */
  private SystemUiHider mSystemUiHider;

  private Card mCard;

  private ScrollView mImageScrollView;
  private ImageView mCardImage;
  private LinearLayout mLayoutButtons;
  private ImageView mButtonMajor;
  private ImageView mButtonCups;
  private ImageView mButtonWands;
  private ImageView mButtonSwords;
  private ImageView mButtonPentacles;
  private ScrollView.LayoutParams mDefaultLayoutParams;

  private void setCard(Card card) {
    if (card == mCard) {
      return;
    }

    mCard = card;

    mCardImage.setLayoutParams(mDefaultLayoutParams);
    mCardImage.setImageResource(mCard.imageNormal);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_view_cards);

    mDefaultLayoutParams = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT,
        ScrollView.LayoutParams.WRAP_CONTENT,
        Gravity.CENTER);

    onImageTouch = new ImageTouchListener(getWindowManager().getDefaultDisplay());

    mImageScrollView = (ScrollView) findViewById(R.id.image_scroll_view);
    mCardImage = (ImageView) findViewById(R.id.card_image);
    mLayoutButtons = (LinearLayout) findViewById(R.id.layout_buttons);
    mButtonMajor = (ImageView) findViewById(R.id.button_major);
    mButtonCups = (ImageView) findViewById(R.id.button_cups);
    mButtonWands = (ImageView) findViewById(R.id.button_wands);
    mButtonSwords = (ImageView) findViewById(R.id.button_swords);
    mButtonPentacles = (ImageView) findViewById(R.id.button_pentacles);

    setCard(Card.BACK);

    mButtonMajor.setOnClickListener(new ShortCutOnClickListener(Card.THE_FOOL));

    mButtonCups.setOnClickListener(new ShortCutOnClickListener(Card.CUP_ACE));

    mButtonWands.setOnClickListener(new ShortCutOnClickListener(Card.WAND_ACE));

    mButtonSwords.setOnClickListener(new ShortCutOnClickListener(Card.SWORD_ACE));

    mButtonPentacles.setOnClickListener(new ShortCutOnClickListener(Card.PENTACLE_ACE));

    mImageScrollView.addTouchables(new ArrayList<View>(Arrays.asList(mCardImage)));
    mImageScrollView.setOnTouchListener(onImageTouch);
    mImageScrollView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mSystemUiHider.show();
      }
    });

    // Set up an instance of SystemUiHider to control the system UI for
    // this activity.
    mSystemUiHider = SystemUiHider.getInstance(this, mCardImage, HIDER_FLAGS);
    mSystemUiHider.setup();
    mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
      // Cached values.
      int mControlsHeight;
      int mShortAnimTime;

      @Override
      @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
      public void onVisibilityChange(boolean visible) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
          // If the ViewPropertyAnimator API is available
          // (Honeycomb MR2 and later), use it to animate the
          // in-layout UI controls at the bottom of the
          // screen.
          if (mControlsHeight == 0) {
            mControlsHeight = mLayoutButtons.getHeight();
          }
          if (mShortAnimTime == 0) {
            mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
          }
          mLayoutButtons.animate().translationY(visible ? 0 : mControlsHeight)
              .setDuration(mShortAnimTime);
        } else {
          // If the ViewPropertyAnimator APIs aren't
          // available, simply show or hide the in-layout UI
          // controls.
          mLayoutButtons.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        if (visible && AUTO_HIDE) {
          // Schedule a hide().
          delayedHide(AUTO_HIDE_DELAY_MILLIS);
        }
      }
    });
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    // Trigger the initial hide() shortly after the activity has been
    // created, to briefly hint to the user that UI controls
    // are available.
    delayedHide(100);
  }

  /**
   * Touch listener to use for in-layout UI controls to delay hiding the system
   * UI. This is to prevent the jarring behavior of controls going away while
   * interacting with activity UI.
   */
  View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
      if (AUTO_HIDE) {
        delayedHide(AUTO_HIDE_DELAY_MILLIS);
      }
      return false;
    }
  };

  Handler mHideHandler = new Handler();
  Runnable mHideRunnable = new Runnable() {
    @Override
    public void run() {
      mSystemUiHider.hide();
    }
  };

  private OnTouchListener onImageTouch;

  class ImageTouchListener implements OnTouchListener {
    private float singlePointDownX;
    private float singlePointDownY;
    private final float threshold;

    public ImageTouchListener(Display display) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
        Point size = new Point();
        display.getSize(size);
        threshold = (float) (size.x * 0.3);
      } else {
        threshold = (float) (display.getWidth() * 0.3);
      }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
      int pointerCount = event.getPointerCount();
      if (pointerCount == 1) {
        switch (event.getActionMasked()) {
          case MotionEvent.ACTION_DOWN:
            // case MotionEvent.ACTION_POINTER_DOWN:
            singlePointDownX = event.getX();
            singlePointDownY = event.getY();
            return true;
            // case MotionEvent.ACTION_MOVE:
            // return true;
          case MotionEvent.ACTION_UP:
            float deltaX = event.getX() - singlePointDownX;
            float deltaY = event.getY() - singlePointDownY;
            float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            if (deltaX > threshold) {
              // previous card
              setCard(mCard.prevCard());
              return true;
            } else if (deltaX < -threshold) {
              // next card
              setCard(mCard.nextCard());
              return true;
            } else if (distance < 10) {
              if (event.getEventTime() - event.getDownTime() < 1000) {
                v.performClick();
              } else {
                v.performLongClick();
              }
            }
            break;
        }
      }
      return false;
    }
  };

  class ShortCutOnClickListener implements OnClickListener {
    private final Card target;

    public ShortCutOnClickListener(Card target) {
      this.target = target;
    }

    @Override
    public void onClick(View v) {
      setCard(target);

      delayedHide(AUTO_HIDE_DELAY_MILLIS);
    }
  }

  /**
   * Schedules a call to hide() in [delay] milliseconds, canceling any
   * previously scheduled calls.
   */
  private void delayedHide(int delayMillis) {
    mHideHandler.removeCallbacks(mHideRunnable);
    mHideHandler.postDelayed(mHideRunnable, delayMillis);
  }
}
