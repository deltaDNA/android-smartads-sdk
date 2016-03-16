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

package com.deltadna.android.sdk.ads.provider.mobfox;

import android.util.Log;

import com.adsdk.sdk.Ad;
import com.adsdk.sdk.AdListener;
import com.deltadna.android.sdk.ads.bindings.AdClosedResult;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;

final class MobFoxEventForwarder implements AdListener {
    
    private final MediationListener listener;
    private final MediationAdapter adapter;
    
    MobFoxEventForwarder(MediationListener listener, MediationAdapter adapter) {
        this.listener = listener;
        this.adapter = adapter;
    }
    
    @Override
    public void adClicked() {
        listener.onAdClicked(adapter);
    }

    @Override
    public void adClosed(Ad ad, boolean completed) {
        listener.onAdClosed(adapter, true);
    }

    @Override
    public void adLoadSucceeded(Ad ad) {
        Log.d(BuildConfig.LOG_TAG, "Ad load succeeded");
        listener.onAdLoaded(adapter);
    }

    @Override
    public void adShown(Ad ad, boolean succeeded) {
        if(succeeded) {
            listener.onAdShowing(adapter);
        } else {
            listener.onAdFailedToShow(adapter, AdClosedResult.ERROR);
        }
    }
    
    @Override
    public void noAdFound() {
        Log.d(BuildConfig.LOG_TAG, "No ad found");
        listener.onAdFailedToLoad(
                adapter,
                AdRequestResult.NoFill,
                "No MobFox ad found");
    }
}
