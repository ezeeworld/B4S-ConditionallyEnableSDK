package com.ezeeworld.b4s.android.sdk.sample;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.ezeeworld.b4s.android.sdk.B4SSettings;
import com.ezeeworld.b4s.android.sdk.monitor.MonitoringManager;
import com.ezeeworld.b4s.android.sdk.notifications.NotificationService;

/**
 * An example application for the B4S SDK that sets up some non-default settings and ensures the monitoring service is properly set up.
 */
public class SampleApp extends Application {

	public final static String YOUR_APP_ID = "YOUR_APP_ID";
	public final static String NEERBY_PREF_ENABLE_KEY = "ShouldEnableNeerbyATR";

	@Override
	public void onCreate() {
		super.onCreate();

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPref != null) {
			try {
				Boolean shouldEnableNeerbyATR = sharedPref.getBoolean(NEERBY_PREF_ENABLE_KEY, false);

				// Initialize the B4S SDK with our app-specific registration ID
				B4SSettings settings = B4SSettings.init(this, YOUR_APP_ID);

				if (shouldEnableNeerbyATR) {

					// Enable ATR mode
					settings.enableLocationTrackingLocally();
				} else {

					// Disable ATR mode
					settings.disableLocationTrackingLocally();
				}

				// Send deep links to our broadcast receiver (instead of the default launcher activity delivery)
				NotificationService.registerDeepLinkStyle(NotificationService.DeepLinkStyle.BroadcastReceiver);

				// Start the monitoring service, if needed
				MonitoringManager.ensureMonitoringService(this);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}

	}

}
