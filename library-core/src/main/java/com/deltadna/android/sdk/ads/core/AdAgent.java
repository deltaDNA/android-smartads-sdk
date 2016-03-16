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
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdClosedResult;
import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;

import org.json.JSONObject;

import java.util.Locale;

class AdAgent implements MediationListener {
    
    private static final int LOAD_TIMEOUT_MILLIS = 15 * 1000;
    private static final int WATERFALL_RESTART_DELAY_MILLIS = 60 * 1000;
    
    private enum State {
        READY,
        LOADING,
        LOADED,
        SHOWING
    }
    
    private final Handler handler = new Handler();
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
    private final int maxPerNetwork;
    
    private MediationAdapter currentAdapter;
    private long lastRequestStart = -1L;
    private long lastRequestEnd = -1L;
    
    private Activity activity;
    private JSONObject configuration;
    
    private State state;
    private boolean adWasClicked;
    private boolean adDidLeaveApplication;

    private String adPoint;
    
    AdAgent(AdAgentListener listener,
            Waterfall waterfall,
            int maxPerNetwork) {
        
        this.listener = listener;
        this.waterfall = waterfall;
        this.maxPerNetwork = maxPerNetwork;
        
        currentAdapter = waterfall.resetAndGetFirst();
        if (currentAdapter == null) {
            Log.e(  BuildConfig.LOG_TAG,
                    "No ad providers supplied, ads will not be available");
        }
        
        this.state = State.READY;
    }

    void requestAd(Activity activity, JSONObject configuration) {
        if (currentAdapter == null) return;     // Not built AdAgent correctly

        this.activity = activity;
        this.configuration = configuration;

        this.adWasClicked = false;
        this.adDidLeaveApplication = false;

        requestAd();
    }

    boolean isAdLoaded() {
        return state == State.LOADED;
    }

    void showAd(String adPoint) {
        this.adPoint = adPoint;
        if (state == State.LOADED) {
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

    void setAdPoint(String adPoint) {
        this.adPoint = adPoint;
    }

    String getAdPoint() {
        return adPoint;
    }
    
    void onResume() {
        for (MediationAdapter adapter : waterfall.getAdapters()) {
            adapter.onResume();
        }
    }
    
    void onPause() {
        for (MediationAdapter adapter : waterfall.getAdapters()) {
            adapter.onPause();
        }
    }
    
    void onDestroy() {
        for (MediationAdapter adapter : waterfall.getAdapters()) {
            adapter.onDestroy();
        }
    }
    
    @Override
    public void onAdLoaded(MediationAdapter mediationAdapter) {
        // some adapters keep loading without being requested to
        if (mediationAdapter == currentAdapter) {
            Log.d(BuildConfig.LOG_TAG, "Ad loaded for " + mediationAdapter);
            
            handler.removeCallbacks(loadTimeout);
            
            lastRequestEnd = System.currentTimeMillis();
            listener.onAdLoaded(
                    this, mediationAdapter, lastRequestEnd - lastRequestStart);
            
            state = State.LOADED;
        } else {
            Log.w(  BuildConfig.LOG_TAG,
                    "Unexpected adapter " + mediationAdapter);
        }
    }
    
    @Override
    public void onAdFailedToLoad(
            MediationAdapter adapter,
            AdRequestResult result,
            String reason) {
        
        // some adapters keep loading without being requested to
        if (adapter == currentAdapter) {
            Log.d(BuildConfig.LOG_TAG, String.format(
                    Locale.US,
                    "Ad failed to load for %s due to %s with reason %s",
                    adapter,
                    result,
                    reason));
            
            handler.removeCallbacks(loadTimeout);
            
            lastRequestEnd = System.currentTimeMillis();
            listener.onAdFailedToLoad(
                    this,
                    adapter,
                    reason,
                    lastRequestEnd - lastRequestStart,
                    result);
            
            // prevent adapters calling this multiple times
            if (state != State.LOADING) {
                return;
            }
            state = State.READY;
            
            if (result.remove()) {
                Log.d( BuildConfig.LOG_TAG, "Removing " + adapter);
                currentAdapter = waterfall.removeAndGetNext();
            } else {
                Log.d(BuildConfig.LOG_TAG, String.format(
                        Locale.US,
                        "Updating %s score due to %s",
                        adapter,
                        result));
                
                currentAdapter.updateScore(result);
                currentAdapter = waterfall.getNext();
            }
            
            if (currentAdapter != null) {
                requestAd();
            } else {
                Log.d(BuildConfig.LOG_TAG, "Reached end of waterfall");
                
                currentAdapter = waterfall.resetAndGetFirst();
                handler.postDelayed(requestAd, WATERFALL_RESTART_DELAY_MILLIS);
            }
        } else {
            Log.w(BuildConfig.LOG_TAG, "Unexpected adapter " + adapter);
        }
    }
    
    @Override
    public void onAdShowing(MediationAdapter mediationAdapter) {
        Log.d(BuildConfig.LOG_TAG, "Ad showing for " + mediationAdapter);
        listener.onAdOpened(this, mediationAdapter);
        
        state = State.SHOWING;
    }
    
    @Override
    public void onAdFailedToShow(MediationAdapter mediationAdapter, AdClosedResult adClosedResult) {
        Log.d(BuildConfig.LOG_TAG, String.format(
                Locale.US,
                "Ad failed to show for %s due to %s",
                mediationAdapter,
                adClosedResult));
        listener.onAdFailedToOpen(
                this,
                currentAdapter,
                "Ad failed to show",
                adClosedResult);
        
        // TODO should the adapter be scored negatively?
        state = State.READY;
        
        changeToNextAdapter();
        requestAd();
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
        Log.d(BuildConfig.LOG_TAG, "Ad closed for " + adapter);
        listener.onAdClosed(this, adapter, complete);
        
        state = State.READY;
        
        changeToNextAdapter();
        requestAd();
    }
    
    /**
     * Uses the current adapter if it can still perform a request, else resets
     * the waterfall and uses the top one.
     */
    private void changeToNextAdapter() {
        if (currentAdapter.getRequests() >= maxPerNetwork) {
            Log.d(  BuildConfig.LOG_TAG,
                    "Reached max requests for " + currentAdapter);
            
            currentAdapter = waterfall.resetAndGetFirst();
        }
    }
    
    private void requestAd() {
        if (currentAdapter != null) {
            final NetworkInfo network = ((ConnectivityManager) activity
                    .getSystemService(Context.CONNECTIVITY_SERVICE))
                    .getActiveNetworkInfo();
            
            if (!network.isConnected()) {
                onAdFailedToLoad(currentAdapter, AdRequestResult.Network, "No connection");
            } else {
                if (state == State.READY) {
                    Log.d(  BuildConfig.LOG_TAG,
                            "Requesting next ad from " + currentAdapter);
                    
                    state = State.LOADING;
                    lastRequestStart = System.currentTimeMillis();
                    
                    currentAdapter.incrementRequests();
                    if (currentAdapter.getRequests() >= maxPerNetwork) {
                        Log.d(BuildConfig.LOG_TAG, String.format(
                                Locale.US,
                                "Updating %s score due to %s",
                                currentAdapter,
                                AdRequestResult.MaxRequests));
                        currentAdapter.updateScore(AdRequestResult.MaxRequests);
                    }
                    
                    handler.postDelayed(loadTimeout, LOAD_TIMEOUT_MILLIS);
                    currentAdapter.requestAd(activity, this, configuration);
                } else {
                    Log.w(  BuildConfig.LOG_TAG,
                            "Not ready to request next ad from " + currentAdapter);
                }
            }
        } else {
            Log.w(BuildConfig.LOG_TAG, "No adapter to request next ad from");
        }
    }
}
