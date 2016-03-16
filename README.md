![deltaDNA logo](https://deltadna.com/wp-content/uploads/2015/06/deltadna_www@1x.png)

# deltaDNA Android SDK SmartAds

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
compile 'com.deltadna.android:deltadna-smartads:1.0.0-SNAPSHOT'

// ad providers
compile 'com.deltadna.android:deltadna-smartads-provider-adcolony:1.0.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-admob:1.0.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-amazon:1.0.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-applovin:1.0.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-chartboost:1.0.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-flurry:1.0.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-inmobi:1.0.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-mobfox:1.0.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-mopub:1.0.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-supersonic:1.0.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-unity:1.0.0-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-vungle:1.0.0-SNAPSHOT'
```

Please note that the versions used for SmartAds and the providers should
be the same. We cannot guarantee that an ad provider will work correctly
if there is a version mismatch.

## Usage
An instance can be retrieved by calling `DDNASmartAds.instance()`, and
registering for ads can be done through the `registerForAds(Activity)`
method. The analytics SDK should be initialised and started before
registering for ads.

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
    Services, can I use a different version of AdMob than what is
    documented?
    
    Yes. If you have added AdMob to your dependencies then you can
    change the version to what you require, for example
    ```Java
    compile 'com.google.android.gms:play-services-ads:8.4.0'
    ```
    We have verified so far that versions 8.* can be used instead of
    version 7.8.

## Changelog
Can be found [here](CHANGELOG.md).

## License
The sources are available under the Apache 2.0 license.
