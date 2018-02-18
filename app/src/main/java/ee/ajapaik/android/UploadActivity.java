package ee.ajapaik.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.List;

import ee.ajapaik.android.data.Upload;
import ee.ajapaik.android.fragment.UploadFragment;
import ee.ajapaik.android.util.WebActivity;

import static ee.ajapaik.android.UploadActivity.CreatedFrom.CAMERA;

public class UploadActivity extends WebActivity {
    private static final String EXTRA_UPLOAD = "upload";

    private static final String TAG_FRAGMENT = "fragment";

    private static CreatedFrom createdFrom;

    public enum CreatedFrom {
        CAMERA, REPHOTOS
    }

    public static Intent getStartIntent(Context context, List<Upload> uploads) {
        Intent intent = new Intent(context, UploadActivity.class);

        JsonArray jsonArray = new JsonArray();
        for (Upload upload : uploads) {
            jsonArray.add(upload.getAttributes().toString());
        }

        intent.putExtra(EXTRA_UPLOAD, jsonArray.toString());

        return intent;
    }

    public static void start(Context context, List<Upload> uploads, CreatedFrom from) {
        createdFrom = from;
        context.startActivity(getStartIntent(context, uploads));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upload);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null) {
            UploadFragment fragment = new UploadFragment();
            fragment.setUploads(getIntent().getExtras().getString(EXTRA_UPLOAD));

            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, TAG_FRAGMENT).commit();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (isFromCameraActivity()) {
            CameraActivity.start(this, getUpload().getPhoto());
        } else {
            RephotoDraftsActivity.start(this);
        }
    }

    private Upload getUpload() {
        JsonParser jsonParser = new JsonParser();
        String uploadAsJsonString =
                jsonParser.parse(getIntent().getStringExtra(EXTRA_UPLOAD))
                        .getAsJsonArray().get(0)
                        .getAsJsonPrimitive()
                        .getAsString();
        return new Upload(jsonParser.parse(uploadAsJsonString).getAsJsonObject());
    }

    public boolean isFromCameraActivity() {
        return CAMERA.equals(createdFrom);
    }
}
