package ee.ajapaik.android.widget.util;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public abstract class OnScaleTouchListener implements View.OnTouchListener {
    private final ScaleGestureDetector m_gestureDetector;

    public OnScaleTouchListener(Context context) {
        m_gestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                OnScaleTouchListener.this.onScale(detector.getScaleFactor());

                return true;
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return m_gestureDetector.onTouchEvent(event);
    }

    public abstract void onScale(float scale);
}

/*

 */