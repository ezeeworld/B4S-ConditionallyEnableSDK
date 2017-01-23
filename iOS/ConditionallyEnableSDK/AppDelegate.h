//
//  AppDelegate.h
//  ConditionallyEnableSDK
//
//  Created by Francois Reboursier on 22/11/16.
//  Copyright © 2016 Ezeeworld. All rights reserved.
//

#import <UIKit/UIKit.h>

@import BeaconForStoreSDK;

extern NSString *const kShouldEnableNeerbySDK;
extern NSString *const kB4sAppIDKey;

@interface AppDelegate : UIResponder <UIApplicationDelegate, B4SDelegate>

@property (strong, nonatomic) UIWindow *window;


@end

