package ee.ajapaik.android.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;

import ee.ajapaik.android.WebService;
import ee.ajapaik.android.util.Bitmaps;
import ee.ajapaik.android.util.Objects;
import ee.ajapaik.android.util.WebActivity;
import ee.ajapaik.android.util.WebImage;

public class WebImageView extends android.support.v7.widget.AppCompatImageView {
    private static final String TAG = "WebImageView";

    private static final String NAMESPACE = "http://schemas.android.com/apk/res/android";
    private static final String KEY_SRC = "src";

    private static final float SCALE_MIN = 0.3F;
    private static final float SCALE_MAX = 4.0F;

    private static final int INVALID_RESOURCE_ID = 0;

    private boolean m_attachedToWindow = false;
    private WebService.Connection m_connection;
    private boolean m_flipped = false;
    private PointF m_offset = null;
    private float m_scale = 1.0F;
    private WebImage m_image;
    private int m_placeholderResourceId = INVALID_RESOURCE_ID;
    private OnLoadListener m_loadListener;
    private Uri m_uri;
    private int m_width = 2;
    private int m_height = 1;

    public WebImageView(Context context) {
        super(context);
    }

    public WebImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if(attrs != null) {
            readAttributes(context, attrs);
        }
    }

    public WebImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if(attrs != null) {
            readAttributes(context, attrs);
        }
    }

    public boolean isFlipped() {
        return m_flipped;
    }

    public void setFlipped(boolean flipped) {
        if(m_flipped != flipped) {
            m_flipped = flipped;
            invalidateBitmap();
        }
    }

    public int getDrawableWidth() {
        return m_width;
    }

    public int getDrawableHeight() {
        return m_height;
    }


    public PointF getOffset() {
        return m_offset;
    }

    public void setOffset(PointF offset) {
        if(m_offset != offset && (m_offset == null || offset == null || !m_offset.equals(offset))) {
            m_offset = offset;

            setScaleType(ScaleType.MATRIX);
            requestLayout();
            invalidate();
        }
    }

    public float getScale() {
        return m_scale;
    }

    public void setScale(float scale) {
        if(scale < SCALE_MIN) {
            scale = SCALE_MIN;
        } else if(scale > SCALE_MAX) {
            scale = SCALE_MAX;
        }

        if(m_scale != scale) {
            m_scale = scale;

            setScaleType(ScaleType.MATRIX);
            requestLayout();
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(getScaleType() == ScaleType.MATRIX) {
            final Drawable drawable = getDrawable();

            if(drawable != null) {
                float viewHeight = getMeasuredHeight();
                float viewWidth = getMeasuredWidth();
                float drawableWidth = drawable.getIntrinsicWidth();
                float drawableHeight = drawable.getIntrinsicHeight();
                Matrix matrix = new Matrix();

                matrix.setRectToRect(
                        new RectF(0.0F, 0.0F, drawableWidth, drawableHeight),
                        new RectF(0.0F, 0.0F, viewWidth, viewHeight),
                        Matrix.ScaleToFit.CENTER);
                matrix.postScale(m_scale, m_scale, Math.round(0.5F * viewWidth), Math.round(0.5F * viewHeight));

                if(m_offset != null) {
                    matrix.postTranslate(m_offset.x, m_offset.y);
                }

                setImageMatrix(matrix);
                requestLayout();
            }
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        if(!Objects.match(m_uri, uri)) {
            stopLoadingImage();

            m_uri = uri;

            if(m_attachedToWindow) {
                startLoadingImage();
            }

            if(m_placeholderResourceId != INVALID_RESOURCE_ID && (m_image == null || m_image.getDrawable() == null)) {
                setImageResource(m_placeholderResourceId);

                if(m_loadListener != null) {
                    m_loadListener.onImageUnloaded();
                }
            }
        }
    }

    public void setOnLoadListener(OnLoadListener loadListener) {
        m_loadListener = loadListener;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        m_attachedToWindow = true;
        startLoadingImage();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopLoadingImage();
        m_attachedToWindow = false;
        super.onDetachedFromWindow();
    }

    private void invalidateBitmap() {
        Drawable drawable;

        if(m_image != null && (drawable = m_image.getDrawable()) != null) {
            if(m_flipped) {
                Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

                setImageBitmap(Bitmaps.flip(bitmap, Bitmaps.FLIP_HORIZONTAL));
            } else {
                setImageDrawable(drawable);
            }
        }
    }

    private void startLoadingImage() {
        if(m_image == null && m_uri != null) {
            Context context = getContext();

            if(m_connection == null) {
                if(context instanceof WebActivity) {
                    m_connection = ((WebActivity)context).getConnection();
                }

                if(m_connection == null) {
                    Log.e(TAG, "Unable to get a web connection");
                    return;
                }
            }

            m_image = m_connection.enqueue(context, new WebImage(context, m_uri), new WebImage.ResultHandler() {
                @Override
                public void onImageResult(int status, Drawable drawable) {
                    if(m_attachedToWindow) {
                        if(drawable != null) {
                            Log.d(TAG, "enqueue: onImageLoaded width: " + drawable.getIntrinsicWidth() + " height: " + drawable.getIntrinsicHeight());
                            m_width =  drawable.getIntrinsicWidth();
                            m_height = drawable.getIntrinsicHeight();

                            if(m_flipped) {
                                Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();

                                setImageBitmap(Bitmaps.flip(bitmap, Bitmaps.FLIP_HORIZONTAL));
                            } else {
                                setImageDrawable(drawable);
                            }

                            if(m_loadListener != null) {
                                m_loadListener.onImageLoaded();
                            }
                        } else {
                            if(m_loadListener != null) {
                                m_loadListener.onImageFailed();
                            }
                        }
                    }
                }
            });
        }
    }

    private void stopLoadingImage() {
        if(m_image != null) {
            m_connection.dequeue(getContext(), m_image);
            m_image = null;
        }
    }

    private void readAttributes(Context context, AttributeSet attrs) {
        m_placeholderResourceId = attrs.getAttributeResourceValue(NAMESPACE, KEY_SRC, INVALID_RESOURCE_ID);
    }

    public interface OnLoadListener {
        void onImageLoaded();
        void onImageUnloaded();
        void onImageFailed();
    }
}
