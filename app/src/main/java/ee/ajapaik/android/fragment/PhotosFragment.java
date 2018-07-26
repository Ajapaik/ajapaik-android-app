package ee.ajapaik.android.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import ee.ajapaik.android.R;
import ee.ajapaik.android.adapter.PhotoAdapter;
import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.data.util.Status;
import ee.ajapaik.android.util.WebAction;
import ee.ajapaik.android.widget.StaggeredGridView;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public abstract class PhotosFragment extends SearchFragment {
    private static final String TAG = "PhotosFragment";

    protected Album m_album;
    private boolean m_mapViewMode = false;
    private MapView m_mapView;

    protected abstract String getPlaceholderString();
    protected abstract void setAlbum(Album album);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_album, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_map, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getSwipeRefreshLayout().setRefreshing(true);

        setSwipeRefreshListener();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_map) {
            if (m_album == null) return true;
            m_mapViewMode = !m_mapViewMode;
            toggleMapView();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleMapView() {
        if (m_mapViewMode) {
            getMapView().setVisibility(VISIBLE);
            getGridView().setVisibility(GONE);
            if (m_mapView == null) {
                initMap();
            }
        } else {
            getMapView().setVisibility(GONE);
            getGridView().setVisibility(VISIBLE);
        }
    }

    private void initMap() {
        m_mapView = getMapView();
        m_mapView.onCreate(null);
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

                LatLng firstPhotoLocation = null;

                if (m_album.hasPhotosWithLocation()) {
                    for (Photo photo : m_album.getPhotos()) {
                        if (photo.getLocation() == null) continue;

                        LatLng photoLocation = new LatLng(photo.getLocation().getLatitude(), photo.getLocation().getLongitude());
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(photoLocation)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_navigation_black_18));

                        mMap.addMarker(markerOptions);

                        if (firstPhotoLocation == null) {
                            firstPhotoLocation = photoLocation;
                        }
                    }

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(firstPhotoLocation)
                            .zoom(13)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                } else {
                    new AlertDialog.Builder(getContext())
                            .setMessage("Unfortunately none of the photos have been geotagged")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .show();
                }
            }
        });
    }

    @Override
    protected void performAction(Context context, WebAction action) {
        if (action != null) {
            getConnection().enqueue(context, action, new WebAction.ResultHandler<Album>() {
                @Override
                public void onActionResult(Status status, Album album) {
                    if (album != null) {
                        setAlbum(album);
                    } else if (m_album == null) {
                        showRequestErrorToast();
                    }
                    handleLoadingFinished();
                }
            });
        }
    }

    protected void setPhotoAdapter(StaggeredGridView gridView, PhotoAdapter.OnPhotoSelectionListener selectionListener) {
        gridView.setAdapter(new PhotoAdapter(gridView.getContext(), m_album.getPhotos(), getSettings().getLocation(), selectionListener));
    }

    protected void initializeEmptyGridView(StaggeredGridView gridView) {
        String text = isSearchResultVisible() ? getString(R.string.no_search_result) : getPlaceholderString();
        getEmptyView().setText(text);
        gridView.setAdapter(null);
    }

    protected TextView getEmptyView() {
        return (TextView)getView().findViewById(R.id.empty);
    }

    protected StaggeredGridView getGridView() {
        return getGridView(getView());
    }

    protected StaggeredGridView getGridView(View view) {
        return (StaggeredGridView)view.findViewById(R.id.grid);
    }

    private MapView getMapView() {
        return (MapView) getView().findViewById(R.id.album_map);
    }
}
