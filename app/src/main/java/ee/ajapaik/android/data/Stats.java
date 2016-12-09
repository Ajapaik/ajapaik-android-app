package ee.ajapaik.android.data;

import com.google.gson.JsonObject;

import ee.ajapaik.android.data.util.Model;

public class Stats extends Model {
    private static final String KEY_REPHOTOD = "rephotod";
    private static final String KEY_TOTAL = "total";

    public static Stats parse(String str) {
        return CREATOR.parse(str);
    }

    private int m_rephotod;
    private int m_total;

    public Stats() {
        m_rephotod = 0;
        m_total = 0;
    }

    public Stats(JsonObject attributes) {
        m_rephotod = readInteger(attributes, KEY_REPHOTOD);
        m_total = readInteger(attributes, KEY_TOTAL);
    }

    @Override
    public JsonObject getAttributes() {
        JsonObject attributes = new JsonObject();

        attributes.addProperty(KEY_REPHOTOD, m_rephotod);
        attributes.addProperty(KEY_TOTAL, m_total);

        return attributes;
    }

    public int getRephotodCount() {
        return m_rephotod;
    }

    public int getTotalCount() {
        return m_total;
    }

    public boolean empty() {
        return (m_rephotod == 0 && m_total == 0) ? true : false;
    }

    @Override
    public boolean equals(Object obj) {
        Stats stats = (Stats)obj;

        if(stats == this) {
            return true;
        }

        if(stats == null ||
                stats.getRephotodCount() != m_rephotod ||
                stats.getTotalCount() != m_total) {
            return false;
        }

        return true;
    }

    public static final Model.Creator<Stats> CREATOR = new Model.Creator<Stats>() {
        @Override
        public Stats newInstance(JsonObject attributes) {
            return new Stats(attributes);
        }

        @Override
        public Stats[] newArray(int size) {
            return new Stats[size];
        }
    };
}
