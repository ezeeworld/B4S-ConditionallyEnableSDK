//
//  AppDelegate.m
//  ConditionallyEnableSDK
//
//  Created by Francois Reboursier on 22/11/16.
//  Copyright © 2016 Ezeeworld. All rights reserved.
//

#import "AppDelegate.h"
@import BeaconForStoreSDK;

NSString *const kShouldStartSDKKey = @"ShouldStartNeerbySDK";

#warning Replace this constant with your application ID
NSString *const kB4sAPIKey = @"YOU_APP_ID";
@interface AppDelegate ()

@end

@implementation AppDelegate

/*
 * Start the SDK if the user default is set to YES
 */

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    
    BOOL shouldStartNeerbySDK = [[NSUserDefaults standardUserDefaults] boolForKey:kShouldStartSDKKey];
    
    if (shouldStartNeerbySDK)
    {
        [[B4SSingleton setupSharedInstanceWithAppId:kB4sAPIKey] startStandAloneMode];
    }
    return YES;
}

- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification
{
    [[B4SSingleton sharedInstance] notificationFeedback:notification.userInfo];
}

@end
