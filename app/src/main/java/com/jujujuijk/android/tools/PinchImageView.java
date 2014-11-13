package com.jujujuijk.android.tools;

import android.content.Context;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Scroller;
import android.view.ScaleGestureDetector;
import android.graphics.Canvas;

import android.util.AttributeSet;

public class PinchImageView extends ImageView {

    public static final float MIN_SCALE = 1f;
    public static final float MAX_SCALE = 3.5f;

    private static final int INVALID_POINTER_ID = -1;

    private float mPosX;
    private float mPosY;

    private float mLastTouchX;
    private float mLastTouchY;
    private float mLastGestureX;
    private float mLastGestureY;
    private int mActivePointerId = INVALID_POINTER_ID;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

    public PinchImageView(Context context) {
        super(context);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public PinchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                if (!mScaleDetector.isInProgress()) {
                    final float x = ev.getX();
                    final float y = ev.getY();

                    mLastTouchX = x;
                    mLastTouchY = y;
                    mActivePointerId = ev.getPointerId(0);
                }
                break;
            }
            case (MotionEvent.ACTION_POINTER_INDEX_MASK | MotionEvent.ACTION_POINTER_DOWN): {
                if (mScaleDetector.isInProgress()) {
                    final float gx = mScaleDetector.getFocusX();
                    final float gy = mScaleDetector.getFocusY();
                    mLastGestureX = gx;
                    mLastGestureY = gy;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {

                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!mScaleDetector.isInProgress()) {
                    final int pointerIndex = ev.findPointerIndex(mActivePointerId);
                    final float x = ev.getX(pointerIndex);
                    final float y = ev.getY(pointerIndex);

                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    mPosX += dx;
                    mPosY += dy;

                    invalidate();

                    mLastTouchX = x;
                    mLastTouchY = y;
                }
                else{
                    final float gx = mScaleDetector.getFocusX();
                    final float gy = mScaleDetector.getFocusY();

                    final float gdx = gx - mLastGestureX;
                    final float gdy = gy - mLastGestureY;

                    mPosX += gdx;
                    mPosY += gdy;

                    invalidate();

                    mLastGestureX = gx;
                    mLastGestureY = gy;
                }

                break;
            }
            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {

                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                else{
                    final int tempPointerIndex = ev.findPointerIndex(mActivePointerId);
                    mLastTouchX = ev.getX(tempPointerIndex);
                    mLastTouchY = ev.getY(tempPointerIndex);
                }

                break;
            }
        }

        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {

        canvas.save();

//        if (mScaleFactor > 1)
//            canvas.translate(mPosX, mPosY);

        if (mScaleDetector.isInProgress()) {
            canvas.scale(mScaleFactor, mScaleFactor, mScaleDetector.getFocusX(), mScaleDetector.getFocusY());
        }
        else{
            canvas.scale(mScaleFactor, mScaleFactor, mLastGestureX, mLastGestureY);
        }

        super.onDraw(canvas);
        canvas.restore();
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(MIN_SCALE, Math.min(mScaleFactor, MAX_SCALE));

            invalidate();
            return true;
        }
    }

    public boolean isZoomed() {
        return mScaleFactor > 1;
    }
}
