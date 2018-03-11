package ee.ajapaik.android;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.facebook.AccessToken;

import ee.ajapaik.android.data.Session;
import ee.ajapaik.android.data.util.Status;
import ee.ajapaik.android.util.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ee.ajapaik.android.util.Authorization.Type.FACEBOOK;

public class WebService extends Service {
    private static final String TAG = "WebService";

//    private static final String API_URL = "https://ajapaik.ee/api/v1/";
    private static final String API_URL = "https://staging.ajapaik.ee/api/v1/";
    private static final int MAX_CONNECTIONS = 4;
    private static final int SHUTDOWN_DELAY_IN_SECONDS = 1;

    private final IBinder m_binder = new LocalBinder();
    private final Handler m_handler = new Handler(Looper.getMainLooper());
    private ExecutorService m_actionQueue = Executors.newFixedThreadPool(1);
    private ExecutorService m_imageQueue = Executors.newFixedThreadPool(MAX_CONNECTIONS - 1);
    private List<Task> m_tasks = new ArrayList<Task>();
    private Session m_session = null;
    private Settings m_settings;

    public WebService() {
    }

    private void stopSelfIfNeeded() {
        synchronized(m_tasks) {
            for(int i = 0, c = m_tasks.size(); i < c; i++) {
                Task task = m_tasks.get(i);

                if(task.isRogue()) {
                    task.getOperation().abortRequest();
                    m_tasks.remove(i);
                    i--;
                    c--;
                }
            }

            if(m_tasks.size() == 0) {
                stopSelf();
            }
        }
    }

    private void runSilentLogin() {
        Authorization authorization = m_settings.getAuthorization();
        if (authorization == null){
            setAuthorizationToAnonymous();
            return;
        } else if (authorization.isAnonymous()) {
            return;
        }
        WebAction<Session> action;

        if (FACEBOOK.equals(authorization.getType())) {
            if (AccessToken.getCurrentAccessToken() == null) {
                setAuthorizationToAnonymous();
            }
        }

        action = Session.createLoginAction(this, authorization);
        action.performRequest(API_URL, null);

        if(action.getStatus() == Status.NONE) {
            m_session = action.getObject();
            m_settings.setSession(m_session);
        } else if (action.getStatus() == Status.ACCESS_DENIED) {
            setAuthorizationToAnonymous();
        }
    }

    private void setAuthorizationToAnonymous() {
        m_settings.setAuthorization(Authorization.getAnonymous());
    }

    private void runOperation(final Task task, final WebOperation operation) {
        final boolean isImageRequest = operation instanceof WebImage;
        ExecutorService queue = isImageRequest ? m_imageQueue : m_actionQueue;

        queue.execute(new Runnable() {
            @Override
            public void run() {
                boolean isSecure = operation.isSecure();
                if (!isImageRequest) runSilentLogin();
                operation.performRequest(API_URL, (isSecure && m_session != null) ? m_session.getWebParameters() : null);

                if(operation.shouldRetry()) {

                    if(m_session != null) {
                        operation.performRequest(API_URL, m_session.getWebParameters());
                    }
                }

                synchronized(m_tasks) {
                    m_tasks.remove(task);
                    task.notifyHandlers();
                }

                m_handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopSelfIfNeeded();
                    }
                }, SHUTDOWN_DELAY_IN_SECONDS * 1000);
            }
        });
    }

    public void enqueueOperation(WebOperation operation, ResultHandler handler) {
        String uniqueId = operation.getUniqueId();
        Task task;

        synchronized(m_tasks) {
            for(int i = 0, c = m_tasks.size(); i < c; i++) {
                Task task_ = m_tasks.get(i);
                WebOperation operation_ = task_.getOperation();

                if(operation == operation_ || (uniqueId != null && Objects.match(uniqueId, operation_.getUniqueId()))) {
                    task_.addHandler(handler);
                    return;
                }
            }

            task = new Task(operation);
            task.addHandler(handler);
            m_tasks.add(task);
        }

        runOperation(task, operation);
    }

    public void dequeueOperation(WebOperation operation) {
        String uniqueId = operation.getUniqueId();

        synchronized(m_tasks) {
            for(int i = 0, c = m_tasks.size(); i < c; i++) {
                Task task = m_tasks.get(i);
                WebOperation operation_ = task.getOperation();

                if((operation == operation_ || (uniqueId != null && Objects.match(uniqueId, operation_.getUniqueId()))) &&
                    task.getHandlerCount() < 2) {
                    operation_.abortRequest();
                    m_tasks.remove(i);
                    break;
                }
            }
        }

        m_handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSelfIfNeeded();
            }
        }, SHUTDOWN_DELAY_IN_SECONDS * 1000);
    }

    @Override
    public void onCreate() {
        if(BuildConfig.DEBUG) {
            Log.d(TAG, "onCreate");
        }

        super.onCreate();

        m_settings = new Settings(this);
        m_session = m_settings.getSession();

        if(m_session != null && m_session.isExpired()) {
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "session was cleared, because it was out of date");
            }

            m_settings.setSession(null);
            m_session = null;
        }
    }

    @Override
    public void onDestroy() {
        if(BuildConfig.DEBUG) {
            Log.d(TAG, "onDestroy");
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return m_binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    private class Task {
        private List<WeakReference<ResultHandler>> m_handlers;
        private WebOperation m_operation;

        public Task(WebOperation operation) {
            m_handlers = new ArrayList<WeakReference<ResultHandler>>();
            m_operation = operation;
        }

        public WebOperation getOperation() {
            return m_operation;
        }

        public int getHandlerCount() {
            int count = 0;

            for(WeakReference<ResultHandler> handlers : m_handlers) {
                ResultHandler handler = handlers.get();

                if(handler != null) {
                    count += 1;
                }
            }

            return count;
        }
        public void addHandler(ResultHandler handler) {
            m_handlers.add(new WeakReference<ResultHandler>(handler));
        }

        public void notifyHandlers() {
            for(WeakReference<ResultHandler> handlers : m_handlers) {
                ResultHandler handler = handlers.get();

                if(handler != null) {
                    handler.onResult(m_operation);
                }
            }
        }

        public boolean isRogue() {
            for(WeakReference<ResultHandler> handlers : m_handlers) {
                ResultHandler handler = handlers.get();

                if(handler != null) {
                    return false;
                }
            }

            return true;
        }
    }

    public static class Connection implements ServiceConnection {
        private List<QueueItem> m_queue = new ArrayList<QueueItem>();
        private boolean m_connecting = false;
        private LocalBinder m_binder;

        @SuppressWarnings("unchecked")
        public <T> WebAction<T> enqueue(Context context, WebAction<T> action, WebAction.ResultHandler<T> handler) {
            String uniqueId = action.getUniqueId();
            ActionItem<T> actionItem;

            if(uniqueId != null) {
                for(QueueItem item : m_queue) {
                    String uniqueId_ = item.getOperation().getUniqueId();

                    if(uniqueId_ != null && uniqueId_.equals(uniqueId)) {
                        try {
                            action = (WebAction<T>)item.getOperation();

                            return action;
                        }
                        catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            actionItem = new ActionItem<T>(context, action, handler);
            m_queue.add(actionItem);

            if(m_binder != null) {
                actionItem.start();
            } else {
                connect(context);
            }

            return action;
        }

        public WebImage enqueue(Context context, WebImage image, WebImage.ResultHandler handler) {
            ImageItem imageItem;

            for(QueueItem item : m_queue) {
                if(image == item.getOperation()) {
                    return image;
                }
            }

            imageItem = new ImageItem(context, image, handler);
            m_queue.add(imageItem);

            if(m_binder != null) {
                imageItem.start();
            } else {
                connect(context);
            }

            return image;
        }

        public void dequeue(Context context, WebOperation operation) {
            for(int i = 0, c = m_queue.size(); i < c; i++) {
                QueueItem item = m_queue.get(i);

                if(item.getOperation() == operation) {
                    item.stop();
                    m_queue.remove(i);
                    break;
                }
            }

            if(m_queue.size() == 0) {
                disconnect(context);
            }
        }

        public void dequeueAll(Context context) {
            for(QueueItem item : m_queue) {
                item.stop();
            }

            m_queue.clear();
            disconnect(context);
        }

        private void connect(Context context) {
            if(!m_connecting) {
                m_connecting = true;
                context.bindService(new Intent(context, WebService.class), this, Context.BIND_AUTO_CREATE);
                context.startService(new Intent(context, WebService.class));
            }
        }

        private void disconnect(Context context) {
            if(m_connecting) {
                m_connecting = false;
                context.unbindService(this);
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "onServiceConnected");
            }

            m_binder = (LocalBinder)service;

            if(m_binder != null) {
                for(QueueItem item : m_queue) {
                    item.start();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "onServiceDisconnected");
            }

            m_binder = null;
        }

        private interface QueueItem {
            WebOperation getOperation();
            void start();
            void stop();
        }

        private class ImageItem implements QueueItem, WebService.ResultHandler {
            private Context m_context;
            private WebImage m_image;
            private WebImage.ResultHandler m_handler;
            private Handler m_handle;

            public ImageItem(Context context, WebImage image, WebImage.ResultHandler handler) {
                m_context = context;
                m_image = image;
                m_handler = handler;
                m_handle = new Handler();
            }

            public WebOperation getOperation() {
                return m_image;
            }

            public void start() {
                m_binder.add(m_image, this);
            }

            public void stop() {
                if(m_binder != null) {
                    m_binder.remove(m_image);
                }

                m_handler = null;
            }

            public void onResult(final WebOperation operation) {
                m_handle.post(new Runnable() {
                    @Override
                    public void run() {
                        m_queue.remove(ImageItem.this);

                        if(m_handler != null) {
                            WebImage image = (WebImage)operation;

                            m_handler.onImageResult(image.getStatus(), image.getDrawable());
                        }

                        if(m_queue.size() == 0) {
                            disconnect(m_context);
                        }
                    }
                });
            }
        }

        private class ActionItem<T> implements QueueItem, WebService.ResultHandler {
            private Context m_context;
            private WebAction<T> m_action;
            private WebAction.ResultHandler<T> m_handler;
            private Handler m_handle;

            public ActionItem(Context context, WebAction<T> action, WebAction.ResultHandler<T> handler) {
                m_context = context;
                m_action = action;
                m_handler = handler;
                m_handle = new Handler();
            }

            public WebOperation getOperation() {
                return m_action;
            }

            public void start() {
                m_binder.add(m_action, this);
            }

            public void stop() {
                //m_binder.remove(m_action);
                m_handler = null;
            }

            public void onResult(final WebOperation operation) {
                m_handle.post(new Runnable() {
                    @Override
                    public void run() {
                        m_queue.remove(ActionItem.this);

                        if(m_handler != null) {
                            WebAction<T> action = (WebAction<T>)operation;

                            m_handler.onActionResult(action.getStatus(), action.getObject());
                        }

                        if(m_queue.size() == 0) {
                            disconnect(m_context);
                        }
                    }
                });
            }
        }
    }

    protected interface ResultHandler {
        void onResult(WebOperation operation);
    }

    protected class LocalBinder extends Binder {
        public void add(WebOperation operation, ResultHandler handler) {
            enqueueOperation(operation, handler);
        }

        public void remove(WebOperation operation) {
            dequeueOperation(operation);
        }
    }
}
