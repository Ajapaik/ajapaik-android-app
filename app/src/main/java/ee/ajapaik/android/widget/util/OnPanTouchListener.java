package ee.ajapaik.android.widget.util;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public abstract class OnPanTouchListener implements View.OnTouchListener {
    private final GestureDetector m_gestureDetector;

    public OnPanTouchListener(Context context) {
        m_gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return m_gestureDetector.onTouchEvent(event);
    }

    public abstract void onPan(float distanceX, float distanceY);

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            onPan(distanceX, distanceY);

            return true;
        }
    }
}
