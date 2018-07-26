package ee.ajapaik.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import ee.ajapaik.android.CameraActivity;
import ee.ajapaik.android.ImmersivePhotoActivity;
import ee.ajapaik.android.R;
import ee.ajapaik.android.adapter.ImagePagerAdapter;
import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.data.Rephoto;
import ee.ajapaik.android.data.util.Status;
import ee.ajapaik.android.fragment.util.ImageFragment;
import ee.ajapaik.android.util.Dates;
import ee.ajapaik.android.util.Images;
import ee.ajapaik.android.util.Objects;
import ee.ajapaik.android.util.Strings;
import ee.ajapaik.android.util.WebAction;
import ee.ajapaik.android.widget.WebImageView;
import ee.ajapaik.android.widget.util.OnCompositeTouchListener;
import ee.ajapaik.android.widget.util.OnSwipeTouchListener;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.view.View.INVISIBLE;
import static android.view.View.OnClickListener;
import static android.view.View.OnTouchListener;
import static android.view.View.VISIBLE;

public class PhotoFragment extends ImageFragment {
    private static final String TAG = "PhotoFragment";

    private static final int REQUEST_CAMERA = 4000;
    private static final int CAMERA_AND_STORAGE_PERMISSION = 6002;

    private static final String KEY_ALBUM = "album";
    private static final String KEY_REPHOTO_VIEW_MODE = "rephoto_view_mode";
    private static final String KEY_LOCATION = "location";

    private boolean m_rephotoViewMode;
    protected boolean m_favorited;
    private Location m_location;
    private Album m_album;

    public Album getAlbum() {
        Bundle arguments = getArguments();

        if(arguments != null) {
            return arguments.getParcelable(KEY_ALBUM);
        }

        return null;
    }

    public void setAlbum(Album album) {
        Bundle arguments = getArguments();

        if(arguments == null) {
            arguments = new Bundle();
        }

        if(album != null) {
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

    private void initMap(Bundle savedInstanceState) {
        final MapView m_mapView = (MapView) getView().findViewById(R.id.photo_details_map);
        m_mapView.onCreate(savedInstanceState);
        m_mapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize map", e);
        }

        m_mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getActivity().checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    mMap.setMyLocationEnabled(true);
                }

                LatLng photoLocation = new LatLng(m_photo.getLocation().getLatitude(), m_photo.getLocation().getLongitude());
                mMap.addMarker(new MarkerOptions().position(photoLocation));

                CameraPosition cameraPosition = new CameraPosition.Builder().target(photoLocation).zoom(13).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            m_album = savedInstanceState.getParcelable(KEY_ALBUM);
            m_photo = savedInstanceState.getParcelable(KEY_PHOTO);
            m_location = savedInstanceState.getParcelable(KEY_LOCATION);
            m_rephotoViewMode = savedInstanceState.getBoolean(KEY_REPHOTO_VIEW_MODE);
            m_flippedMode = savedInstanceState.getBoolean(KEY_FLIPPED_MODE);
        }

        if(m_album == null) {
            m_album = getAlbum();
        }

        if(m_photo == null) {
            m_photo = getPhoto();
        }

        if (m_photo.getLocation() != null) {
            initMap(savedInstanceState);
        }

        if(m_photo == null && m_album != null) {
            m_photo = m_album.getFirstPhoto();
        }

        if(m_location == null) {
            m_location = getSettings().getLocation();
        }

        getImageView().setOnTouchListener(new OnCompositeTouchListener(getActivity(), new OnTouchListener[]{
                new OnSwipeTouchListener(getActivity()) {
                    @Override
                    public void onSingleTap() {
                        ImmersivePhotoActivity.start(getActivity(), m_photo);
                    }
                }
        }));

        getImageView().setFlipped(m_flippedMode);
        getImageView().setImageURI(m_photo.getThumbnail(THUMBNAIL_SIZE));
        getImageView().setOnLoadListener(imageLoadListener());

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

        invalidatePhoto();

        setRephotoViewMode(m_rephotoViewMode);

        getSwipeRefreshLayout().setEnabled(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
        getViewPager().post(new Runnable() {
            @Override
            public void run() {
                pageChangeListener.onPageSelected(0);
            }
        });
    }

    private ViewPager.OnPageChangeListener createOnPageChangeListener(final ImagePagerAdapter adapter) {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Rephoto rephoto = adapter.getRephoto(position);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_favorite) {
            m_favorited = !m_favorited;
            sendFavoriteUpdate(item);
            return true;
        } else if (item.getItemId() == R.id.action_flip) {
            m_flippedMode = !m_flippedMode;
            getImageView().setFlipped(m_flippedMode);
            getRephotoViewOriginalImageView().setFlipped(m_flippedMode);
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
        savedInstanceState.putBoolean(KEY_REPHOTO_VIEW_MODE, m_rephotoViewMode);
        savedInstanceState.putBoolean(KEY_FLIPPED_MODE, m_flippedMode);
        savedInstanceState.putParcelable(KEY_LOCATION, m_location);
        savedInstanceState.putParcelable(KEY_PHOTO, m_photo);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CAMERA) {
            if(resultCode == Activity.RESULT_OK) {
                Context context = getActivity();

                if(m_photo != null) {
                    m_photo = Photo.update(m_photo, m_photo.getRephotosCount() + 1, m_photo.getUploadsCount() + 1);

                    if(m_album != null) {
                        m_album = Album.update(m_album, m_photo);
                    }

                    invalidatePhoto();
                }

                getConnection().enqueue(context, Photo.createStateAction(context, m_photo), new WebAction.ResultHandler<Photo>() {
                    @Override
                    public void onActionResult(Status status, Photo photo) {
                        if(photo != null && (m_photo == null || m_photo.getRephotosCount() <= photo.getRephotosCount())) {
                            if(m_album != null) {
                                m_album = Album.update(m_album, photo);

                                if(m_photo != null && Objects.match(m_photo.getIdentifier(), photo.getIdentifier())) {
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

    private void setRephotoViewMode(boolean flag) {
        m_rephotoViewMode = flag;

        if(m_rephotoViewMode) {
            getActionBar().setDisplayHomeAsUpEnabled(false);
            getRephotosLayout().setVisibility(VISIBLE);
            getMainLayout().setVisibility(INVISIBLE);
            getOriginalPhotoContainer().setImageURI(m_photo.getThumbnail(THUMBNAIL_SIZE));
            final ImagePagerAdapter adapter = new ImagePagerAdapter(getActivity(), m_photo.getRephotos());
            getViewPager().setAdapter(adapter);
            final ViewPager.OnPageChangeListener pageChangeListener = createOnPageChangeListener(adapter);
            getViewPager().addOnPageChangeListener(pageChangeListener);
            selectFirstRephotoToDisplay(pageChangeListener);
        } else {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getMainLayout().setVisibility(VISIBLE);
            getRephotosLayout().setVisibility(INVISIBLE);
        }
        getActivity().invalidateOptionsMenu();
    }

    public void invalidate(Location location, float[] orientation) {
        if(m_photo != null) {
            if(m_location != location) {
                m_location = location;
                invalidateLocation();
            }
        }
    }

    private void invalidatePhoto() {
        m_favorited = m_photo.isFavorited();

        getImageView().setImageURI(m_photo.getThumbnail(THUMBNAIL_SIZE));
        getRephotosCountImageView().setImageResource(Images.toRephotoCountDrawableId(m_photo.getRephotosCount()));

        if(m_photo.getUploadsCount() > 0) {
            getRephotosCountImageView().setColorFilter(getResources().getColor(R.color.tint), PorterDuff.Mode.MULTIPLY);
        } else {
            getRephotosCountImageView().setColorFilter(getResources().getColor(R.color.none), PorterDuff.Mode.SRC_ATOP);
        }

        invalidateLocation();
    }

    private void invalidateLocation() {
        getDistanceView().setText(Strings.toLocalizedDistance(getActivity(), m_photo.getLocation(), m_location));
    }

    private ImageView getRephotoButton() {
        return (ImageView)getView().findViewById(R.id.button_action_rephoto);
    }

    private TextView getDistanceView() {
        return (TextView)getView().findViewById(R.id.text_distance);
    }

    private ImageView getRephotosCountImageView() {
        return (ImageView)getView().findViewById(R.id.image_rephoto);
    }

    private SwipeRefreshLayout getSwipeRefreshLayout() {
        return (SwipeRefreshLayout)getView().findViewById(R.id.swiperefresh);
    }

    protected WebImageView getOriginalPhotoContainer() {
        return (WebImageView) getView().findViewById(R.id.rephotos_original);
    }

    protected ViewPager getViewPager() {
        return (ViewPager) getView().findViewById(R.id.pager);
    }


    private TextView getRephotoDateView() {
        return (TextView) getView().findViewById(R.id.rephoto_date);
    }

    private TextView getRephotoAuthorView() {
        return (TextView) getView().findViewById(R.id.rephoto_author);
    }

    private Button getCloseRephotoButton() {
        return (Button) getView().findViewById(R.id.button_action_close_rephotos);
    }

    private WebImageView getRephotoViewOriginalImageView() {
        return (WebImageView) getView().findViewById(R.id.rephotos_original);
    }

    private LinearLayout getRephotosLayout() {
        return (LinearLayout) getView().findViewById(R.id.rephotos_layout);
    }
}
