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

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.unity3d.ads.android.IUnityAdsListener;

final class UnityRewardedEventForwarder implements IUnityAdsListener {
    
    private final MediationListener listener;
    private final MediationAdapter adapter;
    
    private boolean videoWatched;
    
    UnityRewardedEventForwarder(
            MediationListener listener,
            MediationAdapter adapter) {
        
        this.listener = listener;
        this.adapter = adapter;
    }
    
    @Override
    public void onHide() {
        listener.onAdClosed(adapter, videoWatched);
    }

    @Override
    public void onShow() {
        videoWatched = false;
        listener.onAdShowing(adapter);
    }

    @Override
    public void onVideoStarted() {

    }

    @Override
    public void onVideoCompleted(String itemKey, boolean skipped) {
        videoWatched = !skipped;
    }

    @Override
    public void onFetchCompleted() {
        Log.d(BuildConfig.LOG_TAG, "Ad fetch completed");
        listener.onAdLoaded(adapter);
    }

    @Override
    public void onFetchFailed() {
        Log.w(BuildConfig.LOG_TAG, "Ad fetch failed");
        listener.onAdFailedToLoad(
                adapter,
                AdRequestResult.Error,
                "Unity ad fetch failed");
    }
}
