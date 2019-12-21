package ee.ajapaik.android.fragment;

import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import ee.ajapaik.android.R;
import ee.ajapaik.android.fragment.util.ImageFragment;
import ee.ajapaik.android.widget.WebImageView;
import ee.ajapaik.android.widget.util.OnCompositeTouchListener;
import ee.ajapaik.android.widget.util.OnPanTouchListener;
import ee.ajapaik.android.widget.util.OnScaleTouchListener;

public class ImmersivePhotoFragment extends ImageFragment {

    private PointF m_offset;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_immersive_photo, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            m_photo = savedInstanceState.getParcelable(KEY_PHOTO);
            m_flippedMode = savedInstanceState.getBoolean(KEY_FLIPPED_MODE);
            m_scale = savedInstanceState.getFloat(KEY_SCALE);
        }

        if (m_photo == null) {
            m_photo = getPhoto();
        }

        getImageView().setOnTouchListener(new OnCompositeTouchListener(getActivity(), new View.OnTouchListener[]{
                new OnScaleTouchListener(getActivity()) {
                    @Override
                    public void onScale(float scale) {
                        WebImageView imageView = getImageView();
                        float newScale = imageView.getScale() * scale;
                        if (newScale < 1.0f) {
                            newScale = 1.0f;
                        }
                        ;
                        imageView.setScale(newScale);
                        m_scale = imageView.getScale();
                    }
                },
                new OnPanTouchListener(getActivity()) {
                    @Override
                    public void onPan(float distanceX, float distanceY) {
                        WebImageView imageView = getImageView();

                        m_offset = imageView.getOffset();

                        if (m_offset == null) {
                            m_offset = new PointF();
                        }

                        m_offset = new PointF(m_offset.x - distanceX, m_offset.y - distanceY);
                        avoidScrollingOutOfViewport(imageView);
                        imageView.setOffset(m_offset);
                    }
                },
        }));

        getImageView().setImageURI(m_photo.getThumbnail(THUMBNAIL_SIZE));
        getImageView().setOnLoadListener(imageLoadListener());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_photo, menu);
        menu.findItem(R.id.action_favorite).setVisible(false);
        setFlipIcon(menu.findItem(R.id.action_flip));
    }

    private void setFlipIcon(MenuItem item) {
        item.setIcon(m_flippedMode
                ? R.drawable.ic_flip_white_36dp_selected
                : R.drawable.ic_flip_white_36dp
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_flip) {
            m_flippedMode = !m_flippedMode;
            getImageView().setFlipped(m_flippedMode);
            setFlipIcon(item);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean(KEY_FLIPPED_MODE, m_flippedMode);
        savedInstanceState.putParcelable(KEY_PHOTO, m_photo);
        savedInstanceState.putFloat(KEY_SCALE, m_scale);
    }

    private void avoidScrollingOutOfViewport(WebImageView imageView) {
        int viewWidth = getMainLayout().getWidth();
        int viewHeight = getMainLayout().getHeight();
        float imageViewDrawableRatio = (float) viewWidth / imageView.getDrawable().getIntrinsicWidth();

        avoidScrollingOutOfViewportOnXAxle(imageView, viewWidth, imageViewDrawableRatio);
        avoidScrollingOutOfViewportOnYAxle(imageView, viewHeight, imageViewDrawableRatio);
    }

    private void avoidScrollingOutOfViewportOnYAxle(WebImageView imageView, int viewHeight, float imageViewDrawableRatio) {
        int imageHeight = imageView.getDrawable().getIntrinsicHeight();
        m_offset.y = getPosition(m_offset.y, viewHeight, imageHeight, imageViewDrawableRatio, imageView.getScale());
    }

    private void avoidScrollingOutOfViewportOnXAxle(WebImageView imageView, int viewWidth, float imageViewDrawableRatio) {
        int imageWidth = imageView.getDrawable().getIntrinsicWidth();
        m_offset.x = getPosition(m_offset.x, viewWidth, imageWidth, imageViewDrawableRatio, imageView.getScale());
    }

    private float getPosition(float currentPosition, int viewLength, float imageLength, float imageViewDrawableRatio, float scale) {
        float scaledImageLength = imageLength * imageViewDrawableRatio * scale;
        float positiveEdge = (viewLength - scaledImageLength) / 2;
        float negativeEdge = -positiveEdge;
        if (scaledImageLength < viewLength) {
            return avoidScrollingOutOfViewport(currentPosition, positiveEdge, negativeEdge);
        } else {
            return avoidScrollingOutOfViewport(currentPosition, negativeEdge, positiveEdge);
        }
    }

    private float avoidScrollingOutOfViewport(float currentPosition, float positiveEdge, float negativeEdge) {
        if (currentPosition > positiveEdge) {
            return positiveEdge;
        } else if (currentPosition < negativeEdge) {
            return negativeEdge;
        }
        return currentPosition;
    }
}
