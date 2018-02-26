package me.rahul.plugins.camerapicturebackground;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.CameraInfo;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.res.Configuration;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;
import org.apache.cordova.camera.FileHelper;

public class CameraSurfacePreview extends Service {

    private static final String TAG = "CameraPictureBackground";
    @SuppressWarnings("deprecation")
    private static Camera camera = null;

    private static String cacheDir;
    private static String folderName;
    private static String fileName;
    private static int orientation;
    private static int cameraId;
    private static int screenWidth;
    private static int screenHeight;
    private static int configOrientation;
    private static int quality;
    private static int targetWidth;
    private static int targetHeight;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        debugMessage("Method onStartCommand");

        cacheDir = intent.getStringExtra("cacheDir");
        debugMessage(" + cacheDir = " + cacheDir);
        folderName = intent.getStringExtra("folderName");
        debugMessage(" + folderName = " + folderName);
        fileName = intent.getStringExtra("fileName");
        debugMessage(" + fileName = " + fileName);
        orientation = intent.getIntExtra("orientation", 0);
        debugMessage(" + orientation = " + orientation);
        cameraId = intent.getIntExtra("cameraId", 0);
        debugMessage(" + cameraId = " + cameraId);
        screenWidth = intent.getIntExtra("screenWidth", 0);
        debugMessage(" + screenWidth = " + screenWidth);
        screenHeight = intent.getIntExtra("screenHeight", 0);
        debugMessage(" + screenHeight = " + screenHeight);
        configOrientation = intent.getIntExtra("configOrientation", 0);
        debugMessage(" + configOrientation = " + configOrientation);
        quality = intent.getIntExtra("quality", -1);
        debugMessage(" + quality = " + quality);
        targetWidth = intent.getIntExtra("targetWidth", -1);
        debugMessage(" + targetWidth = " + targetWidth);
        targetHeight = intent.getIntExtra("targetHeight", -1);
        debugMessage(" + targetHeight = " + targetHeight);

        takePhoto(this);

        return START_NOT_STICKY;
    }

    @SuppressWarnings("deprecation")
    private static void takePhoto(final Context context) {
        debugMessage("Method takePhoto");

        try {
            final SurfaceView preview = new SurfaceView(context);
            SurfaceHolder holder = preview.getHolder();
            // deprecated setting, but required on Android versions prior to 3.0
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            holder.addCallback(surfaceCallback);

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1, // Must be at least 1x1
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, 0,
                    // Don't know if this is a safe default
                    PixelFormat.UNKNOWN);

            // Don't set the preview visibility to GONE or INVISIBLE
            wm.addView(preview, params);
        } catch (Exception e) {
            debugMessage("takePhoto - ERROR");

            CameraPictureBackground cpb = new CameraPictureBackground();
            cpb.sendJavaScript("");
        }
    }

    static SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        public void surfaceCreated(SurfaceHolder holder) {
            debugMessage("Method surfaceCreated");

            final CameraPictureBackground cpb = new CameraPictureBackground();

            try {
                camera = Camera.open(cameraId);
                try {
                    camera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                camera.setDisplayOrientation(orientation);
                @SuppressWarnings("deprecation")
                Camera.Parameters params = camera.getParameters();
                @SuppressWarnings("deprecation")
                List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
                debugMessage("preview sizes = " + previewSizes);
                @SuppressWarnings("deprecation")
                Camera.Size previewSize = previewSizes.get(0);
                params.setPreviewSize(previewSize.width, previewSize.height);
                params.setJpegQuality((quality > 0) ? quality : 100);
                if (params.getSceneMode() != null) {
                    params.setSceneMode(Parameters.SCENE_MODE_STEADYPHOTO);
                }
                List<String> focusModes = params.getSupportedFocusModes();
                if (focusModes.contains(Parameters.FOCUS_MODE_FIXED)) {
                    params.setFocusMode(Parameters.FOCUS_MODE_FIXED);
                }
                params.setRotation(orientation);
                camera.setParameters(params);
                camera.startPreview();
                camera.takePicture(null, null, new PictureCallback() {

                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        if (data != null) {
                            Bitmap bitmap = null;

                            try {
                                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                                @SuppressWarnings("deprecation")
                                CameraInfo info = new CameraInfo();
                                Camera.getCameraInfo(cameraId, info);

                                if (configOrientation == Configuration.ORIENTATION_PORTRAIT) {
                                    // Notice that width and height are reversed
                                    // Bitmap scaled = Bitmap.createScaledBitmap(bitmap,
                                    // screenHeight, screenWidth, true);
                                    // int w = scaled.getWidth();
                                    // int h = scaled.getHeight();
                                    // Setting post rotate to 90
                                    Matrix matrix = new Matrix();
                                    matrix.postRotate(90);
                                    if (cameraId == CameraInfo.CAMERA_FACING_FRONT) {
                                        matrix.postRotate(180);
                                    }

                                    // Rotating Bitmap
                                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                                            matrix, true);
                                } else
                                // LANDSCAPE MODE
                                {
                                    Bitmap scaled = Bitmap.createScaledBitmap(bitmap, screenWidth, screenHeight, true);
                                    bitmap = scaled;
                                }
                            } catch (Exception e) {
                                // Do nothing.
                            } catch (Error e) {
                                // Do nothing.
                            }

                            if (bitmap != null) {
								/*
                                ByteArrayOutputStream
                                A specialized OutputStream for class for writing content to an
                                (internal) byte array. As bytes are written to this stream, the byte
                                array may be expanded to hold more bytes. When the writing is
                                considered to be finished, a copy of the byte array can be
                                requested from the class.
								*/

								/*
                                public synchronized byte[] toByteArray ()
                                Returns the contents of this ByteArrayOutputStream as a byte array.
                                Any changes made to the receiver after returning will not be
                                reflected in the byte array returned to the caller.

                                Returns
                                this stream's current contents as a byte array.
								*/

                                // Initializing a new ByteArrayOutputStream
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();

								/*
                                public boolean compress (Bitmap.CompressFormat format, int quality, OutputStream stream)
                                Write a compressed version of the bitmap to the specified outputstream.
                                If this returns true, the bitmap can be reconstructed by passing a
                                corresponding inputstream to BitmapFactory.decodeStream().

                                Note: not all Formats support all bitmap configs directly, so it is
                                possible that the returned bitmap from BitmapFactory could be in
                                a different bitdepth, and/or may have lost per-pixel alpha
                                (e.g. JPEG only supports opaque pixels).

                                Parameters
                                format : The format of the compressed image
                                quality : Hint to the compressor, 0-100. 0 meaning compress for small
                                    size, 100 meaning compress for max quality. Some formats,
                                    like PNG which is lossless, will ignore the quality setting
                                stream : The outputstream to write the compressed data.

                                Returns
								true if successfully compressed to the specified stream.
								*/

								// Resize to targetWidth and to targetHeight
                                if ((targetWidth > 0) && (targetHeight > 0)) {
//                                    BitmapFactory.Options options = new BitmapFactory.Options();
//                                    options.inJustDecodeBounds = true;
//                                    BitmapFactory
                                    //BitmapFactory.decodeStream(FileHelper.getInputStreamFromUriString(uriString, cordova), null, options);

                                    // calc aspect ratio
//                                    int[] retval = calculateAspectRatio(options.outWidth, options.outHeight);

//                                    options.inJustDecodeBounds = false;
//                                    options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, width, height);
//                                    Bitmap unscaledBitmap = BitmapFactory.decodeStream(FileHelper.getInputStreamFromUriString(uriString, cordova), null, options);
                                    //return Bitmap.createScaledBitmap(unscaledBitmap, retval[0], retval[1], true);

                                    int[] retval = calculateAspectRatio(bitmap.getWidth(), bitmap.getHeight());
                                    bitmap = Bitmap.createScaledBitmap(bitmap, retval[0], retval[1], true);
                                }

                                // Compress the bitmap to jpeg format and 100% image quality
                                bitmap.compress(Bitmap.CompressFormat.JPEG, (quality > 0) ? quality : 100, stream);

                                // Create a byte array from ByteArrayOutputStream
                                data = stream.toByteArray();

                                // Create output
                                FileOutputStream outStream = null;

                                File folder = null;
                                if (folderName == null) {
                                    folder = new File(cacheDir);
                                } else {
                                    if (folderName.contains("/")) {
                                        folder = new File(folderName.replace("file://", ""));
                                    } else {
                                        folder = new File(Environment.getExternalStorageDirectory() + "/" + folderName);
                                    }
                                }

                                boolean success = true;
                                if (!folder.exists()) {
                                    success = folder.mkdir();
                                }

                                if (success) {
                                    if (fileName == null) {
                                        fileName = System.currentTimeMillis() + ".jpg";
                                    } else {
                                        fileName = fileName + "-" + System.currentTimeMillis() + ".jpg";
                                    }

                                    File file = new File(folder, fileName);
                                    if (file.exists()) {
                                        file.delete();
                                    }

                                    try {
                                        FileOutputStream out = new FileOutputStream(file);
                                        out.write(data);
                                        debugMessage("Picture saved successfully");
                                        out.close();
                                        cpb.sendJavaScript(file.getAbsolutePath());
                                    } catch (FileNotFoundException e) {
                                        debugMessage("takePhoto.SurfaceHolder.Callback.surfaceCreated.camera.takePicture - ERROR: FileNotFoundException");
                                        debugMessage(e.getMessage());
                                        cpb.sendJavaScript("");
                                    } catch (IOException e) {
                                        debugMessage("takePhoto.SurfaceHolder.Callback.surfaceCreated.camera.takePicture - ERROR: IOException");
                                        debugMessage(e.getMessage());
                                        cpb.sendJavaScript("");
                                    }
                                }
                            } else {
                                debugMessage("takePhoto.SurfaceHolder.Callback.surfaceCreated.camera.takePicture - ERROR: Data fechted couldnt be processed");
                                cpb.sendJavaScript("");
                            }
                        } else {
                            debugMessage("takePhoto.SurfaceHolder.Callback.surfaceCreated.camera.takePicture - ERROR: Data not fetched");
                            cpb.sendJavaScript("");
                        }

                        destroyCamera();
                    }

                    /**
                     * Figure out what ratio we can load our image into memory at while still being bigger than
                     * our desired width and height
                     *
                     * @param srcWidth
                     * @param srcHeight
                     * @param dstWidth
                     * @param dstHeight
                     * @return
                     */
                    private int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
                        final float srcAspect = (float)srcWidth / (float)srcHeight;
                        final float dstAspect = (float)dstWidth / (float)dstHeight;

                        if (srcAspect > dstAspect) {
                            return srcWidth / dstWidth;
                        } else {
                            return srcHeight / dstHeight;
                        }
                    }

                    /**
                     * Maintain the aspect ratio so the resulting image does not look smooshed
                     *
                     * @param origWidth
                     * @param origHeight
                     * @return
                     */
                    private int[] calculateAspectRatio(int origWidth, int origHeight) {
                        int newWidth = targetWidth;
                        int newHeight = targetHeight;

                        // If no new width or height were specified return the original bitmap
                        if (newWidth <= 0 && newHeight <= 0) {
                            newWidth = origWidth;
                            newHeight = origHeight;
                        }
                        // Only the width was specified
                        else if (newWidth > 0 && newHeight <= 0) {
                            newHeight = (newWidth * origHeight) / origWidth;
                        }
                        // only the height was specified
                        else if (newWidth <= 0 && newHeight > 0) {
                            newWidth = (newHeight * origWidth) / origHeight;
                        }
                        // If the user specified both a positive width and height
                        // (potentially different aspect ratio) then the width or height is
                        // scaled so that the image fits while maintaining aspect ratio.
                        // Alternatively, the specified width and height could have been
                        // kept and Bitmap.SCALE_TO_FIT specified when scaling, but this
                        // would result in whitespace in the new image.
                        else {
                            double newRatio = newWidth / (double) newHeight;
                            double origRatio = origWidth / (double) origHeight;

                            if (origRatio > newRatio) {
                                newHeight = (newWidth * origHeight) / origWidth;
                            } else if (origRatio < newRatio) {
                                newWidth = (newHeight * origWidth) / origHeight;
                            }
                        }

                        int[] retval = new int[2];
                        retval[0] = newWidth;
                        retval[1] = newHeight;
                        return retval;
                    }

                });
            } catch (Exception e) {
                destroyCamera();
//                throw new RuntimeException(e);

                debugMessage("takePhoto.SurfaceHolder.Callback.surfaceCreated - ERROR: ");
                debugMessage(e.getMessage());
                cpb.sendJavaScript("");
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            destroyCamera();
        }

        private void destroyCamera() {
            if (camera != null) {
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        }

    };

    private static void debugMessage(String message) {
        Log.d(TAG, message);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

}
