package com.jujujuijk.android.tools;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Scroller;

public class PinchImageView extends ImageView implements OnTouchListener {

	public static final int GROW = 0;
	public static final int SHRINK = 1;

	public static final int DURATION = 100;
	public static final int TOUCH_INTERVAL_MIN = 10;
	public static final int TOUCH_INTERVAL_DOUBLECLICK = 300;

	public static final float MIN_SCALE = 1f;
	public static final float MAX_SCALE = 3.5f;
	public static final float ZOOM = 0.08f;

	private int screenW, screenH;
	private float zoomPosX = 0.5f, zoomPosY = 0.5f;

	private static int _interpolator = android.R.anim.accelerate_interpolator;

	ImageView im = null;

	float xPre, yPre, xSec, ySec;
	float distDelta, distPre = 0;
	float xScale = 1.0f, yScale = 1.0f;

	int mTouchSlop;
	long mLastGestureTime;
	Paint mPaint;
	Scroller mScroller;

	boolean waitin = false; // true if we're waiting for double click

	public PinchImageView(Context context, AttributeSet attr) {
		super(context, attr);
		_init();
	}

	public PinchImageView(Context context) {
		super(context);
		_init();
	}

	public PinchImageView(ImageView im) {
		super(im.getContext());
		_init();
		this.im = im;
		this.im.setOnTouchListener(this);
	}

	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction() & MotionEvent.ACTION_MASK;

		switch (action) {
		case MotionEvent.ACTION_MOVE:
			_doActionMove(event);
			break;
		case MotionEvent.ACTION_DOWN:
			_doActionDown(event);
		default:
			if (xScale <= 1) // No zoom
				super.onTouchEvent(event);
			break;
		}
		mLastGestureTime = android.os.SystemClock.uptimeMillis();
		return true;
	}

	private void _doActionMove(MotionEvent event) {

		int p_count = event.getPointerCount();
		// point 1 coords
		float xCur = event.getX(0);
		float yCur = event.getY(0);
		float distCur = distPre;

		if (p_count > 1) {
			// point 2 coords
			xSec = event.getX(1);
			ySec = event.getY(1);

			// distance between
			distCur = (float) Math.sqrt(Math.pow(xSec - xCur, 2)
					+ Math.pow(ySec - yCur, 2));

			distDelta = distCur - distPre;

			// float rate = ZOOM;
			float rate = ZOOM * (Math.abs(distDelta) > 100 ? 2 : 1);
			long now = android.os.SystemClock.uptimeMillis();
			if (distPre != 0 && now - mLastGestureTime > TOUCH_INTERVAL_MIN
					&& Math.abs(distDelta) > mTouchSlop
					&& Math.abs(distDelta) < 40) {
				mLastGestureTime = 0;

				// Get the center position where we want to zoom
				float posX = ((xSec + xCur) / 2) / screenW;
				float posY = ((ySec + yCur) / 2) / screenH;
				posX = posX < 0 ? 0 : posX > 1 ? 1 : posX;
				posY = posY < 0 ? 0 : posY > 1 ? 1 : posY;

				ScaleAnimation scale = null;
				int mode = distDelta > 0 ? GROW : (distCur == distPre ? 2
						: SHRINK);
				switch (mode) {
				case GROW: // grow
					if (xScale < MAX_SCALE) {
						rate = (xScale + rate > MAX_SCALE) ? (MAX_SCALE - xScale)
								: (rate);
						scale = new ScaleAnimation(xScale, xScale += rate,
								yScale, yScale += rate,
								ScaleAnimation.RELATIVE_TO_SELF,
								rate < 0 ? zoomPosX : (zoomPosX = posX),
								ScaleAnimation.RELATIVE_TO_SELF,
								rate < 0 ? zoomPosY : (zoomPosY = posY));
					}
					break;
				case SHRINK: // shrink
					if (xScale > MIN_SCALE) {
						rate = (xScale - rate < MIN_SCALE) ? (xScale - MIN_SCALE)
								: (rate);
						scale = new ScaleAnimation(xScale, xScale -= rate,
								yScale, yScale -= rate,
								ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
								ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
					}
					break;
				}

                _startAnimation(scale);
			}
		} // else if (xScale > 1) {
			// // translate
			//
			// float xDelta = xPre - xCur, yDelta = yPre - yCur;
			// im.scrollBy((int) (xDelta / xScale - 1.0), (int) (yDelta
			// / yScale - 1.0));
			//
			// // // mScroller.startScroll((int)xPre, (int)yPre,
			// // (int)xDelta,
			// // (int)yDelta);
			// /*
			// * TranslateAnimation scroll = new
			// * TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0,
			// * Animation.RELATIVE_TO_PARENT, -xDelta,
			// * Animation.RELATIVE_TO_PARENT, 0,
			// * Animation.RELATIVE_TO_PARENT, -yDelta);
			// * scroll.setDuration(50); scroll.setFillAfter(true);
			// * scroll.setInterpolator(getContext(), _interpolator);
			// */
			// // im.startAnimation(scroll);
			//
			// // im.layout(im.getLeft() - (int)xDelta, im.getTop() -
			// // (int)yDelta, im.getRight() - (int)xDelta,
			// // im.getBottom() -
			// // (int)yDelta);
			// }

		xPre = xCur;
		yPre = yCur;
		distPre = distCur;
	}

	private void _doActionDown(MotionEvent event) {
		// point 1 coords
		float xCur = event.getX(0);
		float yCur = event.getY(0);
		long now = android.os.SystemClock.uptimeMillis();

		if (waitin == true // Handles double click to zoom in/out
				&& now - mLastGestureTime <= TOUCH_INTERVAL_DOUBLECLICK
				&& Math.abs(xCur - xPre) + Math.abs(yCur - yPre) < 50) {
			float rate = 1.5f;

			ScaleAnimation scale;

			if (xScale > 1) // zoomed in. Go back at MIN_SCALE (1)
				rate = MIN_SCALE - xScale; // negative value here, thats
											// what we want
			else
				// no zoom. Lets run a 'classic' zoom
				rate = (xScale + rate > MAX_SCALE) ? (MAX_SCALE - xScale)
						: (rate);

			float posX = xCur / screenW;
			float posY = yCur / screenH;
			posX = posX < 0 ? 0 : posX > 1 ? 1 : posX;
			posY = posY < 0 ? 0 : posY > 1 ? 1 : posY;

			scale = new ScaleAnimation(xScale, xScale += rate, yScale,
					yScale += rate, ScaleAnimation.RELATIVE_TO_SELF,
					rate < 0 ? zoomPosX : (zoomPosX = posX),
					ScaleAnimation.RELATIVE_TO_SELF, rate < 0 ? zoomPosY
							: (zoomPosY = posY));

            _startAnimation(scale);
			// im.scrollTo(0, 0);
			// im.scrollBy((int) (xDelta / xScale - 1.0), (int) (yDelta
			// / yScale - 1.0));
			waitin = false;
		} else {
			// if (xScale > 1)
			// _replaceCenter(event);
			waitin = true;
		}

		xPre = xCur;
		yPre = yCur;
	}

    private void _startAnimation(ScaleAnimation scale) {
        if (scale != null) {
//            if (xScale > 1) {
//                setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//            } else {
//                setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
//            }
            scale.setDuration(DURATION);
            scale.setFillAfter(true);
            scale.setInterpolator(getContext(), _interpolator);

            im.startAnimation(scale);
        }
    }

	private void _init() {
		im = this;
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mScroller = new Scroller(getContext());
		mLastGestureTime = android.os.SystemClock.uptimeMillis();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return this.onTouchEvent(event);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		screenW = w;
		screenH = h;
	}

	public boolean isZoomed() {
		return xScale > 1;
	}

	// private void _replaceCenter(MotionEvent event) {
	// final float xCur = event.getX(0);
	// final float yCur = event.getY(0);
	//
	// new Thread(new Runnable() {
	// public void run() {
	// try {
	// Thread.sleep(30);
	// long firstGestureTime = mLastGestureTime;
	// long delay = 20;
	//
	// for (long total = 0; total <= TOUCH_INTERVAL_DOUBLECLICK; total += delay)
	// {
	// Thread.sleep(delay);
	// if (mLastGestureTime != firstGestureTime)
	// return; // we quickly got an other click after the
	// // first. Dont move
	// }
	// } catch (InterruptedException e) {
	// Toast.makeText(getContext(), "wait() fail", 0).show();
	// e.printStackTrace();
	// }
	// // We can move
	// // Looper.prepare();
	// // Toast.makeText(getContext(), "Je move", 0).show();
	//
	// Log.v("BOAP", "BOAAAAAP");
	// float posX = xCur / screenW;
	// float posY = yCur / screenH;
	// posX = posX < 0 ? 0 : posX > 1 ? 1 : posX;
	// posY = posY < 0 ? 0 : posY > 1 ? 1 : posY;
	//
	// ScaleAnimation scale = new ScaleAnimation(xScale, xScale,
	// yScale, yScale, ScaleAnimation.RELATIVE_TO_SELF, 0,
	// ScaleAnimation.RELATIVE_TO_SELF, 0);
	//
	// if (scale != null) {
	// scale.setDuration(DURATION);
	// scale.setFillAfter(true);
	// scale.setInterpolator(getContext(), _interpolator);
	//
	// im.startAnimation(scale);
	// }
	// }
	// }).start();
	// }
}
