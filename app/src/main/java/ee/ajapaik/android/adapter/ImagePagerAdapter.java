package ee.ajapaik.android.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import ee.ajapaik.android.data.util.PhotoModel;
import ee.ajapaik.android.widget.WebImageView;

public class ImagePagerAdapter extends PagerAdapter {
    private final Context context;
    private final List<? extends PhotoModel> m_images;
    private static final int THUMBNAIL_SIZE = 400;
    private WebImageView imageView;

    public ImagePagerAdapter(Context context, List<? extends PhotoModel> photos) {
        this.context = context;
        this.m_images = photos;
    }

    @Override
    public int getCount() {
        return m_images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PhotoModel photo = m_images.get(position);
        imageView = new WebImageView(context);
        imageView.setImageURI(photo.getThumbnail(THUMBNAIL_SIZE));
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }

    public PhotoModel getPhotoModel(int position) {
        return m_images.get(position);
    }

    public WebImageView getImageView() {
        return imageView;
    }
}
