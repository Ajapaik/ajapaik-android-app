package ee.ajapaik.android.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ee.ajapaik.android.BuildConfig;

public class WebImage extends WebOperation {
    private static final String TAG = "WebImage";

    private static final String SCHEME_FILE = "file://";

    private static final long MAX_CACHE_AGE = 24 * 60 * 60 * 1000; // 24H
    private static final long MAX_CACHE_SIZE = 20 * 1024 * 1024; // 20MB

    private static final String CACHE_PREFIX = "img_";
    private static final int BUFFER_SIZE = 32000;

    private static final Object s_lock = new Object();

    public static void invalidate(Context context) {
        List<FileEntry> cache = new ArrayList<FileEntry>();
        long timestamp = new Date().getTime();
        File dir = getCacheDir(context);
        File[] files = dir.listFiles();
        long totalSize = 0;

        if(files != null) {
            for(File file : files) {
                if(file.getName().startsWith(CACHE_PREFIX)) {
                    if(Math.abs(file.lastModified() - timestamp) > MAX_CACHE_AGE) {
                        if(BuildConfig.DEBUG) {
                            Log.d(TAG, "Removed cached image (> age, " + file.getName() + ")");
                        }

                        file.delete();
                    } else {
                        cache.add(new FileEntry(file));
                    }
                }
            }
        }

        Collections.sort(cache, new FileEntryComparator());

        for(FileEntry entry : cache) {
            File file = entry.file;
            long fileSize = file.length();

            if(totalSize + fileSize > MAX_CACHE_SIZE) {
                if(BuildConfig.DEBUG) {
                    Log.d(TAG, "Removed cached image (> size, " + file.getName() + ")");
                }

                file.delete();
            } else {
                totalSize += fileSize;
            }
        }
    }

    public static void clear(Context context) {
        File dir = getCacheDir(context);
        File[] files = dir.listFiles();

        if(files != null) {
            for(File file : files) {
                if(file.getName().startsWith(CACHE_PREFIX)) {
                    if(BuildConfig.DEBUG) {
                        Log.d(TAG, "Removed cached image (" + file.getName() + ")");
                    }

                    file.delete();
                }
            }
        }
    }

    private static File getCacheDir(Context context) {
        return context.getFilesDir();
    }

    private Drawable m_drawable;
    private boolean m_cache;
    private String m_path;
    private boolean m_render;
    private int m_status = HTTP_STATUS_NOT_FOUND;

    public WebImage(Context context, Uri uri) {
        this(context, uri.toString());
    }

    public WebImage(Context context, String url) {
        this(context, url, true, true);
    }

    public WebImage(Context context, String url, boolean cache, boolean render) {
        super(context, url, null);

        m_cache = cache;
        m_path = CACHE_PREFIX + SHA1.encode(url);
        m_render = render;
    }

    public int getStatus() {
        return m_status;
    }

    public Drawable getDrawable() {
        return m_drawable;
    }

    public String getPath() {
        return m_path;
    }

    @Override
    public String getUniqueId() {
        return getPath();
    }

    @Override
    protected void onFailure() {
        m_status = HTTP_STATUS_INTERNAL_SERVER_ERROR;
    }

    @Override
    protected void onResponse(int statusCode, InputStream is) {
        if(BuildConfig.DEBUG) {
            Log.d(TAG, "statusCode=" + statusCode + ", stream=" + ((is != null) ? "YES" : "NONE"));
        }

        if(isCancelled()) {
            m_status = HTTP_STATUS_NO_CONTENT;
        } else if(statusCode == HTTP_STATUS_OK) {
            if(is != null) {
                try {
                    FileOutputStream os = m_context.openFileOutput(m_path, Context.MODE_PRIVATE);
                    byte data[] = new byte[BUFFER_SIZE];
                    int count;

                    while((count = is.read(data, 0, BUFFER_SIZE)) != -1 && !isCancelled()) {
                        os.write(data, 0, count);
                    }

                    os.flush();
                    os.close();
                    is.close();

                    m_status = HTTP_STATUS_OK;

                    if(isCancelled()) {
                        Log.d(TAG, "Deleted cancelled download (" + m_path + ")");

                        m_status = HTTP_STATUS_NO_CONTENT;
                        m_context.deleteFile(m_path);
                    }
                } catch(FileNotFoundException e) {
                    Log.e(TAG, "", e);
                    m_status = HTTP_STATUS_NOT_FOUND;
                } catch(IOException e) {
                    Log.d(TAG, "", e);
                    m_status = HTTP_STATUS_INTERNAL_SERVER_ERROR;
                }
            } else {
                m_status = HTTP_STATUS_NO_CONTENT;
            }
        } else {
            m_status = statusCode;
        }
    }

    @Override
    public boolean performRequest(String baseURL, Map<String, String> extraParameters) {
        boolean result = false;
        String url = getUrl();

        if(m_cache) {
            File file;

            synchronized(s_lock) {
                invalidate(m_context);
            }

            file = new File(getCacheDir(m_context), m_path);

            if(file.exists()) {
                file.setLastModified(new Date().getTime());
                m_status = HTTP_STATUS_OK;
                result = true;
            }
        }

        if(url != null && url.startsWith(SCHEME_FILE)) {
            File file = new File(Uri.parse(url).getPath());

            m_status = (file.exists()) ? HTTP_STATUS_OK : HTTP_STATUS_NOT_FOUND;
            result = true;
        }

        if(!result) {
            result = super.performRequest(baseURL, extraParameters);
        }

        if(m_render && m_status == HTTP_STATUS_OK) {
            File file = (url.startsWith(SCHEME_FILE)) ? new File(Uri.parse(url).getPath()) : new File(getCacheDir(m_context), m_path);

            m_drawable = Drawable.createFromPath(file.getAbsolutePath());

            if(m_drawable == null) {
                m_status = HTTP_STATUS_INTERNAL_SERVER_ERROR;
            }
        }

        return result;
    }

    private static class FileEntry {
        public File file;
        public long lastModified;

        public FileEntry(File file) {
            this.file = file;
            this.lastModified = file.lastModified();
        }
    }

    private static class FileEntryComparator implements Comparator<FileEntry> {
        public int compare(FileEntry a, FileEntry b) {
            long aL = a.lastModified;
            long bL = b.lastModified;

            if(aL > bL) {
                return -1;
            }

            if(aL < bL) {
                return 1;
            }

            return 0;
        }
    }

    public interface ResultHandler {
        void onImageResult(int status, Drawable drawable);
    }
}
