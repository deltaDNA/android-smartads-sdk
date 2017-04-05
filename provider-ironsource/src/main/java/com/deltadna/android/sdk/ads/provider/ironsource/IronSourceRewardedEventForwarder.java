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

package com.deltadna.android.sdk.ads.provider.ironsource;

import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdClosedResult;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.RewardedVideoListener;

final class IronSourceRewardedEventForwarder implements RewardedVideoListener {
    
    private final MediationAdapter adapter;
    
    @Nullable
    private MediationListener listener;
    @Nullable
    private Boolean available;
    private boolean complete;
    private boolean requestAwaiting;
    
    IronSourceRewardedEventForwarder(MediationAdapter adapter) {
        this.adapter = adapter;
    }
    
    void requestPerformed(final MediationListener l) {
        listener = l;
        complete = false;
        
        if (available != null && listener != null) {
            if (available) {
                Log.d(BuildConfig.LOG_TAG, "On ad loaded");
                listener.onAdLoaded(adapter);
            } else {
                Log.w(BuildConfig.LOG_TAG, "On ad failed to load");
                
                listener.onAdFailedToLoad(
                        adapter,
                        AdRequestResult.NoFill,
                        "Rewarded video not available");
                // we're not expecting any further callbacks
                listener = null;
            }
        } else {
            requestAwaiting = true;
        }
    }
    
    @Override
    public void onRewardedVideoAvailabilityChanged(boolean available) {
        Log.d(  BuildConfig.LOG_TAG,
                "Rewarded video availability changed: " + available);
        
        this.available = available;
        if (requestAwaiting) {
            requestPerformed(listener);
            requestAwaiting = false;
        }
    }
    
    @Override
    public void onRewardedVideoAdShowFailed(IronSourceError error) {
        Log.w(BuildConfig.LOG_TAG, "Rewarded video ad show failed: " + error);
        if (listener != null) listener.onAdFailedToShow(adapter, AdClosedResult.ERROR);
    }
    
    @Override
    public void onRewardedVideoAdOpened() {
        Log.d(BuildConfig.LOG_TAG, "Rewarded video ad opened");
        if (listener != null) listener.onAdShowing(adapter);
    }
    
    @Override
    public void onRewardedVideoAdStarted() {
        Log.d(BuildConfig.LOG_TAG, "Rewarded video ad started");
    }
    
    @Override
    public void onRewardedVideoAdEnded() {
        Log.d(BuildConfig.LOG_TAG, "Rewarded video ad ended");
    }
    
    @Override
    public void onRewardedVideoAdClosed() {
        Log.d(BuildConfig.LOG_TAG, "Rewarded video ad closed");
        
        if (listener != null) {
            listener.onAdClosed(adapter, complete);
            listener = null;
        }
    }
    
    @Override
    public void onRewardedVideoAdRewarded(Placement placement) {
        Log.d(BuildConfig.LOG_TAG, "Rewarded video ad rewarded");
        complete = true;
    }
}
