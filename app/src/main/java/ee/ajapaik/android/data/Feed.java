package ee.ajapaik.android.data;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ee.ajapaik.android.data.util.Model;
import ee.ajapaik.android.util.Objects;
import ee.ajapaik.android.util.WebAction;

public class Feed extends Model {
    private static final String API_PATH = "/albums/";
    private static final String KEY_ALBUMS = "albums";

    public static WebAction<Feed> createAction(Context context) {
        return new Action(context, API_PATH, null);
    }

    public static Feed parse(String str) {
        return CREATOR.parse(str);
    }

    private List<Album> m_albums;

    public Feed(JsonObject attributes) {
        m_albums = new ArrayList<Album>();

        for(JsonElement tagElement : readArray(attributes, KEY_ALBUMS)) {
            if(tagElement.isJsonObject()) {
                try {
                    m_albums.add(new Album(tagElement.getAsJsonObject()));
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public JsonObject getAttributes() {
        JsonObject attributes = new JsonObject();

        if(m_albums.size() > 0) {
            JsonArray array = new JsonArray();

            for(Album album : m_albums) {
                array.add(album.getAttributes());
            }

            attributes.add(KEY_ALBUMS, array);
        }

        return attributes;
    }

    public List<Album> getAlbums() {
        return m_albums;
    }

    @Override
    public boolean equals(Object obj) {
        Feed feed = (Feed)obj;

        if(feed == this) {
            return true;
        }

        if(feed == null ||
                !Objects.match(feed.getAlbums(), m_albums)) {
            return false;
        }

        return true;
    }

    private static class Action extends WebAction<Feed> {
        public Action(Context context, String path, Map<String, String> parameters) {
            super(context, path, parameters, CREATOR);
        }

        @Override
        public String getUniqueId() {
            return API_PATH;
        }
    }

    public static final Model.Creator<Feed> CREATOR = new Model.Creator<Feed>() {
        @Override
        public Feed newInstance(JsonObject attributes) {
            return new Feed(attributes);
        }

        @Override
        public Feed[] newArray(int size) {
            return new Feed[size];
        }
    };
}
