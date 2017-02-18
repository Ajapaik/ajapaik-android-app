package ee.ajapaik.android.widget.util;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public abstract class OnSwipeTouchListener implements View.OnTouchListener {
    private static final float SWIPE_DISTANCE_THRESHOLD = 100.0F;
    private static final float SWIPE_VELOCITY_THRESHOLD = 100.0F;

    private final GestureDetector m_gestureDetector;
    private boolean m_enabled;

    public OnSwipeTouchListener(Context context) {
        this(context, SWIPE_DISTANCE_THRESHOLD, SWIPE_VELOCITY_THRESHOLD);
    }

    public OnSwipeTouchListener(Context context, float distanceThreshold, float velocityThreshold) {
        m_gestureDetector = new GestureDetector(context, new GestureListener(distanceThreshold, velocityThreshold));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();

        if(action == MotionEvent.ACTION_DOWN) {
            m_enabled = true;
        }

        if(m_enabled && event.getPointerCount() > 1) {
            m_enabled = false;
        }

        return (m_enabled) ? m_gestureDetector.onTouchEvent(event) : false;
    }

    public void onSingleTap() { }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private float m_distanceThreshold;
        private float m_velocityThreshold;

        public GestureListener(float distanceThreshold, float velocityThreshold) {
            m_distanceThreshold = distanceThreshold;
            m_velocityThreshold = velocityThreshold;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onSingleTap();

            return super.onSingleTapUp(e);
        }
    }
}
