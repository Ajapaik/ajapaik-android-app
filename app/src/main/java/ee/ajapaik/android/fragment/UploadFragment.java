package ee.ajapaik.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ToxicBakery.viewpager.transforms.DefaultTransformer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.viewpagerindicator.CirclePageIndicator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import ee.ajapaik.android.CameraActivity;
import ee.ajapaik.android.ProfileActivity;
import ee.ajapaik.android.R;
import ee.ajapaik.android.RephotoDraftsActivity;
import ee.ajapaik.android.UploadActivity;
import ee.ajapaik.android.adapter.UploadPagerAdapter;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.data.Upload;
import ee.ajapaik.android.data.util.Status;
import ee.ajapaik.android.fragment.util.AlertFragment;
import ee.ajapaik.android.fragment.util.DialogInterface;
import ee.ajapaik.android.fragment.util.ProgressFragment;
import ee.ajapaik.android.fragment.util.WebFragment;
import ee.ajapaik.android.util.ExifService;
import ee.ajapaik.android.util.WebAction;
import ee.ajapaik.android.widget.WebImageView;

import static android.content.Context.MODE_PRIVATE;
import static ee.ajapaik.android.SettingsActivity.DEFAULT_PREFERENCES_KEY;
import static ee.ajapaik.android.util.ExifService.USER_COMMENT;

public class UploadFragment extends WebFragment implements DialogInterface {
    private static final String KEY_UPLOAD = "upload";

    private static final int DIALOG_ERROR_NO_CONNECTION = 1;
    private static final int DIALOG_ERROR_UNKNOWN = 2;
    private static final int DIALOG_PROGRESS = 3;
    private static final int DIALOG_SUCCESS = 4;
    private static final int DIALOG_NOT_AUTHENTICATED = 5;
    private static final int DIALOG_NOT_AGREED_TO_TERMS = 6;

    private static final int THUMBNAIL_SIZE = 400;

    private List<Upload> m_uploads;
    private LinkedHashMap<Bitmap, Upload> uploadByRephotoBitmap;
    private Bitmap currentRephoto;
    private final JsonParser jsonParser = new JsonParser();

    public static final String RETURN_ACTIVITY_NAME = "upload";

    public List<Upload> getUpload() {
        Bundle arguments = getArguments();

        if (arguments != null) {
            String uploadsJson = arguments.getString(KEY_UPLOAD);
            return parseUploadsJson(uploadsJson);
        }

        return null;
    }

    private List<Upload> parseUploadsJson(String uploadsJson) {
        List<Upload> uploads = new ArrayList<>();
        JsonArray jsonElements = jsonParser.parse(uploadsJson).getAsJsonArray();
        for (JsonElement jsonElement : jsonElements) {
            uploads.add(new Upload(jsonParser.parse(jsonElement.getAsString()).getAsJsonObject()));
        }
        return uploads;
    }

    public void setUploads(String uploadsJson) {
        Bundle arguments = getArguments();

        if (arguments == null) {
            arguments = new Bundle();
        }

        if (uploadsJson != null) {
            arguments.putString(KEY_UPLOAD, uploadsJson);
        } else {
            arguments.remove(KEY_UPLOAD);
        }

        setArguments(arguments);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upload, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            m_uploads = parseUploadsJson(savedInstanceState.getString(KEY_UPLOAD));
        }

        if (m_uploads == null) {
            m_uploads = getUpload();
        }

        uploadByRephotoBitmap = new LinkedHashMap<>();
        sortUploadsByDate();
        for (Upload upload : m_uploads) {
            Bitmap scaledRephoto = scaleRephoto(upload);
            uploadByRephotoBitmap.put(scaledRephoto, upload);
        }
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        UploadPagerAdapter adapter = new UploadPagerAdapter(getActivity(), new ArrayList<>(uploadByRephotoBitmap.keySet()));
        getViewPager().setAdapter(adapter);
        final ViewPager.OnPageChangeListener pageChangeListener = createOnPageChangeListener(adapter);
        getViewPager().setPageTransformer(true, new DefaultTransformer());

        if (uploadByRephotoBitmap.size() > 1) {
            CirclePageIndicator pageIndicator = getPageIndicator();
            pageIndicator.setViewPager(getViewPager());
            pageIndicator.setOnPageChangeListener(pageChangeListener);
        }

        getOldImageView().setImageURI(uploadByRephotoBitmap.entrySet().iterator().next().getValue().getPhoto().getThumbnail(THUMBNAIL_SIZE));
        getOldImageView().setFlipped(getUpload().get(0).isFlipped());
        selectFirstDraftToDisplay(pageChangeListener);

        getOldImageView().setOnLoadListener(new WebImageView.OnLoadListener() {
            @Override
            public void onImageLoaded() {
                getMainLayout().setVisibility(View.VISIBLE);
            }

            @Override
            public void onImageUnloaded() {
                getMainLayout().setVisibility(View.GONE);
            }

            @Override
            public void onImageFailed() {
            }
        });

        if (((UploadActivity)getActivity()).isFromCameraActivity()) {
            getSaveButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closePreviewAndGoBack();
                }
            });
        } else {
            getSaveButton().setVisibility(View.INVISIBLE);
        }

        getDeleteButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new File(uploadByRephotoBitmap.get(currentRephoto).getPath()).delete();
                removePhotoFromDeviceGallery();
                closePreviewAndGoBack();
            }
        });

        getConfirmButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPhoto();
            }
        });
    }

    private void sortUploadsByDate() {
        Collections.sort(m_uploads, new Comparator<Upload>() {
            @Override
            public int compare(Upload upload, Upload t1) {
                return t1.getPath().compareTo(upload.getPath());
            }
        });
    }

    private void removePhotoFromDeviceGallery() {
        getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(
                new File(uploadByRephotoBitmap.get(currentRephoto).getPath()))));
    }

    private void closePreviewAndGoBack() {
        UploadActivity activity = (UploadActivity) getActivity();

        activity.setResult(Activity.RESULT_FIRST_USER);
        if (activity.isFromCameraActivity()) {
            CameraActivity.start(activity, uploadByRephotoBitmap.get(currentRephoto).getPhoto());
        } else {
            RephotoDraftsActivity.start(activity);
        }
        activity.finish();
    }

    private void selectFirstDraftToDisplay(final ViewPager.OnPageChangeListener pageChangeListener) {
        getViewPager().post(new Runnable() {
            @Override
            public void run() {
                pageChangeListener.onPageSelected(0);
            }
        });
    }

    private ViewPager.OnPageChangeListener createOnPageChangeListener(final UploadPagerAdapter adapter) {
        return new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentRephoto = adapter.getBitmap(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        };
    }

    private Bitmap scaleRephoto(Upload upload) {
        BitmapFactory.Options options = getBitmapOptions(upload);
        float unscaledImageWidth = options.outWidth;
        float unscaledImageHeight = options.outHeight;

        float heightScale = 1.0F;
        float widthScale = 1.0F;
        Photo oldPhoto = upload.getPhoto();

        if (needsHeightScaling(unscaledImageWidth, unscaledImageHeight, oldPhoto)) {
            float scale = unscaledImageWidth / oldPhoto.getWidth();
            heightScale = (oldPhoto.getHeight() * scale) / unscaledImageHeight;
        } else {
            float scale = unscaledImageHeight / oldPhoto.getHeight();
            widthScale = (oldPhoto.getWidth() * scale) / unscaledImageWidth;
        }

        float scaledImageWidth = unscaledImageWidth * widthScale * upload.getScale();
        float scaledImageHeight = unscaledImageHeight * heightScale * upload.getScale();
        float heightDifference = unscaledImageHeight - scaledImageHeight;
        float widthDifference = unscaledImageWidth - scaledImageWidth;
        return Bitmap.createBitmap(
                BitmapFactory.decodeFile(upload.getPath()),
                (int) (Math.max(widthDifference / 2, 0)),
                (int) (Math.max(heightDifference / 2, 0)),
                (int) (Math.min(unscaledImageWidth, scaledImageWidth)),
                (int) (Math.min(unscaledImageHeight, scaledImageHeight)),
                getRotationMatrix(upload.getPath()),
                true );
    }

    @NonNull
    private BitmapFactory.Options getBitmapOptions(Upload upload) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(upload.getPath(), options);
        return options;
    }

    /**
     * This is needed as some devices (e.g. Samsung Galaxy S5) store rotation data in meta info and actual image is not rotated
     *
     * @return Matrix with proper rotation
     * @param path to bitmap needing rotation
     */
    private Matrix getRotationMatrix(String path) {
        Matrix matrix = new Matrix();
        try {
            ExifInterface exif = new ExifInterface(path);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            switch (rotation) {
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.setScale(-1, 1);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.setRotate(180);
                    break;

                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;

                case ExifInterface.ORIENTATION_TRANSPOSE:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.setRotate(90);
                    break;

                case ExifInterface.ORIENTATION_TRANSVERSE:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.setRotate(-90);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    break;
            }
        } catch (IOException e) {
            Log.e("Rephoto preview", "Failed to set rotation for rephoto preview", e);
        }
        return matrix;
    }

    private boolean needsHeightScaling(float unscaledImageWidth, float unscaledImageHeight, Photo oldPhoto) {
        float heightScale = unscaledImageHeight / oldPhoto.getHeight();
        float widthScale = unscaledImageWidth / oldPhoto.getWidth();
        return widthScale < heightScale;
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        JsonArray jsonArray = new JsonArray();
        for (Upload upload : m_uploads) {
            jsonArray.add(upload.getAttributes().toString());
        }
        savedInstanceState.putString(KEY_UPLOAD, jsonArray.toString());
    }

    @Override
    public DialogFragment createDialogFragment(int requestCode) {
        if (requestCode == DIALOG_PROGRESS) {
            return ProgressFragment.create(
                    getString(R.string.upload_dialog_process_title),
                    getString(R.string.upload_dialog_process_message));
        } else if (requestCode == DIALOG_ERROR_NO_CONNECTION) {
            return AlertFragment.create(
                    getString(R.string.upload_dialog_error_connection_title),
                    getString(R.string.upload_dialog_error_connection_message),
                    getString(R.string.upload_dialog_error_connection_ok),
                    getString(R.string.upload_dialog_error_connection_retry));
        } else if (requestCode == DIALOG_ERROR_UNKNOWN) {
            return AlertFragment.create(
                    getString(R.string.upload_dialog_error_unknown_title),
                    getString(R.string.upload_dialog_error_unknown_message),
                    getString(R.string.upload_dialog_error_unknown_ok),
                    getString(R.string.upload_dialog_error_unknown_retry));
        } else if (requestCode == DIALOG_SUCCESS) {
            return AlertFragment.create(
                    getString(R.string.upload_dialog_success_title),
                    getString(R.string.upload_dialog_success_message),
                    getString(R.string.upload_dialog_success_ok));
        } else if (requestCode == DIALOG_NOT_AUTHENTICATED) {
            return AlertFragment.create(
                    getString(R.string.upload_dialog_not_authenticated_title),
                    getString(R.string.upload_dialog_not_authenticated_message),
                    getString(R.string.upload_dialog_not_authenticated_ok));
        } else if (requestCode == DIALOG_NOT_AGREED_TO_TERMS) {
            return AlertFragment.create(
                    "",
                    getString(R.string.upload_dialog_not_agreed_to_terms),
                    getString(R.string.upload_dialog_decline_terms),
                    getString(R.string.upload_dialog_agree_to_terms));
        }

        return super.createDialogFragment(requestCode);
    }

    @Override
    public void onDialogFragmentDismissed(DialogFragment fragment, int requestCode, int resultCode) {
        if (requestCode == DIALOG_PROGRESS) {
            onDialogFragmentCancelled(fragment, requestCode);
        } else if (requestCode == DIALOG_ERROR_NO_CONNECTION ||
                requestCode == DIALOG_ERROR_UNKNOWN) {
            if (resultCode != AlertFragment.RESULT_NEGATIVE) {
                uploadPhoto();
            }
        } else if (requestCode == DIALOG_SUCCESS) {
            success();
        } else if (requestCode == DIALOG_NOT_AUTHENTICATED) {
            ProfileActivity.start(getContext(), RETURN_ACTIVITY_NAME);
        } else if (requestCode == DIALOG_NOT_AGREED_TO_TERMS) {
            if(resultCode == AlertFragment.RESULT_POSITIVE) {
                SharedPreferences.Editor editor = getSharedPreferences().edit();
                editor.putBoolean("agreeToLicenseTerms", true);
                editor.apply();
                uploadPhoto();
            }
        }
    }

    @Override
    public void onDialogFragmentCancelled(DialogFragment fragment, int requestCode) {
        if (requestCode == DIALOG_PROGRESS) {
            getConnection().dequeueAll(getActivity());
        } else if (requestCode == DIALOG_SUCCESS) {
            success();
        } else if (requestCode == DIALOG_ERROR_NO_CONNECTION ||
                requestCode == DIALOG_ERROR_UNKNOWN) {
        }
    }

    private void uploadPhoto() {
        if (getSettings().getAuthorization().isAnonymous()) {
            showDialog(DIALOG_NOT_AUTHENTICATED);
        } else if (!isAgreedToTerms()) {
            showDialog(DIALOG_NOT_AGREED_TO_TERMS);
        } else {
            Context context = getActivity();
            WebAction<Upload> action = Upload.createAction(context, uploadByRephotoBitmap.get(currentRephoto));

            showDialog(DIALOG_PROGRESS);

            getConnection().enqueue(context, action, new WebAction.ResultHandler<Upload>() {
                @Override
                public void onActionResult(Status status, Upload upload) {
                    hideDialog(DIALOG_PROGRESS);

                    if (status.isGood()) {
                        ExifService.deleteField(uploadByRephotoBitmap.get(currentRephoto).getPath(), USER_COMMENT);
                        showDialog(DIALOG_SUCCESS);
                    } else if (status.isNetworkProblem()) {
                        showDialog(DIALOG_ERROR_NO_CONNECTION);
                    } else {
                        showDialog(DIALOG_ERROR_UNKNOWN);
                    }
                }
            });
        }
    }

    private boolean isAgreedToTerms() {
        return getSharedPreferences().getBoolean("agreeToLicenseTerms", true);
    }

    private SharedPreferences getSharedPreferences() {
        return getActivity().getSharedPreferences(DEFAULT_PREFERENCES_KEY, MODE_PRIVATE);
    }

    private void success() {
        Activity activity = getActivity();

        activity.setResult(Activity.RESULT_OK);
        activity.finish();
    }

    private View getMainLayout() {
        return getView().findViewById(R.id.layout_main);
    }

    private WebImageView getOldImageView() {
        return (WebImageView) getView().findViewById(R.id.image_old);
    }

    private Button getSaveButton() {
        return (Button) getView().findViewById(R.id.button_action_save);
    }

    private Button getDeleteButton() {
        return (Button) getView().findViewById(R.id.button_action_delete);
    }

    private Button getConfirmButton() {
        return (Button) getView().findViewById(R.id.button_action_confirm);
    }

    protected ViewPager getViewPager() {
        return (ViewPager) getView().findViewById(R.id.upload_pager);
    }

    private CirclePageIndicator getPageIndicator() {
        return (CirclePageIndicator)getView().findViewById(R.id.pager_indicator);
    }
}
