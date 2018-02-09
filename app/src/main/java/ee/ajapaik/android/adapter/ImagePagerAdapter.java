package ee.ajapaik.android.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import ee.ajapaik.android.data.Rephoto;
import ee.ajapaik.android.widget.WebImageView;

public class ImagePagerAdapter extends PagerAdapter {
    private final Context context;
    private final List<Rephoto> m_images;

    public ImagePagerAdapter(Context context, List<Rephoto> rephotos) {
        this.context = context;
        this.m_images = rephotos;
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
        WebImageView imageView = new WebImageView(context);
        imageView.setImageURI(m_images.get(position).getThumbnail(400));
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
}
