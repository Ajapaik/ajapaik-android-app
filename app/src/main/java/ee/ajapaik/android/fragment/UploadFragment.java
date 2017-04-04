package ee.ajapaik.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import ee.ajapaik.android.ProfileActivity;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.data.Upload;
import ee.ajapaik.android.data.util.Status;
import ee.ajapaik.android.fragment.util.AlertFragment;
import ee.ajapaik.android.fragment.util.DialogInterface;
import ee.ajapaik.android.fragment.util.ProgressFragment;
import ee.ajapaik.android.fragment.util.WebFragment;
import ee.ajapaik.android.test.R;
import ee.ajapaik.android.util.WebAction;
import ee.ajapaik.android.widget.WebImageView;

import java.io.IOException;

public class UploadFragment extends WebFragment implements DialogInterface {
    private static final String KEY_UPLOAD = "upload";

    private static final int DIALOG_ERROR_NO_CONNECTION = 1;
    private static final int DIALOG_ERROR_UNKNOWN = 2;
    private static final int DIALOG_PROGRESS = 3;
    private static final int DIALOG_SUCCESS = 4;
    private static final int DIALOG_NOT_AUTHENTICATED = 5;

    private static final int THUMBNAIL_SIZE = 400;

    private Upload m_upload;

    public Upload getUpload() {
        Bundle arguments = getArguments();

        if (arguments != null) {
            return arguments.getParcelable(KEY_UPLOAD);
        }

        return null;
    }

    public void setUpload(Upload upload) {
        Bundle arguments = getArguments();

        if (arguments == null) {
            arguments = new Bundle();
        }

        if (upload != null) {
            arguments.putParcelable(KEY_UPLOAD, upload);
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
            m_upload = savedInstanceState.getParcelable(KEY_UPLOAD);
        }

        if (m_upload == null) {
            m_upload = getUpload();
        }

        getOldImageView().setFlipped(m_upload.isFlipped());
        getOldImageView().setImageURI(m_upload.getPhoto().getThumbnail(THUMBNAIL_SIZE));
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getOldImageView().setOnLoadListener(new WebImageView.OnLoadListener() {
            @Override
            public void onImageLoaded() {
                getProgressBar().setVisibility(View.GONE);
                getMainLayout().setVisibility(View.VISIBLE);
            }

            @Override
            public void onImageUnloaded() {
                getProgressBar().setVisibility(View.VISIBLE);
                getMainLayout().setVisibility(View.GONE);
            }

            @Override
            public void onImageFailed() {
            }
        });

        getNewImageView().setImageBitmap(scaleRephoto());

        getDeclineButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();

                activity.setResult(Activity.RESULT_FIRST_USER);
                activity.finish();
            }
        });

        getConfirmButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPhoto();
            }
        });
    }

    private Bitmap scaleRephoto() {
        Bitmap unscaledCameraImage = loadAndRotateRephotoPreview(m_upload.getPath());
        float unscaledImageWidth = unscaledCameraImage.getWidth();
        float unscaledImageHeight = unscaledCameraImage.getHeight();

        float heightScale = 1.0F;
        float widthScale = 1.0F;
        Photo oldPhoto = m_upload.getPhoto();

        if (needsHeightScaling(unscaledImageWidth, unscaledImageHeight, oldPhoto)) {
            float scale = unscaledImageWidth / oldPhoto.getWidth();
            heightScale = (oldPhoto.getHeight() * scale) / unscaledImageHeight;
        } else {
            float scale = unscaledImageHeight / oldPhoto.getHeight();
            widthScale = (oldPhoto.getWidth() * scale) / unscaledImageWidth;
        }

        float scaledImageWidth = unscaledImageWidth * widthScale * m_upload.getScale();
        float scaledImageHeight = unscaledImageHeight * heightScale * m_upload.getScale();
        float heightDifference = unscaledImageHeight - scaledImageHeight;
        float widthDifference = unscaledImageWidth - scaledImageWidth;
        return Bitmap.createBitmap(
                unscaledCameraImage,
                (int) (Math.max(widthDifference / 2, 0)),
                (int) (Math.max(heightDifference / 2, 0)),
                (int) (Math.min(unscaledImageWidth, unscaledImageWidth - widthDifference)),
                (int) (Math.min(unscaledImageHeight, unscaledImageHeight - heightDifference)));
    }

    /**
     * This is needed as some devices (e.g. Samsung Galaxy S5) store rotation data in meta info and actual image is not rotated
     *
     * @return Matrix with proper rotation
     * @param path to bitmap needing rotation
     */
    private Bitmap loadAndRotateRephotoPreview(String path) {
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
        Bitmap bitmap = BitmapFactory.decodeFile(m_upload.getPath());
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private boolean needsHeightScaling(float unscaledImageWidth, float unscaledImageHeight, Photo oldPhoto) {
        float heightScale = unscaledImageHeight / oldPhoto.getHeight();
        float widthScale = unscaledImageWidth / oldPhoto.getWidth();
        return widthScale < heightScale;
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(KEY_UPLOAD, m_upload);
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
            ProfileActivity.start(getContext(), "upload");
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
            // Do nothing
        }
    }

    private void uploadPhoto() {
        if (getSettings().getAuthorization().isAnonymous()) {
            showDialog(DIALOG_NOT_AUTHENTICATED);
        } else {
            Context context = getActivity();
            WebAction<Upload> action = Upload.createAction(context, m_upload);

            showDialog(DIALOG_PROGRESS);

            getConnection().enqueue(context, action, new WebAction.ResultHandler<Upload>() {
                @Override
                public void onActionResult(Status status, Upload upload) {
                    hideDialog(DIALOG_PROGRESS);

                    if (status.isGood()) {
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

    private WebImageView getNewImageView() {
        return (WebImageView) getView().findViewById(R.id.image_new);
    }

    private ProgressBar getProgressBar() {
        return (ProgressBar) getView().findViewById(R.id.progress_bar);
    }

    private Button getDeclineButton() {
        return (Button) getView().findViewById(R.id.button_action_decline);
    }

    private Button getConfirmButton() {
        return (Button) getView().findViewById(R.id.button_action_confirm);
    }
}
