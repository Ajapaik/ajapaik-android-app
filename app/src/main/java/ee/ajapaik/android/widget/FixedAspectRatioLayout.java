package ee.ajapaik.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import ee.ajapaik.android.R;

public class FixedAspectRatioLayout extends FrameLayout {
    private float m_aspectRatioWidth = 1.0F;
    private float m_aspectRatioHeight = 1.0F;
    private boolean m_aspectRatioVertical = true;

    public FixedAspectRatioLayout(Context context) {
        super(context);
    }

    public FixedAspectRatioLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        if(attrs != null) {
            readAttributes(context, attrs);
        }
    }

    public FixedAspectRatioLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if(attrs != null) {
            readAttributes(context, attrs);
        }
    }

    public boolean isAspectRatioVertical() {
        return m_aspectRatioVertical;
    }

    public float getAspectRatioWidth() {
        return m_aspectRatioWidth;
    }

    public void setAspectRatioWidth(float aspectRatioWidth) {
        m_aspectRatioWidth = aspectRatioWidth;
        m_aspectRatioHeight = 1.0F;
        m_aspectRatioVertical = false;
        requestLayout();
        invalidate();
    }

    public float getAspectRatioHeight() {
        return m_aspectRatioHeight;
    }

    public void setAspectRatioHeight(float aspectRatioHeight) {
        m_aspectRatioWidth = 1.0F;
        m_aspectRatioHeight = aspectRatioHeight;
        m_aspectRatioVertical = true;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);
        int calculatedWidth, calculatedHeight;

        if(m_aspectRatioVertical) {
            calculatedWidth = originalWidth;
            calculatedHeight = Math.round(originalWidth * m_aspectRatioHeight / m_aspectRatioWidth);
        } else {
            calculatedHeight = originalHeight;
            calculatedWidth = Math.round(originalHeight * m_aspectRatioWidth / m_aspectRatioHeight);
        }

        super.onMeasure(MeasureSpec.makeMeasureSpec(calculatedWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(calculatedHeight, MeasureSpec.EXACTLY));
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FixedAspectRatioLayout);

        m_aspectRatioWidth = a.getFloat(R.styleable.FixedAspectRatioLayout_aspectRatioWidth, m_aspectRatioWidth);
        m_aspectRatioHeight = a.getFloat(R.styleable.FixedAspectRatioLayout_aspectRatioHeight, m_aspectRatioHeight);
        a.recycle();
    }
}