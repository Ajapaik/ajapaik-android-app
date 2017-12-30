package ee.ajapaik.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ee.ajapaik.android.R;
import ee.ajapaik.android.adapter.PhotoAdapter;
import ee.ajapaik.android.data.Album;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.data.util.Status;
import ee.ajapaik.android.util.WebAction;
import ee.ajapaik.android.widget.StaggeredGridView;

public abstract class PhotosFragment extends SearchFragment {

    protected abstract String getPlaceholderString();
    protected abstract void setAlbum(Album album);

    protected Album m_album;

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getSwipeRefreshLayout().setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refresh();
                    }
                }
        );

        getSwipeRefreshLayout().setRefreshing(true);
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
                        Toast.makeText(getActivity(), getResources().getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                    }
                    getSwipeRefreshLayout().setRefreshing(false);
                }
            });
        }
    }

    protected void setPhotoAdapter(StaggeredGridView gridView, List<Photo> photos, PhotoAdapter.OnPhotoSelectionListener selectionListener) {
        gridView.setAdapter(new PhotoAdapter(gridView.getContext(), photos, getSettings().getLocation(), selectionListener));
    }

    protected void initializeEmptyGridView(StaggeredGridView gridView) {
        getEmptyView().setText(getPlaceholderString());
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
}
