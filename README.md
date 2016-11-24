# Conditionally enable the BeaconForStore SDK

This sample code shows how to conditionally enable the BeaconForStore / Neerby if the user has given his/her approval.
This approval is based on pop-up shown the first time the user starts the app if a specific area. The area is bounded by a list of zip code hard-coded within the application


## iOS

This sample uses CocoaPods to embedd the SDK; see http://ezeeworld.github.io/B4S-iOS-SDK/docs/Integrating%20the%20SDK%20with%20your%20Xcode%20project.html for more details.

To use this application:
 * Run `pod install` within the project directory
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
 * Add the following methods in your main Activity : updateSDKStatus(), checkLocationPermission(), onRequestPermissionsResult(), requestSDKActivation(), startSDK
 * Change ZipCodes list in the validPostalCodes array.