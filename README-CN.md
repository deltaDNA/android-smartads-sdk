![deltaDNA logo](https://deltadna.com/wp-content/uploads/2015/06/deltadna_www@1x.png)

# deltaDNA Android SDK智能广告
[![Build Status](https://travis-ci.org/deltaDNA/android-smartads-sdk.svg)](https://travis-ci.org/deltaDNA/android-smartads-sdk)
[![codecov.io](https://codecov.io/github/deltaDNA/android-smartads-sdk/coverage.svg)](https://codecov.io/github/deltaDNA/android-smartads-sdk)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/438f868ae71a444b8a1f8ebce32c3176)](https://www.codacy.com/app/deltaDNA/android-smartads-sdk)
[![Apache2 licensed](https://img.shields.io/badge/license-Apache-blue.svg)](./LICENSE.txt)
[![Download](https://api.bintray.com/packages/deltadna/android/deltadna-smartads/images/download.svg)](https://bintray.com/deltadna/android/deltadna-smartads/_latestVersion)

deltaDNA智能广告SDK用于将你的Android游戏接入我们的智能广告中间平台。它同时支持空闲广告和奖励广告。

## 目录
* [添加至项目](#添加至项目)
* [初始化](#初始化)
* [显示广告](#显示广告)
* [权限](#权限)
* [防反编译](#防反编译)
* [常见问题解答](#常见问题解答)
* [更新日志](#更新日志)
* [迁移](#迁移)
* [授权](#授权)

## 添加至项目
deltaDNA智能广告SDK可以用于基于第15版和更新版本（Android 4.0.3+）内核SDK的Android项目。

### Gradle
在你的顶层构建脚本
```groovy
allprojects {
    repositories {
        maven { url 'http://deltadna.bintray.com/android' }
        // 存储你的其他依赖...
    }
}
```
在你APP的构建脚本
```groovy
compile 'com.deltadna.android:deltadna-sdk:VERSION'
compile 'com.deltadna.android:deltadna-smartads:1.2.6-SNAPSHOT'

// 广告提供商
compile 'com.deltadna.android:deltadna-smartads-provider-adcolony:1.2.6-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-admob:1.2.6-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-amazon:1.2.6-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-chartboost:1.2.6-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-flurry:1.2.6-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-inmobi:1.2.6-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-mobfox:1.2.6-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-mopub:1.2.6-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-supersonic:1.2.6-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-unity:1.2.6-SNAPSHOT'
compile 'com.deltadna.android:deltadna-smartads-provider-vungle:1.2.6-SNAPSHOT'
```
上述广告提供商的任何组合都可以在你的构建脚本中定义，这取决于你想在你的应用程序中使用哪个广告网络。

请注意智能广告使用的版本应当与提供商的版本相同。我们不能保证如果版本不匹配时一个广告提供商可以正常工作。

## 初始化
一个实例可以通过调用`DDNASmartAds.instance()`函数被取回，同时标记广告可以通过`registerForAds(Activity)`方法实现。这个分析SDK需要在标记广告前初始化和启用。
```java
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    DDNA.instance().startSdk();
    DDNASmartAds.instance().registerForAds(this);
}
```

当你的`Activity`被重写、暂停和销毁时，智能广告需要通过优先于生命周期的方法和调用`DDNASmartAds`中的合适方法被告知。
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

可以通过使用`setAdRegistrationListener(AdRegistrationListener)`方法来监听空闲广告和奖励广告的注册状态。
```java
DDNASmartAds.instance().setAdRegistrationListener(new AdRegistrationListener() {
    // 回调方法
});
```

## 显示广告
可以通过创建一个`InterstitialAd`的实例并调用`show()`来显示空闲广告。在`create()`被调用后，结果应当进行null检查，因为如果超过了时间或会话限制后创建可能会失败。
```java
InterstitialAd ad = InterstitialAd.create();
if (ad != null) {
    ad.show();
}
```
奖励广告的创建方式也相似，但是要通过`RewardedAd`类创建。

广告也可以通过执行一个吸引（Engage）请求并创建一个来自返回的`Engagement`的`InterstitialAd`或`RewardedAd`实例被创建。
```java
DDNA.instance().requestEngagement(
        new Engagement("myDecisionPoint"),
        new EngageListener<Engagement>() {
            @Override
            public void onCompleted(Engagement engagement) {
                RewardedAd reward = RewardedAd.create(engagement);
                ImageMessage image = ImageMessage.create(engagement);
                
                if (image != null) {
                    // 用于显示图像消息的代码
                } else if (reward != null) {
                    reward.show();
                }
            }
            
            @Override
            public void onError(Throwable t) {
                // 获取错误
            }
        }
);
```

这两个类都允许一个监听者在创建监听广告生命周期事件时进入。

## 权限
这个库在其清单文件中包括所有要求的权限，这个清单文件可以在编译过程中被合并并包含在Android的清单中。这些包含的权限是确保广告提供商功能的最基本要求。所有的这些权限都不是危险权限，因此在Android 6以后的版本都不需要明确的授权。

其他（往往是危险的）权限可能被添加以提高广告网络提供商的功能和性能。这些权限在每一个提供商的清单文件中以注释列出。你可以根据需求添加其中的任一一些到你的应用程序清单中。

## 防反编译
如果你为你的应用设置`minifyEnabled true`，那么没有必要在你的ProGuard配置中添加额外的代码。因为这个库提供了其自己的配置文件，可以在编译过程中被Android编译工具包含进去。

## 常见问题解答
1.  我的项目在较新版本的Google Play Services中有一个依赖，我可以使用一个不同于智能广告默认的版本吗？
    
    是的。如果你已经添加了任何其他的Play Service模块到你的依赖，这时你可以将版本改成你需要的。例如
    ```java
    compile 'com.google.android.gms:play-services-maps:8.4.0'
    ```
    到目前为止，我们已经确认第8、9和10版本可以替代7.8版本。
2.  当我的项目编译时，我在`transformClassesWithDexForDebug`任务中得到了一个`TransformException`警示。
    
    如果你引入了太多的广告提供商导致你的应用包括超过65K的方法时这种情况可能发生。广告提供商可以被移除以减小方法的数量，或者使用一个[官方解决方案](http://developer.android.com/tools/building/multidex.html#mdex-gradle)。

## 更新日志
可以从[这里](CHANGELOG.md)找到。

## 迁移
* [版本1.1](docs/migrations/1.1.md)
* [版本1.2](docs/migrations/1.2.md)

## 授权
该资源适用于Apache 2.0授权。
