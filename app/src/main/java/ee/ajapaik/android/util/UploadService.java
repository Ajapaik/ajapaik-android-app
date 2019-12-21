package ee.ajapaik.android.util;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ee.ajapaik.android.R;
import ee.ajapaik.android.WebService;
import ee.ajapaik.android.data.Upload;
import ee.ajapaik.android.data.util.Status;

import static ee.ajapaik.android.util.ExifService.USER_COMMENT;
import static ee.ajapaik.android.util.NotificationChannel.NOTIFICATION_CHANNEL;

public class UploadService extends Service {

    public static final String BITMAP_KEY = "bitmap";

    private static final int NOTIFICATION_ID = 1000;

    private WebService.Connection m_connection = new WebService.Connection();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int i = super.onStartCommand(intent, flags, startId);
        uploadPhoto(intent);
        return i;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showNotification(String title, String content) {
        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL.name())
                .setSmallIcon(R.drawable.ic_add_to_photos_white_36dp)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setOngoing(false).build();
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification);
    }

    private void uploadPhoto(Intent intent) {
        Upload rephotoBitmap = (Upload) intent.getExtras().get(BITMAP_KEY);
        if (rephotoBitmap == null) {
            return;
        }
        showNotification(getString(R.string.upload_notification_title), getString(R.string.upload_notification_in_progress));
        WebAction<Upload> action = Upload.createAction(getApplicationContext(), rephotoBitmap);

        m_connection.enqueue(getApplicationContext(), action, new WebAction.ResultHandler<Upload>() {
            @Override
            public void onActionResult(Status status, Upload upload) {
                if (status.isGood()) {
                    ExifService.deleteField(rephotoBitmap.getPath(), USER_COMMENT);
                    showNotification(getString(R.string.upload_dialog_success_title), getString(R.string.upload_dialog_success_message));
                    stopSelf();
                } else {
                    showNotification(getString(R.string.upload_notification_title), getString(R.string.upload_notification_failure));
                }
            }
        });
    }

}
