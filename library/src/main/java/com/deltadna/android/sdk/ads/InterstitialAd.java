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

import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.Engagement;
import com.deltadna.android.sdk.ads.listeners.InterstitialAdsListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class for creating and showing an interstitial ad.
 * <p>
 * The ad can be created through one of the static {@code create} helpers,
 * from an {@link Engagement} as well as without one.
 * <p>
 * The ad can be shown through {@link #show()}.
 * <p>
 * {@link DDNASmartAds} must be registered for ads beforehand.
 */
public final class InterstitialAd {
    
    private static final String TAG = BuildConfig.LOG_TAG
            + ' '
            + InterstitialAd.class.getSimpleName();
    private static final String AD_SHOW_POINT = "adShowPoint";
    
    /**
     * Parameters from the Engage response if the ad was created from a
     * successful {@link Engagement}, else {@code null}.
     */
    @Nullable
    public final JSONObject params;
    @Nullable
    private final InterstitialAdsListener listener;
    
    private final DDNASmartAds smartAds = DDNASmartAds.instance();
    
    private InterstitialAd(
            @Nullable JSONObject params,
            @Nullable final InterstitialAdsListener listener) {
        
        this.params = params;
        this.listener = listener;
    }
    
    /**
     * Gets whether an ad is available to be shown.
     *
     * @return {@code true} when an ad is available, else {@code false}
     */
    public boolean isReady() {
        return (smartAds.getAds() != null
                && smartAds.getAds().isInterstitialAdAvailable());
    }
    
    /**
     * Shows an ad, if one is available.
     *
     * @return this instance
     */
    public InterstitialAd show() {
        final Ads ads = smartAds.getAds();
        if (ads != null) {
            ads.setInterstitialAdsListener(listener);
            ads.showInterstitialAd(null);
        }
        
        return this;
    }
    
    /**
     * Creates an interstitial ad.
     *
     * @return the interstitial ad
     */
    public static InterstitialAd create() {
        return create((InterstitialAdsListener) null);
    }
    
    /**
     * Creates an interstitial ad.
     *
     * @param listener  the listener for events within the ad lifecycle, may be
     *                  {@code null}
     *
     * @return the interstitial ad
     */
    public static InterstitialAd create(
            @Nullable InterstitialAdsListener listener) {
        
        return new InterstitialAd(null, listener);
    }
    
    /**
     * Creates an interstitial ad from an Engage,ent once it has been populated
     * with response data after a successful request.
     * <p>
     * {@code null} may be returned in case the Engagement was not set-up to
     * show an interstitial ad.
     *
     * @param engagement the Engagement with response data
     *
     * @return  the interstitial ad created from {@code engagement}, else
     *          {@code null}
     */
    @Nullable
    public static InterstitialAd create(Engagement engagement) {
        return create(engagement, null);
    }
    
    /**
     * Creates an Interstitial Ad from an Engagement once it has been populated
     * with response data after a successful request.
     * <p>
     * {@code null} may be returned in case the Engagement was not set-up to
     * show an interstitial ad.
     *
     * @param engagement    the Engagement with response data
     * @param listener      the listener for events within the ad lifecycle,
     *                      may be {@code null}
     *
     * @return  the interstitial ad created from {@code engagement}, else
     *          {@code null}
     */
    @Nullable
    public static InterstitialAd create(
            Engagement engagement,
            @Nullable InterstitialAdsListener listener) {
        
        if (engagement.isSuccessful() && engagement.getJson() != null) {
            if (engagement.getJson().has("parameters")) {
                try {
                    final JSONObject params = engagement.getJson()
                            .getJSONObject("parameters");
                    
                    if (    params.has(AD_SHOW_POINT)
                            && !params.getBoolean(AD_SHOW_POINT)) {
                        return null;
                    }
                    
                    return new InterstitialAd(params, listener);
                } catch (JSONException e) {
                    Log.w(TAG, "Failed to get parameters from " + engagement, e);
                }
            }
        }
        
        return new InterstitialAd(null, listener);
    }
}
