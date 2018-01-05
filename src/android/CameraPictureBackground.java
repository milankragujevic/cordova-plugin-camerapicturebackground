package me.rahul.plugins.camerapicturebackground;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;

import android.os.Build;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.annotation.TargetApi;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CameraPictureBackground extends CordovaPlugin {

	public static final String TAG = "CameraPictureBackground";
	PluginResult plresult = new PluginResult(PluginResult.Status.NO_RESULT);
	private static CordovaWebView cw;
	private static CallbackContext ctx = null;

	public static final int ANDROID_VERSION_MARSHMALLOW = 23;
	public static final int REQUEST_SYSTEM_ALERT_WINDOW = 1;

	/**
	 * Sets the context of the Command. This can then be used to do things like
	 * get file paths associated with the Activity.
	 *
	 * @param cordova The context of the main Activity.
	 * @param webView The CordovaWebView Cordova is running in.
	 */
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		cw = webView;
	}

	public boolean execute(final String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		debugMessage("Method execute");

		ctx = callbackContext;

		if (action.equalsIgnoreCase("takePicture")) {
			debugMessage("Action: takePicture");

			String folderName = null;
			String fileName = null;
			String orientation = null;
			int degrees = 0;
			String cameraDirection = null;
			final Bundle bundle = new Bundle();

			try {
				JSONObject jsonObject = args.getJSONObject(0);

				// Take the values from the arguments if they're not already defined (this is tricky)

				bundle.putString("cacheDir", getCacheDirectoryPath());
				debugMessage(" + cacheDir = " + getCacheDirectoryPath());

				if (jsonObject.has("folderName")) {
					folderName = jsonObject.getString("folderName");
					debugMessage(" + folderName = " + folderName);
				}
				bundle.putString("folderName", folderName);

				if (jsonObject.has("fileName")) {
					fileName = jsonObject.getString("fileName");
					debugMessage(" + fileName = " + fileName);
				}
				bundle.putString("fileName", fileName);

				if (jsonObject.has("orientation")) {
					orientation = jsonObject.getString("orientation");
					debugMessage(" + orientation = " + orientation);
					if (orientation.equalsIgnoreCase("portrait")) {
						degrees = 90;
					}
				}
				bundle.putInt("orientation", degrees);

				if (jsonObject.has("cameraDirection")) {
					cameraDirection = jsonObject.getString("cameraDirection");
				} else {
					cameraDirection = "back";
				}
				debugMessage(" + cameraDirection = " + cameraDirection);
				final int cameraId = findCamera(cameraDirection);
				debugMessage(" + cameraId = " + cameraId);
				bundle.putInt("cameraId", cameraId);

				plresult.setKeepCallback(true);
			} catch (JSONException e) {
				e.printStackTrace();
				callbackContext.error("Invalid Arguments");

				return false;
			}

			cordova.getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (!hasPermission()) {
						requestPermission();
					}

					if (hasPermission()) {
						Intent intent = new Intent();
						intent.setClassName(cordova.getActivity().getApplicationContext(),
								"me.rahul.plugins.camerapicturebackground.CameraSurfacePreview");
						intent.putExtras(bundle);
						cordova.getActivity().startService(intent);
					}
				}

			});

			// callbackContext.success();
			return true;
		}

		if (action.equalsIgnoreCase("hasPermission")) {
			debugMessage("Action: hasPermission");

			return hasPermission();
		}

		if (action.equalsIgnoreCase("requestPermission")) {
			debugMessage("Action: requestPermission");

			requestPermission();
		}

		return true;
	}

	private int findCamera(String type) {
		int frontCameraID = -1;
		int backCameraID = -1;
		CameraInfo camInfo = new CameraInfo();
		int numberofCameras = Camera.getNumberOfCameras();

		for (int i = 0; i < numberofCameras; i++) {
			Camera.getCameraInfo(i, camInfo);

			if (camInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				backCameraID = i;
			} else {
				frontCameraID = i;
			}
		}

		if (type.equalsIgnoreCase("back")) {
			return backCameraID;
		} else {
			return (frontCameraID >= 0) ? frontCameraID : backCameraID;
		}
	}

	public void sendJavaScript(String path) {
		debugMessage("Method sendJavaScript");

		if (path != null) {
			debugMessage("1st");

			if (ctx != null) {
				debugMessage("2nd");
				plresult = new PluginResult(PluginResult.Status.OK, path);
				ctx.sendPluginResult(plresult);
			}
		}
	}

	private String getCacheDirectoryPath() {
		File cache = null;

		// SD Card Mounted
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			cache = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/"
					+ cordova.getActivity().getPackageName() + "/cache/");
		} else {
			// Use internal storage
			cache = cordova.getActivity().getCacheDir();
		}

		// Create the cache directory if it doesn't exist
		cache.mkdirs();
		return cache.getAbsolutePath();
	}

	private static void debugMessage(String message) {
		Log.d(TAG, message);
	}

	private boolean hasPermission() {
		debugMessage("Method hasPermission");

		if (Build.VERSION.SDK_INT < ANDROID_VERSION_MARSHMALLOW) {
			debugMessage("This build version less than Marshmallow");
			return true;
		} else {
			boolean permitted = Settings.canDrawOverlays(cordova.getActivity());
			if (permitted) {
				debugMessage("It is permitted");
			} else {
				debugMessage("It not is permitted");
			}

			return permitted;
		}
	}

	private void requestPermission() {
		debugMessage("Method hasPermission");

		if (Build.VERSION.SDK_INT >= ANDROID_VERSION_MARSHMALLOW) {
			debugMessage("This build version is greater or equals to Marshmallow");
			debugMessage("Request is launched");

			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
					Uri.parse("package:" + cordova.getActivity().getPackageName()));
			cordova.startActivityForResult((CordovaPlugin) this, intent, REQUEST_SYSTEM_ALERT_WINDOW);
		}
	}

}
