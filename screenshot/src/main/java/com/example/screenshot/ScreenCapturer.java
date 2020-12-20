package com.example.screenshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.FileOutputStream;

public class ScreenCapturer {
    private static MediaProjection sMediaProjection;
    boolean isScreenCaptureStarted;
    OnScreenCapturedListener mListener;
    private int mDensity;
    private Display mDisplay;
    private int mWidth;
    private int mHeight;
    private ImageReader mImageReader;
    private VirtualDisplay mVirtualDisplay;
    private Handler mHandler;
    private String mImagePath;
    private Context mContext;
    private static final String LOG_TAG = "ScreenCapture";

    public ScreenCapturer(Context context, MediaProjection mediaProjection) {
        sMediaProjection = mediaProjection;
        mContext = context;
        isScreenCaptureStarted = false;

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                mHandler = new Handler();
                Looper.loop();
            }
        }.start();
    }

    public void startProjection(String imageSavePath, OnScreenCapturedListener listener) {
        try {
            if (sMediaProjection == null)
                throw new Exception("System service is required");

            mImagePath = imageSavePath;
            mListener = listener;

            Thread.sleep(500); // 防止截屏截到 显示截屏权限的窗口
            isScreenCaptureStarted = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        WindowManager window = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mDisplay = window.getDefaultDisplay();
        final DisplayMetrics metrics = new DisplayMetrics();
        // use getMetrics is 2030, use getRealMetrics is 2160, the diff is NavigationBar's height
        mDisplay.getRealMetrics(metrics);
        mDensity = metrics.densityDpi;
        Log.d(LOG_TAG, "metrics.widthPixels is " + metrics.widthPixels);
        Log.d(LOG_TAG, "metrics.heightPixels is " + metrics.heightPixels);
        mWidth = metrics.widthPixels;//size.x;
        mHeight = metrics.heightPixels;//size.y;

        //start capture reader
        mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mVirtualDisplay = sMediaProjection.createVirtualDisplay(
                "ScreenShot",
                mWidth,
                mHeight,
                mDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                mImageReader.getSurface(),
                null,
                mHandler);

        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                if (isScreenCaptureStarted) {
                    try {
                        Image image = reader.acquireLatestImage();
                        if (image == null)
                            throw new Exception("Failed to acquire image");

                        FileOutputStream fos = new FileOutputStream(mImagePath);
                        Bitmap bitmap = ImageUtils.image_2_bitmap(image, Bitmap.Config.ARGB_8888);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        bitmap.recycle();
                        fos.close();
                        image.close();
                        Log.d(LOG_TAG, "End now!!!!!!  Screenshot saved in " + mImagePath);
                        stopProjection();

                        if (null != mListener) {
                            mListener.imageCaptured(true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (null != mListener) {
                            mListener.imageCaptured(false);
                        }
                    }
                }
            }
        }, mHandler);
        sMediaProjection.registerCallback(new MediaProjectionStopCallback(), mHandler);
    }

    public void stopProjection() {
        isScreenCaptureStarted = false;
        Log.d(LOG_TAG, "Screen-capture is over");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (sMediaProjection != null) {
                    sMediaProjection.stop();
                }
            }
        });
    }

    public interface OnScreenCapturedListener {
        public void imageCaptured(boolean successful);
    }

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mVirtualDisplay != null) {
                        mVirtualDisplay.release();
                    }
                    if (mImageReader != null) {
                        mImageReader.setOnImageAvailableListener(null, null);
                    }
                    sMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                }
            });
        }
    }
}
