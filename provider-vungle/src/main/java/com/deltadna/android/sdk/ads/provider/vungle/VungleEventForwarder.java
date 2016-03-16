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

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.vungle.publisher.EventListener;

final class VungleEventForwarder implements EventListener {
    
    private final MediationListener listener;
    private final MediationAdapter adapter;
    
    private boolean available;
    private boolean complete;
    
    public VungleEventForwarder(
            MediationListener listener,
            MediationAdapter adapter) {
        
        this.listener = listener;
        this.adapter = adapter;
    }
    
    @Override
    public void onAdEnd(boolean wasCallToActionClicked) {
        if(wasCallToActionClicked) {
            listener.onAdClicked(adapter);
        }

        listener.onAdClosed(adapter, complete);
    }

    @Override
    public void onAdStart() {
        complete = false;
        listener.onAdShowing(adapter);
    }

    @Override
    public void onAdUnavailable(String reason) {
        Log.w(BuildConfig.LOG_TAG, "Ad unavailable: " + reason);
        listener.onAdFailedToLoad(
                adapter,
                AdRequestResult.Error,
                "Vungle ad unavailable: " + reason);
    }

    @Override
    public void onAdPlayableChanged(boolean isAdPlayable) {
        this.available = isAdPlayable;
    }

    @Override
    public void onVideoView(boolean isCompletedView, int watchedMillis, int videoMillis) {
        complete = isCompletedView;
    }

    public boolean isAvailable() {
        return available;
    }
}
