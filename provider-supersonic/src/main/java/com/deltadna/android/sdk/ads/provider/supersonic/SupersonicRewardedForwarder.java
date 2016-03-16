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

package com.deltadna.android.sdk.ads.provider.supersonic;

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdClosedResult;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.supersonic.mediationsdk.logger.SupersonicError;
import com.supersonic.mediationsdk.model.Placement;
import com.supersonic.mediationsdk.sdk.RewardedVideoListener;

import java.util.Locale;

final class SupersonicRewardedForwarder implements RewardedVideoListener {
    
    private final SupersonicRewardedAdapter adapter;
    private final MediationListener listener;
    
    private boolean completed;
    
    SupersonicRewardedForwarder(
            SupersonicRewardedAdapter adapter,
            MediationListener listener) {
        
        this.adapter = adapter;
        this.listener = listener;
    }
    
    @Override
    public void onRewardedVideoInitSuccess() {}
    
    @Override
    public void onRewardedVideoInitFail(SupersonicError error) {
        Log.w(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Rewarded video init fail with code %d and message %s",
                error.getErrorCode(),
                error.getErrorMessage()));
        
        final AdRequestResult result;
        switch (error.getErrorCode()) {
            case SupersonicError.ERROR_CODE_NO_CONFIGURATION_AVAILABLE:
            case SupersonicError.ERROR_CODE_USING_CACHED_CONFIGURATION:
            case SupersonicError.ERROR_CODE_APP_KEY_NOT_SET:
            case SupersonicError.ERROR_CODE_APP_KEY_INCORRECT:
            case SupersonicError.ERROR_CODE_KEY_NOT_SET_FOR_PROVIDER:
            case SupersonicError.ERROR_CODE_INVALID_KEY_VALUE:
                result = AdRequestResult.Configuration;
                break;
            
            case SupersonicError.ERROR_CODE_UNSUPPORTED_SDK_VERSION:
            case SupersonicError.ERROR_CODE_ADAPTER_INIT_FAILED:
            case SupersonicError.ERROR_CODE_GENERIC:
            case SupersonicError.ERROR_CODE_SHOW_VIDEO_FAILED:
                result = AdRequestResult.Error;
                break;
            
            default:
                result = AdRequestResult.Error;
                Log.w(  BuildConfig.LOG_TAG,
                        "Unknown case: " + error.getErrorCode());
        }
        
        listener.onAdFailedToLoad(adapter, result, error.getErrorMessage());
    }
    
    @Override
    public void onRewardedVideoAdOpened() {
        listener.onAdShowing(adapter);
    }
    
    @Override
    public void onRewardedVideoAdClosed() {
        listener.onAdClosed(adapter, completed);
    }
    
    @Override
    public void onVideoAvailabilityChanged(boolean available) {
        if (available) {
            listener.onAdLoaded(adapter);
        } else {
            listener.onAdFailedToLoad(
                    adapter,
                    AdRequestResult.NoFill,
                    "Rewarded video ad not available");
        }
    }
    
    @Override
    public void onVideoStart() {
        completed = false;
    }
    
    @Override
    public void onVideoEnd() {
        // does not appear to get called
    }
    
    @Override
    public void onRewardedVideoAdRewarded(Placement placement) {
        completed = true;
    }
    
    @Override
    public void onRewardedVideoShowFail(SupersonicError error) {
        Log.w(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Rewarded video show fail with code %d and message %s",
                error.getErrorCode(),
                error.getErrorMessage()));
        
        listener.onAdFailedToShow(adapter, AdClosedResult.ERROR);
    }
}
