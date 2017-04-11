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

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdClosedResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.vungle.publisher.EventListener;

import java.util.Locale;

final class VungleEventForwarder implements EventListener {
    
    private final MediationListener listener;
    private final MediationAdapter adapter;
    
    VungleEventForwarder(MediationListener listener, MediationAdapter adapter) {
        this.listener = listener;
        this.adapter = adapter;
    }
    
    @Override
    public void onAdUnavailable(final String reason) {
        Log.w(BuildConfig.LOG_TAG, "Ad unavailable: " + reason);
        listener.onAdFailedToShow(adapter, AdClosedResult.EXPIRED);
    }
    
    @Override
    public synchronized void onAdPlayableChanged(final boolean isAdPlayable) {
        // handled by the adapter
    }
    
    @Override
    public void onAdStart() {
        Log.d(BuildConfig.LOG_TAG, "Ad start");
        listener.onAdShowing(adapter);
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
        
        if (wasCallToActionClicked) {
            listener.onAdClicked(adapter);
        }
        listener.onAdClosed(adapter, wasSuccessfulView);
    }
    
    @Override
    public void onVideoView(
            boolean isCompletedView,
            int watchedMillis,
            int videoMillis) {
        
        // deprecated
    }
}
