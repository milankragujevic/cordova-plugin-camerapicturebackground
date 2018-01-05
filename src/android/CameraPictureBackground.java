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
		ctx = callbackContext;
		String filename = null;
		String folderName = null;
		String orientation = null;
		int degrees = 0;
		String cameraType = null;

		if (action.equalsIgnoreCase("takepicture")) {
			final Bundle bundle = new Bundle();

			try {
				JSONObject jobj = args.getJSONObject(0);

				filename = jobj.getString("name");
				// Log.d(TAG, "Filename = " + filename);
				bundle.putString("filename", filename);

				folderName = jobj.getString("dirName");
				// Log.d(TAG, "dirName = " + filename);
				bundle.putString("dirName", folderName);

				orientation = jobj.getString("orientation");
				// Log.d(TAG, "orientation = " + filename);
				if (orientation.equalsIgnoreCase("portrait")) {
					degrees = 90;
				}
				bundle.putInt("orientation", degrees);

				cameraType = jobj.getString("type");
				// Log.d(TAG, "cameraType = " + cameraType);
				final int camid = findCamera(cameraType);
				// Log.d(TAG, "camid = " + camid);
				bundle.putInt("camType", camid);

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
			return hasPermission();
		}

		if (action.equalsIgnoreCase("requestPermission")) {
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

	public void sendJavascript(String path) {
		if (path != null) {
			// Log.d(TAG, "1st");
			if (ctx != null) {
				// Log.d(TAG, "2nd");
				plresult = new PluginResult(PluginResult.Status.OK, path);
				ctx.sendPluginResult(plresult);
			}
		}
	}

	private boolean hasPermission() {
		Log.d(TAG, "Method hasPermission");
		if (Build.VERSION.SDK_INT < ANDROID_VERSION_MARSHMALLOW) {
			Log.d(TAG, "This build version less than Marshmallow");
			return true;
		} else {
			boolean permitted = Settings.canDrawOverlays(cordova.getActivity());
			if (permitted) {
				Log.d(TAG, "It is permitted");
			} else {
				Log.d(TAG, "It not is permitted");
			}

			return permitted;
		}
	}

	private void requestPermission() {
		Log.d(TAG, "Method hasPermission");
		if (Build.VERSION.SDK_INT >= ANDROID_VERSION_MARSHMALLOW) {
			Log.d(TAG, "This build version is greater or equals to Marshmallow");
			Log.d(TAG, "Request is launched");

			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
					Uri.parse("package:" + cordova.getActivity().getPackageName()));
			cordova.startActivityForResult((CordovaPlugin) this, intent, REQUEST_SYSTEM_ALERT_WINDOW);
		}
	}

}
