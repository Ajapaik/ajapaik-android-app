package ee.ajapaik.android.data;

import android.net.Uri;
import android.text.Html;
import android.text.Spanned;

import com.google.gson.JsonObject;

import ee.ajapaik.android.data.util.Model;
import ee.ajapaik.android.util.Objects;

public class Hyperlink extends Model {
    private static final String KEY_NAME = "name";
    private static final String KEY_URL = "url";

    public static Hyperlink parse(String str) {
        return CREATOR.parse(str);
    }

    public static Hyperlink parse(JsonObject attributes) {
        String name = readString(attributes, KEY_NAME);
        Uri url = readUri(attributes, KEY_URL);

        return (url != null) ? new Hyperlink(url, name) : null;
    }

    private String m_name;
    private Uri m_url;

    public Hyperlink(JsonObject attributes) {
        m_name = readString(attributes, KEY_NAME);
        m_url = readUri(attributes, KEY_URL);

        if(m_url == null) {
            throw new IllegalArgumentException();
        }
    }

    public Hyperlink(Uri url, String name) {
        m_url = url;
        m_name = name;
    }

    @Override
    public JsonObject getAttributes() {
        JsonObject attributes = new JsonObject();

        write(attributes, KEY_NAME, m_name);
        write(attributes, KEY_URL, m_url);

        return attributes;
    }

    public String getName() {
        return m_name;
    }

    public Uri getURL() {
        return m_url;
    }

    @Override
    public boolean equals(Object obj) {
        Hyperlink hyperlink = (Hyperlink)obj;

        if(hyperlink == this) {
            return true;
        }

        if(hyperlink == null ||
           !Objects.match(hyperlink.getName(), m_name) ||
           !Objects.match(hyperlink.getURL(), m_url)) {
            return false;
        }

        return true;
    }

    public Spanned toHtml() {
        if(m_url != null) {
            String url = m_url.toString();

            return Html.fromHtml(String.format("<a href='%s'>%s</a>", url, ((m_name != null) ? m_name : url)));
        }

        return Html.fromHtml((m_name != null) ? m_name : "");
    }

    @Override
    public String toString() {
        return m_url.toString();
    }

    public static final Model.Creator<Hyperlink> CREATOR = new Model.Creator<Hyperlink>() {
        @Override
        public Hyperlink newInstance(JsonObject attributes) {
            return new Hyperlink(attributes);
        }

        @Override
        public Hyperlink[] newArray(int size) {
            return new Hyperlink[size];
        }
    };
}
