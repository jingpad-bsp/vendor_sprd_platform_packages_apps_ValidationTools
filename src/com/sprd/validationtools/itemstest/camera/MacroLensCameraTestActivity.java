package com.sprd.validationtools.itemstest.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;

public class MacroLensCameraTestActivity extends BaseActivity implements
        TextureView.SurfaceTextureListener {
    private static final String TAG = "MacroLensCameraTestActivity";
    private Handler mHandler;
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;
    private CaptureRequest.Builder mPreviewBuilder;
    private TextureView mPreviewView;
    private CameraDevice mCameraDevice;

    private String mCameraID = "4";

    private CameraCaptureSession mSession;

    private ImageReader mImageReader;

    private Button mTakePhotoBtn;
    private static final String TAKE_IMAGE = "/storage/emulated/0/mmi/MacroLens_backphoto.jpg";

    protected boolean mNeedThumb = false;
    protected ImageReader mThumbnailReader;
    private boolean hasmacrolensbackcamera = false;
    private TextView mLightMsg = null;
    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the
     * camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "onImageAvailable");
            mHandler.post(new ImageSaver(reader.acquireNextImage(), new
             File(TAKE_IMAGE)));
        }
    };
    private Runnable mTimeOut = new Runnable() {
        public void run() {
            MacroLensCameraTestActivity.this.storeRusult(false);
            MacroLensCameraTestActivity.this.finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.back_camera_result);
        mPreviewView = (TextureView) findViewById(R.id.surfaceView);
        mPreviewView.setSurfaceTextureListener(this);
        mLightMsg = (TextView) findViewById(R.id.light_msg);
        mLightMsg.setText(R.string.secondary_msg_text);
        //mLightMsg.setVisibility(View.GONE);
        Log.d(TAG, "onCreate mCameraID=" + mCameraID);
        mTakePhotoBtn = (Button) findViewById(R.id.start_take_picture);
        mTakePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureStillPicture();
            }
        });
        mTakePhotoBtn.setVisibility(View.GONE);
        startBackgroundThread();
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            for (String strcameraId : cameraIdList) {
                Log.i(TAG, " onCreate strcameraId = " + strcameraId);
                int cameraId = Integer.parseInt(strcameraId);
                Log.d(TAG, " onCreate cameraId = " + cameraId + " ; mCameraID = " + mCameraID);
                if (cameraId == Integer.parseInt(mCameraID))
                    hasmacrolensbackcamera = true;
            }
            Log.d(TAG, " onCreate hasmacrolensbackcamera = " + hasmacrolensbackcamera);
            if(!hasmacrolensbackcamera)
            {
                mPassButton.setVisibility(View.GONE);
                mLightMsg.setText(R.string.macrolens_camera_open_failue);
            }
            else
            {
                mLightMsg.setVisibility(View.GONE);
            }
        } catch (CameraAccessException ex) {
            Log.e(TAG, "Unable to read camera list.", ex);
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mHandler = new Handler(mBackgroundThread.getLooper());
        mHandler.postDelayed(mTimeOut, 120000);
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        try {
            mBackgroundThread.quitSafely();
            mBackgroundThread.join();
            mBackgroundThread = null;
            mHandler = null;
        } catch (NullPointerException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In
        // that case, we can open a camera and start preview from here (otherwise, we wait until the
        // surface is ready in the SurfaceTextureListener).
        if (mPreviewView.isAvailable()) {
            Log.d(TAG, "onResume isAvailable reopen camera");
            Log.i(TAG, " onResume hasmacrolensbackcamera = " + hasmacrolensbackcamera);
            if (!hasmacrolensbackcamera) {

            } else {
                openCamera(mCameraID);
            }
        } else {
            mPreviewView.setSurfaceTextureListener(this);
        }
        super.onResume();
    }

    private void captureStillPicture() {
        try {
            final CaptureRequest.Builder captureBuilder = mCameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureBuilder.addTarget(mImageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                        CaptureRequest request, TotalCaptureResult result) {
                    session.close();
                    session = null;
                }
            };
            Log.d(TAG, "capture  in camera");
            mSession.capture(captureBuilder.build(), CaptureCallback, mHandler);
        } catch (NullPointerException | CameraAccessException e) {
            Log.d(TAG, "capture a picture1 fail" + e.toString());
        }
    }

    private void openCamera(String cameraId) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                Toast.makeText(MacroLensCameraTestActivity.this, "no CloseLock",
                        Toast.LENGTH_LONG).show();
                return;
            }
            mImageReader = ImageReader.newInstance(2592, 1944, ImageFormat.JPEG, 1);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,
                    mHandler);
            Log.d(TAG, "openCamera openCamera cameraId:"
                    + cameraId);
            cameraManager.openCamera(cameraId, mCameraDeviceStateCallback,
                    mHandler);
            Log.d(TAG, "openCamera openCamera end");
        } catch (CameraAccessException e) {
            Log.d(TAG, "CameraAccessException" + e.toString());
        } catch (InterruptedException e) {
            Log.d(TAG, "InterruptedException" + e.toString());
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
            int height) {
        Log.d(TAG, "onSurfaceTextureAvailable");
        Log.i(TAG, " hasmacrolensbackcamera = " + hasmacrolensbackcamera);
        if (!hasmacrolensbackcamera) {

        } else {
            openCamera(mCameraID);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
            int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            mCameraOpenCloseLock.release();
            Log.i(TAG, "CameraDevice.StateCallback onOpened in");
            mCameraDevice = camera;
            startPreview(camera);
            Log.i(TAG, "CameraDevice.StateCallback onOpened out");
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            mCameraOpenCloseLock.release();
            camera.close();
            Log.i(TAG, "CameraDevice.StateCallback1 stay onDisconnected");
            if (mThumbnailReader != null) {
                Log.i(TAG, "mThumbnailReader.close");
                mThumbnailReader.close();
                mThumbnailReader = null;
            }
            mNeedThumb = false;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            camera.close();
            Log.i(TAG, "CameraDevice.StateCallback1 stay onError");
        }
    };

    private void startPreview(CameraDevice camera) {
        Log.i(TAG, "start preview ");
        SurfaceTexture texture = mPreviewView.getSurfaceTexture();
        texture.setDefaultBufferSize(960, 720);
        Surface surface = new Surface(texture);
        try {

            mPreviewBuilder = camera
                    .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            Log.i(TAG, e.toString());
        }

        mPreviewBuilder.addTarget(surface);

        if (mThumbnailReader != null) {
            mThumbnailReader.close();
        }
        try {
            if (mNeedThumb) {
                mThumbnailReader = ImageReader.newInstance(320, 240,
                        ImageFormat.YUV_420_888, 1);
                camera.createCaptureSession(Arrays.asList(surface,
                        mImageReader.getSurface(),
                        mThumbnailReader.getSurface()),
                        mCameraCaptureSessionStateCallback, mHandler);
            } else {
                camera.createCaptureSession(
                        Arrays.asList(surface, mImageReader.getSurface()),
                        mCameraCaptureSessionStateCallback, mHandler);
            }
        } catch (CameraAccessException ex) {
            Log.e(TAG, "Failed to create camera capture session", ex);
        }
    }

    private CameraCaptureSession.StateCallback mCameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                mSession = session;
                mSession.setRepeatingRequest(mPreviewBuilder.build(),
                        mSessionCaptureCallback, mHandler);

            } catch (CameraAccessException e) {
                Log.i(TAG, e.toString());
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }

        @Override
        public void onActive(CameraCaptureSession session) {

        }
    };
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(CameraCaptureSession session,
                CaptureRequest request, TotalCaptureResult result) {
            mSession = session;
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session,
                CaptureRequest request, CaptureResult partialResult) {
            mSession = session;
        }
    };

    @Override
    protected void onPause() {
        closeCamera();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mTimeOut);
        }
        stopBackgroundThread();
        super.onDestroy();
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mSession) {
                mSession.close();
                mSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            Log.d(TAG, "InterruptedException" + e.toString());
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private static class ImageSaver implements Runnable {

        private final Image mImage;

        private final File mFile;

        public ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                if (!mFile.getParentFile().exists()) {
                    mFile.getParentFile().mkdirs();
                }
                if (!mFile.exists()) {
                    mFile.createNewFile();
                }
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
