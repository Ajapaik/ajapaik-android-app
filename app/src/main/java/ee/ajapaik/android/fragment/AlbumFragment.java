package ee.ajapaik.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import ee.ajapaik.android.AlbumsActivity;
import ee.ajapaik.android.R;
import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.data.util.Status;
import ee.ajapaik.android.util.Objects;
import ee.ajapaik.android.util.WebAction;
import ee.ajapaik.android.widget.StaggeredGridView;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;

public class AlbumFragment extends PhotosFragment {
    private static final String KEY_ALBUM_IDENTIFIER = "album_id";

    private static final String KEY_ALBUM = "album";
    private static final String KEY_LAYOUT = "layout";

    private Album m_album;

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
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(KEY_ALBUM, m_album);
    }

    public Album getAlbum() {
        return m_album;
    }

    public void setAlbum(Album album) {
        setAlbum(album, null);
    }

    public void setAlbum(Album album, Parcelable state) {
        if(!Objects.match(m_album, album)) {
            StaggeredGridView gridView = getGridView();

            m_album = album;

            if(m_album != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(m_album.getTitle() != null ? m_album.getTitle() : getString(R.string.nearest_title));
            }

            if(state == null) {
                state = gridView.getLayoutManager().onSaveInstanceState();
            }

            if(m_album != null && m_album.getPhotos().size() > 0) {
                getEmptyView().setText("");
                getNoDataButton().setVisibility(GONE);
                setPhotoAdapter(gridView, m_album);
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

        if(action != null) {
            getConnection().enqueue(context, action, new WebAction.ResultHandler<Album>() {
                @Override
                public void onActionResult(Status status, Album album) {

                    if(album != null) {
                        setAlbum(album);
                    } else if(m_album == null) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                    }
                    getSwipeRefreshLayout().setRefreshing(false);
                }
            });
        }
    }

    @Override
    protected String getPlaceholderString() {
        return getString(R.string.albums_label_no_data);
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
