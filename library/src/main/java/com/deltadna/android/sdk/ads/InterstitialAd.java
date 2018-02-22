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

/**
 * Class for creating and showing an interstitial ad.
 * <p>
 * The ad can be created through one of the static {@code create} helpers,
 * from an {@link Engagement}.
 * <p>
 * The ad can be shown through {@link #show()}.
 */
public final class InterstitialAd extends Ad {
    
    @Nullable
    InterstitialAdsListener listener;
    
    private InterstitialAd(
            @Nullable Engagement engagement,
            @Nullable final InterstitialAdsListener listener) {
        
        super(engagement);
        
        this.listener = listener;
    }
    
    public InterstitialAd setListener(@Nullable InterstitialAdsListener listener) {
        this.listener = listener;
        return this;
    }
    
    @Override
    public boolean isReady() {
        final Ads ads = DDNASmartAds.instance().getAds();
        
        if (engagement == null) {
            return ads.hasLoadedInterstitialAd();
        } else {
            return (ads.isInterstitialAdAllowed(engagement, true)
                    && ads.hasLoadedInterstitialAd());
        }
    }
    
    @Override
    public InterstitialAd show() {
        final Ads ads = DDNASmartAds.instance().getAds();
        
        // this is the instance that will receive callbacks
        ads.setInterstitialAd(this);
        
        if (engagement == null) {
            Log.w(BuildConfig.LOG_TAG, "Prefer showing ads with Engagements");
            ads.showInterstitialAd(null);
        } else {
            ads.showInterstitialAd(engagement);
        }
        
        return this;
    }
    
    /**
     * Creates an interstitial ad.
     * <p>
     * {@code null} may be returned in case the ad is now allowed to show (ie
     * too many ads shown during the session).
     *
     * @return the interstitial ad, or {@code null}
     */
    @Nullable
    public static InterstitialAd create() {
        return create((InterstitialAdsListener) null);
    }
    
    /**
     * Creates an interstitial ad.
     * <p>
     * {@code null} may be returned in case the ad is now allowed to show (ie
     * too many ads shown during the session).
     *
     * @param listener  the listener for events within the ad lifecycle, may be
     *                  {@code null}
     *
     * @return the interstitial ad, or {@code null}
     */
    @Nullable
    public static InterstitialAd create(
            @Nullable InterstitialAdsListener listener) {
        
        return create(null, listener);
    }
    
    /**
     * Creates an interstitial ad from an Engagement once it has been populated
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
     * Creates an interstitial ad from an Engagement once it has been populated
     * with response data after a successful request.
     * <p>
     * {@code null} may be returned in case the Engagement was not set-up to
     * show an ad, or the ad is now allowed to show (ie too many ads shown
     * during the session).
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
            @Nullable Engagement engagement,
            @Nullable InterstitialAdsListener listener) {
        
        final Ads ads = DDNASmartAds.instance().getAds();
        if (ads == null || !ads.isInterstitialAdAllowed(engagement, false))  {
            return null;
        } else {
            return new InterstitialAd(engagement, listener);
        }
    }
    
    static InterstitialAd createUnchecked(
            @Nullable Engagement engagement,
            @Nullable InterstitialAdsListener listener) {
        
        if (engagement != null && engagement.getJson() == null) {
            return new InterstitialAd(null, listener);
        } else {
            return new InterstitialAd(engagement, listener);
        }
    }
}
