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

package com.deltadna.android.sdk.ads.core.network;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationListener;

class DummyEventForwarder implements DummyListener {
    
    private final MediationListener listener;
    private final DummyAdapter adapter;
    
    DummyEventForwarder(MediationListener listener, DummyAdapter adapter) {
        this.listener = listener;
        this.adapter = adapter;
    }
    
    @Override
    public void onAdLoaded() {
        listener.onAdLoaded(adapter);
    }
    
    @Override
    public void onAdFailedToLoad(int errorCode) {
        switch (errorCode) {
            case DummyInterstitial.REQUEST_FAIL:
                listener.onAdFailedToLoad(
                        adapter,
                        AdRequestResult.Error,
                        "Dummy error: " + errorCode);
                break;
            
            default:
                listener.onAdFailedToLoad(
                        adapter,
                        AdRequestResult.Error,
                        "Dummy error: " + errorCode);
        }
    }
    
    @Override
    public void onAdOpened() {
        listener.onAdShowing(adapter);
    }
    
    @Override
    public void onAdClosed() {
        listener.onAdClosed(adapter, true);
    }
}
