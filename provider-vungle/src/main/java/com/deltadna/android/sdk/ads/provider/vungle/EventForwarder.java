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

package com.deltadna.android.sdk.ads.provider.vungle;

import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdClosedResult;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.vungle.publisher.EventListener;

import java.util.Locale;

final class EventForwarder implements EventListener {
    
    private final MediationAdapter adapter;
    @Nullable
    private MediationListener listener;
    
    @Nullable
    private Boolean available;
    private boolean showing;
    
    EventForwarder(
            MediationAdapter adapter,
            @Nullable MediationListener listener) {
        
        this.adapter = adapter;
        this.listener = listener;
    }
    
    @Override
    public void onAdPlayableChanged(final boolean isAdPlayable) {
        Log.d(BuildConfig.LOG_TAG, "Ad playable changed: " + isAdPlayable);
        
        // may be called while an ad is being played
        if (showing) return;
        
        available = isAdPlayable;
        
        if (listener != null) {
            if (available) {
                listener.onAdLoaded(adapter);
            } else {
                listener.onAdFailedToLoad(
                        adapter,
                        AdRequestResult.NoFill,
                        "No fill");
            }
        }
    }
    
    @Override
    public void onAdUnavailable(final String reason) {
        Log.w(BuildConfig.LOG_TAG, "Ad unavailable: " + reason);
        
        if (listener != null) listener.onAdFailedToShow(
                adapter,
                AdClosedResult.EXPIRED);
    }
    
    @Override
    public void onAdStart() {
        Log.d(BuildConfig.LOG_TAG, "Ad start");
        
        showing = true;
        
        if (listener != null) listener.onAdShowing(adapter);
    }
    
    @Override
    public void onAdEnd(
            boolean wasSuccessfulView,
            boolean wasCallToActionClicked) {
        
        Log.d(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Ad end: %s/%s",
                wasSuccessfulView,
                wasCallToActionClicked));
        
        if (listener != null) {
            /*
             * Avoids odd interaction between the adapter and agent when Vungle
             * invokes this callback out of sequence sometimes
             */
            final MediationListener listener = this.listener;
            this.listener = null;
            
            if (wasCallToActionClicked) {
                listener.onAdClicked(adapter);
            }
            listener.onAdClosed(adapter, wasSuccessfulView);
        }
    }
    
    @Override
    public void onVideoView(
            boolean isCompletedView,
            int watchedMillis,
            int videoMillis) {
        
        // deprecated
    }
    
    void requestPerformed(MediationListener listener) {
        this.listener = listener;
        
        if (available != null) {
            if (available) {
                listener.onAdLoaded(adapter);
            } else {
                listener.onAdFailedToLoad(
                        adapter,
                        AdRequestResult.NoFill,
                        "No fill");
            }
        }
    }
}
