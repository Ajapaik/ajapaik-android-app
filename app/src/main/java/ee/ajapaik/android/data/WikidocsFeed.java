package ee.ajapaik.android.data;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import ee.ajapaik.android.data.util.Model;
import ee.ajapaik.android.util.Objects;
import ee.ajapaik.android.util.WebAction;

public class WikidocsFeed extends Model {
/*    private static final String API_PATH = "/wikidocumentaries/";
    private static final String API_SEARCH_PATH = "/wikidocumentaries/search/";
    private static final String KEY_ALBUMS = "wikidocumentaries";*/

    private static final String TAG = "WikidocsFeed";
    private static final String API_PATH = "/wikidocumentaries/";
    private static final String API_SEARCH_PATH = "/wikidocumentaries/";
    private static final String KEY_ALBUMS = "albums";


    public static WebAction<WikidocsFeed> createAction(Context context, Location location) {
        Map<String, String> parameters = new Hashtable<String, String>();
        String latitude_ = Double.toString(location.getLatitude());
        String longitude_ = Double.toString(location.getLongitude());

        parameters.put("latitude", latitude_);
        parameters.put("longitude", longitude_);
        return new Action(context, API_PATH, parameters);
    }

    public static WebAction<WikidocsFeed> createSearchAction(Context context, String query, Location location) {
        Map<String, String> parameters = new Hashtable<String, String>();
        String latitude_ = Double.toString(location.getLatitude());
        String longitude_ = Double.toString(location.getLongitude());

        parameters.put("latitude", latitude_);
        parameters.put("longitude", longitude_);

        parameters.put("query", query);

        return new Action(context, API_SEARCH_PATH, parameters);
    }

    public static WikidocsFeed parse(String str) {
        return CREATOR.parse(str);
    }

    private List<Album> m_albums;

    public WikidocsFeed(JsonObject attributes) {
        m_albums = new ArrayList<Album>();

        for(JsonElement tagElement : readArray(attributes, KEY_ALBUMS)) {
            if(tagElement.isJsonObject()) {
                try {
                    Log.d(TAG, "WikidocsFeed: new Album:" + tagElement);
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
        WikidocsFeed feed = (WikidocsFeed)obj;

        if(feed == this) {
            return true;
        }

        if(feed == null ||
                !Objects.match(feed.getAlbums(), m_albums)) {
            return false;
        }

        return true;
    }

    private static class Action extends WebAction<WikidocsFeed> {
        public Action(Context context, String path, Map<String, String> parameters) {
            super(context, path, parameters, CREATOR);
        }

        @Override
        public String getUniqueId() {
            return API_PATH;
        }
    }

    public static final Creator<WikidocsFeed> CREATOR = new Creator<WikidocsFeed>() {
        @Override
        public WikidocsFeed newInstance(JsonObject attributes) {
            return new WikidocsFeed(attributes);
        }

        @Override
        public WikidocsFeed[] newArray(int size) {
            return new WikidocsFeed[size];
        }
    };
}
