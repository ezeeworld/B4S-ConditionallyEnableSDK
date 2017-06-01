# Conditionally enable the Neerby SDK

This sample code shows how to conditionally enable  Neerby SDK if the user has given his/her approval.
This approval is based on pop-up shown the first time the user starts the app if a specific area. The app calls a web service to check if the Zip code is within the proper area, and if the user phone is a supported model.


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

Bluetooth permission :
The Neerby SDK supports iBeacon devices detection. This feature requires Bluetooth permissions to be set on your application. These permissions are automatically set when you import the b4s-android-sdk.aar library. If you do not need/want iBeacon support, you can remove these permissions by adding the following rules in your application Manifest :
  ```xml
  	<uses-permission android:name="android.permission.BLUETOOTH" tools:node="remove"/>
	<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" tools:node="remove"/>
  ```
 You can suppress these lines too from your Manifest :
   ```xml
  	<uses-feature
		android:name="android.hardware.bluetooth_le"
 		android:required="true" />
  ```

Disable SDK locally with code :
You can disable locally the SDK on your application with the following sample code :
```java
	if (!B4SSettings.isInitialized()) {
		B4SSettings.init(this);
	}
	AppInfo appInfo = InteractionsApi.get().getAppInfo(false, false);
	if (appInfo != null) {
		appInfo.sdkEnabled = false;
	}
```
You can use this code in your Application::onCreate() method for example.

## All platforms

  * Optin names must be the same for both platforms (iOS and ANDROID). We encourage the use of dot notation like privacy.export.enabled
