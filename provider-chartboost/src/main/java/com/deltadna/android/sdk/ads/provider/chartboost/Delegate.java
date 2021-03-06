/*
 * Copyright (c) 2017 deltaDNA Ltd. All rights reserved.
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

package com.deltadna.android.sdk.ads.provider.chartboost;

import android.support.annotation.Nullable;
import android.util.Log;

import com.chartboost.sdk.ChartboostDelegate;
import com.chartboost.sdk.Model.CBError;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.AdShowResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;

import java.util.Locale;

final class Delegate extends ChartboostDelegate {
    
    @Nullable
    private InterstitialDelegate interstitial;
    @Nullable
    private RewardedDelegate rewarded;
    
    void setInterstitial(
            @Nullable MediationListener listener,
            @Nullable MediationAdapter adapter) {
        
        if (listener == null || adapter == null) {
            interstitial = null;
        } else {
            interstitial = new InterstitialDelegate(listener, adapter);
        }
    }
    
    void setRewarded(
            @Nullable MediationListener listener,
            @Nullable MediationAdapter adapter) {
        
        if (listener == null || adapter == null) {
            rewarded = null;
        } else {
            rewarded = new RewardedDelegate(listener, adapter);
        }
    }
    
    @Override
    public void didCacheInterstitial(String location) {
        Log.d(BuildConfig.LOG_TAG, "Did cache interstitial: " + location);
        
        if (interstitial != null)
            interstitial.didCacheInterstitial(location);
    }
    
    @Override
    public void didFailToLoadInterstitial(
            String location,
            CBError.CBImpressionError error) {
        
        Log.w(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Did fail to load/show interstitial: %s/%s",
                location,
                error.name()));
        
        if (interstitial != null)
            interstitial.didFailToLoadInterstitial(location, error);
    }
    
    @Override
    public void didDisplayInterstitial(String location) {
        Log.d(BuildConfig.LOG_TAG, "Did display interstitial: " + location);
        
        if (interstitial != null)
            interstitial.didDisplayInterstitial(location);
    }
    
    @Override
    public void didClickInterstitial(String location) {
        Log.d(BuildConfig.LOG_TAG, "Did click interstitial: " + location);
        
        if (interstitial != null)
            interstitial.didClickInterstitial(location);
    }
    
    @Override
    public void didCloseInterstitial(String location) {
        Log.d(BuildConfig.LOG_TAG, "Did close interstitial: " + location);
        
        if (interstitial != null)
            interstitial.didCloseInterstitial(location);
    }
    
    @Override
    public void didCacheRewardedVideo(String location) {
        Log.d(BuildConfig.LOG_TAG, "Did cache rewarded: " + location);
        
        if (rewarded != null)
            rewarded.didCacheRewardedVideo(location);
    }
    
    @Override
    public void didFailToLoadRewardedVideo(
            String location,
            CBError.CBImpressionError error) {
        
        Log.w(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Did fail to load/show rewarded: %s/%s",
                location,
                error.name()));
        
        if (rewarded != null)
            rewarded.didFailToLoadRewardedVideo(location, error);
    }
    
    @Override
    public void didDisplayRewardedVideo(String location) {
        Log.d(BuildConfig.LOG_TAG, "Did display rewarded: " + location);
        
        if (rewarded != null)
            rewarded.didDisplayRewardedVideo(location);
    }
    
    @Override
    public void didClickRewardedVideo(String location) {
        Log.d(BuildConfig.LOG_TAG, "Did click rewarded: " + location);
        
        if (rewarded != null)
            rewarded.didClickRewardedVideo(location);
    }
    
    @Override
    public void didCompleteRewardedVideo(String location, int reward) {
        Log.d(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Did complete rewarded: %s/%d",
                location,
                reward));
        
        if (rewarded != null)
            rewarded.didCompleteRewardedVideo(location, reward);
    }
    
    @Override
    public void didCloseRewardedVideo(String location) {
        Log.d(BuildConfig.LOG_TAG, "Did close rewarded: " + location);
        
        if (rewarded != null)
            rewarded.didCloseRewardedVideo(location);
    }
    
    private static void handleLoadFailure(
            CBError.CBImpressionError error,
            MediationListener listener,
            MediationAdapter adapter) {
        
        handleFailure(error, listener, adapter, true);
    }
    
    private static void handleShowFailure(
            CBError.CBImpressionError error,
            MediationListener listener,
            MediationAdapter adapter) {
        
        handleFailure(error, listener, adapter, false);
    }
    
    private static void handleFailure(
            CBError.CBImpressionError error,
            MediationListener listener,
            MediationAdapter adapter,
            boolean loadFailure) {
        
        final AdRequestResult loadResult;
        AdShowResult showResult = AdShowResult.ERROR;
        switch (error) {
            case NO_AD_FOUND:
                loadResult = AdRequestResult.NoFill;
                showResult = AdShowResult.EXPIRED;
                break;
            
            case INTERNET_UNAVAILABLE:
            case TOO_MANY_CONNECTIONS:
            case NETWORK_FAILURE:
                loadResult = AdRequestResult.Network;
                break;
            
            case WRONG_ORIENTATION:
            case FIRST_SESSION_INTERSTITIALS_DISABLED:
            case INVALID_LOCATION:
            case ACTIVITY_MISSING_IN_MANIFEST:
            case END_POINT_DISABLED:
            case HARDWARE_ACCELERATION_DISABLED:
                loadResult = AdRequestResult.Configuration;
                break;
            
            default:
                loadResult = AdRequestResult.Error;
        }
        
        if (loadFailure) {
            listener.onAdFailedToLoad(adapter, loadResult, error.name());
        } else {
            listener.onAdFailedToShow(adapter, showResult);
        }
    }
    
    private static final class InterstitialDelegate extends ChartboostDelegate {
        
        private final MediationListener listener;
        private final MediationAdapter adapter;
        
        // can be called more than once, but we only care about the first call
        private boolean notifiedLoadedOrNot;
        
        InterstitialDelegate(
                MediationListener listener,
                MediationAdapter adapter) {
            
            this.listener = listener;
            this.adapter = adapter;
        }
        
        @Override
        public void didCacheInterstitial(String location) {
            if (!notifiedLoadedOrNot) {
                listener.onAdLoaded(adapter);
                notifiedLoadedOrNot = true;
            }
        }
        
        @Override
        public void didFailToLoadInterstitial(
                String location,
                CBError.CBImpressionError error) {
            
            if (!notifiedLoadedOrNot) {
                Delegate.handleLoadFailure(error, listener, adapter);
                notifiedLoadedOrNot = true;
            } else {
                Delegate.handleShowFailure(error, listener, adapter);
            }
        }
        
        @Override
        public void didDisplayInterstitial(String location) {
            listener.onAdShowing(adapter);
        }
        
        @Override
        public void didClickInterstitial(String location) {
            listener.onAdClicked(adapter);
        }
        
        @Override
        public void didCloseInterstitial(String location) {
            listener.onAdClosed(adapter, true);
        }
    }
    
    private static final class RewardedDelegate extends ChartboostDelegate {
        
        private final MediationListener listener;
        private final MediationAdapter adapter;
        
        private boolean complete;
        
        // can be called more than once, but we only care about the first call
        private boolean notifiedLoadedOrNot;
        
        RewardedDelegate(
                MediationListener listener,
                MediationAdapter adapter) {
            
            this.listener = listener;
            this.adapter = adapter;
        }
        
        @Override
        public void didCacheRewardedVideo(String location) {
            if (!notifiedLoadedOrNot) {
                listener.onAdLoaded(adapter);
                notifiedLoadedOrNot = true;
            }
        }
        
        @Override
        public void didFailToLoadRewardedVideo(
                String location,
                CBError.CBImpressionError error) {
            
            if (!notifiedLoadedOrNot) {
                Delegate.handleLoadFailure(error, listener, adapter);
                notifiedLoadedOrNot = true;
            } else {
                Delegate.handleShowFailure(error, listener, adapter);
            }
        }
        
        @Override
        public void didDisplayRewardedVideo(String location) {
            listener.onAdShowing(adapter);
        }
        
        @Override
        public void didClickRewardedVideo(String location) {
            listener.onAdClicked(adapter);
        }
        
        @Override
        public void didCompleteRewardedVideo(String location, int reward) {
            complete = true;
        }
        
        @Override
        public void didCloseRewardedVideo(String location) {
            listener.onAdClosed(adapter, complete);
        }
    }
}
