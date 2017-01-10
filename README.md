# Conditionally enable the BeaconForStore SDK

This sample code shows how to conditionally enable the BeaconForStore / Neerby if the user has given his/her approval.
This approval is based on pop-up shown the first time the user starts the app if a specific area. The area is bounded by a list of zip code hard-coded within the application


## iOS

To use this application:
 * Open ConditionallyEnableSDK.xcworkspace
 * In AppDelegate.m replace `YOU_APP_ID` with your actual application ID
 * Run the application on a device 

## Android

To use this application:
 * Open the project with Android Studio
 * In SampleApp.java, edit YOUR_APP_ID variable value with your actual application ID
 * Run the application on your device

Code integration :
 * Copy / Paste code inside the onCreate() method of the SampleApp class
 * In your main Activity, add the following code in the onStart() method :
 ```java
	public void onStart() {
		super.onStart();

		if (updateSDKStatus()) { // If the SDK is already started bail out
			return;
		}

		// Request location permission before requesting location
		if (checkLocationPermission()) {

			// Permission was already given, request geolocated activation
			requestSDKActivation();
		}
	}
```
 * Add the following methods in your main Activity : updateSDKStatus(), checkLocationPermission(), onRequestPermissionsResult(), requestSDKActivation(), getDeviceModel(), startSDK(), testLocation()
 * Add the CheckRemoteActivationConditionsTask class (server side device elligibility check)
 
 * In method the onPostExecute of class CheckRemoteActivationConditionsTask you may replace the AlertDialog with your own optin dialog.
 * The startSDK method accept the optins value as parameters. Each optins have to be set one time only with the following code : 
  ```java
  	B4SUserProperty.get().store(PRIVACY_EXPORT_ENABLED, optin1 ? 1 : 0);
  ```
  * Optin names must be the same for both platforms (iOS and ANDROID). We encourage the use of dot notation like privacy.export.enabled