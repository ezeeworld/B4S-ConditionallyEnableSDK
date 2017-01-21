package com.ezeeworld.b4s.android.sdk.sample;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ezeeworld.b4s.android.sample.R;
import com.ezeeworld.b4s.android.sdk.B4SSettings;
import com.ezeeworld.b4s.android.sdk.B4SUserProperty;
import com.ezeeworld.b4s.android.sdk.monitor.MonitoringManager;
import com.ezeeworld.b4s.android.sdk.notifications.NotificationService;

/**
 * An example application for the B4S SDK that sets up some non-default settings and ensures the monitoring service is properly set up.
 */
public class SampleApp extends Application implements NotificationService.NotificationModifier {

	// Replace YOUR_APP_ID value with your own APP_ID
	public final static String YOUR_APP_ID = "MY_APP_ID";
	public final static String YOUR_GOOGLE_SENDER_ID = "MY-GOOGLE-SENDER-ID";
	public final static String NEERBY_PREF_ENABLE_KEY = "ShouldEnableNeerbySDK";
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

        // Register the current instance as NotificationModifier to allow notification monitoring
        // and notification message to be customized.
        NotificationService.registerNotificationModifier(this);
	}

	public static void startSDK(Application application) {
		// Initialize the B4S SDK with our app-specific registration ID
		B4SSettings settings = B4SSettings.init(application, YOUR_APP_ID);
        settings.setShouldVibrateOnNotification(true);
        settings.setNotificationBackgroundColor(0xff111111);
        settings.setCustomNotificationSmallIcon(R.drawable.ic_notifsmall);
        settings.setCustomNotificationLargeIcon(R.drawable.ic_notiflarge);

		// Enable remote push notifications
		// settings.setPushMessagingSenderId(YOUR_GOOGLE_SENDER_ID);

        B4SUserProperty.get().store(B4SUserProperty.USER_EMAIL, "jmb@ezeeworld.com");
        B4SUserProperty.get().store(B4SUserProperty.USER_FIRST_NAME, "jean-michel");
        B4SUserProperty.get().store(B4SUserProperty.USER_LAST_NAME, "bécatresse");
        B4SUserProperty.get().store(B4SUserProperty.USER_CLIENT_REF, "EE123456789FR");

		// Send deep links to our broadcast receiver (instead of the default launcher activity delivery)
		NotificationService.registerDeepLinkStyle(NotificationService.DeepLinkStyle.BroadcastReceiver);

		// Start the monitoring service, if needed
		MonitoringManager.ensureMonitoringService(application);
	}

    /**
     * This callback is called at notification generation time. It gives opportunity to modify
     * the notification title just before it will be displayed.
     * The extras params can be used to monitor notification activity.
     * You can even change notification icon accordingly to the data values associated to the notification.
     * @param extras
     * @return
     */
    public String modifyNotificationTitle(Bundle extras) {
        Log.d("B4S"," >> "+extras.getString(NotificationService.INTENT_TITLE));
        Log.d("B4S"," > "+extras.getString(NotificationService.INTENT_MESSAGE));
        Log.d("B4S"," > "+extras.getDouble(NotificationService.INTENT_SHOPLATITUDE, 0));
        Log.d("B4S"," > "+extras.getDouble(NotificationService.INTENT_SHOPLONGITUDE, 0));
        Log.d("B4S"," > "+extras.getInt(NotificationService.INTENT_INTERACTIONRADIUS, 0));
        Log.d("B4S"," > "+extras.getString(NotificationService.INTENT_SHOPCITY));
        Log.d("B4S"," > "+extras.getString(NotificationService.INTENT_SHOPZIPCODE));
        Log.d("B4S"," > "+extras.getString(NotificationService.INTENT_SHOPNAME));
        Log.d("B4S"," > "+extras.getString(NotificationService.INTENT_SHOPCLIENTREF));
        Log.d("B4S"," > "+extras.getString(NotificationService.INTENT_BEACONID));
        Log.d("B4S"," > "+extras.getString(NotificationService.INTENT_BEACONNAME));
        Log.d("B4S"," > "+extras.getString(NotificationService.INTENT_BEACONCLIENTREF));
        Log.d("B4S"," > "+extras.getString(NotificationService.INTENT_CAMPAIGNNAME));
        Log.d("B4S"," > "+extras.getString(NotificationService.INTENT_INTERACTIONNAME));

        if (extras.getString(NotificationService.INTENT_CAMPAIGNNAME).indexOf("APP") == 0) {
            B4SSettings.get().setShouldVibrateOnNotification(false);
            B4SSettings.get().setCustomNotificationSmallIcon(R.drawable.ic_notifsmall);
            B4SSettings.get().setCustomNotificationLargeIcon(this.getApplicationInfo().icon);

            return "ALT DESIGN";
        }

        return extras.getString(NotificationService.INTENT_TITLE);
    }

    /**
     * This callback is called at notification generation time. It gives opportunity to modify
     * the notification message just before it will be displayed.
     * @param extras
     * @return
     */
    public String modifyNotificationMessage(Bundle extras) {
        Log.d("B4S"," >> "+extras.getString(NotificationService.INTENT_MESSAGE));
        return extras.getString(NotificationService.INTENT_MESSAGE);
    }

}
