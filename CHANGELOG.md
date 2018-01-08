# Change Log

## [1.7.0](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.7.0) (YYYY-MM-DD)
Added MachineZone ads.  
Updated build tools and runtime libraries.  
Updated AdColony ads  
Updated AppLovin ads.  
Updated Chartboost ads.  
Updated Facebook ads.  
Updated Flurry ads.  
Updated HyprMX ads.  
Updated InMobi ads.  
Updated IronSource ads.  
Updated MobFox ads.  
Updated MoPub ads.  
Updated Tapjoy ads.  
Updated Unity ads.  
Updated Vungle ads.  
Updated IronSource to set mediation type.  

## [1.6.1](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.6.1) (2017-11-27)
Fixed InMobi completed callbacks not invoking.  
Fixed database cursors not being potentially closed.  

## [1.6.0](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.6.0) (2017-11-09)
Added rewarded ads for Facebook.  
Added AdMob app id support.  
Fixed MobFox completed state.  
Updated analytics dependency.  
Updated AdMob ads.  
Updated AppLovin ads.  
Updated Chartboost ads.  
Updated Facebook ads.  
Updated Flurry ads.  
Updated InMobi ads.  
Updated IronSource ads.  
Updated MobFox ads.  
Updated Unity ads.  
Updated Vungle ads.  
Updated build and runtime dependencies.  

## [1.5.3](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.5.3) (2017-09-20)
Workaround for hardware not supported error when using Tapjoy on Unity.  
Updated AdColony ads.  
Updated analytics SDK dependency.  

## [1.5.2](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.5.2) (2017-09-11)
Fixed missing icons in Tapjoy on Unity.  

## [1.5.1](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.5.1) (2017-09-11)
Fixed missing resources in Tapjoy.  

## [1.5.0](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.5.0) (2017-08-22)
Added rewarded ads for AdMob.  
Added rewarded Tapjoy ads.  
Added LoopMe ads.  
Added HyprMX ads.  
Added IronSource ads placement support.  
Fixed IronSource only loading a single type of ad.  
Fixed invalid events due to long ad error strings.  
Updated Thirdpresence ads.  
Updated MoPub ads.  
Updated MobFox ads.  
Updated IronSource ads.  
Updated Flurry ads.  
Updated Facebook ads.  
Updated Vungle ads.  

## [1.4.3](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.4.3) (2017-06-14)
Added network crash detection and exclusion from following runs.  
Fixed Chartboost misreporting show failure callbacks as load failures.  
Fixed Vungle callbacks not invoked after the first ad show.  
Fixed IronSource callbacks not invoked after the first ad show.  
Fixed Unity callbacks not invoked after the first ad show.  
Fixed misreporting of adShow events.  
Fixed behaviour when ads requested/shown from non-UI threads.  
Updated IronSource dependency.  
Updated AppLovin dependency.  
Updated Vungle dependency.  
Updated InMobi dependency.  
Updated Facebook dependency.  
Updated Google Play Services and Firebase dependencies.  
Updated deltaDNA analytics dependency.  

## [1.4.2](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.4.2) (2017-05-17)
Fixed AppLovin reporting duplicate ad loads.  
Fixed AdColony not reporting expired ads correctly.  
Updated AppLovin dependency.  

## [1.4.1](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.4.1) (2017-05-03)
Fixed Chartboost callbacks.  
Fixed Unity callbacks.  
Fixed Vungle callbacks.  
Fixed misreporting of ad show events.  

## [1.4.0](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.4.0) (2017-04-17)
Added Facebook ad network.  
Added IronSource ad network.  
Updated minimum API level to 16 (Jelly Bean).  
Updated AdColony dependency.  
Updated AppLovin dependency.  
Updated ChartBoost dependency.  
Updated Flurry dependency.  
Updated InMobi dependency.  
Updated MobFox dependency.  
Updated MoPub dependency.  
Updated ThirdPresence dependency.  
Updated Unity dependency.  
Updated Vungle dependency.  
Updated Play Services and Support libraries.  

## [1.3.0](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.3.0) (2017-03-13)
Added AppLovin ad network.  
Added Thirdpresence ad network.  
Updated Google Play Services dependencies.  
Updated AdMob dependency.  
Updated AdColony dependency.  
Updated Flurry dependency.  
Updated InMobi dependency.  
Updated MobFox dependency.  
Updated Unity dependency.  
Updated Vungle dependency.  

## [1.2.6](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.2.6) (2017-01-31)
Fixed some adapters not respecting the waterfall index.  
Fixed ad requests being made after session limit has been reached.  

## [1.2.5](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.2.5) (2016-12-19)
Updated Chartboost network library.  

## [1.2.4](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.2.4) (2016-12-12)
Fixed ad configuration retries for some network cases.  
Fixed max requests and demote code not being read from configuration.  
Updated analytics SDK dependency.  

## [1.2.3](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.2.3) (2016-11-23)
Fixed Amazon ad events.  
Fixed MobFox crash.  
Fixed Vungle misreporting ad completed watch status.  
Updated analytics SDK dependency.  
Updated AdColony network library.  
Updated InMobi network library.  
Updated MoPub network library.  
Updated target Android version.  

## [1.2.2](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.2.2) (2016-09-15)
Updated ad network libraries.  

## [1.2.1](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.2.1) (2016-08-18)
Fixed crash when no networks are added from the configuration.  

## [1.2.0](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.2.0) (2016-05-27)
Fixed creation of ads to take session and time limits into account.  
Fixed session and time limits not being independent between interstitial and rewarded ads.  
Fixed configuration and limits not being updated when the session changes.  
Fixed ad networks not being cycled correctly.  
Fixed configuration not being read correctly.  

## [1.1.4](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.1.4) (2016-05-11)
Updated AdColony library dependency to fix bugs.  
Fixed AdColony reporting wrong ad shown state.  
Fixed minimum interval between ads not being respected.

## [1.1.3](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.1.3) (2016-05-09)
Fixed crash when requesting ads without a network connection.

## [1.1.2](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.1.2) (2016-05-03)
Updated MoPub library dependency to fix security issue.  
Fixed reporting of interstitial ad clicks for MoPub.

## [1.1.1](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.1.1) (2016-04-29)
Fixed analytics dependency version.

## [1.1.0](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.1.0) (2016-04-29)
Added new interstitial and rewarded ad APIs.  
Fixed Unity reporting 'no fill' on a cold start.  
Fixed listener memory leaks.  
Fixed requesting of WRITE_EXTERNAL_STORAGE permission on API 19+.

## [1.0.3](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.0.3) (2016-04-06)
Fixed JavaDoc links to Android/Java classes.  
Fixed Vungle reporting incorrect ad completion.  
Updated Vungle library dependency.

## [1.0.2](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.0.2) (2016-03-30)
Fixed build failure with MobFox provider.

## [1.0.1](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.0.1) (2016-03-29)
Updated analytics SDK dependency.  
Updated AdColony library dependency.

## [1.0.0](https://github.com/deltaDNA/android-smartads-sdk/releases/tag/1.0.0) (2016-03-16)
Initial version 1.0 release.
