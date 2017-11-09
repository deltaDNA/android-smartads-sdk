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

package com.deltadna.android.sdk.ads.provider.thirdpresence;

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.thirdpresence.adsdk.sdk.VideoAd;

import java.util.Locale;

class ThirdPresenceRewardedEventForwarder implements VideoAd.Listener {
    
    private final MediationListener listener;
    private final MediationAdapter adapter;
    
    private boolean complete;
    
    ThirdPresenceRewardedEventForwarder(
            MediationListener listener,
            MediationAdapter adapter) {
        
        this.listener = listener;
        this.adapter = adapter;
    }
    
    @Override
    public void onAdEvent(
            String eventName,
            String arg1,
            String arg2,
            String arg3) {
        
        Log.d(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Ad event: %s/%s/%s/%s",
                eventName,
                arg1,
                arg2,
                arg3));
        
        switch (eventName) {
            case VideoAd.Events.AD_ERROR:
                switch (arg1.toLowerCase(Locale.US)) {
                    case "no fill":
                        listener.onAdFailedToLoad(
                                adapter,
                                AdRequestResult.NoFill,
                                arg1);
                        break;
                    
                    case "invalid response":
                        listener.onAdFailedToLoad(
                                adapter,
                                AdRequestResult.Configuration,
                                arg1);
                        break;
                    
                    case "failed to load player":
                        listener.onAdFailedToLoad(
                                adapter,
                                AdRequestResult.Error,
                                arg1);
                        break;
                }
                break;
            
            case VideoAd.Events.AD_LOADED:
                listener.onAdLoaded(adapter);
                break;
            
            case VideoAd.Events.AD_PLAYING:
                listener.onAdShowing(adapter);
                break;
            
            case VideoAd.Events.AD_CLICKTHRU:
                listener.onAdClicked(adapter);
                break;
            
            case VideoAd.Events.AD_SKIPPED:
                complete = false;
                break;
            
            case VideoAd.Events.AD_VIDEO_COMPLETE:
                complete = true;
                break;
            
            case VideoAd.Events.AD_LEFT_APPLICATION:
                listener.onAdLeftApplication(adapter);
                break;
        }
    }
    
    @Override
    public void onPlayerReady() {
        Log.d(BuildConfig.LOG_TAG, "Player ready");
    }
    
    @Override
    public void onError(VideoAd.ErrorCode errorCode, String message) {
        Log.w(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Error: %s/%s",
                errorCode,
                message));
        
        switch (errorCode) {
            case NO_FILL:
                listener.onAdFailedToLoad(
                        adapter,
                        AdRequestResult.NoFill,
                        message);
                break;
            
            case NETWORK_FAILURE:
            case NETWORK_TIMEOUT:
                listener.onAdFailedToLoad(
                        adapter,
                        AdRequestResult.Network,
                        message);
                break;
            
            default:
                listener.onAdFailedToLoad(
                        adapter,
                        AdRequestResult.Error,
                        message);
        }
    }
    
    boolean hasCompleted() {
        return complete;
    }
}
