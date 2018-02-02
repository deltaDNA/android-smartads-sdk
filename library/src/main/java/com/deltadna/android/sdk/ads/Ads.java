/*
 * Copyright (c) 2016 deltaDNA Ltd. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.deltadna.android.sdk.ads;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.DDNA;
import com.deltadna.android.sdk.Engagement;
import com.deltadna.android.sdk.Event;
import com.deltadna.android.sdk.Params;
import com.deltadna.android.sdk.ads.core.AdService;
import com.deltadna.android.sdk.ads.core.AdServiceListener;
import com.deltadna.android.sdk.ads.core.AdServiceWrapper;
import com.deltadna.android.sdk.ads.core.EngagementListener;
import com.deltadna.android.sdk.ads.exceptions.EngagementFailureException;
import com.deltadna.android.sdk.ads.listeners.AdRegistrationListener;
import com.deltadna.android.sdk.ads.listeners.InterstitialAdsListener;
import com.deltadna.android.sdk.ads.listeners.RewardedAdsListener;
import com.deltadna.android.sdk.listeners.EngageListener;
import com.deltadna.android.sdk.listeners.EventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

class Ads implements
        AdServiceListener,
        EventListener,
        ActivityCatcher.LifecycleCallbacks {
    
    private static final String DECISION_POINT = "advertising";
    
    private final Runnable serviceCreator = new Runnable() {
        @Override
        public void run() {
            Log.d(BuildConfig.LOG_TAG, "Creating service");
            service = AdServiceWrapper.create(
                    catcher.getActivity(),
                    Ads.this,
                    BuildConfig.VERSION_NAME);
        }
    };
    
    private final ActivityCatcher catcher;
    
    @Nullable
    private AdService service;
    
    private WeakReference<AdRegistrationListener> registrationListener =
            new WeakReference<>(null);
    private WeakReference<InterstitialAdsListener> interstitialListener =
            new WeakReference<>(null);
    private WeakReference<RewardedAdsListener> rewardedListener =
            new WeakReference<>(null);
    
    Ads(Application application, @Nullable Class<? extends Activity> activity) {
        if (activity != null) {
            catcher = new ConcreteActivityCatcher(this, activity);
        } else {
            catcher = new DynamicActivityCatcher(this);
        }
        
        DDNA.instance().register(this);
        application.registerActivityLifecycleCallbacks(catcher);
    }
    
    void setAdRegistrationListener(
            @Nullable AdRegistrationListener listener) {
        
        registrationListener = new WeakReference<>(listener);
    }
    
    void setInterstitialAdsListener(
            @Nullable InterstitialAdsListener listener) {
        
        interstitialListener = new WeakReference<>(listener);
    }
    
    void setRewardedAdsListener(
            @Nullable RewardedAdsListener listener) {
        
        rewardedListener = new WeakReference<>(listener);
    }
    
    boolean isInterstitialAdAllowed(@Nullable Engagement engagement) {
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
            return false;
        }
        
        return service.isInterstitialAdAllowed(
                (engagement == null) ? null : engagement.getDecisionPoint(),
                (engagement == null || engagement.getJson() == null)
                        ? null
                        : engagement.getJson().optJSONObject("parameters"));
    }
    
    boolean isRewardedAdAllowed(@Nullable Engagement engagement) {
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
            return false;
        }
        
        return service.isRewardedAdAllowed(
                (engagement == null) ? null : engagement.getDecisionPoint(),
                (engagement == null || engagement.getJson() == null)
                        ? null
                        : engagement.getJson().optJSONObject("parameters"));
    }
    
    boolean isInterstitialAdAvailable() {
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
            return false;
        }
        
        return service.isInterstitialAdAvailable();
    }
    
    boolean isRewardedAdAvailable() {
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
            return false;
        }
        
        return service.isRewardedAdAvailable();
    }
    
    void showInterstitialAd(@Nullable String adPoint) {
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
        } else {
            service.showInterstitialAd(adPoint);
        }
    }
    
    void showRewardedAd(@Nullable String adPoint) {
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
        } else {
            service.showRewardedAd(adPoint);
        }
    }
    
    @Override
    public void onRegisteredForInterstitialAds() {
        on(registrationListener, new Action<AdRegistrationListener>() {
            @Override
            public void perform(AdRegistrationListener item) {
                item.onRegisteredForInterstitial();
            }
        });
    }
    
    @Override
    public void onFailedToRegisterForInterstitialAds(final String reason) {
        on(registrationListener, new Action<AdRegistrationListener>() {
            @Override
            public void perform(AdRegistrationListener item) {
                item.onFailedToRegisterForInterstitial(reason);
            }
        });
    }
    
    @Override
    public void onRegisteredForRewardedAds() {
        on(registrationListener, new Action<AdRegistrationListener>() {
            @Override
            public void perform(AdRegistrationListener item) {
                item.onRegisteredForRewarded();
            }
        });
    }
    
    @Override
    public void onFailedToRegisterForRewardedAds(final String reason) {
        on(registrationListener, new Action<AdRegistrationListener>() {
            @Override
            public void perform(AdRegistrationListener item) {
                item.onFailedToRegisterForRewarded(reason);
            }
        });
    }
    
    @Override
    public void onInterstitialAdOpened() {
        on(interstitialListener, new Action<InterstitialAdsListener>() {
            @Override
            public void perform(InterstitialAdsListener item) {
                item.onOpened();
            }
        });
    }
    
    @Override
    public void onInterstitialAdFailedToOpen(final String reason) {
        on(interstitialListener, new Action<InterstitialAdsListener>() {
            @Override
            public void perform(InterstitialAdsListener item) {
                item.onFailedToOpen(reason);
            }
        });
    }
    
    @Override
    public void onInterstitialAdClosed() {
        on(interstitialListener, new Action<InterstitialAdsListener>() {
            @Override
            public void perform(InterstitialAdsListener item) {
                item.onClosed();
            }
        });
    }
    
    @Override
    public void onRewardedAdOpened() {
        on(rewardedListener, new Action<RewardedAdsListener>() {
            @Override
            public void perform(RewardedAdsListener item) {
                item.onOpened();
            }
        });
    }
    
    @Override
    public void onRewardedAdFailedToOpen(final String reason) {
        on(rewardedListener, new Action<RewardedAdsListener>() {
            @Override
            public void perform(RewardedAdsListener item) {
                item.onFailedToOpen(reason);
            }
        });
    }
    
    @Override
    public void onRewardedAdClosed(final boolean completed) {
        on(rewardedListener, new Action<RewardedAdsListener>() {
            @Override
            public void perform(RewardedAdsListener item) {
                item.onClosed(completed);
            }
        });
    }
    
    @Override
    public void onRecordEvent(String eventName, String eventParamJson) {
        try {
            DDNA.instance().recordEvent(new Event(
                    eventName,
                    new Params(new JSONObject(eventParamJson))));
        } catch (JSONException e) {
            Log.e(BuildConfig.LOG_TAG, "Ad event record failed", e);
        }
    }
    
    @Override
    public void onRequestEngagement(
            String decisionPoint,
            String flavour,
            String version,
            final EngagementListener listener) {
        
        DDNA.instance().requestEngagement(
                new Engagement(decisionPoint, flavour)
                        .putParam("adSdkVersion", version),
                new EngageListener<Engagement>() {
                    @Override
                    public void onCompleted(Engagement engagement) {
                        if (engagement.isSuccessful()) {
                            listener.onSuccess(engagement.getJson());
                        } else {
                            listener.onFailure(new EngagementFailureException(
                                    engagement));
                        }
                    }
                    
                    @Override
                    public void onError(Throwable t) {
                        listener.onFailure(t);
                    }
                });
    }
    
    @Override
    public void onStarted() {
        Log.d(BuildConfig.LOG_TAG, "Received onStarted event");
        catcher.onAnalyticsStarted();
        
        if (service == null) {
            if (catcher.getActivity() != null) {
                serviceCreator.run();
                service.registerForAds(DECISION_POINT);
            } else {
                Log.w(BuildConfig.LOG_TAG, "Activity has not been captured");
            }
        }
    }
    
    @Override
    public void onStopped() {
        Log.d(BuildConfig.LOG_TAG, "Received onStopped event");
        catcher.onAnalyticsStopped();
        
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
        } else {
            service.onDestroy();
        }
    }
    
    @Override
    public void onNewSession() {
        Log.d(BuildConfig.LOG_TAG, "Received onNewSession event");
        
        if (service == null) {
            if (catcher.getActivity() != null) {
                serviceCreator.run();
            } else {
                // may happen on first-time analytics creation
                Log.w(BuildConfig.LOG_TAG, "Activity has not been captured");
            }
        }
        
        if (service != null) {
            service.registerForAds(DECISION_POINT);
            service.onNewSession();
        }
    }
    
    @Override
    public void onResumed() {
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
        } else {
            service.onResume();
        }
    }
    
    @Override
    public void onPaused() {
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
        } else {
            service.onPause();
        }
    }
    
    private <T> void on(WeakReference<T> reference, Action<T> action) {
        final T item = reference.get();
        if (item != null) {
            action.perform(item);
        }
    }
    
    private interface Action<T> {
        
        void perform(T item);
    }
}
