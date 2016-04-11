![deltaDNA logo](https://deltadna.com/wp-content/uploads/2015/06/deltadna_www@1x.png)

# deltaDNA Android SDK智能广告
[![Build Status](https://travis-ci.org/deltaDNA/android-smartads-sdk.svg)](https://travis-ci.org/deltaDNA/android-smartads-sdk)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/438f868ae71a444b8a1f8ebce32c3176)](https://www.codacy.com/app/deltaDNA/android-smartads-sdk)
[![Apache2 licensed](https://img.shields.io/badge/license-Apache-blue.svg)](./LICENSE.txt)
[![Download](https://api.bintray.com/packages/deltadna/android/deltadna-smartads/images/download.svg)](https://bintray.com/deltadna/android/deltadna-smartads/_latestVersion)

deltaDNA智能广告SDK用于将你的Android游戏接入我们的智能广告中间平台。它同时支持空闲广告和奖励广告。

## 目录
* [添加至项目](#添加至项目)
* [用法](#用法)
* [权限](#权限)
* [防反编译（ProGuard）](#防反编译（ProGuard）)
* [常见问题解答](#常见问题解答)
* [更新日志](#更新日志)
* [授权](#授权)

## 添加至项目
deltaDNA SDK可以用于基于第15版和更新版本（Android 4.0.3+）内核SDK的Android项目。

### Gradle
在你的顶层构建脚本
```groovy
allprojects {
    repositories {
        maven { url 'http://deltadna.bintray.com/android' }
        // repositories为你其他的dependencies...
    }
}
```
在你APP的构建脚本
```groovy
compile 'com.deltadna.android:deltadna-sdk:VERSION'
compile 'com.deltadna.android:deltadna-smartads:1.0.4-SNAPSHOT'

// 广告提供商
compile 'com.deltadna.android:deltadna-smartads-provider-adcolony:1.0.4-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-admob:1.0.4-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-amazon:1.0.4-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-applovin:1.0.4-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-chartboost:1.0.4-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-flurry:1.0.4-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-inmobi:1.0.4-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-mobfox:1.0.4-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-mopub:1.0.4-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-supersonic:1.0.4-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-unity:1.0.4-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-vungle:1.0.4-SNAPSHOT'
```
上述广告提供商的任何组合都可以在你的构建脚本中定义，这取决于你想在你的应用程序中使用哪个广告网络。

请注意智能广告使用的版本应当与提供商的版本相同。我们不能保证如果版本不匹配时一个广告提供商可以正常工作。

## 用法
一个实例可以通过调用`DDNASmartAds.instance()`函数被取回，同时标记广告可以通过`registerForAds(Activity)`方法实现。这个分析SDK需要在标记广告前初始化和启用。
```java
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    DDNA.instance().startSdk();
    DDNASmartAds.instance.registerForAds(this);
}
```

当你的`Activity`被重写、暂停和销毁时，智能广告需要被通过优先于生命周期的方法和调用`DDNASmartAds`中的合适方法告知。
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

可以通过`isInterstitialAdAvailable()`或`isRewardedAdAvailable()`方法分别检测一个空闲广告或奖励广告是否可用。

广告最终可以分别通过调用`showInterstitialAd()`或`showRewardedAd()`方法显示空闲广告或奖励广告。

为了监听广告的生命周期，`setAdsListener(AdsListener)`或`setRewardedAdsListener(RewardedAdsListener)`方法可以被用来设置监听器。

## 权限
这个库在其清单文件中包括所有要求的权限，这个清单文件可以在编译过程中被合并并包含在Android的清单中。这些包含的权限是确保广告提供商功能的最基本要求。所有的这些权限都不是危险权限，因此在Android 6以后的版本都不需要明确的授权。

其他（往往是危险的）权限可能被添加以提高广告网络提供商的功能和性能。这些权限在每一个提供商的清单文件中以注释列出。你可以根据需求添加其中的任一一些到你的应用程序清单中。

## 防反编译（ProGuard）
如果你为你的应用设置`minifyEnabled true`，那么没有必要在你的ProGuard配置中添加额外的代码。因为这个库提供了其自己的配置文件，可以在编译过程中被Android编译工具包含进去。

## 常见问题解答
1.  我的项目在较新版本的Google Play Services中有一个Dependency，我可以使用一个不同于智能广告默认的版本吗？
    
    是的。如果你已经添加了任何其他的Play Service模块到你的Dependency，这时你可以将版本改成你需要的。例如
    ```Java
    compile 'com.google.android.gms:play-services-maps:8.4.0'
    ```
    到目前为止，我们已经确认8.*版本可以替代7.8版本。
2.  当我的项目编译时，我在`TransformException`任务中得到了一个`transformClassesWithDexForDebug`警示。
    
    如果你引入了太多的广告提供商导致你的应用包括超过65K的方法时这种情况可能发生。广告提供商可以被移除以减小方法的数量，或者使用一个[官方解决方案](http://developer.android.com/tools/building/multidex.html#mdex-gradle)。

## 更新日志
可以从[这里](CHANGELOG.md)找到。

## 授权
该资源适用于Apache 2.0授权。
