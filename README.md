![deltaDNA logo](https://deltadna.com/wp-content/uploads/2015/06/deltadna_www@1x.png)

# deltaDNA Android SDK SmartAds
[![Build Status](https://travis-ci.org/deltaDNA/android-smartads-sdk.svg)](https://travis-ci.org/deltaDNA/android-smartads-sdk)
[![codecov.io](https://codecov.io/github/deltaDNA/android-smartads-sdk/coverage.svg)](https://codecov.io/github/deltaDNA/android-smartads-sdk)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/438f868ae71a444b8a1f8ebce32c3176)](https://www.codacy.com/app/deltaDNA/android-smartads-sdk)
[![Apache2 licensed](https://img.shields.io/badge/license-Apache-blue.svg)](./LICENSE.txt)
[![Download](https://api.bintray.com/packages/deltadna/android/deltadna-smartads/images/download.svg)](https://bintray.com/deltadna/android/deltadna-smartads/_latestVersion)

The deltaDNA SmartAds SDK provides your Android game with access to our intelligent ad mediation platform.  It supports both interstitial and rewarded type ads.

## Contents
* [Adding to a project](#adding-to-a-project)
* [Initialising](#initialising)
* [Showing ads](#showing-ads)
* [Permissions](#permissions)
* [ProGuard](#proguard)
* [FAQs](#faqs)
* [Changelog](#changelog)
* [Migrations](#migrations)
* [License](#license)

## Adding to a project
The deltaDNA SmartAds SDK can be used in Android projects using minimum SDK version 16 and newer (Android 4.1+).

### Gradle
In your top-level build script
```groovy
allprojects {
    repositories {
        maven { url 'http://deltadna.bintray.com/android' }
        // repositories for your other dependencies...
    }
}
```
In your app's build script
```groovy
compile 'com.deltadna.android:deltadna-sdk:VERSION'
compile 'com.deltadna.android:deltadna-smartads:1.6.0-SNAPSHOT'

// ad providers
compile 'com.deltadna.android:deltadna-smartads-provider-adcolony:1.6.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-admob:1.6.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-amazon:1.6.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-applovin:1.6.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-chartboost:1.6.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-facebook:1.6.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-flurry:1.6.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-hyprmx:1.6.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-inmobi:1.6.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-ironsource:1.6.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-loopme:1.6.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-mobfox:1.6.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-mopub:1.6.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-tapjoy:1.6.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-thirdpresence:1.6.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-unity:1.6.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-vungle:1.6.0-SNAPSHOT'
```
Any combination of the above ad providers can be defined in your build script, depending on which ad networks you would like to use in your application.

Please note that the versions used for SmartAds and the providers should be the same. We cannot guarantee that an ad provider will work correctly if there is a version mismatch.

## Initialising
An instance can be retrieved by calling `DDNASmartAds.instance()`, and registering for ads can be done through the `registerForAds(Activity)` method. The analytics SDK should be initialised and started before registering for ads.
```java
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    DDNA.instance().startSdk();
    DDNASmartAds.instance().registerForAds(this);
}
```

SmartAds should be notified of when your `Activity` is resumed, paused, and destroyed by overriding the lifecycle methods and calling the appropriate methods on `DDNASmartAds`.
```java
@Override
public void onResume() {
    super.onResume();
    
    DDNASmartAds.instance().onResume();
}

@Override
public void onPause() {
    super.onPause();
    
    DDNASmartAds.instance().onPause();
}

@Override
public void onDestroy() {
    DDNASmartAds.instance().onDestroy();
    DDNA.instance().stopSdk();
    
    super.onDestroy();
}
```

Listening to the registration status for interstitial and rewarded ads can be done by using the `setAdRegistrationListener(AdRegistrationListener)` method.
```java
DDNASmartAds.instance().setAdRegistrationListener(new AdRegistrationListener() {
    // callback methods
});
```

## Showing ads
Showing interstitial ads can be done by creating an instance of an `InterstitialAd` and calling `show()`. The result should be null-checked after `create()` is called as the creation may fail if the time or session limits have been exceeded.
```java
InterstitialAd ad = InterstitialAd.create();
if (ad != null) {
    ad.show();
}
```
Rewarded ads are created in a similar way, but through the `RewardedAd` class instead.

Ads can also be created by performing an Engage request and creating an `InterstitialAd` or `RewardedAd` instance from the returned `Engagement`.
```java
DDNA.instance().requestEngagement(
        new Engagement("myDecisionPoint"),
        new EngageListener<Engagement>() {
            @Override
            public void onCompleted(Engagement engagement) {
                RewardedAd reward = RewardedAd.create(engagement);
                ImageMessage image = ImageMessage.create(engagement);
                
                if (image != null) {
                    // code for showing Image Message
                } else if (reward != null) {
                    reward.show();
                }
            }
            
            @Override
            public void onError(Throwable t) {
                // act on error
            }
        }
);
```

Both classes allow for a listener to be passed in at creation for listening to ad lifecycle events.

## Permissions
The library includes all the required permissions in its manifest file which will get included by Android's manifest merger during the build process. The included permissions are the minimal required set for ensuring functionality of the ad providers, and all of them are non-dangerous permissions and thus don't require explicit granting of permissions on Android 6+ versions.

Additional (often dangerous) permissions may be added to increase the functionality/performance for the ad network providers. These have been listed in every provider's manifest file as a comment. You may add any of these to your application's manifest as needed.

## ProGuard
There is no need to add additional directives in your ProGuard configuration if you are setting `minifyEnabled true` for your application as the library provides its own configuration file which gets included by the Android build tools during the build process.

## FAQs
1.  I'm getting a `TransformException` on the `transformClassesWithDexForDebug` task when my project is being built.
    
    This can happen if you have included more ad providers which can result in your app containing more than 65K methods. Ad providers can either be removed to decrease the method count, or an [official workaround](http://developer.android.com/tools/building/multidex.html#mdex-gradle) can be implemented.

## Changelog
Can be found [here](CHANGELOG.md).

## Migrations
* [Version 1.1](docs/migrations/1.1.md)
* [Version 1.2](docs/migrations/1.2.md)

## License
The sources are available under the Apache 2.0 license.
