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

	public final static String MY_NEERBY_APP_ID = "MY-APP-ID";
	public final static String NEERBY_PREF_ENABLE_KEY = "ShouldStartNeerbySDK";

	@Override
	public void onCreate() {
		super.onCreate();


		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPref != null) {
			try {
				Boolean shouldStartNeerbySDK = sharedPref.getBoolean(NEERBY_PREF_ENABLE_KEY, false);

				if (shouldStartNeerbySDK) {

					// Initialize the B4S SDK with our app-specific registration ID
					B4SSettings settings = B4SSettings.init(this, MY_NEERBY_APP_ID);

					// Send deep links to our broadcast receiver (instead of the default launcher activity delivery)
					NotificationService.registerDeepLinkStyle(NotificationService.DeepLinkStyle.BroadcastReceiver);

					// Start the monitoring service, if needed
					MonitoringManager.ensureMonitoringService(this);
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}

	}

}
