![deltaDNA logo](https://deltadna.com/wp-content/uploads/2015/06/deltadna_www@1x.png)

# deltaDNA Android SDK SmartAds
[![Build Status](https://travis-ci.org/deltaDNA/android-smartads-sdk.svg)](https://travis-ci.org/deltaDNA/android-smartads-sdk)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/438f868ae71a444b8a1f8ebce32c3176)](https://www.codacy.com/app/deltaDNA/android-smartads-sdk)
[![Apache2 licensed](https://img.shields.io/badge/license-Apache-blue.svg)](./LICENSE.txt)
[![Download](https://api.bintray.com/packages/deltadna/android/deltadna-smartads/images/download.svg)](https://bintray.com/deltadna/android/deltadna-smartads/_latestVersion)

The deltaDNA SmartAds SDK provides your Android game with access to our
intelligent ad mediation platform.  It supports both interstitial and
rewarded type ads.

## Contents
* [Adding to a project](#adding-to-a-project)
* [Usage](#usage)
* [Permissions](#permissions)
* [ProGuard](#proguard)
* [FAQs](#faqs)
* [Changelog](#changelog)
* [License](#license)

## Adding to a project
The deltaDNA SmartAds SDK can be used in Android projects using minimum
SDK version 15 and newer (Android 4.0.3+).

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
compile 'com.deltadna.android:deltadna-smartads:1.1.0-SNAPSHOT'

// ad providers
compile 'com.deltadna.android:deltadna-smartads-provider-adcolony:1.1.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-admob:1.1.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-amazon:1.1.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-applovin:1.1.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-chartboost:1.1.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-flurry:1.1.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-inmobi:1.1.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-mobfox:1.1.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-mopub:1.1.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-supersonic:1.1.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-unity:1.1.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-vungle:1.1.0-SNAPSHOT'
```
Any combination of the above ad providers can be defined in your build
script, depending on which ad networks you would like to use in your
application.

Please note that the versions used for SmartAds and the providers should
be the same. We cannot guarantee that an ad provider will work correctly
if there is a version mismatch.

## Usage
An instance can be retrieved by calling `DDNASmartAds.instance()`, and
registering for ads can be done through the `registerForAds(Activity)`
method. The analytics SDK should be initialised and started before
registering for ads.
```java
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    DDNA.instance().startSdk();
    DDNASmartAds.instance.registerForAds(this);
}
```

SmartAds should be notified of when your `Activity` is resumed, paused,
and destroyed by overriding the lifecycle methods and calling the
appropriate methods on `DDNASmartAds`.
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

Whether an ad is available can be checked by using
`isInterstitialAdAvailable()` for interstitial ads, and
`isRewardedAdAvailable()` for rewarded ads.

Ads can finally be shown by calling `showInterstitialAd()` or
`showRewardedAd()` for rewarded ads.

In order to listen to the lifecycle of ads `setAdsListener(AdsListener)`
or `setRewardedAdsListener(RewardedAdsListener)` can be used to set a
listener.

## Permissions
The library includes all the required permissions in its manifest file
which will get included by Android's manifest merger during the build
process. The included permissions are the minimal required set for
ensuring functionality of the ad providers, and all of them are
non-dangerous permissions and thus don't require explicit granting of
permissions on Android 6+ versions.

Additional (often dangerous) permissions may be added to increase the
functionality/performance for the ad network providers. These have been
listed in every provider's manifest file as a comment. You may add any
of these to your application's manifest as needed.

## ProGuard
There is no need to add additional directives in your ProGuard
configuration if you are setting `minifyEnabled true` for your
application as the library provides its own configuration file which
gets included by the Android build tools during the build process.

## FAQs
1.  My project has a dependency on a newer version of Google Play
    Services, can I use a different version than what SmartAds uses
    internally?
    
    Yes. If you have added any of the other Play Service modules to
    your dependencies then you can change the version to what you
    require, for example
    ```Java
    compile 'com.google.android.gms:play-services-maps:8.4.0'
    ```
    We have verified so far that versions 8.* can be used instead of
    version 7.8.
2.  I'm getting a `TransformException` on the
    `transformClassesWithDexForDebug` task when my project is being
    built.
    
    This can happen if you have included more ad providers which can
    result in your app containing more than 65K methods. Ad providers
    can either be removed to decrease the method count, or an
    [official workaround](http://developer.android.com/tools/building/multidex.html#mdex-gradle)
    can be implemented.

## Changelog
Can be found [here](CHANGELOG.md).

## License
The sources are available under the Apache 2.0 license.
