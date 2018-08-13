package ee.ajapaik.android.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import ee.ajapaik.android.R;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.widget.WebImageView;

public class MapViewGridAdapter extends RecyclerView.Adapter<MapViewGridAdapter.MapViewGridHolder> {
    private List<Photo> photos;
    private Context context;

    public MapViewGridAdapter(List<Photo> photos, Context context) {
        this.photos = photos;
        this.context = context;
    }

    @NonNull
    @Override
    public MapViewGridHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View groceryProductView = LayoutInflater.from(parent.getContext()).inflate(R.layout.map_view_grid_item, parent, false);
        return new MapViewGridHolder(groceryProductView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MapViewGridHolder holder, final int position) {
        holder.imageView.setImageURI(photos.get(position).getThumbnail(400));
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                Toast.makeText(context, position + " is selected. Adapter position = " + adapterPosition, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    class MapViewGridHolder extends RecyclerView.ViewHolder {
        WebImageView imageView;

        MapViewGridHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.map_view_grid_item_image);
        }
    }
}