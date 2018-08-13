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

import android.app.Activity;
import android.util.Log;

import com.chartboost.sdk.Chartboost;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.Privacy;

final class Helper {
    
    private static final Delegate delegate = new Delegate();
    
    private static boolean initialised;
    
    synchronized static void initialise(
            Activity activity,
            String appId,
            String appSignature,
            MediationAdapter adapter,
            MediationListener listener) {
        
        if (!initialised) {
            Log.d(BuildConfig.LOG_TAG, "Initialising");
            
            try {
                Chartboost.restrictDataCollection(activity, true);
                Chartboost.startWithAppId(activity, appId, appSignature);
                Chartboost.setDelegate(delegate);
                Chartboost.onCreate(activity);
                Chartboost.onStart(activity);
                
                initialised = true;
                Log.d(BuildConfig.LOG_TAG, "Initialised");
            } catch (Exception e) {
                Log.w(BuildConfig.LOG_TAG, "Failed to initialise", e);
                listener.onAdFailedToLoad(
                        adapter,
                        AdRequestResult.Configuration,
                        "Failed to initialise Chartboost: " + e.getMessage());
            }
        }
    }
    
    synchronized static void requestInterstitial(
            String location,
            MediationListener listener,
            MediationAdapter adapter) {
        
        if (initialised) {
            delegate.setInterstitial(listener, adapter);
            Chartboost.cacheInterstitial(location);
        }
    }
    
    synchronized static void requestRewarded(
            String location,
            MediationListener listener,
            MediationAdapter adapter) {
        
        if (initialised) {
            delegate.setRewarded(listener, adapter);
            Chartboost.cacheRewardedVideo(location);
        }
    }
    
    synchronized static void showInterstitial(String location) {
        if (initialised && Chartboost.hasInterstitial(location)) {
            Chartboost.showInterstitial(location);
        }
    }
    
    synchronized static void showRewarded(String location) {
        if (initialised && Chartboost.hasRewardedVideo(location)) {
            Chartboost.showRewardedVideo(location);
        }
    }
    
    static void onResume(Activity activity) {
        if (initialised) Chartboost.onResume(activity);
    }
    
    static void onPause(Activity activity) {
        if (initialised) Chartboost.onPause(activity);
    }
    
    static void onDestroy(Activity activity) {
        if (initialised) Chartboost.onDestroy(activity);
    }
    
    private Helper() {}
}
