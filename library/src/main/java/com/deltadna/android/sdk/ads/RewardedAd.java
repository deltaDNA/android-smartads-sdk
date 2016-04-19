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
import com.deltadna.android.sdk.ads.listeners.RewardedAdsListener;

import org.json.JSONException;
import org.json.JSONObject;

public final class RewardedAd {
    
    private static final String TAG = BuildConfig.LOG_TAG
            + ' '
            + RewardedAd.class.getSimpleName();
    private static final String AD_SHOW_POINT = "adShowPoint";
    
    /**
     * Parameters from the Engage response if the ad was created from a
     * successful {@link Engagement}, else {@code null}.
     */
    @Nullable
    public final JSONObject params;
    
    private final DDNASmartAds smartAds = DDNASmartAds.instance();
    
    private RewardedAd(
            @Nullable JSONObject params,
            @Nullable RewardedAdsListener listener) {
        
        this.params = params;
        
        if (smartAds.getAds() != null) {
            smartAds.getAds().setRewardedAdsListener(listener);
        }
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
        if (smartAds.getAds() != null) {
            smartAds.getAds().showRewardedAd(null);
        }
        
        return this;
    }
    
    /**
     * Creates a rewarded ad.
     *
     * @return the rewarded ad
     */
    public static RewardedAd create() {
        return create((RewardedAdsListener) null);
    }
    
    /**
     * Creates a rewarded ad.
     *
     * @param listener  the listener for events within the ad lifecycle, may be
     *                  {@code null}
     *
     * @return the rewarded ad
     */
    public static RewardedAd create(RewardedAdsListener listener) {
        return new RewardedAd(null, listener);
    }
    
    /**
     * Creates a rewarded ad from an Engage,ent once it has been populated
     * with response data after a successful request.
     * <p>
     * {@code null} may be returned in case the Engagement was not set-up to
     * show a rewarded ad.
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
     * Creates an rewarded ad from an Engagement once it has been populated
     * with response data after a successful request.
     * <p>
     * {@code null} may be returned in case the Engagement was not set-up to
     * show a rewarded ad.
     *
     * @param engagement    the Engagement with response data
     * @param listener      the listener for events within the ad lifecycle,
     *                      may be {@code null}
     *
     * @return  the rewarded ad created from {@code engagement}, else
     *          {@code null}
     */
    @Nullable
    public static RewardedAd create(
            Engagement engagement,
            @Nullable RewardedAdsListener listener) {
        
        if (engagement.isSuccessful() && engagement.getJson() != null) {
            if (engagement.getJson().has("parameters")) {
                try {
                    final JSONObject params = engagement.getJson()
                            .getJSONObject("parameters");
                    
                    if (    params.has(AD_SHOW_POINT)
                            && !params.getBoolean(AD_SHOW_POINT)) {
                        return null;
                    }
                    
                    return new RewardedAd(params, listener);
                } catch (JSONException e) {
                    Log.w(TAG, "Failed to get parameters from " + engagement, e);
                }
            }
        }
        
        return new RewardedAd(null, listener);
    }
}
