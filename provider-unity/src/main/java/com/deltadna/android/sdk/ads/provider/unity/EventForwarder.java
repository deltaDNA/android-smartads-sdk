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

package com.deltadna.android.sdk.ads.provider.unity;

import android.support.annotation.Nullable;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.mediation.IUnityAdsExtendedListener;

import java.util.Locale;

final class EventForwarder implements IUnityAdsExtendedListener {
    
    private final MediationAdapter adapter;
    private final String placementId;
    @Nullable
    private MediationListener listener;
    
    @Nullable
    private Boolean available;
    @Nullable
    private UnityAds.PlacementState lastPlacementState;
    @Nullable
    private UnityAds.UnityAdsError lastError;
    @Nullable
    private String lastMessage;
    
    EventForwarder(
            MediationAdapter adapter,
            String placementId,
            @Nullable MediationListener listener) {
        
        this.adapter = adapter;
        this.placementId = placementId;
        this.listener = listener;
    }
    
    @Override
    @Deprecated
    public void onUnityAdsReady(String placementId) {
        Log.d(BuildConfig.LOG_TAG, "Unity ads ready: " + placementId);
    }
    
    @Override
    public void onUnityAdsPlacementStateChanged(
            String placementId,
            UnityAds.PlacementState oldState,
            UnityAds.PlacementState newState) {
        
        Log.d(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Unity ads placement state changed: %s/%s/%s",
                placementId,
                oldState,
                newState));
        
        if (!samePlacement(placementId)) {
            Log.w(BuildConfig.LOG_TAG, "Placement ids are different");
            return;
        }
        
        lastPlacementState = newState;
        
        if (listener != null) {
            switch (newState) {
                case READY:
                    available = true;
                    listener.onAdLoaded(adapter);
                    break;
                    
                case NOT_AVAILABLE:
                    available = false;
                    listener.onAdFailedToLoad(
                            adapter,
                            AdRequestResult.Configuration,
                            newState.name());
                    break;
                    
                case DISABLED:
                    available = false;
                    listener.onAdFailedToLoad(
                            adapter,
                            AdRequestResult.Configuration,
                            newState.name());
                    break;
                    
                case WAITING:
                    available = false;
                    break;
                    
                case NO_FILL:
                    available = false;
                    listener.onAdFailedToLoad(
                            adapter,
                            AdRequestResult.NoFill,
                            newState.name());
                    break;
            }
        }
    }
    
    @Override
    public void onUnityAdsError(UnityAds.UnityAdsError error, String message) {
        Log.w(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Unity ads error: %s/%s",
                error,
                message));
        
        available = false;
        lastError = error;
        lastMessage = message;
        
        final AdRequestResult reason;
        switch (error) {
            case NOT_INITIALIZED:
            case INITIALIZE_FAILED:
            case INVALID_ARGUMENT:
            case INIT_SANITY_CHECK_FAIL:
                reason = AdRequestResult.Configuration;
                break;
            
            default:
                reason = AdRequestResult.Error;
        }
        
        if (listener != null) listener.onAdFailedToLoad(
                adapter,
                reason,
                message);
    }
    
    @Override
    public void onUnityAdsStart(String placementId) {
        Log.d(BuildConfig.LOG_TAG, "Unity ads start: " + placementId);
        
        if (!samePlacement(placementId)) {
            Log.w(BuildConfig.LOG_TAG, "Placement ids are different");
            return;
        }
        
        if (listener != null) listener.onAdShowing(adapter);
    }
    
    @Override
    public void onUnityAdsFinish(
            String placementId,
            UnityAds.FinishState state) {
        
        Log.d(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Unity ads finish: %s/%s",
                placementId,
                state));
        
        if (!samePlacement(placementId)) {
            Log.w(BuildConfig.LOG_TAG, "Placement ids are different");
            return;
        }
        
        if (listener != null) {
            // stops further unexpected callbacks by the network
            final MediationListener listener = this.listener;
            this.listener = null;
            
            listener.onAdClosed(adapter, state == UnityAds.FinishState.COMPLETED);
        }
    }
    
    @Override
    public void onUnityAdsClick(String placementId) {
        Log.d(BuildConfig.LOG_TAG, "Unity ads click: " + placementId);
        
        if (!samePlacement(placementId)) {
            Log.w(BuildConfig.LOG_TAG, "Placement ids are different");
            return;
        }
        
        if (listener != null) listener.onAdClicked(adapter);
    }
    
    void requestPerformed(MediationListener listener) {
        this.listener = listener;
        
        if (available != null) {
            if (available) {
                onUnityAdsPlacementStateChanged(
                        placementId,
                        UnityAds.PlacementState.READY,
                        UnityAds.PlacementState.READY);
            } else {
                if (lastPlacementState != null) {
                    onUnityAdsPlacementStateChanged(
                            placementId,
                            lastPlacementState,
                            lastPlacementState);
                } else if (lastError != null && lastMessage != null) {
                    onUnityAdsError(lastError, lastMessage);
                }
            }
        }
    }
    
    private boolean samePlacement(String value) {
        return placementId.equals(value);
    }
}
