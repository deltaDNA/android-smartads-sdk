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

package com.deltadna.android.sdk.ads.core;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdClosedResult;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;

import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

class AdAgent implements MediationListener {
    
    private static final String TAG = BuildConfig.LOG_TAG
            + ' '
            + AdAgent.class.getSimpleName();
    private static final int LOAD_TIMEOUT_MILLIS = 15 * 1000;
    private static final int WATERFALL_RESTART_DELAY_MILLIS = 60 * 1000;
    
    private enum State {
        READY,
        LOADING,
        LOADED,
        SHOWING
    }
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable loadTimeout = new Runnable() {
        @Override
        public void run() {
            onAdFailedToLoad(
                    currentAdapter,
                    AdRequestResult.Timeout,
                    "Ad load timed out");
        }
    };
    private final Runnable requestAd = new Runnable() {
        @Override
        public void run() {
            requestAd();
        }
    };
    
    private final AdAgentListener listener;
    private final Waterfall waterfall;
    private final int adMaxPerSession;
    private final ExceptionHandler exceptionHandler;
    
    long lastShownTime;
    int shownCount;
    
    private MediationAdapter currentAdapter;
    
    private Activity activity;
    private JSONObject configuration;
    
    private State state;
    private boolean adWasClicked;
    private boolean adDidLeaveApplication;
    
    private long lastRequestStart;
    private long lastRequestEnd;
    
    @Nullable
    private String adPoint;
    
    AdAgent(AdAgentListener listener,
            Waterfall waterfall,
            int adMaxPerSession,
            ExceptionHandler exceptionHandler) {
        
        this.listener = listener;
        this.waterfall = waterfall;
        this.adMaxPerSession = adMaxPerSession;
        this.exceptionHandler = exceptionHandler;
        
        currentAdapter = waterfall.resetAndGetFirst();
        if (currentAdapter == null) {
            Log.w(TAG, "No ad providers, ads will not be available");
        }
        
        this.state = State.READY;
    }
    
    @UiThread
    void requestAd(Activity activity, JSONObject configuration) {
        if (currentAdapter == null) {
            Log.w(TAG, "Ignoring ad request due to no providers");
            return;
        } else if (hasReachedAdLimit()) {
            Log.w(TAG, "Ignoring ad request due to session limit");
            return;
        } else if (state != State.READY) {
            Log.w(  TAG,
                    "Ignoring ad request due to an existing request in progress");
            return;
        }
        
        this.activity = activity;
        this.configuration = configuration;
        
        this.adWasClicked = false;
        this.adDidLeaveApplication = false;
        
        requestAd();
    }
    
    boolean isAdLoaded() {
        return state == State.LOADED;
    }
    
    @UiThread
    void showAd(String adPoint) {
        this.adPoint = adPoint;
        if (isAdLoaded()) {
            currentAdapter.showAd();
        } else {
            listener.onAdFailedToOpen(this, currentAdapter, "Not loaded an ad", AdClosedResult.NOT_READY);
        }
    }

    boolean adWasClicked() {
        return adWasClicked;
    }

    boolean adDidLeaveApplication() {
        return adDidLeaveApplication;
    }

    MediationAdapter getCurrentAdapter() {
        return currentAdapter;
    }

    void setAdPoint(@Nullable String adPoint) {
        this.adPoint = adPoint;
    }
    
    @Nullable
    String getAdPoint() {
        return adPoint;
    }
    
    @UiThread
    void onResume() {
        for (MediationAdapter adapter : waterfall.adapters) {
            adapter.onResume();
        }
    }
    
    @UiThread
    void onPause() {
        for (MediationAdapter adapter : waterfall.adapters) {
            adapter.onPause();
        }
    }
    
    @UiThread
    void onDestroy() {
        for (MediationAdapter adapter : waterfall.adapters) {
            adapter.onDestroy();
        }
    }
    
    @Override
    public void onAdLoaded(MediationAdapter adapter) {
        // some adapters keep loading without being requested to
        if (adapter.equals(currentAdapter)) {
            Log.d(TAG, "Ad loaded for " + adapter);
            
            handler.removeCallbacksAndMessages(null);
            
            lastRequestEnd = System.currentTimeMillis();
            listener.onAdLoaded(
                    this, adapter, lastRequestEnd - lastRequestStart);
            
            state = State.LOADED;
            
            waterfall.score(adapter, AdRequestResult.Loaded);
        } else {
            Log.w(TAG, "Unexpected adapter " + adapter);
        }
    }
    
    @Override
    public void onAdFailedToLoad(
            MediationAdapter adapter,
            AdRequestResult result,
            String reason) {
        
        // some adapters keep loading without being requested to
        if (adapter.equals(currentAdapter)) {
            // prevent adapters calling this multiple times
            if (state != State.LOADING) return;
            
            Log.d(TAG, String.format(
                    Locale.US,
                    "Ad failed to load for %s due to %s with reason %s",
                    adapter,
                    result,
                    reason));
            
            handler.removeCallbacksAndMessages(null);
            
            lastRequestEnd = System.currentTimeMillis();
            listener.onAdFailedToLoad(
                    this,
                    adapter,
                    reason,
                    lastRequestEnd - lastRequestStart,
                    result);
            
            state = State.READY;
            
            waterfall.score(adapter, result);
            changeToNextAdapter(false);
            
            if (currentAdapter != null) {
                requestAd();
            } else {
                Log.d(TAG, "Reached end of waterfall");
                
                changeToNextAdapter(true);
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(requestAd, WATERFALL_RESTART_DELAY_MILLIS);
            }
        } else {
            Log.w(TAG, "Unexpected adapter " + adapter);
        }
    }
    
    @Override
    public void onAdShowing(MediationAdapter adapter) {
        if (adapter.equals(currentAdapter)) {
            if (state != State.SHOWING) {
                Log.d(TAG, "Ad showing for " + adapter);
                
                listener.onAdOpened(this, adapter);
                
                shownCount++;
                state = State.SHOWING;
            } else {
                Log.w(TAG, "Ad already showing for " + adapter);
            }
        } else {
            Log.w(TAG, "Unexpected adapter " + adapter);
        }
    }
    
    @Override
    public void onAdFailedToShow(
            MediationAdapter adapter,
            AdClosedResult result) {
        
        if (adapter.equals(currentAdapter)) {
            Log.d(TAG, String.format(
                    Locale.US,
                    "Ad failed to show for %s due to %s",
                    adapter,
                    result));
            listener.onAdFailedToOpen(
                    this,
                    currentAdapter,
                    "Ad failed to show",
                    result);
            
            state = State.READY;
            
            waterfall.remove(adapter);
            changeToNextAdapter(true);
            requestAd();
        } else {
            Log.w(TAG, "Unexpected adapter " + adapter);
        }
    }
    
    @Override
    public void onAdClicked(MediationAdapter mediationAdapter) {
        adWasClicked = true;
    }
    
    @Override
    public void onAdLeftApplication(MediationAdapter mediationAdapter) {
        adDidLeaveApplication = true;
    }
    
    @Override
    public void onAdClosed(MediationAdapter adapter, boolean complete) {
        if (adapter.equals(currentAdapter)) {
            if (state == State.SHOWING) {
                Log.d(TAG, "Ad closed for " + adapter);
                listener.onAdClosed(this, adapter, complete);
                
                lastShownTime = System.currentTimeMillis();
                state = State.READY;
                
                changeToNextAdapter(true);
                requestAd();
            } else {
                Log.w(TAG, "Unexpected state " + state);
            }
        } else {
            Log.w(TAG, "Unexpected adapter " + adapter);
        }
    }
    
    private void changeToNextAdapter(boolean reset) {
        if (currentAdapter != null) currentAdapter.onSwappedOut();
        
        currentAdapter = (reset)
                ? waterfall.resetAndGetFirst()
                : waterfall.getNext();
    }
    
    private void requestAd() {
        if (hasReachedAdLimit()) {
            Log.d(TAG, "Not requesting next ad due to session limit");
        } else if (state != State.READY) {
            Log.d(TAG, "Not ready to request next ad");
        } else if (currentAdapter != null) {
            Log.d(TAG, "Requesting next ad from " + currentAdapter);
            
            state = State.LOADING;
            lastRequestStart = System.currentTimeMillis();
            
            final NetworkInfo network = ((ConnectivityManager) activity
                    .getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();
            
            if (network == null || !network.isConnected()) {
                onAdFailedToLoad(
                        currentAdapter,
                        AdRequestResult.Network,
                        "No connection");
            } else {
                final List<String> crashes = exceptionHandler.listCrashes(
                        AdProvider.defines(currentAdapter));
                if (!crashes.isEmpty()) {
                    Log.d(TAG, String.format(
                            Locale.US,
                            "Not requesting next ad from %s due to previous crash",
                            currentAdapter));
                    
                    onAdFailedToLoad(
                            currentAdapter,
                            AdRequestResult.Error,
                            crashes.get(0));
                } else {
                    handler.removeCallbacksAndMessages(null);
                    handler.postDelayed(loadTimeout, LOAD_TIMEOUT_MILLIS);
                    currentAdapter.requestAd(activity, this, configuration);
                }
            }
        } else {
            Log.d(TAG, "No adapter to request ad from");
        }
    }
    
    private boolean hasReachedAdLimit() {
        return (adMaxPerSession != -1 && shownCount >= adMaxPerSession);
    }
}
