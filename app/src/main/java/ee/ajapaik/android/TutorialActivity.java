package ee.ajapaik.android;

import android.os.Handler;
import ee.ajapaik.android.test.R;
import ee.ajapaik.android.util.WebActivity;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class TutorialActivity extends WebActivity {
    void waitForMenuToBeCreatedAndShowTutorial() {
        Handler myHandler = new Handler();
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tutorial();            }
        }, 100);
    }

    private void tutorial() {
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "ThisValueIsStoredAndTutorialIsContinuedWhereLeftPreviousTime");
        if (sequence.hasFired()) return;

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view
        sequence.setConfig(config);

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(findViewById(R.id.image))
                .setTitleText(R.string.tutorial_hide_title)
                .setContentText(R.string.tutorial_hide_content)
                .setDismissText(R.string.tutorial_dismiss)
                .setTargetTouchable(true)
                .setFadeDuration(500)
                .setShapePadding(-500)
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(findViewById(R.id.image))
                .setTitleText(R.string.tutorial_show_title)
                .setContentText(R.string.tutorial_show_content)
                .setDismissText(R.string.tutorial_dismiss)
                .setTargetTouchable(true)
                .setShapePadding(-500)
                .setFadeDuration(500)
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(findViewById(R.id.image))
                .setTitleText(R.string.tutorial_opacity_title)
                .setContentText(R.string.tutorial_opacity_content)
                .setDismissText(R.string.tutorial_dismiss)
                .setTargetTouchable(true)
                .setFadeDuration(500)
                .setShapePadding(-250)
                .withRectangleShape()
                .setHideTimeout(2000)
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(findViewById(R.id.image))
                .setTitleText(R.string.tutorial_zoom_title)
                .setContentText(R.string.tutorial_zoom_content)
                .setDismissText(R.string.tutorial_dismiss)
                .setTargetTouchable(true)
                .setFadeDuration(500)
                .setShapePadding(-250)
                .withRectangleShape()
                .setHideTimeout(2000)
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(findViewById(R.id.action_flip))
                .setTitleText(R.string.tutorial_flip_title)
                .setContentText(R.string.tutorial_flip_content)
                .setDismissText(R.string.tutorial_dismiss)
                .setTargetTouchable(true)
                .setFadeDuration(500)
                .build());

        sequence.addSequenceItem(new MaterialShowcaseView.Builder(this)
                .setTarget(findViewById(R.id.button_action_camera))
                .setTitleText(R.string.tutorial_take_picture_title)
                .setContentText(R.string.tutorial_take_picture_content)
                .setDismissText(R.string.tutorial_dismiss)
                .setTargetTouchable(true)
                .setFadeDuration(500)
                .build());

        sequence.start();
    }
}
