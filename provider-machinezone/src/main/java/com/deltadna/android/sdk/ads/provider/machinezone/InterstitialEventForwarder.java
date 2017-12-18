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

package com.deltadna.android.sdk.ads.provider.machinezone;

import com.fractionalmedia.sdk.AdRequest;
import com.fractionalmedia.sdk.AdRequestListener;
import com.fractionalmedia.sdk.AdZoneError;

final class InterstitialEventForwarder implements AdRequestListener {
    
    private final EventForwarder delegate;
    
    InterstitialEventForwarder(EventForwarder delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void OnLoaded(AdRequest request) {
        delegate.onLoaded(request);
    }
    
    @Override
    public void OnFailed(AdRequest request, AdZoneError error) {
        delegate.onFailed(request, error);
    }
    
    @Override
    public void OnExpanded(AdRequest request) {
        delegate.onExpanded(request);
    }
    
    @Override
    public void OnClicked(AdRequest request) {
        delegate.onClicked(request);
    }
    
    @Override
    public void OnCollapsed(AdRequest request) {
        delegate.onCollapsed(request, true);
    }
}
