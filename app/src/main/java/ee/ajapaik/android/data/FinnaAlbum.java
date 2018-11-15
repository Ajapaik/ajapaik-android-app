package ee.ajapaik.android.data;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.gson.JsonObject;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import ee.ajapaik.android.util.WebAction;

public class FinnaAlbum extends Album {
    private static final String TAG = "FinnaAlbum";
    private static final String API_NEAREST_PATH = "/finna/nearest/";
    private static final String API_SEARCH_PATH  = "/finna/nearest/";

    public FinnaAlbum(JsonObject attributes, Album baseAlbum, String baseIdentifier) {
        super(attributes, baseAlbum, baseIdentifier);
    }

    public FinnaAlbum(List<Photo> photos, String identifier) {
        super(photos, identifier);
    }

    public FinnaAlbum(JsonObject attributes) {
        super(attributes);
    }


    public static WebAction<Album> createNearestAction(Context context, Location location, String state, int range, String finnaAlbum) {
        Log.d(TAG, "FinnaAlbum: createNearestAction");

        Map<String, String> parameters = new Hashtable<String, String>();
        String latitude_ = Double.toString(location.getLatitude());
        String longitude_ = Double.toString(location.getLongitude());

        parameters.put("latitude", latitude_);
        parameters.put("longitude", longitude_);


        if(range > 0) {
            parameters.put("range", Integer.toString(range));
        }

        if(state != null) {
            parameters.put("state", state);
        }

        if (!finnaAlbum.isEmpty()) {
            parameters.put("album", finnaAlbum);
        }

        return new FinnaAlbum.Action(context, API_NEAREST_PATH, parameters, null,
                latitude_.substring(0, Math.min(latitude_.length(), 9)) + "," +
                        longitude_.substring(0, Math.min(longitude_.length(), 9)));
    }

    public static WebAction<Album> createSearchAction(Context context, String query, Location location, int range, String finnaAlbum) {
        String baseIdentifier = "all-albums|" + query.replaceAll(" ", "-");

        Map<String, String> parameters = new Hashtable<String, String>();
        parameters.put("query", query);

        String latitude_ = Double.toString(location.getLatitude());
        String longitude_ = Double.toString(location.getLongitude());

        parameters.put("latitude", latitude_);
        parameters.put("longitude", longitude_);

        if(range > 0) {
            parameters.put("range", Integer.toString(range));
        }

        if (!finnaAlbum.isEmpty()) {
            parameters.put("album", finnaAlbum);
        }

        return new FinnaAlbum.Action(context, API_SEARCH_PATH, parameters, null, baseIdentifier);
    }

    private static class Action extends WebAction<Album> {
        private Album m_baseAlbum;
        private String m_baseIdentifier;

        public Action(Context context, String path, Map<String, String> parameters, Album baseAlbum, String baseIdentifier) {
            super(context, path, parameters, CREATOR);
            m_baseAlbum = baseAlbum;
            m_baseIdentifier = baseIdentifier;
        }

        @Override
        public String getUniqueId() {
            return getUrl() + m_baseIdentifier + "/" + ((m_baseAlbum != null && m_baseAlbum.getState() != null) ? m_baseAlbum.getState() : "");
        }

        @Override
        protected Album parseObject(JsonObject attributes) {
            return new Album(attributes, m_baseAlbum, m_baseIdentifier);
        }
    }

}
