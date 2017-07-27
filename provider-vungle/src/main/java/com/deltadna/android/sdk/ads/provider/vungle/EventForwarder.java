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

import android.support.annotation.NonNull;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdClosedResult;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.vungle.publisher.VungleAdEventListener;
import com.vungle.publisher.VunglePub;

import java.util.Locale;

final class EventForwarder implements VungleAdEventListener {
    
    private final VunglePub vunglePub;
    private final String placementId;
    private final MediationAdapter adapter;
    private MediationListener listener;
    
    private boolean showing;
    
    EventForwarder(
            VunglePub vunglePub,
            String placementId,
            MediationAdapter adapter,
            MediationListener listener) {
        
        this.vunglePub = vunglePub;
        this.placementId = placementId;
        this.adapter = adapter;
        this.listener = listener;
    }
    
    @Override
    public void onAdAvailabilityUpdate(
            @NonNull String placementId,
            boolean isAdAvailable) {
        
        Log.d(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Ad availability update: %s/%s",
                placementId,
                isAdAvailable));
        
        if (!isSamePlacement(placementId)) return;
        
        if (showing) return;
        
        if (isAdAvailable) {
            /*
             * This isn't great as it creates a cyclic dependency, however
             * Vungle will notify us after an ad show just finished that an
             * ad is available despite this not being true. The adapter takes
             * care of this scenario by polling for the ready state. This does
             * not happen with test ads.
             */
            if (vunglePub.isAdPlayable(placementId)) {
                listener.onAdLoaded(adapter);
            } else {
                Log.w(  BuildConfig.LOG_TAG,
                        "Ad not actually playable for: " + placementId);
            }
        } else {
            listener.onAdFailedToLoad(
                    adapter,
                    AdRequestResult.NoFill,
                    "No fill");
        }
    }
    
    @Override
    public void onUnableToPlayAd(@NonNull String placementId, String reason) {
        Log.w(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Unable to play ad: %s/%s",
                placementId,
                reason));
        
        if (!isSamePlacement(placementId)) return;
        
        listener.onAdFailedToShow(adapter, AdClosedResult.EXPIRED);
    }
    
    @Override
    public void onAdStart(@NonNull String placementId) {
        Log.d(BuildConfig.LOG_TAG, "Ad start: " + placementId);
        
        if (!isSamePlacement(placementId)) return;
        
        showing = true;
        
        listener.onAdShowing(adapter);
    }
    
    @Override
    public void onAdEnd(
            @NonNull String placementId,
            boolean wasSuccessfulView,
            boolean wasCallToActionClicked) {
        
        Log.d(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Ad end: %s/%s/%s",
                placementId,
                wasSuccessfulView,
                wasCallToActionClicked));
        
        if (!isSamePlacement(placementId)) return;
        
        showing = false;
        
        if (wasCallToActionClicked) {
            listener.onAdClicked(adapter);
        }
        listener.onAdClosed(adapter, wasSuccessfulView);
    }
    
    private boolean isSamePlacement(String value) {
        return placementId != null && placementId.equals(value);
    }
}
