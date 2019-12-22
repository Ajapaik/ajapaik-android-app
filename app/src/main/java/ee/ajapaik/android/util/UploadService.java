package ee.ajapaik.android.util;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ee.ajapaik.android.NearestActivity;
import ee.ajapaik.android.PhotoActivity;
import ee.ajapaik.android.R;
import ee.ajapaik.android.RephotoDraftsActivity;
import ee.ajapaik.android.WebService;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.data.Upload;
import ee.ajapaik.android.data.util.Status;

import static ee.ajapaik.android.util.ExifService.USER_COMMENT;
import static ee.ajapaik.android.util.NotificationChannel.NOTIFICATION_CHANNEL;

public class UploadService extends Service {

    public static final String UPLOAD_KEY = "upload";

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

    private void showNotification(String title, Photo photo, Intent startIntent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL.name())
                .setSmallIcon(R.drawable.ic_add_to_photos_white_36dp)
                .setContentTitle(title)
                .setContentText(photo.getTitle())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setOngoing(false);

        if (startIntent != null) {
            Intent launcherIntent = new Intent(this, NearestActivity.class);
            launcherIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            notificationBuilder.setContentIntent(PendingIntent.getActivities(
                    this,
                    0,
                    new Intent[]{launcherIntent, startIntent},
                    PendingIntent.FLAG_UPDATE_CURRENT));
        }
        NotificationManagerCompat.from(this).notify(photo.getIdentifier(), NOTIFICATION_ID, notificationBuilder.build());
    }

    private void uploadPhoto(Intent intent) {
        Upload upload = (Upload) intent.getExtras().get(UPLOAD_KEY);
        if (upload == null) {
            return;
        }
        Photo originalPhoto = upload.getPhoto();
        showNotification(getString(R.string.upload_notification_title), originalPhoto, null);
        WebAction<Upload> action = Upload.createAction(getApplicationContext(), upload);

        m_connection.enqueue(getApplicationContext(), action, new WebAction.ResultHandler<Upload>() {
            @Override
            public void onActionResult(Status status, Upload requestBody) {
                if (status.isGood()) {
                    ExifService.deleteField(upload.getPath(), USER_COMMENT);

                    Intent startIntent = PhotoActivity.getStartIntent(UploadService.this, originalPhoto, null);
                    showNotification(getString(R.string.upload_dialog_success_title), originalPhoto, startIntent);
                    stopSelf();
                } else {
                    Intent startIntent = new Intent(UploadService.this, RephotoDraftsActivity.class);
                    showNotification(getString(R.string.upload_notification_title), originalPhoto, startIntent);
                }
            }
        });
    }
}
