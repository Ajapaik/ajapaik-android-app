package ee.ajapaik.android.fragment.util;


import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.R;
import ee.ajapaik.android.widget.WebImageView;

public abstract class ImageFragment extends WebFragment {

    protected boolean m_flippedMode = false;
    protected Photo m_photo;
    protected float m_scale = DEFAULT_SCALE;

    protected static final float DEFAULT_SCALE = 1.0F;
    protected static final String KEY_FLIPPED_MODE = "flipped_mode";
    protected static final String KEY_PHOTO = "photo";
    protected static final String KEY_SCALE = "scale";

    public Photo getPhoto() {
        Bundle arguments = getArguments();

        if(arguments != null) {
            return arguments.getParcelable(KEY_PHOTO);
        }

        return null;
    }

    public void setPhoto(Photo photo) {
        Bundle arguments = getArguments();

        if(arguments == null) {
            arguments = new Bundle();
        }

        if(photo != null) {
            arguments.putParcelable(KEY_PHOTO, photo);
        } else {
            arguments.remove(KEY_PHOTO);
        }

        setArguments(arguments);
    }

    protected WebImageView.OnLoadListener imageLoadListener() {
        return new WebImageView.OnLoadListener() {
            @Override
            public void onImageLoaded() {
                getMainLayout().setVisibility(View.VISIBLE);
            }

            @Override
            public void onImageUnloaded() {
                getMainLayout().setVisibility(View.GONE);
            }

            @Override
            public void onImageFailed() {
            }
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_flip) {
            m_flippedMode = !m_flippedMode;
            getImageView().setFlipped(m_flippedMode);
            item.setIcon(m_flippedMode ? R.drawable.ic_flip_white_36dp_selected : R.drawable.ic_flip_white_36dp);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected View getMainLayout() {
        return getView().findViewById(R.id.layout_main);
    }

    protected WebImageView getImageView() {
        return (WebImageView) getView().findViewById(R.id.image);
    }
}
