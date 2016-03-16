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
import android.util.Log;

import com.deltadna.android.sdk.DDNA;
import com.deltadna.android.sdk.Engagement;
import com.deltadna.android.sdk.Event;
import com.deltadna.android.sdk.Params;
import com.deltadna.android.sdk.ads.core.AdService;
import com.deltadna.android.sdk.ads.core.AdServiceListener;
import com.deltadna.android.sdk.ads.core.listeners.AdsListener;
import com.deltadna.android.sdk.ads.core.engage.EngagementListener;
import com.deltadna.android.sdk.ads.core.listeners.RewardedAdsListener;
import com.deltadna.android.sdk.listeners.EngageListener;

import org.json.JSONException;
import org.json.JSONObject;

final class Ads implements AdServiceListener {
    
    private static final String DECISION_POINT = "advertising";

    private final AdService adService;
    private AdsListener adsListener;
    private RewardedAdsListener rewardedAdsListener;

    Ads(Activity activity) {
        this.adService = new AdService(activity, this);
    }

    void setAdsListener(AdsListener adsListener) {
        this.adsListener = adsListener;
    }

    void setRewardedAdsListener(RewardedAdsListener rewardedAdsListener) {
        this.rewardedAdsListener = rewardedAdsListener;
    }

    void registerForAds() {
        adService.init(DECISION_POINT);
    }

    void showAd() {
        adService.showAd();
    }

    void showAd(String adPoint) {
        if (adPoint == null || adPoint.length() == 0) {
            this.showAd();
        }
        else {
            adService.showAd(adPoint);
        }
    }

    boolean isInterstitialAdAvailable() {
        return adService.isInterstitialAdAvailable();
    }

    boolean isRewardedAdAvailable() {
        return adService.isRewardedAdAvailable();
    }

    void showRewardedAd() {
        adService.showRewardedAd();
    }

    void showRewardedAd(String adPoint) {
        if (adPoint == null || adPoint.length() == 0) {
            this.showRewardedAd();
        }
        else {
            adService.showRewardedAd(adPoint);
        }
    }

    void onPause() {
        adService.onPause();
    }

    void onResume() {
        adService.onResume();
    }

    void onDestroy() {
        adService.onDestroy();
    }

    @Override
    public void onRegisteredForAds() {
        if (adsListener != null) {
            adsListener.onRegisteredForAds();
        }
    }

    @Override
    public void onRegisteredForRewardedAds() {
        if(rewardedAdsListener != null) {
            rewardedAdsListener.onRegisteredForAds();
        }
    }

    @Override
    public void onFailedToRegisterForAds(String errorReason) {
        if (adsListener != null) {
            adsListener.onFailedToRegisterForAds(errorReason);
        }
    }

    @Override
    public void onFailedToRegisterForRewardedAds(String errorReason) {
        if(rewardedAdsListener != null) {
            rewardedAdsListener.onFailedToRegisterForAds(errorReason);
        }
    }

    @Override
    public void onAdOpened() {
        if (adsListener != null) {
            adsListener.onAdOpened();
        }
    }

    @Override
    public void onAdFailedToOpen() {
        if (adsListener != null) {
            adsListener.onAdFailedToOpen();
        }
    }

    @Override
    public void onAdClosed() {
        if (adsListener != null) {
            adsListener.onAdClosed();
        }
    }

    @Override
    public void onRewardedAdOpened() {
        if(rewardedAdsListener != null) {
            rewardedAdsListener.onAdOpened();
        }
    }

    @Override
    public void onRewardedAdFailedToOpen() {
        if(rewardedAdsListener != null) {
            rewardedAdsListener.onAdFailedToOpen();
        }
    }

    @Override
    public void onRewardedAdClosed(boolean completed) {
        if(rewardedAdsListener != null) {
            rewardedAdsListener.onAdClosed(completed);
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
                new EngageListener() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        listener.onSuccess(result);
                    }
                    
                    @Override
                    public void onFailure(Throwable t) {
                        listener.onFailure(t);
                    }
                });
    }
}
