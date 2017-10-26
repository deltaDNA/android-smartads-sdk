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
import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdClosedResult;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.vungle.publisher.VungleAdEventListener;

import java.util.Locale;

final class EventForwarder implements VungleAdEventListener {
    
    private final String placementId;
    private final MediationAdapter adapter;
    private MediationListener listener;
    
    @Nullable
    private Boolean availability;
    
    EventForwarder(
            String placementId,
            MediationAdapter adapter,
            MediationListener listener) {
        
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
        
        if (availability == null) {
            availability = isAdAvailable;
            
            if (availability) {
                listener.onAdLoaded(adapter);
            } else {
                listener.onAdFailedToLoad(
                        adapter,
                        AdRequestResult.NoFill,
                        "No fill");
            }
        } else {
            availability = isAdAvailable;
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
        
        if (wasCallToActionClicked) {
            listener.onAdClicked(adapter);
        }
        listener.onAdClosed(adapter, wasSuccessfulView);
    }
    
    void requestPerformed(MediationListener listener) {
        this.listener = listener;
        
        if (availability != null) {
            if (availability) {
                listener.onAdLoaded(adapter);
            } else {
                listener.onAdFailedToLoad(
                        adapter,
                        AdRequestResult.NoFill,
                        "No fill");
            }
        }
    }
    
    private boolean isSamePlacement(String value) {
        return placementId != null && placementId.equals(value);
    }
}
