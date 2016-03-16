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

import com.deltadna.android.sdk.ads.core.listeners.AdsListener;
import com.deltadna.android.sdk.ads.core.listeners.RewardedAdsListener;

/**
 * Singleton class for accessing the deltaDNA SmartAds SDK.
 * <p>
 * An instance should be retrieved by calling {@link #instance()}.
 * {@link com.deltadna.android.sdk.DDNA} should be initialised and started
 * before registering for ads through {@link #registerForAds(Activity)}. At
 * the point where you would like to display an ad, use one of the
 * {@link #showInterstitialAd()} or {@link #showRewardedAd()} methods.
 * <p>
 * In order to listen to the lifecycle of ads
 * {@link #setAdsListener(AdsListener)} or
 * {@link #setRewardedAdsListener(RewardedAdsListener)} can be used to set a
 * listener.
 */
public final class DDNASmartAds {
    
    private static DDNASmartAds instance = null;
    
    @Nullable
    private Ads ads;
    
    @Nullable
    private AdsListener adsListener;
    @Nullable
    private RewardedAdsListener rewardedAdsListener;
    
    public static synchronized DDNASmartAds instance() {
        if (instance == null) {
            instance = new DDNASmartAds();
        }
        
        return instance;
    }
    
    /**
     * Registers for ads.
     *
     * @param activity the activity to register with ads
     */
    public void registerForAds(Activity activity) {
        try {
            if (ads == null) {
                ads = new Ads(activity);
                
                if (adsListener != null) {
                    ads.setAdsListener(adsListener);
                }
                if (rewardedAdsListener != null) {
                    ads.setRewardedAdsListener(rewardedAdsListener);
                }
                
                ads.registerForAds();
            } else {
                Log.w(BuildConfig.LOG_TAG, "Already registered for ads");
            }
        } catch (Exception e) {
            Log.e(BuildConfig.LOG_TAG, "Error registering for ads", e);
            
            if (adsListener != null) {
                adsListener.onFailedToRegisterForAds(
                        "Exception not handled " + e);
            }
            if (rewardedAdsListener != null) {
                rewardedAdsListener.onFailedToRegisterForAds(
                        "Exception not handled " + e);
            }
        }
    }
    
    /**
     * Checks whether an interstitial ad is available to be shown.
     *
     * @return {@code true} if an ad is available, else {@code false}
     */
    public boolean isInterstitialAdAvailable() {
        return ads != null && ads.isInterstitialAdAvailable();
    }
    
    /**
     * Checks whether a rewarded ad is available to be shown.
     *
     * @return {@code true} if an ad is available, else {@code false}
     */
    public boolean isRewardedAdAvailable() {
        return ads != null && ads.isRewardedAdAvailable();
    }
    
    /**
     * Shows an interstitial ad, if one is available.
     */
    public void showInterstitialAd() {
        showInterstitialAd(null);
    }
    
    /**
     * Shows an interstitial ad, if one is available and if the user is included
     * in the set of users who should receive ads for the specified decision
     * point.
     *
     * @param decisionPoint the decision point to show ads for, may be
     *                      {@code null}
     */
    public void showInterstitialAd(@Nullable String decisionPoint) {
        try {
            if (ads != null) {
                ads.showAd(decisionPoint);
            } else {
                Log.w(BuildConfig.LOG_TAG, "Not registered for ads");
                
                if (adsListener != null) {
                    adsListener.onAdFailedToOpen();
                }
            }
        } catch (Exception e) {
            Log.e(  BuildConfig.LOG_TAG,
                    "Error showing interstitial ad for " + decisionPoint,
                    e);
            
            if (adsListener != null) {
                adsListener.onAdFailedToOpen();
            }
        }
    }
    
    /**
     * Shows a rewarded ad, if one is available.
     */
    public void showRewardedAd() {
        showRewardedAd(null);
    }
    
    /**
     * Shows a rewarded ad, if one is available and if the user is included
     * in the set of users who should receive ads for the specified decision
     * point.
     *
     * @param decisionPoint the decision point to show ads for, may be
     *                      {@code null}
     */
    public void showRewardedAd(@Nullable String decisionPoint) {
        try {
            if (ads != null) {
                ads.showRewardedAd(decisionPoint);
            } else {
                Log.w(BuildConfig.LOG_TAG, "Not registered for ads");
                
                if (rewardedAdsListener != null) {
                    rewardedAdsListener.onAdFailedToOpen();
                }
            }
        } catch (Exception e) {
            Log.e(  BuildConfig.LOG_TAG,
                    "Error showing rewarded ad for " + decisionPoint,
                    e);
            
            if (rewardedAdsListener != null) {
                rewardedAdsListener.onAdFailedToOpen();
            }
        }
    }
    
    /**
     * Sets a listener for responding to events within the interstitial ad
     * lifecycle.
     *
     * @param listener the listener, may be {@code null}
     */
    public void setAdsListener(@Nullable AdsListener listener) {
        adsListener = listener;
        
        if (ads != null) {
            ads.setAdsListener(listener);
        }
    }
    
    /**
     * Sets a listener for responding to events within the rewarded ad
     * lifecycle.
     *
     * @param listener the listener, may be {@code null}
     */
    public void setRewardedAdsListener(@Nullable RewardedAdsListener listener) {
        rewardedAdsListener = listener;
        
        if (ads != null) {
            ads.setRewardedAdsListener(listener);
        }
    }
    
    public void onPause() {
        if (ads != null) {
            ads.onPause();
        }
    }
    
    public void onResume() {
        if (ads != null) {
            ads.onResume();
        }
    }
    
    public void onDestroy() {
        if (ads != null) {
            ads.onDestroy();
        }
    }
    
    private DDNASmartAds() {}
}
