package com.ezeeworld.b4s.android.sdk.sample;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ezeeworld.b4s.android.sdk.B4SSettings;
import com.ezeeworld.b4s.android.sdk.monitor.MonitoringManager;
import com.ezeeworld.b4s.android.sdk.notifications.NotificationService;

/**
 * An example application for the B4S SDK that sets up some non-default settings and ensures the monitoring service is properly set up.
 */
public class SampleApp extends Application {

	// Replace YOUR_APP_ID value with your own APP_ID
	public final static String YOUR_APP_ID = "MY_APP_ID";
	public final static String YOUR_GOOGLE_SENDER_ID = "MY-GOOGLE-SENDER-ID";
	public final static String NEERBY_PREF_ENABLE_KEY = "ShouldEnableNeerbyATR";
	private static final String TAG = "APP";

	@Override
	public void onCreate() {
		super.onCreate();

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPref != null) {
			try {
				// We try to find a previous user acceptance for SDK.
				Boolean shouldStartNeerbySDK = sharedPref.getBoolean(NEERBY_PREF_ENABLE_KEY, false);

				if (shouldStartNeerbySDK) {

					startSDK(this);
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}

	}

	public static void startSDK(Application application) {
		// Initialize the B4S SDK with our app-specific registration ID
		B4SSettings settings = B4SSettings.init(application, YOUR_APP_ID);

		// Enable remote push notifications
		// settings.setPushMessagingSenderId(YOUR_GOOGLE_SENDER_ID);

		// Send deep links to our broadcast receiver (instead of the default launcher activity delivery)
		NotificationService.registerDeepLinkStyle(NotificationService.DeepLinkStyle.BroadcastReceiver);

		// Start the monitoring service, if needed
		MonitoringManager.ensureMonitoringService(application);
	}

}
