//
//  AppDelegate.m
//  ConditionallyEnableSDK
//
//  Created by Francois Reboursier on 22/11/16.
//  Copyright © 2016 Ezeeworld. All rights reserved.
//

#import "AppDelegate.h"

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
        // This is optional, if you want to enable push notifications
        [[B4SSingleton sharedInstance] enablePushNotifications];
        
        // Required for the customizeNotificationText:andData:andUserInfo:userInfos:completion: to be called
        [B4SSingleton sharedInstance].delegate = self;
        
        [[B4SSingleton sharedInstance] setUserProperty:@"jmb@ezeeworld.com" withString:kB4SUserPropertyUsereMailKey];
        [[B4SSingleton sharedInstance] setUserProperty:@"jean-michel" withString:kB4SUserPropertyUserFirstNameKey];
        [[B4SSingleton sharedInstance] setUserProperty:@"bécatresse" withString:kB4SUserPropertyUserLastNameKey];
        [[B4SSingleton sharedInstance] setUserProperty:@"EE123456789FR" withString:kB4SUserPropertyUserClientRefKey];
    }
    
    return YES;
}

- (void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification
{
    [[B4SSingleton sharedInstance] notificationFeedback:notification.userInfo];
}

// Implementing this method is mandatory to use push notifications
// Refer to the documentation for more information
- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    [[B4SSingleton sharedInstance] registerPushNotificationDeviceToken:deviceToken];
}

// This method is called before the notification is played. You get all the needed info to create an history of the notifications
- (void)customizeNotificationText:(NSString *)aText andData:(NSString *)aData andUserInfo:(NSMutableDictionary *)userInfos completion:(void (^)(NSString *, NSString *, NSMutableDictionary *))completion
{
    NSLog(@"userInfos: %@", userInfos);
    NSLog(@"Shop name: %@", userInfos[@"sShopName"]);
    NSLog(@"Shop client ref: %@", userInfos[@"sStoreClientRef"]);
    NSLog(@"Shop zip code: %@", userInfos[@"sShopZipCode"]);
    NSLog(@"Shop city: %@", userInfos[@"sShopCity"]);
    NSLog(@"Shop latitude: %@", userInfos[@"sShopLatitude"]);
    NSLog(@"Shop longitude: %@", userInfos[@"sShopLongitude"]);
    NSLog(@"Beacon ID: %@", userInfos[@"sBeaconId"]);
    NSLog(@"Beacon name: %@", userInfos[@"sBeaconName"]);
    NSLog(@"Beacon client ref: %@", userInfos[@"sBeaconClientRef"]);
    NSLog(@"Campaign name: %@", userInfos[@"sCampaignName"]);
    NSLog(@"Interaction distance in cm: %@", userInfos[@"sInteractionDistance"]);
    
    
    completion(aText, aData, userInfos);
}
@end
