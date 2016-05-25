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
import com.deltadna.android.sdk.ads.listeners.AdRegistrationListener;
import com.deltadna.android.sdk.ads.listeners.InterstitialAdsListener;
import com.deltadna.android.sdk.ads.listeners.RewardedAdsListener;
import com.deltadna.android.sdk.ads.exceptions.EngagementFailureException;
import com.deltadna.android.sdk.listeners.EngageListener;
import com.deltadna.android.sdk.listeners.SessionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

class Ads implements AdServiceListener, SessionListener {
    
    private static final String DECISION_POINT = "advertising";
    
    private final AdService service;
    
    private WeakReference<AdRegistrationListener> registrationListener =
            new WeakReference<>(null);
    private WeakReference<InterstitialAdsListener> interstitialListener =
            new WeakReference<>(null);
    private WeakReference<RewardedAdsListener> rewardedListener =
            new WeakReference<>(null);
    
    Ads(Activity activity) {
        service = AdServiceWrapper.create(activity, this);
        
        DDNA.instance().register(this);
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
    
    void registerForAds() {
        service.init(DECISION_POINT);
    }
    
    boolean isInterstitialAdAllowed(@Nullable Engagement engagement) {
        return service.isInterstitialAdAllowed(
                (engagement == null) ? null : engagement.getDecisionPoint(),
                (engagement == null || engagement.getJson() == null)
                        ? null
                        : engagement.getJson().optJSONObject("parameters"));
    }
    
    boolean isRewardedAdAllowed(@Nullable Engagement engagement) {
        return service.isRewardedAdAllowed(
                (engagement == null) ? null : engagement.getDecisionPoint(),
                (engagement == null || engagement.getJson() == null)
                        ? null
                        : engagement.getJson().optJSONObject("parameters"));
    }
    
    boolean isInterstitialAdAvailable() {
        return service.isInterstitialAdAvailable();
    }
    
    boolean isRewardedAdAvailable() {
        return service.isRewardedAdAvailable();
    }
    
    void showInterstitialAd(@Nullable String adPoint) {
        service.showInterstitialAd(adPoint);
    }
    
    void showRewardedAd(@Nullable String adPoint) {
        service.showRewardedAd(adPoint);
    }
    
    void onPause() {
        service.onPause();
    }
    
    void onResume() {
        service.onResume();
    }
    
    void onDestroy() {
        service.onDestroy();
    }
    
    @Override
    public void onRegisteredForInterstitialAds() {
        on(registrationListener, new WeakAction<AdRegistrationListener>() {
            @Override
            public void perform(AdRegistrationListener item) {
                item.onRegisteredForInterstitial();
            }
        });
    }
    
    @Override
    public void onFailedToRegisterForInterstitialAds(final String reason) {
        on(registrationListener, new WeakAction<AdRegistrationListener>() {
            @Override
            public void perform(AdRegistrationListener item) {
                item.onFailedToRegisterForInterstitial(reason);
            }
        });
    }
    
    @Override
    public void onRegisteredForRewardedAds() {
        on(registrationListener, new WeakAction<AdRegistrationListener>() {
            @Override
            public void perform(AdRegistrationListener item) {
                item.onRegisteredForRewarded();
            }
        });
    }
    
    @Override
    public void onFailedToRegisterForRewardedAds(final String reason) {
        on(registrationListener, new WeakAction<AdRegistrationListener>() {
            @Override
            public void perform(AdRegistrationListener item) {
                item.onFailedToRegisterForRewarded(reason);
            }
        });
    }
    
    @Override
    public void onInterstitialAdOpened() {
        on(interstitialListener, new WeakAction<InterstitialAdsListener>() {
            @Override
            public void perform(InterstitialAdsListener item) {
                item.onOpened();
            }
        });
    }
    
    @Override
    public void onInterstitialAdFailedToOpen(final String reason) {
        on(interstitialListener, new WeakAction<InterstitialAdsListener>() {
            @Override
            public void perform(InterstitialAdsListener item) {
                item.onFailedToOpen(reason);
            }
        });
    }
    
    @Override
    public void onInterstitialAdClosed() {
        on(interstitialListener, new WeakAction<InterstitialAdsListener>() {
            @Override
            public void perform(InterstitialAdsListener item) {
                item.onClosed();
            }
        });
    }
    
    @Override
    public void onRewardedAdOpened() {
        on(rewardedListener, new WeakAction<RewardedAdsListener>() {
            @Override
            public void perform(RewardedAdsListener item) {
                item.onOpened();
            }
        });
    }
    
    @Override
    public void onRewardedAdFailedToOpen(final String reason) {
        on(rewardedListener, new WeakAction<RewardedAdsListener>() {
            @Override
            public void perform(RewardedAdsListener item) {
                item.onFailedToOpen(reason);
            }
        });
    }
    
    @Override
    public void onRewardedAdClosed(final boolean completed) {
        on(rewardedListener, new WeakAction<RewardedAdsListener>() {
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
            final EngagementListener listener) {
        
        DDNA.instance().requestEngagement(
                new Engagement(decisionPoint, flavour),
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
    public void onSessionUpdated() {
        registerForAds();
    }
    
    private <T> void on(WeakReference<T> reference, WeakAction<T> action) {
        final T item = reference.get();
        if (item != null) {
            action.perform(item);
        }
    }
    
    private interface WeakAction<T> {
        
        void perform(T item);
    }
}
