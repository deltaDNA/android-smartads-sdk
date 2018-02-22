/*
 * Copyright (c) 2018 deltaDNA Ltd. All rights reserved.
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

import com.deltadna.android.sdk.DDNA;
import com.deltadna.android.sdk.Engagement;
import com.deltadna.android.sdk.Params;
import com.deltadna.android.sdk.listeners.EngageListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * {@inheritDoc}
 */
public final class EngageFactory extends com.deltadna.android.sdk.EngageFactory {
    
    private static final SimpleDateFormat TIMESTAMP_FORMAT;
    static {
        final SimpleDateFormat format = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        TIMESTAMP_FORMAT = format;
    }
    
    private final Ads ads;
    
    EngageFactory(DDNA analytics, Ads ads) {
        super(analytics);
        
        this.ads = ads;
    }
    
    /**
     * Requests an {@link InterstitialAd} at {@code decisionPoint}.
     *
     * @param decisionPoint the decision point
     * @param callback      the callback for completion notification
     *
     * @throws IllegalArgumentException if the {@code decisionPoint} is null or
     *                                  empty
     */
    public void requestInterstitialAd(
            String decisionPoint,
            Callback<InterstitialAd> callback) {
        
        requestInterstitialAd(decisionPoint, null, callback);
    }
    
    /**
     * Requests an {@link InterstitialAd} at {@code decisionPoint} with
     * {@code parameters}.
     *
     * @param decisionPoint the decision point
     * @param parameters    the parameters for the request
     * @param callback      the callback for completion notification
     *
     * @throws IllegalArgumentException if the {@code decisionPoint} is null or
     *                                  empty
     */
    public void requestInterstitialAd(
            String decisionPoint,
            @Nullable Params parameters,
            final Callback<InterstitialAd> callback) {
        
        final Engagement engagement = build(decisionPoint, parameters)
                .putParam("ddnaAdSessionCount", ads.getSessionCount(decisionPoint))
                .putParam("ddnaAdDailyCount", ads.getDailyCount(decisionPoint));
        final Date lastShown = ads.getLastShown(decisionPoint);
        if (lastShown != null) {
            engagement.putParam(
                    "ddnaAdLastShownTime", TIMESTAMP_FORMAT.format(lastShown));
        }
        
        analytics.requestEngagement(
                engagement,
                new EngageListener<Engagement>() {
                    @Override
                    public void onCompleted(Engagement engagement) {
                        callback.onCompleted(InterstitialAd.createUnchecked(
                                engagement,
                                null));
                    }
                    
                    @Override
                    public void onError(Throwable t) {
                        Log.w(  BuildConfig.LOG_TAG,
                                "Creating interstitial ad despite failed Engage request",
                                t);
                        callback.onCompleted(InterstitialAd.createUnchecked(
                                engagement,
                                null));
                    }
                });
    }
    
    /**
     * Requests a {@link RewardedAd} at {@code decisionPoint}.
     *
     * @param decisionPoint the decision point
     * @param callback      the callback for completion notification
     *
     * @throws IllegalArgumentException if the {@code decisionPoint} is null or
     *                                  empty
     */
    public void requestRewardedAd(
            String decisionPoint,
            Callback<RewardedAd> callback) {
        
        requestRewardedAd(decisionPoint, null, callback);
    }
    
    /**
     * Requests a {@link RewardedAd} at {@code decisionPoint} with
     * {@code parameters}.
     *
     * @param decisionPoint the decision point
     * @param parameters    the parameters for the request
     * @param callback      the callback for completion notification
     *
     * @throws IllegalArgumentException if the {@code decisionPoint} is null or
     *                                  empty
     */
    public void requestRewardedAd(
            String decisionPoint,
            @Nullable Params parameters,
            final Callback<RewardedAd> callback) {
        
        final Engagement engagement = build(decisionPoint, parameters)
                .putParam("ddnaAdSessionCount", ads.getSessionCount(decisionPoint))
                .putParam("ddnaAdDailyCount", ads.getDailyCount(decisionPoint));
        final Date lastShown = ads.getLastShown(decisionPoint);
        if (lastShown != null) {
            engagement.putParam(
                    "ddnaAdLastShownTime", TIMESTAMP_FORMAT.format(lastShown));
        }
        
        analytics.requestEngagement(
                engagement,
                new EngageListener<Engagement>() {
                    @Override
                    public void onCompleted(Engagement engagement) {
                        callback.onCompleted(RewardedAd.createUnchecked(
                                engagement,
                                null));
                    }
                    
                    @Override
                    public void onError(Throwable t) {
                        Log.w(  BuildConfig.LOG_TAG,
                                "Creating rewarded ad despite failed Engage request",
                                t);
                        callback.onCompleted(RewardedAd.createUnchecked(
                                engagement,
                                null));
                    }
                });
    }
}
