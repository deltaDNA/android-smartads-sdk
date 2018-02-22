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

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.Engagement;
import com.deltadna.android.sdk.ads.listeners.RewardedAdsListener;

import org.json.JSONObject;

/**
 * Class for creating and showing a rewarded ad.
 * <p>
 * The ad can be created through one of the static {@code create} helpers,
 * from an {@link Engagement}.
 * <p>
 * The ad can be shown through {@link #show()}.
 */
public final class RewardedAd extends Ad {
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    
    @Nullable
    RewardedAdsListener listener;
    
    private boolean waitingToLoad;
    
    private RewardedAd(
            @Nullable Engagement engagement,
            @Nullable RewardedAdsListener listener) {
        
        super(engagement);
        
        this.listener = listener;
        
        DDNASmartAds.instance().getAds().registerRewardedAd(this);
    }
    
    void onLoaded() {
        final Ads ads = DDNASmartAds.instance().getAds();
        
        if (ads.isRewardedAdAllowed(engagement, true)) {
            waitingToLoad = false;
            
            if (listener != null) listener.onLoaded(this);
        } else if (!waitingToLoad) {
            waitingToLoad = true;
            
            handler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (waitingToLoad) {
                                waitingToLoad = false;
                                
                                if (    ads.hasLoadedRewardedAd()
                                        && listener != null) {
                                    listener.onLoaded(RewardedAd.this);
                                }
                            }
                        }
                    },
                    ads.timeUntilRewardedAdAllowed(engagement) * 1000L);
        }
    }
    
    void onOpened(String decisionPoint) {
        if (    engagement != null
                && !engagement.getDecisionPoint().equals(decisionPoint)
                && !waitingToLoad
                && listener != null) {
            listener.onExpired(this);
        }
    }
    
    public RewardedAd setListener(@Nullable RewardedAdsListener listener) {
        this.listener = listener;
        return this;
    }
    
    @Override
    public boolean isReady() {
        final Ads ads = DDNASmartAds.instance().getAds();
        
        if (engagement == null) {
            return ads.hasLoadedRewardedAd();
        } else {
            return (ads.isRewardedAdAllowed(engagement, true)
                    && ads.hasLoadedRewardedAd());
        }
    }
    
    @Override
    public RewardedAd show() {
        final Ads ads = DDNASmartAds.instance().getAds();
        
        // this is the instance that will receive callbacks
        ads.setRewardedAd(this);
        
        if (engagement == null) {
            Log.w(BuildConfig.LOG_TAG, "Prefer showing ads with Engagements");
            ads.showRewardedAd(null);
        } else {
            ads.showRewardedAd(engagement);
        }
        
        return this;
    }
    
    /**
     * Gets the type of the reward returned by this ad.
     *
     * @return the type of the reward, or {@code null} if no reward was given
     */
    @Nullable
    public String getRewardType() {
        final JSONObject params = getParameters();
        return (params != null) ? params.optString("ddnaAdRewardType", null) : null;
    }
    
    /**
     * Gets the amount of reward returned by this ad.
     *
     * @return the amount of reward
     */
    public int getRewardAmount() {
        final JSONObject params = getParameters();
        return (params != null) ? params.optInt("ddnaAdRewardAmount", 0) : 0;
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
        if (ads == null || !ads.isRewardedAdAllowed(engagement, false))  {
            return null;
        } else {
            return new RewardedAd(engagement, listener);
        }
    }
    
    static RewardedAd createUnchecked(
            @Nullable Engagement engagement,
            @Nullable RewardedAdsListener listener) {
        
        if (engagement != null && engagement.getJson() == null) {
            return new RewardedAd(null, listener);
        } else {
            return new RewardedAd(engagement, listener);
        }
    }
}
