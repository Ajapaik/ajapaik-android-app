package ee.ajapaik.android.widget.util;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public abstract class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector m_gestureDetector;
    private boolean m_enabled;

    public OnSwipeTouchListener(Context context) {
        m_gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();

        if(action == MotionEvent.ACTION_DOWN) {
            m_enabled = true;
        }

        return (m_enabled) && m_gestureDetector.onTouchEvent(event);
    }

    public void onSingleTap() { }
    public void onLongHold(MotionEvent e) { }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        GestureListener() { }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onSingleTap();

            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            onLongHold(e);
            super.onLongPress(e);
        }
    }
}
