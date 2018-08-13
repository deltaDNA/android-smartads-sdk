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
import com.deltadna.android.sdk.listeners.EngageListener;
import com.deltadna.android.sdk.listeners.internal.IEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.WeakHashMap;

class Ads implements
        AdServiceListener,
        IEventListener,
        ActivityCatcher.LifecycleCallbacks {
    
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
    
    private final WeakHashMap<RewardedAd, Void> rewardedAds = new WeakHashMap<>();
    
    private final Settings settings;
    private final ActivityCatcher catcher;
    
    @Nullable
    private AdService service;
    
    private WeakReference<AdRegistrationListener> registrationListener =
            new WeakReference<>(null);
    
    private WeakReference<InterstitialAd> interstitialAd = new WeakReference<>(null);
    private WeakReference<RewardedAd> rewardedAd = new WeakReference<>(null);
    
    Ads(    Settings settings,
            Application application,
            @Nullable Class<? extends Activity> activity)  {
        
        this.settings = settings;
        
        if (activity != null) {
            catcher = new ConcreteActivityCatcher(this, activity);
        } else {
            catcher = new DynamicActivityCatcher(this);
        }
        
        DDNA.instance().register(this);
        application.registerActivityLifecycleCallbacks(catcher);
    }
    
    Settings getSettings() {
        return settings;
    }
    
    void setAdRegistrationListener(
            @Nullable AdRegistrationListener listener) {
        
        registrationListener = new WeakReference<>(listener);
    }
    
    void setInterstitialAd(@Nullable InterstitialAd ad) {
        interstitialAd = new WeakReference<>(ad);
    }
    
    void setRewardedAd(@Nullable RewardedAd ad) {
        rewardedAd = new WeakReference<>(ad);
    }
    
    void registerRewardedAd(RewardedAd ad) {
        rewardedAds.put(ad, null);
    }
    
    boolean isInterstitialAdAllowed(
            @Nullable Engagement engagement,
            boolean checkTime) {
        
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
            return false;
        } else {
            return service.isInterstitialAdAllowed(
                    (engagement == null)
                            ? null
                            : engagement.getDecisionPoint(),
                    (engagement == null)
                            ? null
                            : (engagement.getJson() == null)
                            ? null
                            : engagement.getJson().optJSONObject("parameters"),
                    checkTime);
        }
    }
    
    boolean isRewardedAdAllowed(
            @Nullable Engagement engagement,
            boolean checkTime) {
        
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
            return false;
        } else {
            return service.isRewardedAdAllowed(
                    (engagement == null)
                            ? null
                            : engagement.getDecisionPoint(),
                    (engagement == null)
                            ? null
                            : (engagement.getJson() == null)
                            ? null
                            : engagement.getJson().optJSONObject("parameters"),
                    checkTime);
        }
    }
    
    int timeUntilRewardedAdAllowed(@Nullable Engagement engagement) {
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
            return 0;
        } else {
            return service.timeUntilRewardedAdAllowed(
                    (engagement == null)
                            ? null
                            : engagement.getDecisionPoint(),
                    (engagement == null)
                            ? null
                            : (engagement.getJson() == null)
                            ? null
                            : engagement.getJson().optJSONObject("parameters"));
        }
    }
    
    boolean hasLoadedInterstitialAd() {
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
            return false;
        } else {
            return service.hasLoadedInterstitialAd();
        }
    }
    
    boolean hasLoadedRewardedAd() {
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
            return false;
        } else {
            return service.hasLoadedRewardedAd();
        }
    }
    
    void showInterstitialAd(@Nullable Engagement engagement) {
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
        } else {
            service.showInterstitialAd(
                    (engagement == null)
                            ? null
                            : engagement.getDecisionPoint(),
                    (engagement == null)
                            ? null
                            : (engagement.getJson() == null)
                                    ? null
                                    : engagement.getJson().optJSONObject("parameters"));
        }
    }
    
    void showRewardedAd(@Nullable Engagement engagement) {
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
        } else {
            service.showRewardedAd(
                    (engagement == null)
                            ? null
                            : engagement.getDecisionPoint(),
                    (engagement == null)
                            ? null
                            : (engagement.getJson() == null)
                            ? null
                            : engagement.getJson().optJSONObject("parameters"));
        }
    }
    
    @Nullable
    Date getLastShown(String decisionPoint) {
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
            return null;
        } else {
            return service.getLastShown(decisionPoint);
        }
    }
    
    int getSessionCount(String decisionPoint) {
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
            return 0;
        } else {
            return service.getSessionCount(decisionPoint);
        }
    } 
    
    int getDailyCount(String decisionPoint) {
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
            return 0;
        } else {
            return service.getDailyCount(decisionPoint);
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
        on(interstitialAd, new Action<InterstitialAd>() {
            @Override
            public void perform(InterstitialAd item) {
                if (item.listener != null) item.listener.onOpened(item);
            }
        });
    }
    
    @Override
    public void onInterstitialAdFailedToOpen(final String reason) {
        on(interstitialAd, new Action<InterstitialAd>() {
            @Override
            public void perform(InterstitialAd item) {
                if (item.listener != null)
                    item.listener.onFailedToOpen(item, reason);
            }
        });
        
        interstitialAd.clear();
    }
    
    @Override
    public void onInterstitialAdClosed() {
        on(interstitialAd, new Action<InterstitialAd>() {
            @Override
            public void perform(InterstitialAd item) {
                if (item.listener != null) item.listener.onClosed(item);
            }
        });
        
        interstitialAd.clear();
    }
    
    @Override
    public void onRewardedAdLoaded() {
        for (final RewardedAd ad : rewardedAds.keySet()) {
            ad.onLoaded();
        }
    }
    
    @Override
    public void onRewardedAdOpened(String decisionPoint) {
        on(rewardedAd, new Action<RewardedAd>() {
            @Override
            public void perform(RewardedAd item) {
                if (item.listener != null) item.listener.onOpened(item);
            }
        });
        
        for (final RewardedAd ad : rewardedAds.keySet()) {
            ad.onOpened(decisionPoint);
        }
    }
    
    @Override
    public void onRewardedAdFailedToOpen(final String reason) {
        on(rewardedAd, new Action<RewardedAd>() {
            @Override
            public void perform(RewardedAd item) {
                if (item.listener != null) item.listener.onFailedToOpen(item, reason);
            }
        });
        
        rewardedAd.clear();
    }
    
    @Override
    public void onRewardedAdClosed(final boolean completed) {
        on(rewardedAd, new Action<RewardedAd>() {
            @Override
            public void perform(RewardedAd item) {
                if (item.listener != null) item.listener.onClosed(item, completed);
            }
        });
        
        rewardedAd.clear();
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
    public void onStarted() {
        Log.v(BuildConfig.LOG_TAG, "Received onStarted event");
        catcher.onAnalyticsStarted();
        
        if (service == null) {
            if (catcher.getActivity() != null) {
                serviceCreator.run();
            } else {
                // may happen on first-time analytics creation
                Log.w(BuildConfig.LOG_TAG, "Activity has not been captured");
            }
        }
    }
    
    @Override
    public void onStopped() {
        Log.v(BuildConfig.LOG_TAG, "Received onStopped event");
        catcher.onAnalyticsStopped();
        
        if (service == null) {
            Log.w(BuildConfig.LOG_TAG, "Service has not been initialised");
        } else {
            service.onDestroy();
        }
    }
    
    @Override
    public void onNewSession() {
        Log.v(BuildConfig.LOG_TAG, "Received onNewSession event");
        
        if (service == null) {
            if (catcher.getActivity() != null) {
                serviceCreator.run();
            } else {
                // may happen on first-time analytics creation
                Log.w(BuildConfig.LOG_TAG, "Activity has not been captured");
            }
        }
        
        if (service != null) service.onNewSession();
    }
    
    @Override
    public void onSessionConfigured(boolean cached, JSONObject config) {
        Log.v(BuildConfig.LOG_TAG, "Received onSessionConfigured event");
        
        if (service == null) {
            if (catcher.getActivity() != null) {
                serviceCreator.run();
            } else {
                // may happen on first-time analytics creation
                Log.w(BuildConfig.LOG_TAG, "Activity has not been captured");
            }
        }
        
        if (service != null) service.configure(
                config,
                cached,
                settings.isUserConsent(),
                settings.isAgeRestrictedUser());
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
