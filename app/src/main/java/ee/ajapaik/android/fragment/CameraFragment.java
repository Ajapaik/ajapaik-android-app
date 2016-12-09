package ee.ajapaik.android.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.io.IOException;

import ee.ajapaik.android.CameraActivity;
import ee.ajapaik.android.UploadActivity;
import ee.ajapaik.android.data.Photo;
import ee.ajapaik.android.data.Upload;
import ee.ajapaik.android.fragment.util.WebFragment;
import ee.ajapaik.android.test.R;
import ee.ajapaik.android.util.Settings;
import ee.ajapaik.android.widget.FixedAspectRatioLayout;
import ee.ajapaik.android.widget.WebImageView;
import ee.ajapaik.android.widget.util.OnCompositeTouchListener;
import ee.ajapaik.android.widget.util.OnScaleTouchListener;
import ee.ajapaik.android.widget.util.OnSwipeTouchListener;

public class CameraFragment extends WebFragment implements SurfaceHolder.Callback, Camera.ShutterCallback, Camera.PictureCallback {
    private static final String TAG = "CameraFragment";

    private static final int REQUEST_UPLOAD = 5000;

    private static final String KEY_FLIPPED_MODE = "flipped_mode";
    private static final String KEY_OPACITY = "opacity";
    private static final String KEY_PHOTO = "photo";
    private static final String KEY_SCALE = "scale";

    private static final float DEFAULT_OPACITY = 0.5F;
    private static final float DEFAULT_SCALE = 1.0F;

    private static final int THUMBNAIL_SIZE = 800;
    private static final float OPACITY_LIMIT = 0.1F;
    private static final float OPACITY_FACTOR = 1.5F;

    private static final int CAMERA_MIN_RESOLUTON = 1024;
    private static final int CAMERA_MAX_RESOLUTION = 1920;

    private Camera m_camera;
    private boolean m_flippedMode = false;
    private float m_scale = DEFAULT_SCALE;
    private float m_opacity = DEFAULT_OPACITY;
    private Photo m_photo;

    public Photo getPhoto() {
        Bundle arguments = getArguments();

        if(arguments != null) {
            return arguments.getParcelable(KEY_PHOTO);
        }

        return null;
    }

    public void setPhoto(Photo photo) {
        Bundle arguments = getArguments();

        if(arguments == null) {
            arguments = new Bundle();
        }

        if(photo != null) {
            arguments.putParcelable(KEY_PHOTO, photo);
        } else {
            arguments.remove(KEY_PHOTO);
        }

        setArguments(arguments);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            m_flippedMode = savedInstanceState.getBoolean(KEY_FLIPPED_MODE);
            m_opacity = savedInstanceState.getFloat(KEY_OPACITY, DEFAULT_OPACITY);
            m_photo = savedInstanceState.getParcelable(KEY_PHOTO);
            m_scale = savedInstanceState.getFloat(KEY_SCALE, DEFAULT_SCALE);
        }

        if(m_photo == null) {
            m_photo = getPhoto();
        }

        getImageView().setFlipped(m_flippedMode);
        getImageView().setScale(m_scale);
        getImageView().setImageURI(m_photo.getThumbnail(THUMBNAIL_SIZE));
        getImageView().setOnLoadListener(new WebImageView.OnLoadListener() {
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
        getImageView().setOnTouchListener(new OnCompositeTouchListener(getActivity(), new View.OnTouchListener[]{
                new OnScaleTouchListener(getActivity()) {
                    @Override
                    public void onScale(float scale) {
                        WebImageView imageView = getImageView();

                        imageView.setScale(imageView.getScale() * scale);
                        m_scale = imageView.getScale();
                    }
                },
                new OnSwipeTouchListener(getActivity()) {
                    @Override
                    public void onSwipeLeft() {
                        m_opacity /= OPACITY_FACTOR;

                        if(m_opacity < OPACITY_LIMIT) {
                            m_opacity = OPACITY_LIMIT;
                        }

                        getImageView().setAlpha(m_opacity);
                    }

                    @Override
                    public void onSwipeRight() {
                        m_opacity *= OPACITY_FACTOR;

                        if(m_opacity > 1.0F - OPACITY_LIMIT) {
                            m_opacity = 1.0F - OPACITY_LIMIT;
                        }

                        getImageView().setAlpha(m_opacity);
                    }

                    @Override
                    public void onSingleTap() {
                    }
                }
        }));

        getCameraButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeRephoto();
            }
        });

        getSurfaceView().getHolder().addCallback(this);
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean(KEY_FLIPPED_MODE, m_flippedMode);
        savedInstanceState.putFloat(KEY_OPACITY, m_opacity);
        savedInstanceState.putParcelable(KEY_PHOTO, m_photo);
        savedInstanceState.putFloat(KEY_SCALE, m_scale);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_flip) {
            m_flippedMode = !m_flippedMode;
            getImageView().setFlipped(m_flippedMode);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateCameraMode();
    }

    @Override
    public void onPause() {
        if(m_camera != null) {
            m_camera.stopPreview();
            m_camera.release();
            m_camera = null;
        }

        super.onPause();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(m_camera != null && holder.getSurface() != null) {
            Camera.Parameters parameters = m_camera.getParameters();
            Camera.Size size = getBestPreviewSize(width, height);

            try {
                m_camera.stopPreview();
            }
            catch(Exception e) {
            }

            parameters.setPreviewSize(size.width, size.height);
            m_camera.setParameters(parameters);

            try {
                m_camera.setPreviewDisplay(holder);
                m_camera.startPreview();
            }
            catch(Exception e) {
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(m_camera != null) {
            invalidateCameraView();

            try {
                m_camera.setPreviewDisplay(holder);
                m_camera.startPreview();
            } catch(IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        CameraActivity activity = (CameraActivity)getActivity();
        Settings settings = getSettings();
        Upload upload = settings.getUpload();
        float cameraRatio = (m_photo.isLandscape()) ?
                (float)m_camera.getParameters().getPictureSize().width / (float)m_camera.getParameters().getPictureSize().height :
                (float)m_camera.getParameters().getPictureSize().height / (float)m_camera.getParameters().getPictureSize().width;
        float photoRatio = (float)m_photo.getWidth() / (float)m_photo.getHeight();

        if(upload != null) {
            //upload.unsave(activity);
            settings.setUpload(null);
        }

        upload = new Upload(m_photo, m_flippedMode, (cameraRatio / photoRatio) / m_scale, null, getSettings().getLocation(), activity.getOrientation());

        if(upload.save(data, getActivity().getRequestedOrientation())) {
            settings.setUpload(upload);
            startActivityForResult(UploadActivity.getStartIntent(activity, upload), REQUEST_UPLOAD);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_UPLOAD) {
            getSettings().setUpload(null);

            if(resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_FIRST_USER) {
                Activity activity = getActivity();

                activity.setResult(resultCode);
                activity.finish();
            }
        }
    }

    @Override
    public void onShutter() {
    }

    private Camera.Size getBestPreviewSize(int width, int height) {
        Camera.Size result = null;
        Camera.Parameters parameters = m_camera.getParameters();

        for(Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if(size.width <= width && size.height <= height) {
                if(result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if(newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }

        return result;
    }

    private Camera.Size getBestPictureSize(int width, int height) {
        Camera.Size result = null;
        Camera.Parameters parameters = m_camera.getParameters();
        int photoAspectRatio = Math.round(10.0F * ((float)width / (float)height));
        int resultAspectRatio = 0;

        // Pick the best fitting resolution that is closest to the photo aspect ratio
        for(Camera.Size size : parameters.getSupportedPictureSizes()) {
            int sizeAspectRatio = Math.round(10.0F * ((float)size.width / (float)size.height));

            if(resultAspectRatio == 0 || Math.abs(sizeAspectRatio - photoAspectRatio) <= Math.abs(resultAspectRatio - photoAspectRatio)) {
                if(size.width >= CAMERA_MIN_RESOLUTON && size.width <= CAMERA_MAX_RESOLUTION && (result == null || size.width >= result.width)) {
                    resultAspectRatio = sizeAspectRatio;
                    result = size;
                }
            }
        }

        // Oh, no! No match. Pick the largest resolution
        if(result == null) {
            for(Camera.Size size : parameters.getSupportedPictureSizes()) {
                if(result == null || size.width > result.width) {
                    result = size;
                }
            }
        }

        return result;
    }

    private void takeRephoto() {
        if(m_camera != null) {
            m_camera.takePicture(this, null, this);
        }
    }

    private void invalidateCameraView() {
        if(m_camera != null) {
            View layout = getSurfaceLayout();
            SurfaceView view = getSurfaceView();
            float width = layout.getMeasuredWidth();
            float height = layout.getMeasuredHeight();

            if(width > 0.0F && height > 0.0F) {
                Camera.Size size = m_camera.getParameters().getPictureSize();
                FrameLayout.LayoutParams lp;
                float scale;

                if(!m_photo.isLandscape()) {
                    size = m_camera.new Size(size.height, size.width);
                }

                scale = (width / (float)size.width < height / (float)size.height) ? width / (float)size.width : height / (float)size.height;
                lp = new FrameLayout.LayoutParams(Math.round((float)size.width * scale), Math.round((float)size.height * scale));
                lp.gravity = Gravity.CENTER;
                view.setLayoutParams(lp);
            }
        }
    }

    private void invalidateCameraMode() {
        getSurfaceView().setVisibility(View.VISIBLE);
        getImageView().setAlpha(m_opacity);
        getImageView().setOffset(null);

        if(m_camera == null) {
            try {
                int orientation = getActivity().getRequestedOrientation();
                FixedAspectRatioLayout imageLayout = getImageLayout();
                boolean reverse = (orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE || orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) ? true : false;
                Camera.Parameters parameters;
                Camera.Size pictureSize;
                int cameraId = 0;

                for(int index = 0, count = Camera.getNumberOfCameras(); index < count; index++) {
                    Camera.CameraInfo info = new Camera.CameraInfo();

                    Camera.getCameraInfo(index, info);

                    if(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        cameraId = index;
                        break;
                    }
                }

                m_camera = Camera.open(cameraId);
                
                pictureSize = getBestPictureSize(m_photo.getWidth(), m_photo.getHeight());

                parameters = m_camera.getParameters();
                parameters.setJpegQuality(90);
                parameters.setPictureSize(pictureSize.width, pictureSize.height);

                if(parameters.getPictureFormat() != ImageFormat.JPEG) {
                    for(int i : parameters.getSupportedPictureFormats()) {
                        Log.d(TAG, "supportedImageFormat=" + i);

                        // try to fall back to JPEG
                        if(i == ImageFormat.JPEG) {
                            parameters.setPictureFormat(ImageFormat.JPEG);
                            break;
                        }
                    }
                }

                if(m_photo.isLandscape()) {
                    imageLayout.setAspectRatioWidth((float)pictureSize.width / (float)pictureSize.height);
                } else {
                    imageLayout.setAspectRatioHeight((float)pictureSize.width / (float)pictureSize.height);
                }

                m_camera.setParameters(parameters);
                m_camera.setDisplayOrientation((m_photo.isLandscape()) ? ((reverse) ? 180 : 0) : ((reverse) ? 270 : 90));
                m_camera.setPreviewDisplay(getSurfaceView().getHolder());
                m_camera.startPreview();

                invalidateCameraView();
            }
            catch(Exception e) {
                Log.e(TAG, "open", e);
            }
        }
    }

    private View getMainLayout() {
        return getView().findViewById(R.id.layout_main);
    }

    private FixedAspectRatioLayout getImageLayout() {
        return (FixedAspectRatioLayout)getView().findViewById(R.id.layout_image);
    }

    private View getSurfaceLayout() {
        return getView().findViewById(R.id.layout_surface);
    }

    private WebImageView getImageView() {
        return (WebImageView)getView().findViewById(R.id.image);
    }

    private ProgressBar getProgressBar() {
        return (ProgressBar)getView().findViewById(R.id.progress_bar);
    }

    private SurfaceView getSurfaceView() {
        return (SurfaceView)getView().findViewById(R.id.surface);
    }

    private Button getCameraButton() {
        return (Button)getView().findViewById(R.id.button_action_camera);
    }
}
