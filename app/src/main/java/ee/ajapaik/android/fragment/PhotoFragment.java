package ee.ajapaik.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ee.ajapaik.android.CameraActivity;
import ee.ajapaik.android.R;
import ee.ajapaik.android.adapter.ImagePagerAdapter;
import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.data.Hyperlink;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.data.Rephoto;
import ee.ajapaik.android.data.util.Status;
import ee.ajapaik.android.fragment.util.ImageFragment;
import ee.ajapaik.android.util.Dates;
import ee.ajapaik.android.util.Images;
import ee.ajapaik.android.util.Locations;
import ee.ajapaik.android.util.Objects;
import ee.ajapaik.android.util.Strings;
import ee.ajapaik.android.util.WebAction;
import ee.ajapaik.android.widget.WebImageView;
import ee.ajapaik.android.widget.util.OnCompositeTouchListener;
import ee.ajapaik.android.widget.util.OnPanTouchListener;
import ee.ajapaik.android.widget.util.OnScaleTouchListener;
import ee.ajapaik.android.widget.util.OnSwipeTouchListener;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;

public class PhotoFragment extends ImageFragment {
    private static final int THUMBNAIL_SIZE = 400;

    private static final int REQUEST_CAMERA = 4000;
    private static final int CAMERA_AND_STORAGE_PERMISSION = 6002;

    private static final String KEY_ALBUM = "album";
    private static final String KEY_AZIMUTH = "azimuth";
    private static final String KEY_IMMERSIVE_MODE = "immersive_mode";
    private static final String KEY_REPHOTO_VIEW_MODE = "rephoto_view_mode";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_OFFSET = "offset";

    private int m_azimuth;
    private boolean m_immersiveMode;
    private boolean m_rephotoViewMode;
    protected boolean m_favorited;
    private Location m_location;
    private Album m_album;
    private PointF m_offset = null;
    private Menu menu;

    public Album getAlbum() {
        Bundle arguments = getArguments();

        if (arguments != null) {
            return arguments.getParcelable(KEY_ALBUM);
        }

        return null;
    }

    public void setAlbum(Album album) {
        Bundle arguments = getArguments();

        if (arguments == null) {
            arguments = new Bundle();
        }

        if (album != null) {
            arguments.putParcelable(KEY_ALBUM, album);
        } else {
            arguments.remove(KEY_ALBUM);
        }

        setArguments(arguments);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            m_album = savedInstanceState.getParcelable(KEY_ALBUM);
            m_azimuth = savedInstanceState.getInt(KEY_AZIMUTH, 0);
            m_photo = savedInstanceState.getParcelable(KEY_PHOTO);
            m_location = savedInstanceState.getParcelable(KEY_LOCATION);
            m_immersiveMode = savedInstanceState.getBoolean(KEY_IMMERSIVE_MODE);
            m_rephotoViewMode = savedInstanceState.getBoolean(KEY_REPHOTO_VIEW_MODE);
            m_flippedMode = savedInstanceState.getBoolean(KEY_FLIPPED_MODE);
            m_offset = savedInstanceState.getParcelable(KEY_OFFSET);
            m_scale = savedInstanceState.getFloat(KEY_SCALE, DEFAULT_SCALE);
        }

        if (m_album == null) {
            m_album = getAlbum();
        }

        if (m_photo == null) {
            m_photo = getPhoto();
        }

        if (m_photo == null && m_album != null) {
            m_photo = m_album.getFirstPhoto();
        }

        if (m_location == null) {
            m_location = getSettings().getLocation();
        }
        initializeImmersiveMode();

        getOriginalPhotoViewPager().setVisibility(VISIBLE);
        final ImagePagerAdapter adapter = new ImagePagerAdapter(getActivity(), m_album.getPhotos());
        getOriginalPhotoViewPager().setAdapter(adapter);
        final ViewPager.OnPageChangeListener pageChangeListener = createOriginalPhotoOnPageChangeListener(adapter);
        getOriginalPhotoViewPager().addOnPageChangeListener(pageChangeListener);
        getOriginalPhotoViewPager().setCurrentItem(m_album.getPhotos().indexOf(m_photo));
        invalidatePhoto();

        getSubtitleView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_photo != null) {
                    Hyperlink link = m_photo.getSource();

                    if (link != null && link.getURL() != null) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);

                        intent.setData(link.getURL());
                        startActivity(intent);
                    }
                }
            }
        });

        getRephotoButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> permissionsNeeded = permissionsNeeded();

                if (!permissionsNeeded.isEmpty()) {
                    ActivityCompat.requestPermissions(getActivity(), permissionsNeeded.toArray(new String[permissionsNeeded.size()]), CAMERA_AND_STORAGE_PERMISSION);
                } else {
                    startActivityForResult(CameraActivity.getStartIntent(getActivity(), m_photo), REQUEST_CAMERA);
                }
            }
        });

        getRephotosCountImageView().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setRephotoViewMode(true);
            }
        });

        getCloseRephotoButton().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setRephotoViewMode(false);
            }
        });
    }

    private void initializeImmersiveMode() {
        getImmersiveModeLayout().setOnTouchListener(new OnCompositeTouchListener(getActivity(), new View.OnTouchListener[]{
                new OnScaleTouchListener(getActivity()) {
                    @Override
                    public void onScale(float scale) {
                        if (m_immersiveMode) {
                            WebImageView imageView = getImmersiveImage();
                            float newScale = imageView.getScale() * scale;
                            if (newScale < 1.0f) {
                                newScale = 1.0f;
                            }
                            ;
                            imageView.setScale(newScale);
                            m_scale = imageView.getScale();
                        }
                    }
                },
                new OnPanTouchListener(getActivity()) {
                    @Override
                    public void onPan(float distanceX, float distanceY) {
                        if (m_immersiveMode) {
                            WebImageView imageView = getImmersiveImage();

                            m_offset = imageView.getOffset();

                            if (m_offset == null) {
                                m_offset = new PointF();
                            }

                            m_offset = new PointF(m_offset.x - distanceX, m_offset.y - distanceY);
                            avoidScrollingOutOfViewport(imageView);
                            imageView.setOffset(m_offset);
                        }
                    }
                },
                new OnSwipeTouchListener(getActivity()) {
                    @Override
                    public void onSingleTap() {
                        setImmersiveMode(false);
                    }
                }
        }));

        getMainLayout().setOnTouchListener(
                new OnSwipeTouchListener(getActivity()) {
                    @Override
                    public void onSingleTap() {
                        setImmersiveMode(true);
                    }
                }
        );
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        inflater.inflate(R.menu.menu_photo, menu);
        setFlipIcon(menu.findItem(R.id.action_flip));
        if (m_rephotoViewMode) {
            menu.findItem(R.id.action_favorite).setVisible(false);
        } else {
            menu.findItem(R.id.action_favorite).setVisible(true);
            setFavoritedIcon(menu.findItem(R.id.action_favorite));
        }
    }

    private void setFlipIcon(MenuItem item) {
        item.setIcon(
                m_flippedMode
                        ? R.drawable.ic_flip_white_36dp_selected
                        : R.drawable.ic_flip_white_36dp
        );
    }

    private void setFavoritedIcon(MenuItem item) {
        item.setIcon(
                m_photo.isFavorited()
                        ? R.drawable.ic_favorite_white_36dp
                        : R.drawable.ic_favorite_border_white_36dp
        );
    }

    private void selectFirstRephotoToDisplay(final ViewPager.OnPageChangeListener pageChangeListener) {
        getRephotoViewPager().post(new Runnable() {
            @Override
            public void run() {
                pageChangeListener.onPageSelected(0);
            }
        });
    }

    private ViewPager.OnPageChangeListener createRephotoOnPageChangeListener(final ImagePagerAdapter adapter) {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Rephoto rephoto = (Rephoto) adapter.getPhotoModel(position);
                TextView authorView = getRephotoAuthorView();
                TextView dateView = getRephotoDateView();
                authorView.setText(rephoto.getAuthor());
                int textColor = rephoto.isUploadedByCurrentUser() ? R.color.tint : R.color.none;
                authorView.setTextColor(getResources().getColor(textColor));
                dateView.setText(Dates.toDDMMYYYYString(rephoto.getDate()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        };
    }

    private ViewPager.OnPageChangeListener createOriginalPhotoOnPageChangeListener(final ImagePagerAdapter adapter) {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                m_photo = (Photo) adapter.getPhotoModel(position);

                m_flippedMode = false;
                m_immersiveMode = false;
                m_rephotoViewMode = false;
                m_offset = null;

                invalidateAzimuth();
                invalidatePhoto();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_favorite) {
            m_favorited = !m_favorited;
            sendFavoriteUpdate(item);
            return true;
        } else if (item.getItemId() == R.id.action_flip) {
            m_flippedMode = !m_flippedMode;
            getImmersiveImage().setFlipped(m_flippedMode);
            getRephotoViewOriginalPhotoContainer().setFlipped(m_flippedMode);
            ImagePagerAdapter originalPhotoPager = (ImagePagerAdapter) getOriginalPhotoViewPager().getAdapter();

//            TODO this does not work
            WebImageView imageView = originalPhotoPager.getImageView();
            imageView.setFlipped(m_flippedMode);
            setFlipIcon(item);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isFavorited() {
        return m_favorited;
    }

    private void sendFavoriteUpdate(final MenuItem item) {
        getSwipeRefreshLayout().setRefreshing(true);
        WebAction<Photo> action = Photo.createFavoritingAction(getActivity(), m_photo.getIdentifier(), m_favorited);

        getConnection().enqueue(getActivity(), action, new WebAction.ResultHandler<Photo>() {
            @Override
            public void onActionResult(Status status, Photo data) {
                if (status.isGood()) {
                    item.setIcon(m_favorited ? R.drawable.ic_favorite_white_36dp : R.drawable.ic_favorite_border_white_36dp);
                } else {
                    showRequestErrorToast();
                }
                getSwipeRefreshLayout().setRefreshing(false);
            }
        });
    }

    private void avoidScrollingOutOfViewport(WebImageView imageView) {
        int viewWidth = getImmersiveModeLayout().getWidth();
        int viewHeight = getImmersiveModeLayout().getHeight();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_AND_STORAGE_PERMISSION: {
                if (grantResults.length > 0 && areAllNeededPermissionsGranted()) {
                    startActivityForResult(CameraActivity.getStartIntent(getActivity(), m_photo), REQUEST_CAMERA);
                }
            }
        }
    }

    private List<String> permissionsNeeded() {
        List<String> permissionsNeeded = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(getActivity(), CAMERA) != PERMISSION_GRANTED) {
            permissionsNeeded.add(CAMERA);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            permissionsNeeded.add(WRITE_EXTERNAL_STORAGE);
        }
        return permissionsNeeded;
    }

    private boolean areAllNeededPermissionsGranted() {
        return permissionsNeeded().isEmpty();
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(KEY_ALBUM, m_album);
        savedInstanceState.putBoolean(KEY_IMMERSIVE_MODE, m_immersiveMode);
        savedInstanceState.putBoolean(KEY_REPHOTO_VIEW_MODE, m_rephotoViewMode);
        savedInstanceState.putBoolean(KEY_FLIPPED_MODE, m_flippedMode);
        savedInstanceState.putInt(KEY_AZIMUTH, m_azimuth);
        savedInstanceState.putParcelable(KEY_LOCATION, m_location);
        savedInstanceState.putParcelable(KEY_PHOTO, m_photo);
        savedInstanceState.putParcelable(KEY_OFFSET, m_offset);
        savedInstanceState.putFloat(KEY_SCALE, m_scale);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                Context context = getActivity();

                if (m_photo != null) {
                    m_photo = Photo.update(m_photo, m_photo.getRephotosCount() + 1, m_photo.getUploadsCount() + 1);

                    if (m_album != null) {
                        m_album = Album.update(m_album, m_photo);
                    }

                    invalidatePhoto();
                }

                getConnection().enqueue(context, Photo.createStateAction(context, m_photo), new WebAction.ResultHandler<Photo>() {
                    @Override
                    public void onActionResult(Status status, Photo photo) {
                        if (photo != null && (m_photo == null || m_photo.getRephotosCount() <= photo.getRephotosCount())) {
                            if (m_album != null) {
                                m_album = Album.update(m_album, photo);

                                if (m_photo != null && Objects.match(m_photo.getIdentifier(), photo.getIdentifier())) {
                                    m_photo = photo;
                                    invalidatePhoto();
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    //    TODO avoid sending boolean as param
    private void setImmersiveMode(boolean flag) {
        m_immersiveMode = flag;

        if (m_immersiveMode) {
            getImmersiveImage().setImageURI(m_photo.getThumbnail(THUMBNAIL_SIZE));
            getDetailsViewLayout().setVisibility(INVISIBLE);
            getDetailsViewImagesLayout().setVisibility(INVISIBLE);
            getRephotosViewLayout().setVisibility(INVISIBLE);
            getImmersiveModeLayout().setVisibility(VISIBLE);
            getActionBar().hide();
        } else {
            getDetailsViewLayout().setVisibility(VISIBLE);
            getDetailsViewImagesLayout().setVisibility(VISIBLE);
            getRephotosViewLayout().setVisibility(INVISIBLE);
            getImmersiveModeLayout().setVisibility(INVISIBLE);
            getActionBar().show();
            getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.photo_background)));
        }
    }

//    TODO avoid sending boolean as param
    private void setRephotoViewMode(boolean flag) {
        m_rephotoViewMode = flag;

        if (m_rephotoViewMode) {
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getDetailsViewLayout().setVisibility(INVISIBLE);
            getDetailsViewImagesLayout().setVisibility(INVISIBLE);
            getRephotosViewLayout().setVisibility(VISIBLE);
            getRephotoViewOriginalPhotoContainer().setImageURI(m_photo.getThumbnail(THUMBNAIL_SIZE));
            final ImagePagerAdapter adapter = new ImagePagerAdapter(getActivity(), m_photo.getRephotos());
            getRephotoViewPager().setAdapter(adapter);
            final ViewPager.OnPageChangeListener pageChangeListener = createRephotoOnPageChangeListener(adapter);
            getRephotoViewPager().addOnPageChangeListener(pageChangeListener);
            selectFirstRephotoToDisplay(pageChangeListener);
        } else {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getDetailsViewLayout().setVisibility(VISIBLE);
            getDetailsViewImagesLayout().setVisibility(VISIBLE);
            getRephotosViewLayout().setVisibility(INVISIBLE);
        }
        getActivity().invalidateOptionsMenu();
    }

    public void invalidate(Location location, float[] orientation) {
        if (m_photo != null) {
            int azimuth = Math.round(Locations.getAzimuthInDegrees(getActivity(), location, m_photo.getLocation(), orientation));

            if (m_location != location) {
                m_location = location;
                invalidateLocation();
            }

            if (m_azimuth != azimuth) {
                m_azimuth = azimuth;
                invalidateAzimuth();
            }
        }
    }

    private void invalidatePhoto() {
        String title = m_photo.getTitle();
        String author = m_photo.getAuthor();
        Date date = m_photo.getDate();
        m_favorited = m_photo.isFavorited();

        getRephotosCountImageView().setImageResource(Images.toRephotoCountDrawableId(m_photo.getRephotosCount()));

        if (m_photo.getUploadsCount() > 0) {
            getRephotosCountImageView().setColorFilter(getResources().getColor(R.color.tint), PorterDuff.Mode.MULTIPLY);
        } else {
            getRephotosCountImageView().setColorFilter(getResources().getColor(R.color.none), PorterDuff.Mode.SRC_ATOP);
        }

        if (menu != null) {
            setFavoritedIcon(menu.findItem(R.id.action_favorite));
        }

        if (author != null && author.length() == 0) {
            author = null;
        }

        if (m_photo.getSource() != null) {
            getSubtitleView().setText(m_photo.getSource().toHtml());
        }

        if (title != null) {
            getTitleView().setText(title);
        }

        if (author != null) {
            getAuthorView().setText(author);
        }

        if (date != null) {
            String dateString = Strings.toLocalizedDate(getActivity(), date);
            if (author != null) {
                dateString = ", " + dateString;
            }
            getDateView().setText(dateString);
        }

        invalidateLocation();
    }

    private void invalidateAzimuth() {
        ImageButton azimuthButton = getAzimuthButton();

        if (m_photo.getLocation() != null) {
            azimuthButton.setRotation((float) m_azimuth);
            azimuthButton.setVisibility(VISIBLE);
        } else {
            azimuthButton.setVisibility(GONE);
        }
    }

    private void invalidateLocation() {
        getDistanceView().setText(Strings.toLocalizedDistance(getActivity(), m_photo.getLocation(), m_location));
    }

    private ImageButton getAzimuthButton() {
        return (ImageButton) getView().findViewById(R.id.button_action_azimuth);
    }

    private Button getRephotoButton() {
        return (Button) getView().findViewById(R.id.button_action_rephoto);
    }

    private TextView getDistanceView() {
        return (TextView) getView().findViewById(R.id.text_distance);
    }

    private TextView getTitleView() {
        return (TextView) getView().findViewById(R.id.text_title);
    }

    private TextView getAuthorView() {
        return (TextView) getView().findViewById(R.id.text_author);
    }

    private TextView getDateView() {
        return (TextView) getView().findViewById(R.id.text_date);
    }

    private ImageView getRephotosCountImageView() {
        return (ImageView) getView().findViewById(R.id.image_rephoto);
    }

    private Button getSubtitleView() {
        return (Button) getView().findViewById(R.id.button_subtitle);
    }

    private SwipeRefreshLayout getSwipeRefreshLayout() {
        return (SwipeRefreshLayout) getView().findViewById(R.id.swiperefresh);
    }

    protected WebImageView getRephotoViewOriginalPhotoContainer() {
        return (WebImageView) getView().findViewById(R.id.rephotos_original_image);
    }

    protected ViewPager getRephotoViewPager() {
        return (ViewPager) getView().findViewById(R.id.rephoto_pager);
    }

    protected ViewPager getOriginalPhotoViewPager() {
        return (ViewPager) getView().findViewById(R.id.original_photo_pager);
    }

    private TextView getRephotoDateView() {
        return (TextView) getView().findViewById(R.id.rephoto_date);
    }

    private TextView getRephotoAuthorView() {
        return (TextView) getView().findViewById(R.id.rephoto_author);
    }

    private LinearLayout getRephotosViewLayout() {
        return getView().findViewById(R.id.rephotos_layout);
    }

    private RelativeLayout getDetailsViewLayout() {
        return getView().findViewById(R.id.details_view_layout);
    }

    private LinearLayout getImmersiveModeLayout() {
        return getView().findViewById(R.id.immersive_layout);
    }

    private LinearLayout getDetailsViewImagesLayout() {
        return getView().findViewById(R.id.details_view_images_layout);
    }

    private WebImageView getImmersiveImage() {
        return getView().findViewById(R.id.immersive_image);
    }

    private Button getCloseRephotoButton() {
        return (Button) getView().findViewById(R.id.button_action_close_rephotos);
    }
}
