package ee.ajapaik.android.data;

import android.content.Context;
import android.location.Location;
import android.net.Uri;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import ee.ajapaik.android.data.util.Model;
import ee.ajapaik.android.util.Objects;
import ee.ajapaik.android.util.WebAction;

public class Album extends Model {
    private static final String API_NEAREST_PATH = "/album/nearest/";
    private static final String API_FAVORITES_PATH = "/album/favorites/";
    private static final String API_STATE_PATH = "/album/state/";
    private static final String KEY_IDENTIFIER = "id";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_TITLE = "title";
    private static final String KEY_STATE = "state";
    private static final String KEY_STATS = "stats";
    private static final String KEY_PHOTOS = "photos";
    private static final String KEY_PHOTOS_ADD = "photos+";
    private static final String KEY_PHOTOS_REMOVE = "photos-";

    public static WebAction<Album> createNearestAction(Context context, Location location, String state, int range) {
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

        return new Action(context, API_NEAREST_PATH, parameters, null,
                latitude_.substring(0, Math.min(latitude_.length(), 9)) + "," +
                longitude_.substring(0, Math.min(longitude_.length(), 9)));
    }

    public static WebAction<Album> createFavoritesAction(Context context) {
        return new Action(context, API_FAVORITES_PATH, new HashMap<String, String>(), null, "favorites");
    }

    public static WebAction<Album> createStateAction(Context context, Album album) {
        Map<String, String> parameters = new Hashtable<String, String>();

        parameters.put("id", album.getIdentifier());

        if(album.getState() != null) {
            parameters.put("state", album.getState());
        }

        return new Action(context, API_STATE_PATH, parameters, album, album.getIdentifier());
    }

    public static WebAction<Album> createStateAction(Context context, String albumIdentifier) {
        Map<String, String> parameters = new Hashtable<String, String>();

        parameters.put("id", albumIdentifier);

        return new Action(context, API_STATE_PATH, parameters, null, albumIdentifier);
    }

    public static Album parse(String str) {
        return CREATOR.parse(str);
    }

    public static Album update(Album album, Photo photo) {
        if(album.getPhoto(photo.getIdentifier()) != null) {
            JsonObject attributes = new JsonObject();
            JsonArray photos = new JsonArray();

            photos.add(photo.getAttributes());
            attributes.add(KEY_PHOTOS_ADD, photos);

            return new Album(attributes, album, album.getIdentifier());
        }

        return album;
    }

    private String m_identifier;
    private Uri m_image;
    private String m_title;
    private String m_state;
    private Stats m_stats;
    private List<Photo> m_photos;

    public Album(JsonObject attributes) {
        this(attributes, null, null);
    }

    public Album(List<Photo> photos, String identifier) {
        m_identifier = identifier;
        m_photos = photos;
        m_state = String.valueOf(System.currentTimeMillis());
    }

    public Album(JsonObject attributes, Album baseAlbum, String baseIdentifier) {
        JsonObject stats = readObject(attributes, KEY_STATS);
        JsonElement element = attributes.get(KEY_PHOTOS);

        m_identifier = readIdentifier(attributes, KEY_IDENTIFIER);
        m_image = readUri(attributes, KEY_IMAGE, (baseAlbum != null) ? baseAlbum.getImage() : null);
        m_title = readString(attributes, KEY_TITLE, (baseAlbum != null) ? baseAlbum.getTitle() : null);
        m_state = readString(attributes, KEY_STATE, (baseAlbum != null) ? baseAlbum.getState() : null);
        m_stats = (stats != null) ? new Stats(stats) : ((baseAlbum != null) ? baseAlbum.getStats() : null);
        m_photos = new ArrayList<Photo>();

        if(m_identifier == null && baseIdentifier != null) {
            m_identifier = baseIdentifier;
        }

        if(element != null && element.isJsonArray()) {
            for(JsonElement photoElement : element.getAsJsonArray()) {
                if(photoElement.isJsonObject()) {
                    try {
                        m_photos.add(new Photo(photoElement.getAsJsonObject()));
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if(baseAlbum != null) {
            List<Photo> photos = baseAlbum.getPhotos();

            if(photos != null && photos.size() > 0) {
                for(Photo photo : photos) {
                    m_photos.add(photo);
                }
            }
        }

        for(JsonElement photoToRemoveElement : readArray(attributes, KEY_PHOTOS_REMOVE)) {
            if(photoToRemoveElement.isJsonPrimitive()) {
                JsonPrimitive photoPrimitive = photoToRemoveElement.getAsJsonPrimitive();
                Photo photo = null;

                if(photoPrimitive.isString()) {
                    photo = getPhoto(photoPrimitive.getAsString());
                } else if(photoPrimitive.isNumber()) {
                    photo = getPhoto(photoPrimitive.toString());
                }

                if(photo != null) {
                    m_photos.remove(photo);
                }
            }
        }

        for(JsonElement photoToAddElement : readArray(attributes, KEY_PHOTOS_ADD)) {
            if(photoToAddElement.isJsonObject()) {
                try {
                    JsonObject photoObject = photoToAddElement.getAsJsonObject();
                    Photo oldPhoto = getPhoto(readIdentifier(photoObject, KEY_IDENTIFIER));
                    Photo newPhoto = new Photo(photoObject, oldPhoto);

                    if(oldPhoto == null) {
                        m_photos.add(newPhoto);
                    } else if(!Objects.match(oldPhoto, newPhoto)) {
                        m_photos.set(m_photos.indexOf(oldPhoto), newPhoto);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if(m_identifier == null) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public JsonObject getAttributes() {
        JsonObject attributes = new JsonObject();

        write(attributes, KEY_IDENTIFIER, m_identifier);
        write(attributes, KEY_IMAGE, m_image);
        write(attributes, KEY_TITLE, m_title);

        if(m_stats != null && !m_stats.empty()) {
            attributes.add(KEY_STATS, m_stats.getAttributes());
        }

        if(m_photos != null && m_photos.size() > 0) {
            JsonArray array = new JsonArray();

            for(Photo photo : m_photos) {
                array.add(photo.getAttributes());
            }

            attributes.add(KEY_PHOTOS, array);
        }

        return attributes;
    }

    public String getIdentifier() {
        return m_identifier;
    }

    public Uri getImage() {
        return m_image;
    }

    public Uri getThumbnail(int preferredDimension) {
        return Photo.resolve(m_image, preferredDimension);
    }

    public String getTitle() {
        return m_title;
    }

    public String getState() {
        return m_state;
    }

    public Stats getStats() {
        return m_stats;
    }

    public Photo getFirstPhoto() {
        if(m_photos != null) {
            for(Photo photo : m_photos) {
                return photo;
            }

            if(m_photos.size() > 0) {
                return m_photos.get(0);
            }
        }

        return null;
    }

    public Photo getLastPhoto() {
        if(m_photos != null) {
            for(int i = m_photos.size() - 1; i >= 0; i--) {
                Photo photo = m_photos.get(i);

                return photo;
            }

            if(m_photos.size() > 0) {
                return m_photos.get(m_photos.size() - 1);
            }
        }

        return null;
    }

    public Photo getPrevPhoto(String identifier) {
        if(identifier != null && m_photos != null) {
            for(int i = 0, c = m_photos.size(); i < c; i++) {
                Photo photo = m_photos.get(i);

                if(photo.getIdentifier().equals(identifier)) {
                    return (i > 0) ? m_photos.get(i - 1) : null;
                }
            }
        }

        return null;
    }

    public Photo getNextPhoto(String identifier) {
        if(identifier != null && m_photos != null) {
            for(int i = 0, c = m_photos.size(); i < c; i++) {
                Photo photo = m_photos.get(i);

                if(photo.getIdentifier().equals(identifier)) {
                    return (i + 1 < c) ? m_photos.get(i + 1) : null;
                }
            }
        }

        return null;
    }

    public Photo getPhoto(String identifier) {
        if(identifier != null && m_photos != null) {
            for(Photo photo : m_photos) {
                if(photo.getIdentifier().equals(identifier)) {
                    return photo;
                }
            }
        }

        return null;
    }

    public List<Photo> getPhotos() {
        return m_photos;
    }

    @Override
    public boolean equals(Object obj) {
        Album album = (Album)obj;

        if(album == this) {
            return true;
        }

        if(album == null ||
                !Objects.match(album.getIdentifier(), m_identifier) ||
                !Objects.match(album.getImage(), m_image) ||
                !Objects.match(album.getTitle(), m_title) ||
                !Objects.match(album.getState(), m_state) ||
                !Objects.match(album.getStats(), m_stats) ||
                !Objects.match(album.getPhotos(), m_photos)) {
            return false;
        }

        return true;
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

    public static final Model.Creator<Album> CREATOR = new Model.Creator<Album>() {
        @Override
        public Album newInstance(JsonObject attributes) {
            return new Album(attributes);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
}