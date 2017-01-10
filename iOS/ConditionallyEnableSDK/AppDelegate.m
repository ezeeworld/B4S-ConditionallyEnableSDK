//
//  AppDelegate.m
//  ConditionallyEnableSDK
//
//  Created by Francois Reboursier on 22/11/16.
//  Copyright © 2016 Ezeeworld. All rights reserved.
//

#import "AppDelegate.h"
@import BeaconForStoreSDK;

#warning Replace this constant with your application ID
NSString *const kB4sAppIDKey = @"YOU_APP_ID";
NSString    *const  kShouldEnableNeerbySDK  = @"kShouldEnableNeerbySDK";

@interface AppDelegate ()

@end

@implementation AppDelegate

/*
 * Start the SDK if the user default is set to YES
 */

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    BOOL    shouldEnableSDK = [[NSUserDefaults standardUserDefaults] boolForKey:kShouldEnableNeerbySDK];
    
    if (shouldEnableSDK)
    {
        [B4SSingleton setupSharedInstanceWithAppId:kB4sAppIDKey];
        [[B4SSingleton sharedInstance] startStandAloneMode];
    }
    
    return YES;
}

- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification
{
    [[B4SSingleton sharedInstance] notificationFeedback:notification.userInfo];
}

@end
