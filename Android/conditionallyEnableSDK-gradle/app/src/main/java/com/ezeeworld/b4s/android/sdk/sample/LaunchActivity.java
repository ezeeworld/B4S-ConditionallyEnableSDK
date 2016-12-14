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

import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LaunchActivity extends Activity {

	// Optins
	private static final String PRIVACY_EXPORT_ENABLED = "privacy.export.enabled";

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

		if (updateSDKStatus()) { // If the SDK is already enabled bail out
			return;
		}

		// Request location permission before requesting location
		if (checkLocationPermission()) {

			// Permission was already given, request geolocated activation
			requestSDKActivation();
		}
	}

	/**
	 * Optin settins filled by the user.
	 * @param optin1
	 * @param optin2
	 * @param optin3
     */
	private void setOptins(boolean optin1,boolean optin2, boolean optin3) {
		// Properties name used here MUST be the same on iOS and Android.
		B4SUserProperty.get().store(PRIVACY_EXPORT_ENABLED, optin1 ? 1 : 0);
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

	/**
	 * Check ATR mode status.
	 * @return
     */
	private Boolean updateSDKStatus() {
		Boolean atrStatus = B4SSettings.get().locationTrackingEnabled(); // Check if the SDK is initialized

		if (atrStatus) {
			Log.d(TAG, "ATR is already enabled");

			statusLabel.setText("ATR is ENABLED");
			statusLabel.setTextColor(Color.GREEN);
		} else {
			Log.d(TAG, "ATR is disabled");

			statusLabel.setText("ATR is DISABLED");
			statusLabel.setTextColor(Color.RED);
		}

		return atrStatus;
	}

	private void requestSDKActivation() {

		// setOptins method will be called after user had filled the optin panel
		// Boolean value should match user inputs.
		setOptins(true, true, true);

		if (ContextCompat.checkSelfPermission(this,
				android.Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {

			// Request a location
			final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Location deviceLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));

			if (deviceLocation != null) {
				testLocation(deviceLocation);
			} else {
				Log.d(TAG, "GoogleApi no location");

				// Define a listener that responds to location updates
				LocationListener locationListener = new LocationListener() {
					public void onLocationChanged(Location location) {
						Log.d(TAG, "Finnaly got a location");

						testLocation(location);
						try {
							locationManager.removeUpdates(this);
						} catch (SecurityException se) { }
					}

					public void onStatusChanged(String provider, int status, Bundle extras) {}

					public void onProviderEnabled(String provider) {}

					public void onProviderDisabled(String provider) {}
				};

				// Register the listener with the Location Manager to receive location updates
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
			}
		} else {
			Log.d(TAG, "Geolocation was declined");
		}
	}

	/**
	 * We request for latest location. We use the matching zipcode to go further or not with the user.
	 * @param deviceLocation
     */
	private void testLocation(Location deviceLocation) {
		Log.d(TAG, "current location=" + deviceLocation);

		try {
			// Request reverse geocoding to obtain zipcode from current location
			Geocoder geocoder = new Geocoder(this, Locale.getDefault());
			List<Address> addresses = geocoder.getFromLocation(deviceLocation.getLatitude(), deviceLocation.getLongitude(), 1);

			if (addresses.size() > 0) {
				Log.d(TAG, "current location address="+addresses.get(0));
				String localZipCode = addresses.get(0).getPostalCode();
				Log.d(TAG, "current location zipCode="+localZipCode);

				// If an address was obtained, we are looking for a match with a predefined list of zipCode
				// We are looking for Neuilly sur Seine, Issy les Moulineaux, Boulogne Billancourt
				String[] validPostalCodes = {"92200", "92130", "92100"};
				if (Arrays.asList(validPostalCodes).contains(localZipCode)) {
					Log.d(TAG, "ZipCode match a requested one !");
					new AlertDialog
							.Builder(this)
							.setTitle(R.string.enableNeerby_title)
							.setMessage(R.string.enableNeerby_content)
							.setPositiveButton(R.string.enableNeerby_accept, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									// Service accepted, enable the ATR.
									B4SSettings.get().enableLocationTrackingLocally();
									statusLabel.setText("ATR is ENABLED");
									statusLabel.setTextColor(Color.GREEN);

									// Record user choice. It will be used at next cold start to enable ATR mode
									SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
									editor.putBoolean(SampleApp.NEERBY_PREF_ENABLE_KEY, true);
									editor.commit();
									Log.d(TAG, "Record user selection in SharedPreference");
								}
							})
							.setNegativeButton(R.string.enableNeerby_decline, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									// Service accepted, disable the ATR.
									B4SSettings.get().disableLocationTrackingLocally();
									statusLabel.setText("ATR is DISABLED");
									statusLabel.setTextColor(Color.RED);

									// Record user choice. It will be used at next cold start to disable ATR mode
									SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
									editor.putBoolean(SampleApp.NEERBY_PREF_ENABLE_KEY, false);
									editor.commit();
									Log.d(TAG, "Record user selection in SharedPreference");
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
	}
}
