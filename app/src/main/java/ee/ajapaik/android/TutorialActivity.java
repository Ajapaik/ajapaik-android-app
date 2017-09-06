package ee.ajapaik.android;

import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.View;
import ee.ajapaik.android.fragment.CameraFragment;
import ee.ajapaik.android.fragment.util.AlertFragment;
import ee.ajapaik.android.test.R;
import ee.ajapaik.android.util.WebActivity;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import java.util.Date;

import static ee.ajapaik.android.SettingsActivity.DEFAULT_PREFERENCES_KEY;

public class TutorialActivity extends WebActivity {

    private static final String SHOW_TUTORIAL_PREFERENCE_KEY = "showTutorialPreference";
    private static final String SHOW_TUTORIAL_WITHOUT_ASKING_PREFERENCE_KEY = "showTutorialWithoutAsking";
    private static final String TAG_FRAGMENT = "fragment";
    private MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "ThisValueIsStoredAndTutorialIsContinuedWhereLeftPreviousTime");
    private static final int DIALOG_SHOW_TUTORIAL_AGAIN = 12;

    public boolean isTutorialCompleted() {
        return sequence.hasFired();
    }

    void waitForMenuToBeCreatedAndShowTutorial() {
        Handler myHandler = new Handler();
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tutorial();            }
        }, 100);
    }

    private void tutorial() {
        if (!(getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT) instanceof CameraFragment)) return;

        if (sequence.hasFired()) {
            if (!showTutorial()) return;
            if (showTutorialWithoutAsking()) {
                sequence = new MaterialShowcaseSequence(this, new Date().toString());
                SharedPreferences.Editor editor = getPreferences().edit();
                editor.putBoolean(SHOW_TUTORIAL_WITHOUT_ASKING_PREFERENCE_KEY, false);
                editor.apply();
            } else {
                showDialogFragment(DIALOG_SHOW_TUTORIAL_AGAIN);
            }
        }
        setUpAndStartTutorial();
    }

    private void setUpAndStartTutorial() {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view
        sequence.setConfig(config);

        View upperTutorialLayout = findViewById(R.id.tutorial_layout_up);
        View bottomTutorialLayout = findViewById(R.id.tutorial_layout_down);

        if (upperTutorialLayout == null || bottomTutorialLayout == null) return;

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(upperTutorialLayout)
                .setContentText(R.string.tutorial_opacity_zoom)
                .setDismissText(R.string.tutorial_dismiss)
                .setTargetTouchable(true)
                .setDismissOnTargetTouch(false)
                .setFadeDuration(500)
                .withRectangleShape()
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(upperTutorialLayout)
                .setContentText(R.string.tutorial_hide_show_flip)
                .setDismissText(R.string.tutorial_dismiss)
                .setTargetTouchable(true)
                .setDismissOnTargetTouch(false)
                .setFadeDuration(500)
                .withRectangleShape()
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(bottomTutorialLayout)
                .setContentText(R.string.tutorial_take_picture)
                .setDismissText(R.string.tutorial_dismiss)
                .setTargetTouchable(true)
                .setDismissOnTargetTouch(false)
                .setFadeDuration(500)
                .withRectangleShape()
                .build());

        sequence.start();
    }

    private boolean showTutorial() {
        return getPreferences().getBoolean(SHOW_TUTORIAL_PREFERENCE_KEY, false);
    }

    private boolean showTutorialWithoutAsking() {
        return getPreferences().getBoolean(SHOW_TUTORIAL_WITHOUT_ASKING_PREFERENCE_KEY, true);
    }

    private SharedPreferences getPreferences() {
        return getSharedPreferences(DEFAULT_PREFERENCES_KEY, MODE_PRIVATE);
    }

    @Override
    protected DialogFragment createDialogFragment(int requestCode) {
        if(requestCode == DIALOG_SHOW_TUTORIAL_AGAIN) {
            return AlertFragment.create(
                    getString(R.string.tutorial_dialog_show_again_title),
                    getString(R.string.tutorial_dialog_show_again_message),
                    getString(R.string.tutorial_dialog_show_again_no),
                    getString(R.string.tutorial_dialog_show_again_yes));
        }

        return super.createDialogFragment(requestCode);
    }

    @Override
    public void onDialogFragmentDismissed(DialogFragment fragment, int requestCode, int resultCode) {
        if(requestCode == DIALOG_SHOW_TUTORIAL_AGAIN) {
            SharedPreferences.Editor editor = getPreferences().edit();
            if(resultCode == AlertFragment.RESULT_POSITIVE) {
                editor.putBoolean(SHOW_TUTORIAL_PREFERENCE_KEY, true);
                sequence = new MaterialShowcaseSequence(this, new Date().toString());
                setUpAndStartTutorial();
            } else {
                editor.putBoolean(SHOW_TUTORIAL_PREFERENCE_KEY, false);
            }
            editor.apply();
            return;
        }

        super.onDialogFragmentDismissed(fragment, requestCode, resultCode);
    }
}
