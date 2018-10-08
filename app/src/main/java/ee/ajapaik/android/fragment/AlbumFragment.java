package ee.ajapaik.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import ee.ajapaik.android.AlbumsActivity;
import ee.ajapaik.android.PhotoActivity;
import ee.ajapaik.android.R;
import ee.ajapaik.android.adapter.PhotoAdapter;
import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.util.Objects;
import ee.ajapaik.android.util.WebAction;
import ee.ajapaik.android.widget.StaggeredGridView;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static ee.ajapaik.android.PhotoActivity.IS_FAVORITED_KEY;
import static ee.ajapaik.android.PhotoActivity.PHOTO_IDENTIFIER_KEY;

public class AlbumFragment extends PhotosFragment {
    private static final String KEY_ALBUM_IDENTIFIER = "album_id";

    private static final String KEY_ALBUM = "album";
    private static final String KEY_LAYOUT = "layout";
    private int PHOTO_ACTIVITY_REQUEST_CODE = 1001;

    public void invalidate() {
        getSwipeRefreshLayout().setRefreshing(true);
        refresh();
    }

    public String getAlbumIdentifier() {
        Bundle arguments = getArguments();

        return (arguments != null) ? arguments.getString(KEY_ALBUM_IDENTIFIER) : null;
    }

    public void setAlbumIdentifier(String albumIdentifier) {
        Bundle arguments = getArguments();

        if(arguments == null) {
            arguments = new Bundle();
        }

        if(albumIdentifier != null) {
            arguments.putString(KEY_ALBUM_IDENTIFIER, albumIdentifier);
        } else {
            arguments.remove(KEY_ALBUM_IDENTIFIER);
        }

        setArguments(arguments);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            Album album = savedInstanceState.getParcelable(KEY_ALBUM);
            Parcelable layout = savedInstanceState.getParcelable(KEY_LAYOUT);

            setAlbum(album, layout);
        }

        if (!isNearestFragment()) {
            refresh();
        } else {
            getSwipeRefreshLayout().setRefreshing(false);
        }
    }

    @Override
    protected WebAction<Album> createSearchAction(String query) {
        return Album.createSearchAction(getActivity(), getAlbum(), query);
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(KEY_ALBUM, m_album);
    }

    public Album getAlbum() {
        return m_album;
    }

    public String getAlbumTitle() {
        return m_album.getTitle();
    }

    @Override
    public void setAlbum(Album album) {
        setAlbum(album, null);
    }

    public void setAlbum(Album album, Parcelable state) {
        if(!Objects.match(m_album, album)) {
            StaggeredGridView gridView = getGridView();

            m_album = album;

            if(m_album != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getAlbumTitle());
            }

            if(state == null) {
                state = gridView.getLayoutManager().onSaveInstanceState();
            }

            if(m_album != null && m_album.getPhotos().size() > 0) {
                getEmptyView().setText("");
                getNoDataButton().setVisibility(GONE);
                setPhotoAdapter(gridView, new PhotoAdapter.OnPhotoSelectionListener() {
                    @Override
                    public void onSelect(Photo photo) {
                        getSwipeRefreshLayout().setRefreshing(false);
                        startActivityForResult(PhotoActivity.getStartIntent(getActivity(), photo, m_album), PHOTO_ACTIVITY_REQUEST_CODE);
                    }
                });
            } else {
                initializeEmptyGridView(gridView);
                if (isNearestFragment()) {
                    getNoDataButton().setVisibility(VISIBLE);
                    getNoDataButton().setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlbumsActivity.start(getActivity());
                            getActivity().finish();
                        }
                    });
                }
            }

            if(state != null) {
                gridView.getLayoutManager().onRestoreInstanceState(state);
            }
        }
    }

    @Override
    protected void refresh() {
        Context context = getActivity();
        WebAction<Album> action = createAction(context);
        performAction(context, action);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String photoIdentifier = data.getStringExtra(PHOTO_IDENTIFIER_KEY);
                if (photoIdentifier != null) {
                    boolean isFavorited = data.getBooleanExtra(IS_FAVORITED_KEY, false);
                    Photo photo = getAlbum().getPhoto(photoIdentifier);
                    if (photo != null && photo.isFavorited() != isFavorited) {
                        photo.setFavorited(isFavorited);
                        getGridView().getAdapter().notifyDataSetChanged();
                    }
                }
            }
        }
    }

    @Override
    protected String getPlaceholderString() {
        return getString(R.string.album_no_data);
    }

    protected boolean isNearestFragment() {
        return false;
    }

    protected WebAction<Album> createAction(Context context) {
        return (m_album != null) ? Album.createStateAction(context, m_album) : Album.createStateAction(context, getAlbumIdentifier());
    }

    private Button getNoDataButton() {
        return (Button)getView().findViewById(R.id.nearest_no_data_action);
    }
}
