//
//  ViewController.m
//  ConditionallyEnableSDK
//
//  Created by Francois Reboursier on 22/11/16.
//  Copyright © 2016 Ezeeworld. All rights reserved.
//

#import "ViewController.h"
#import "AppDelegate.h"

@import BeaconForStoreSDK;
@import CoreLocation;

@interface ViewController ()  <CLLocationManagerDelegate>
@property (strong, nonatomic)  CLLocationManager       *locationManager;
@property (nonatomic, weak) IBOutlet        UILabel     *uiSdkStatusLabel;
@end

@implementation ViewController


- (BOOL)updateSDKStatus
{
    BOOL sdkStatus = ([B4SSingleton sharedInstance] != nil); // The easiest way to check if the SDK is enabled is the check if the B4SSingleton is not equal to nil
    
    if (sdkStatus)
    {
        NSLog(@"SDK is already enabled");
        self.uiSdkStatusLabel.text = @"SDK is ENABLED";
        self.uiSdkStatusLabel.textColor = [UIColor greenColor];
    }
    else
    {
        NSLog(@"SDK is disabled");
        self.uiSdkStatusLabel.text = @"SDK is DISABLED";
        self.uiSdkStatusLabel.textColor = [UIColor redColor];
   }
    
    return sdkStatus;
}

- (void)viewDidAppear:(BOOL)animated
{
    if ([self updateSDKStatus]) // If the SDK is already started bail out
    {
        return;
    }
    
    if ([CLLocationManager authorizationStatus] == kCLAuthorizationStatusDenied ||
        [CLLocationManager authorizationStatus] == kCLAuthorizationStatusRestricted )
    {
        NSLog(@"[ERROR] Location manager authorization status prevents the app to get the user location"); //If the user previously declined to allow the aopp to access his location we cannot ask him again
        return;
    }
    
    if (self.locationManager == nil) // Create a CLLocationManager instance
    {
        self.locationManager = [[CLLocationManager alloc] init];
        self.locationManager.delegate = self;
        self.locationManager.distanceFilter = 10.0f;
    }

    [self.locationManager requestAlwaysAuthorization]; // Demand the user permission to access his location
    [self.locationManager startUpdatingLocation]; // Get an up-to-date user location
    NSLog(@"startUpdatingLocation called...");
}

- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation
{
    [self.locationManager stopUpdatingLocation]; // Once we get an up-to-date location do not request updates
    
    NSLog(@"didUpdateToLocation: Lat = %.4f  Lon = %.4f", newLocation.coordinate.latitude, newLocation.coordinate.longitude);
    
    CLGeocoder *geocoder = [[CLGeocoder alloc] init];

    [geocoder reverseGeocodeLocation:newLocation completionHandler:^(NSArray *placemarks, NSError *error) { // Do a reverse geocode on the received location
        if (error)
        {
            NSLog(@"[ERROR] Geocode failed with error: %@", error);
            return;
        }
        
        NSLog(@"Found %lu placemark(s) for location", (unsigned long)[placemarks count]);
        
        CLPlacemark *firstPlacemark = [placemarks firstObject]; // Multiple placemarks can match, use the first one
        NSLog(@"Zip code: %@", firstPlacemark.postalCode);
        
        NSArray *validPostalCodes = @[@"92200", @"92130", @"92100"]; // The postal code list is hard-coded. It could be fetched from a web service
        
        if ([validPostalCodes containsObject:firstPlacemark.postalCode])
        {
            UIAlertController    *alertViewController = [UIAlertController alertControllerWithTitle:@"Do you want to enable the SDK ?" message:@"By clicking 'Accept' you allow this app to display relevant messages based on you location" preferredStyle:UIAlertControllerStyleAlert];

            
            [alertViewController addAction:[UIAlertAction actionWithTitle:@"Accept" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                [[NSUserDefaults standardUserDefaults] setBool:YES forKey:kShouldStartSDKKey]; // Store YES in NSUserDefauls to start the SDK on next app launch
                [[B4SSingleton setupSharedInstanceWithAppId:kB4sAPIKey] startStandAloneMode]; // Start the SDK now
                [self updateSDKStatus];
            }]];
            
            [alertViewController addAction:[UIAlertAction actionWithTitle:@"Decline" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
                NSLog(@"User has declined to take part in the experiment");
            }]];
            
            dispatch_async(dispatch_get_main_queue(), ^{
                [self presentViewController:alertViewController animated:YES completion:nil];
            });
            
        }
        else
        {
            NSLog(@"The placemark postal code (%@) is not included in the valid list", firstPlacemark.postalCode);
        }

    }];

    
}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error
{
    NSLog(@"[ERROR] Location manager failed to determine user location: %@", error);
    
    [self.locationManager stopUpdatingLocation];
}

@end
