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

package com.deltadna.android.sdk.ads.provider.tapjoy;

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.tapjoy.TJActionRequest;
import com.tapjoy.TJError;
import com.tapjoy.TJPlacement;
import com.tapjoy.TJPlacementListener;
import com.tapjoy.TJPlacementVideoListener;

import java.util.Locale;

final class EventForwarder implements TJPlacementListener, TJPlacementVideoListener {
    
    static final TJPlacementListener EMPTY = new Empty();
    
    private final MediationAdapter adapter;
    private final MediationListener listener;
    private final String placement;
    
    // true by default for interstitial, rewarded this to false on video start
    private boolean complete = true;
    
    EventForwarder(
            MediationAdapter adapter,
            MediationListener listener,
            String placement) {
        
        this.adapter = adapter;
        this.listener = listener;
        this.placement = placement;
    }
    
    @Override
    public void onRequestSuccess(TJPlacement p) {
        Log.d(BuildConfig.LOG_TAG, "Request success: " + p.getName());
    }
    
    @Override
    public void onRequestFailure(TJPlacement p, TJError e) {
        Log.w(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Request failure: %s/%d/%s",
                p.getName(),
                e.code,
                e.message));
        
        if (samePlacement(p)) {
            listener.onAdFailedToLoad(
                    adapter,
                    AdRequestResult.NoFill,
                    e.message);
        }
    }
    
    @Override
    public void onContentReady(TJPlacement p) {
        Log.d(BuildConfig.LOG_TAG, "Content ready: " + p.getName());
        
        if (samePlacement(p)) {
            p.setVideoListener(this);
            listener.onAdLoaded(adapter);
        }
    }
    
    @Override
    public void onContentShow(TJPlacement p) {
        Log.d(BuildConfig.LOG_TAG, "Content show: " + p.getName());
        
        if (samePlacement(p)) {
            listener.onAdShowing(adapter);
        }
    }
    
    @Override
    public void onContentDismiss(TJPlacement p) {
        Log.d(BuildConfig.LOG_TAG, "Content dismiss: " + p.getName());
        
        if (samePlacement(p)) {
            listener.onAdClosed(adapter, complete);
        }
    }
    
    @Override
    public void onPurchaseRequest(
            TJPlacement p,
            TJActionRequest r,
            String s) {
        
        Log.d(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Purchase request: %s/%s/%s",
                p.getName(),
                r.getRequestId(),
                s));
    }
    
    @Override
    public void onRewardRequest(
            TJPlacement p,
            TJActionRequest r,
            String s,
            int i) {
        
        Log.d(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Reward request: %s/%s/%s/%d",
                p.getName(),
                r.getRequestId(),
                s,
                i));
    }
    
    @Override
    public void onVideoStart(TJPlacement p) {
        Log.d(BuildConfig.LOG_TAG, "Video start: " + p.getName());
        
        if (samePlacement(p)) {
            complete = false;
        }
    }
    
    @Override
    public void onVideoError(TJPlacement p, String s) {
        Log.d(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Video error: %s/%s",
                p.getName(),
                s));
    }
    
    @Override
    public void onVideoComplete(TJPlacement p) {
        Log.d(BuildConfig.LOG_TAG, "Video complete: " + p.getName());
        
        if (samePlacement(p)) {
            complete = true;
        }
    }
    
    private boolean samePlacement(TJPlacement p) {
        return placement.equals(p.getName());
    }
    
    private static class Empty implements TJPlacementListener {
        
        @Override
        public void onRequestSuccess(TJPlacement tjPlacement) {}
        
        @Override
        public void onRequestFailure(TJPlacement tjPlacement, TJError tjError) {}
        
        @Override
        public void onContentReady(TJPlacement tjPlacement) {}
        
        @Override
        public void onContentShow(TJPlacement tjPlacement) {}
        
        @Override
        public void onContentDismiss(TJPlacement tjPlacement) {}
        
        @Override
        public void onPurchaseRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s) {}
        
        @Override
        public void onRewardRequest(TJPlacement tjPlacement, TJActionRequest tjActionRequest, String s, int i) {}
    }
}
