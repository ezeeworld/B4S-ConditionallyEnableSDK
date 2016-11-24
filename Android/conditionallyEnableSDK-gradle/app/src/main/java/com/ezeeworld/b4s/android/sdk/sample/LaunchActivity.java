package com.ezeeworld.b4s.android.sdk.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.ezeeworld.b4s.android.sample.R;
import com.ezeeworld.b4s.android.sdk.B4SSettings;
import com.ezeeworld.b4s.android.sdk.monitor.MonitoringManager;
import com.ezeeworld.b4s.android.sdk.notifications.NotificationService;

import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LaunchActivity extends Activity {

	private static final String TAG = "B4S";
	public static final int PERMISSIONS_REQUEST_LOCATION = 99;
	private TextView statusLabel = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launch);

		if (getIntent().getExtras() != null && getIntent().hasExtra(NotificationService.INTENT_ACTIONID)) {
			String actionId = getIntent().getStringExtra(NotificationService.INTENT_ACTIONID);
			// TODO Do something with the action ID, such as start an activity for a specific product
		}

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

	public boolean checkLocationPermission() {
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

	private void requestSDKActivation() {
		if (ContextCompat.checkSelfPermission(this,
				android.Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {

			// Request a location
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Location deviceLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));

			if (deviceLocation != null) {
				Log.d(TAG, "current location=" + deviceLocation);

				try {
					// Request reverse geocoding to obtain zipcode from current location
					Geocoder geocoder = new Geocoder(this, Locale.getDefault());
					List<Address> addresses = geocoder.getFromLocation(deviceLocation.getLatitude(), deviceLocation.getLongitude(), 1);

					if (addresses.size() > 0) {
						String localZipCode = addresses.get(0).getPostalCode();
						Log.d(TAG, "current location zipCode="+localZipCode);

						// If an address was obtained, we are looking for a match with a predefined list of zipCode
						String[] validPostalCodes = {"92200", "92130", "92100"};
						if (Arrays.asList(validPostalCodes).contains(localZipCode)) {
							Log.d(TAG, "ZipCode match a requested one !");
							new AlertDialog
									.Builder(this)
									.setTitle(R.string.enableNeerby_title)
									.setMessage(R.string.enableNeerby_content)
									.setPositiveButton(R.string.enableNeerby_accept, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											// Service accpeted, start the SDK.
											startSDK();
										}
									})
									.setNegativeButton(R.string.enableNeerby_decline, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											// do nothing
										}
									})
									.setIcon(android.R.drawable.ic_dialog_alert)
									.show();
						}
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

	protected void startSDK() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.edit().putBoolean(SampleApp.NEERBY_PREF_ENABLE_KEY, true).commit();

		// Initialize the B4S SDK with our app-specific registration ID
		B4SSettings settings = B4SSettings.init(getApplication(), SampleApp.MY_NEERBY_APP_ID);

		// Send deep links to our broadcast receiver (instead of the default launcher activity delivery)
		NotificationService.registerDeepLinkStyle(NotificationService.DeepLinkStyle.BroadcastReceiver);

		// Start the monitoring service, if needed
		MonitoringManager.ensureMonitoringService(getApplication());

		statusLabel.setText("SDK is ENABLED");
		statusLabel.setTextColor(Color.GREEN);
	}
}
