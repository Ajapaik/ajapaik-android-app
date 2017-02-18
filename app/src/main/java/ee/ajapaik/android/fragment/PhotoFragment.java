package ee.ajapaik.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import ee.ajapaik.android.CameraActivity;
import ee.ajapaik.android.ProfileActivity;
import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.data.Hyperlink;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.data.util.Status;
import ee.ajapaik.android.fragment.util.WebFragment;
import ee.ajapaik.android.test.R;
import ee.ajapaik.android.util.*;
import ee.ajapaik.android.widget.WebImageView;
import ee.ajapaik.android.widget.util.OnCompositeTouchListener;
import ee.ajapaik.android.widget.util.OnPanTouchListener;
import ee.ajapaik.android.widget.util.OnScaleTouchListener;
import ee.ajapaik.android.widget.util.OnSwipeTouchListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PhotoFragment extends WebFragment {
    private static final int THUMBNAIL_SIZE = 400;

    private static final int REQUEST_CAMERA = 4000;
    private static final int CAMERA_AND_STORAGE_PERMISSION = 6002;

    private static final float DEFAULT_SCALE = 1.0F;

    private static final String KEY_ALBUM = "album";
    private static final String KEY_AZIMUTH = "azimuth";
    private static final String KEY_FLIPPED_MODE = "flipped_mode";
    private static final String KEY_IMMERSIVE_MODE = "immersive_mode";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_PHOTO = "photo";
    private static final String KEY_OFFSET = "offset";
    private static final String KEY_SCALE = "scale";

    private int m_azimuth;
    private boolean m_immersiveMode;
    private Location m_location;
    private Album m_album;
    private Photo m_photo;
    private float m_scale = DEFAULT_SCALE;
    private PointF m_offset = null;
    private boolean m_flippedMode;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            m_album = savedInstanceState.getParcelable(KEY_ALBUM);
            m_azimuth = savedInstanceState.getInt(KEY_AZIMUTH, 0);
            m_photo = savedInstanceState.getParcelable(KEY_PHOTO);
            m_location = savedInstanceState.getParcelable(KEY_LOCATION);
            m_immersiveMode = savedInstanceState.getBoolean(KEY_IMMERSIVE_MODE);
            m_flippedMode = savedInstanceState.getBoolean(KEY_FLIPPED_MODE);
            m_offset = savedInstanceState.getParcelable(KEY_OFFSET);
            m_scale = savedInstanceState.getFloat(KEY_SCALE, DEFAULT_SCALE);
        }

        if(m_album == null) {
            m_album = getAlbum();
        }

        if(m_photo == null) {
            m_photo = getPhoto();
        }

        if(m_photo == null && m_album != null) {
            m_photo = m_album.getFirstPhoto();
        }

        if(m_location == null) {
            m_location = getSettings().getLocation();
        }

        getImageView().setOffset(m_offset);

        getImageView().setOnTouchListener(new OnCompositeTouchListener(getActivity(), new View.OnTouchListener[]{
                new OnScaleTouchListener(getActivity()) {
                    @Override
                    public void onScale(float scale) {
                        if(m_immersiveMode) {
                            WebImageView imageView = getImageView();

                            imageView.setScale(imageView.getScale() * scale);
                            m_scale = imageView.getScale();
                        }
                    }
                },
                new OnPanTouchListener(getActivity()) {
                    @Override
                    public void onPan(float distanceX, float distanceY) {
                        if(m_immersiveMode) {
                            WebImageView imageView = getImageView();

                            m_offset = imageView.getOffset();

                            if(m_offset == null) {
                                m_offset = new PointF();
                            }

                            m_offset = new PointF(m_offset.x - distanceX, m_offset.y - distanceY);
                            imageView.setOffset(m_offset);
                        }
                    }
                },
                new OnSwipeTouchListener(getActivity()) {
                    @Override
                    public void onSingleTap() {
                        setImmersiveMode(!m_immersiveMode);
                    }
                }
        }));

        getImageView().setScale(m_scale);
        getImageView().setFlipped(m_flippedMode);
        getImageView().setImageURI(m_photo.getThumbnail(THUMBNAIL_SIZE));
        getImageView().setOnLoadListener(new WebImageView.OnLoadListener() {
            @Override
            public void onImageLoaded() {
                getProgressBar().setVisibility(View.GONE);
                getMainLayout().setVisibility(View.VISIBLE);
            }

            @Override
            public void onImageUnloaded() {
                getProgressBar().setVisibility(View.VISIBLE);
                getMainLayout().setVisibility(View.GONE);
            }

            @Override
            public void onImageFailed() {
            }
        });

        getSubtitleView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(m_photo != null) {
                    Hyperlink link = m_photo.getSource();

                    if(link != null && link.getURL() != null) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);

                        intent.setData(link.getURL());
                        startActivity(intent);
                    }
                }
            }
        });

        getRephotoButton().setOnClickListener(new View.OnClickListener() {
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

        invalidateAzimuth();
        invalidatePhoto();

        setImmersiveMode(m_immersiveMode);
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
        savedInstanceState.putBoolean(KEY_FLIPPED_MODE, m_flippedMode);
        savedInstanceState.putInt(KEY_AZIMUTH, m_azimuth);
        savedInstanceState.putParcelable(KEY_LOCATION, m_location);
        savedInstanceState.putParcelable(KEY_PHOTO, m_photo);
        savedInstanceState.putParcelable(KEY_OFFSET, m_offset);
        savedInstanceState.putFloat(KEY_SCALE, m_scale);
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

    protected void setImmersiveMode(boolean flag) {
        m_immersiveMode = flag;

        if(m_immersiveMode) {
            getInfoLayout().setVisibility(View.INVISIBLE);
            getOverlayLayout().setVisibility(View.INVISIBLE);
            getActionBar().hide();
        } else {
            getInfoLayout().setVisibility(View.VISIBLE);
            getOverlayLayout().setVisibility(View.VISIBLE);
            getActionBar().show();
        }
    }

    public void invalidate(Location location, float[] orientation) {
        if(m_photo != null) {
            int azimuth = Math.round(Locations.getAzimuthInDegrees(getActivity(), location, m_photo.getLocation(), orientation));

            if(m_location != location) {
                m_location = location;
                invalidateLocation();
            }

            if(m_azimuth != azimuth) {
                m_azimuth = azimuth;
                invalidateAzimuth();
            }
        }
    }

    private void invalidatePhoto() {
        String title = m_photo.getTitle();
        String author = m_photo.getAuthor();
        Date date = m_photo.getDate();

        getImageView().setImageURI(m_photo.getThumbnail(THUMBNAIL_SIZE));
        getRephotosCountImageView().setImageResource(Images.toRephotoCountDrawableId(m_photo.getRephotosCount()));

        if(m_photo.getUploadsCount() > 0) {
            getRephotosCountImageView().setColorFilter(getResources().getColor(R.color.tint), PorterDuff.Mode.MULTIPLY);
        } else {
            getRephotosCountImageView().setColorFilter(getResources().getColor(R.color.none), PorterDuff.Mode.SRC_ATOP);
        }

        if(author != null && author.length() == 0) {
            author = null;
        }

        if(m_photo.getSource() != null) {
            getSubtitleView().setText(m_photo.getSource().toHtml());
        } else if(author != null) {
            getSubtitleView().setText(author);
            author = null;
        } else if(date != null) {
            getSubtitleView().setText(Strings.toLocalizedDate(getActivity(), date));
            date = null;
        }

        if(title == null) {
            title = "";
        }

        if(author != null || date != null) {
            if(title.length() > 0) {
                title += "\n";
            }

            if(author != null) {
                title += author;
            }

            if(date != null) {
                if(!title.endsWith("\n")) {
                    title += ", ";
                }

                title += Strings.toLocalizedDate(getActivity(), date);
            }
        }

        getTitleView().setText(title);
        invalidateLocation();
    }

    private void invalidateAzimuth() {
        ImageButton azimuthButton = getAzimuthButton();

        if(m_photo.getLocation() != null) {
            azimuthButton.setRotation((float)m_azimuth);
            azimuthButton.setVisibility(View.VISIBLE);
        } else {
            azimuthButton.setVisibility(View.GONE);
        }
    }

    private void invalidateLocation() {
        getDistanceView().setText(Strings.toLocalizedDistance(getActivity(), m_photo.getLocation(), m_location));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_flip) {
            m_flippedMode = !m_flippedMode;
            getImageView().setFlipped(m_flippedMode);

            return true;
        } else if (id == R.id.action_profile) {
            ProfileActivity.start(getContext());
        }

        return super.onOptionsItemSelected(item);
    }

    private View getMainLayout() {
        return getView().findViewById(R.id.layout_main);
    }

    private View getInfoLayout() {
        return getView().findViewById(R.id.layout_info);
    }

    private View getOverlayLayout() {
        return getView().findViewById(R.id.layout_overlay);
    }

    private ImageButton getAzimuthButton() {
        return (ImageButton)getView().findViewById(R.id.button_action_azimuth);
    }

    private Button getRephotoButton() {
        return (Button)getView().findViewById(R.id.button_action_rephoto);
    }

    private WebImageView getImageView() {
        return (WebImageView)getView().findViewById(R.id.image);
    }

    private TextView getDistanceView() {
        return (TextView)getView().findViewById(R.id.text_distance);
    }

    private TextView getTitleView() {
        return (TextView)getView().findViewById(R.id.text_title);
    }

    private ImageView getRephotosCountImageView() {
        return (ImageView)getView().findViewById(R.id.image_rephoto);
    }

    private Button getSubtitleView() {
        return (Button)getView().findViewById(R.id.button_subtitle);
    }

    private ProgressBar getProgressBar() {
        return (ProgressBar)getView().findViewById(R.id.progress_bar);
    }
}
