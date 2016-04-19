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
import com.deltadna.android.sdk.ads.core.engage.EngagementListener;
import com.deltadna.android.sdk.ads.listeners.AdRegistrationListener;
import com.deltadna.android.sdk.ads.listeners.InterstitialAdsListener;
import com.deltadna.android.sdk.ads.listeners.RewardedAdsListener;
import com.deltadna.android.sdk.ads.exceptions.EngagementFailureException;
import com.deltadna.android.sdk.listeners.EngageListener;

import org.json.JSONException;
import org.json.JSONObject;

final class Ads implements AdServiceListener {
    
    private static final String DECISION_POINT = "advertising";
    
    private final AdService service;
    
    @Nullable
    private AdRegistrationListener registrationListener;
    @Nullable
    private InterstitialAdsListener interstitialListener;
    @Nullable
    private RewardedAdsListener rewardedListener;
    
    Ads(Activity activity) {
        service = new AdService(activity, this);
    }
    
    void setAdRegistrationListener(
            @Nullable AdRegistrationListener listener) {
        
        registrationListener = listener;
    }
    
    void setInterstitialAdsListener(
            @Nullable InterstitialAdsListener listener) {
        
        interstitialListener = listener;
    }
    
    void setRewardedAdsListener(
            @Nullable RewardedAdsListener listener) {
        
        rewardedListener = listener;
    }
    
    void registerForAds() {
        service.init(DECISION_POINT);
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
        if (registrationListener != null) {
            registrationListener.onRegisteredForInterstitial();
        }
    }
    
    @Override
    public void onFailedToRegisterForInterstitialAds(String reason) {
        if (registrationListener != null) {
            registrationListener.onFailedToRegisterForInterstitial(reason);
        }
    }
    
    @Override
    public void onRegisteredForRewardedAds() {
        if (registrationListener != null) {
            registrationListener.onRegisteredForRewarded();
        }
    }
    
    @Override
    public void onFailedToRegisterForRewardedAds(String reason) {
        if (registrationListener != null) {
            registrationListener.onFailedToRegisterForRewarded(reason);
        }
    }
    
    @Override
    public void onInterstitialAdOpened() {
        if (interstitialListener != null) {
            interstitialListener.onOpened();
        }
    }
    
    @Override
    public void onInterstitialAdFailedToOpen(String reason) {
        if (interstitialListener != null) {
            interstitialListener.onFailedToOpen(reason);
        }
    }
    
    @Override
    public void onInterstitialAdClosed() {
        if (interstitialListener != null) {
            interstitialListener.onClosed();
        }
    }
    
    @Override
    public void onRewardedAdOpened() {
        if (rewardedListener != null) {
            rewardedListener.onOpened();
        }
    }
    
    @Override
    public void onRewardedAdFailedToOpen(String reason) {
        if (rewardedListener != null) {
            rewardedListener.onFailedToOpen(reason);
        }
    }
    
    @Override
    public void onRewardedAdClosed(boolean completed) {
        if (rewardedListener != null) {
            rewardedListener.onClosed(completed);
        }
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
}
