package com.ezeeworld.b4s.android.sdk.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.ezeeworld.b4s.android.sample.R;
import com.ezeeworld.b4s.android.sdk.B4SSettings;
import com.ezeeworld.b4s.android.sdk.B4SUserProperty;
import com.ezeeworld.b4s.android.sdk.monitor.MonitoringManager;
import com.ezeeworld.b4s.android.sdk.notifications.NotificationService;

import android.net.Uri;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LaunchActivity extends Activity {

	// Optins
	private static final String PRIVACY_EXPORT_ENABLED = "privacy.export.enabled";

	private static final String ACTIVATION_URL = "https://apps.ezeeworld.com/pagesjaunes/json-sample-app/validate-device.php";

	private static final String TAG = "B4S";
	public static final int PERMISSIONS_REQUEST_LOCATION = 99;
	private TextView statusLabel = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launch);

		statusLabel = ((TextView) findViewById(R.id.sdkStatus_text));
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onStart() {
		super.onStart();

		Log.d(TAG, "onStart");

		if (updateSDKStatus()) { // If the SDK is already started bail out
			return;
		}

		// Request location permission before requesting location
		if (checkLocationPermission()) {

			// Permission was already given, request geolocated activation
			requestSDKActivation();
		}
	}

	private boolean checkLocationPermission() {
		Log.d(TAG, "checkLocationPermission");
		if (ContextCompat.checkSelfPermission(this,
				android.Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {

			ActivityCompat.requestPermissions(this,
					new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
					PERMISSIONS_REQUEST_LOCATION);

			return false;
		} else {
			return true;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

		switch (requestCode) {
			case PERMISSIONS_REQUEST_LOCATION: {

				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					// Permission was granted, request geolocated activation
					requestSDKActivation();

				} else {

					Log.d(TAG, "onRequestPermissionsResult Not granted");

				}
				return;
			}

		}
	}

	private Boolean updateSDKStatus() {
		Boolean sdkStatus = B4SSettings.isInitialized(); // Check if the SDK is initialized

		if (sdkStatus) {
			Log.d(TAG, "SDK is already enabled");

			statusLabel.setText("SDK is ENABLED");
			statusLabel.setTextColor(Color.GREEN);
		} else {
			Log.d(TAG, "SDK is disabled");

			statusLabel.setText("SDK is DISABLED");
			statusLabel.setTextColor(Color.RED);
		}

		return sdkStatus;
	}

	/**
	 * Returns the normalized device model as reported by Android.
	 * @return The unique device model string in the form 'MANUFACTURER_NAME MODEL_NAME', such as 'LGE Nexus 5'
	 */
	public static String getDeviceModel() {
		String model = Build.MODEL.startsWith(Build.MANUFACTURER) ? Build.MODEL : Build.MANUFACTURER + " " + Build.MODEL;

		return model.replaceAll("[^\\x00-\\x7F]", "");
	}

	private Boolean checkRemoteActivationConditions(String zipCode) {
		try {
			String deviceModel = getDeviceModel();

			Log.d(TAG, "[checkRemoteActivationConditions] Device="+deviceModel+" ZipCode="+zipCode);
			Log.d(TAG, "[checkRemoteActivationConditions] "+ACTIVATION_URL+"?zip_code="+zipCode+"&device_model="+deviceModel);

			URL urlObj = new URL(ACTIVATION_URL+"?zip_code="+zipCode+"&device_model="+deviceModel);
			Log.d(TAG, "[checkRemoteActivationConditions] "+urlObj);
			HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection();
			int status = urlConnection.getResponseCode();
			Log.d(TAG, "[checkRemoteActivationConditions] status="+status);
			if (status != HttpURLConnection.HTTP_OK) {
				return false;
			}
		} catch(Throwable e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private void requestSDKActivation() {

		if (ContextCompat.checkSelfPermission(this,
				android.Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {

			// Request a location
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Location deviceLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));

			Log.d(TAG, "requestSDKActivation deviceLocation="+deviceLocation);
			if (deviceLocation != null) {
				Log.d(TAG, "current location=" + deviceLocation);

				try {
					// Request reverse geocoding to obtain zipcode from current location
					Geocoder geocoder = new Geocoder(this, Locale.getDefault());
					List<Address> addresses = geocoder.getFromLocation(deviceLocation.getLatitude(), deviceLocation.getLongitude(), 1);

					if (addresses.size() > 0) {
						String localZipCode = addresses.get(0).getPostalCode();
                        Log.d(TAG, "Add="+addresses.get(0));
                        String city = addresses.get(0).getLocality();
						Log.d(TAG, "current location zipCode="+localZipCode);

						// If an address was obtained, we are looking for a match with a predefined list of zipCode
                        // Stored on Neerby servers
						new CheckRemoteActivationConditionsTask().execute(localZipCode, city);
					}
				} catch (Exception e) {
					Log.e(TAG, "Geocoding failled:" + e.toString());
					e.printStackTrace();
				}
			} else {
				Log.d(TAG, "GoogleApi no location");
			}
		} else {
			Log.d(TAG, "Geolocation was declined");
		}
	}

	protected void startSDK(boolean optin1,boolean optin2, boolean optin3) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.edit().putBoolean(SampleApp.NEERBY_PREF_ENABLE_KEY, true).commit();

		// Initialize the B4S SDK with our app-specific registration ID
		B4SSettings settings = B4SSettings.init(getApplication(), SampleApp.YOUR_APP_ID);

		// Send deep links to our broadcast receiver (instead of the default launcher activity delivery)
		NotificationService.registerDeepLinkStyle(NotificationService.DeepLinkStyle.BroadcastReceiver);

		// Start the monitoring service, if needed
		MonitoringManager.ensureMonitoringService(getApplication());

		statusLabel.setText("SDK is ENABLED");
		statusLabel.setTextColor(Color.GREEN);

		// Properties name used here MUST be the same on iOS and Android.
		B4SUserProperty.get().store(PRIVACY_EXPORT_ENABLED, optin1 ? 1 : 0);
	}

    /**
     * Thread requesting activation status for a given zipCode and the device model
     */
    private class CheckRemoteActivationConditionsTask extends AsyncTask<String, Void, String> {

        public CheckRemoteActivationConditionsTask() {
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String zipCode = strings[0];
                String city = strings[1];
                String deviceModel = getDeviceModel();

                Log.d(TAG, "[CheckRemoteActivationConditionsTask] Device="+deviceModel+" ZipCode="+zipCode);

                String url = Uri.parse(ACTIVATION_URL)
                        .buildUpon()
                        .appendQueryParameter("zip_code", zipCode)
                        .appendQueryParameter("device_model", deviceModel)
                        .build().toString();
                URL urlObj = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) urlObj.openConnection();
                int httpCode = urlConnection.getResponseCode();
                Log.d(TAG, "[CheckRemoteActivationConditionsTask] httpCode="+httpCode);
                if (httpCode == HttpURLConnection.HTTP_OK) {
                    return city;
                }
            } catch(Throwable e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String activationCity) {
            if (activationCity != null) {
                Log.d(TAG, "Device model admitted and ZipCode match a requested one !");
                new AlertDialog
                        .Builder(LaunchActivity.this)
                        .setTitle(getResources().getString(R.string.enableNeerby_title, activationCity))
                        .setMessage(R.string.enableNeerby_content)
                        .setPositiveButton(R.string.enableNeerby_accept, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Service accepted, start the SDK.
                                startSDK(true, true, false);
                            }
                        })
                        .setNegativeButton(R.string.enableNeerby_decline, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else {
                // Silently do nothing
            }
        }
    }
}
