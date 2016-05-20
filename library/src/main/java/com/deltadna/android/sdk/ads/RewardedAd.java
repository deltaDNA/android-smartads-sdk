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

import com.deltadna.android.sdk.Engagement;
import com.deltadna.android.sdk.ads.listeners.RewardedAdsListener;

import org.json.JSONObject;

/**
 * Class for creating and showing a rewarded ad.
 * <p>
 * The ad can be created through one of the static {@code create} helpers,
 * from an {@link Engagement} as well as without one.
 * <p>
 * The ad can be shown through {@link #show()}.
 * <p>
 * {@link DDNASmartAds} must be registered for ads beforehand.
 */
public final class RewardedAd {
    
    /**
     * Parameters from the Engage response if the ad was created from a
     * successful {@link Engagement}, else {@code null}.
     */
    @Nullable
    public final JSONObject params;
    @Nullable
    private final RewardedAdsListener listener;
    
    private final DDNASmartAds smartAds = DDNASmartAds.instance();
    
    private RewardedAd(
            @Nullable JSONObject params,
            @Nullable RewardedAdsListener listener) {
        
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
                && smartAds.getAds().isRewardedAdAvailable());
    }
    
    /**
     * Shows an ad, if one is available.
     *
     * @return this instance
     */
    public RewardedAd show() {
        final Ads ads = smartAds.getAds();
        if (ads != null) {
            ads.setRewardedAdsListener(listener);
            ads.showRewardedAd(null);
        }
        
        return this;
    }
    
    /**
     * Creates a rewarded ad.
     * <p>
     * {@code null} may be returned in case the Engagement was not set-up to
     * show a rewarded ad.
     *
     * @return the rewarded ad, or {@code null}
     */
    @Nullable
    public static RewardedAd create() {
        return create((RewardedAdsListener) null);
    }
    
    /**
     * Creates a rewarded ad.
     * <p>
     * {@code null} may be returned in case the ad is now allowed to show (ie
     * too many ads shown during the session).
     *
     * @param listener  the listener for events within the ad lifecycle, may be
     *                  {@code null}
     *
     * @return the rewarded ad, or {@code null}
     */
    @Nullable
    public static RewardedAd create(RewardedAdsListener listener) {
        return create(null, listener);
    }
    
    /**
     * Creates a rewarded ad from an Engagement once it has been populated
     * with response data after a successful request.
     * <p>
     * {@code null} may be returned in case the Engagement was not set-up to
     * show an ad, or the ad is now allowed to show (ie too many ads shown
     * during the session).
     *
     * @param engagement the Engagement with response data
     *
     * @return  the rewarded ad created from {@code engagement}, else
     *          {@code null}
     */
    @Nullable
    public static RewardedAd create(Engagement engagement) {
        return create(engagement, null);
    }
    
    /**
     * Creates a rewarded ad from an Engagement once it has been populated
     * with response data after a successful request.
     * <p>
     * {@code null} may be returned in case the Engagement was not set-up to
     * show an ad, or the ad is now allowed to show (ie too many ads shown
     * during the session).
     *
     * @param engagement    the Engagement with response data, may be
     *                      {@code null}
     * @param listener      the listener for events within the ad lifecycle,
     *                      may be {@code null}
     *
     * @return  the rewarded ad created from {@code engagement}, else
     *          {@code null}
     */
    @Nullable
    public static RewardedAd create(
            @Nullable Engagement engagement,
            @Nullable RewardedAdsListener listener) {
        
        final Ads ads = DDNASmartAds.instance().getAds();
        if (ads == null || !ads.isRewardedAdAllowed(engagement))  {
            return null;
        } else {
            return new RewardedAd(
                    (engagement == null || engagement.getJson() == null)
                            ? null
                            : engagement.getJson().optJSONObject("parameters"),
                    listener);
        }
    }
}
