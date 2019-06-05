package ee.ajapaik.android;

import android.util.Log;
import com.facebook.FacebookSdk;
import ee.ajapaik.android.util.Settings;
import ee.ajapaik.android.util.WebImage;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import static ee.ajapaik.android.util.WebActivity.FACEBOOK_SIGN_IN_RESOLUTION_REQUEST;

@ReportsCrashes(formKey="",
        mode = ReportingInteractionMode.DIALOG,
        mailTo = "reports@ajapaik.ee",
        resDialogText = R.string.acra_dialog_text,
        resDialogTitle = R.string.acra_dialog_title,
        resDialogCommentPrompt = R.string.acra_dialog_comment_prompt,
        resDialogOkToast = R.string.acra_dialog_ok_toast)
public class Application extends android.app.Application {
    private static final String TAG = "Application";

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        super.onCreate();

        if(!BuildConfig.DEBUG) {
            ACRA.init(this);
        }
        Settings settings = new Settings(this);
        settings.setSessionDirty(true);

        FacebookSdk.sdkInitialize(this, FACEBOOK_SIGN_IN_RESOLUTION_REQUEST);
        WebImage.invalidate(this);
    }
}
