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
@import AFNetworking;
@import Darwin.POSIX.sys.utsname;

@interface ViewController ()  <CLLocationManagerDelegate>
@property (strong, nonatomic)  CLLocationManager       *locationManager;
@property (nonatomic, weak) IBOutlet        UILabel     *uiSdkStatusLabel;
@end

@implementation ViewController


- (BOOL)updateStatus
{
    BOOL sdkStatus = ([B4SSingleton sharedInstance] != nil); // The easiest way to check if the SDK is enabled is the check if the B4SSingleton is not equal to nil
    NSLog(@"SDK Status is %d", sdkStatus);
    
    if (sdkStatus)
    {
        self.uiSdkStatusLabel.text = @"SDK is ENABLED";
        self.uiSdkStatusLabel.textColor = [UIColor greenColor];
    }
    else
    {
        self.uiSdkStatusLabel.text = @"SDK is DISABLED";
        self.uiSdkStatusLabel.textColor = [UIColor redColor];
    }
    
    return sdkStatus;
}

- (void)viewDidAppear:(BOOL)animated
{
    if ([self updateStatus]) // If the SDK is already started bail out
    {
        NSLog(@"The SDK is already enabled");
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
        NSString        *deviceModel = [self deviceModel];
        
        NSLog(@"Zip code: %@ / device model: %@", firstPlacemark.postalCode, deviceModel);
        
        AFHTTPSessionManager *manager = [AFHTTPSessionManager manager];
        manager.responseSerializer = [AFHTTPResponseSerializer serializer];
        NSDictionary    *parameters = @{@"zip_code": firstPlacemark.postalCode, @"device_model": deviceModel};
        
        
        [manager GET:@"https://apps.ezeeworld.com/pagesjaunes/json-sample-app/validate-device.php" parameters:parameters progress:nil success:^(NSURLSessionTask *task, id responseObject) {
            
            UIAlertController    *alertViewController = [UIAlertController alertControllerWithTitle:@"Do you want to enable the SDK ?" message:@"By clicking 'Accept' you allow this app to display relevant messages based on you location" preferredStyle:UIAlertControllerStyleAlert];
            
            [alertViewController addAction:[UIAlertAction actionWithTitle:@"Accept" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                [[NSUserDefaults standardUserDefaults] setBool:YES forKey:kShouldEnableNeerbySDK]; // Store YES in NSUserDefauls to start the SDK on next app launch ...
                
                [[B4SSingleton setupSharedInstanceWithAppId:kB4sAppIDKey] startStandAloneMode]; // ...and start the SDK immediately
                [[B4SSingleton sharedInstance] setUserProperty:@"privacy.export.enabled" withInteger:1]; // You can add multiple properties depending on the user prefences
                
                [self updateStatus];
            }]];
            
            [alertViewController addAction:[UIAlertAction actionWithTitle:@"Decline" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
                NSLog(@"User has declined to take part in the experiment");
                [[NSUserDefaults standardUserDefaults] setBool:NO forKey:kShouldEnableNeerbySDK]; // Store NO in NSUserDefauls to prevent starting the SDK on next app launch
                [self updateStatus];
                
            }]];
            
            dispatch_async(dispatch_get_main_queue(), ^{
                [self presentViewController:alertViewController animated:YES completion:nil];
            });
            
        }failure:^(NSURLSessionTask *operation, NSError *error) {
            NSLog(@"[ERROR] Failed to check if the device model / zip code are valid %@", error);
        }];
        
        
    }];
    
}

- (NSString *)deviceModel
{
    struct utsname systemInfo;
    uname(&systemInfo);
    
    return [NSString stringWithCString:systemInfo.machine
                              encoding:NSUTF8StringEncoding];
    
}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error
{
    NSLog(@"[ERROR] Location manager failed to determine user location: %@", error);
    
    [self.locationManager stopUpdatingLocation];
}

@end
