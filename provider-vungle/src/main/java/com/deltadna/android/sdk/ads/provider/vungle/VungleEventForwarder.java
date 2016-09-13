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

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.AdRequestResult;
import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.bindings.MediationListener;
import com.vungle.publisher.EventListener;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

final class VungleEventForwarder implements EventListener {
    
    /**
     * Single thread onto which we redirect the Vungle callback invocation, as
     * these come back on random background threads.
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    /**
     * Main thread {@link Handler} onto which we redirect listener invocations.
     */
    final Handler handler = new Handler(Looper.getMainLooper());
    
    private final MediationListener listener;
    private final MediationAdapter adapter;
    
    private boolean available;
    
    private CompleteTask completeTask;
    private Future<Boolean> completeResult;
    
    VungleEventForwarder(MediationListener listener, MediationAdapter adapter) {
        this.listener = listener;
        this.adapter = adapter;
    }
    
    @Override
    public void onAdEnd(final boolean wasCallToActionClicked) {
        Log.d(BuildConfig.LOG_TAG, "Ad end with wasCallToActionClicked "
                + wasCallToActionClicked);
        
        boolean complete;
        try {
            complete = completeResult.get();
        } catch (InterruptedException | ExecutionException e) {
            Log.w(BuildConfig.LOG_TAG, e);
            complete = false;
        }
        
        final boolean completeFinal = complete;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (wasCallToActionClicked) {
                    listener.onAdClicked(adapter);
                }
                
                listener.onAdClosed(adapter, completeFinal);
            }
        });
    }
    
    @Override
    public void onAdStart() {
        Log.d(BuildConfig.LOG_TAG, "Ad start");
        
        completeTask = new CompleteTask();
        completeResult = executor.submit(completeTask);
        
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.onAdShowing(adapter);
            }
        });
    }
    
    @Override
    public void onAdUnavailable(final String reason) {
        Log.w(BuildConfig.LOG_TAG, "Ad unavailable: " + reason);
        
        handler.post(new Runnable() {
            @Override
            public void run() {
                listener.onAdFailedToLoad(
                        adapter,
                        AdRequestResult.Error,
                        "Vungle ad unavailable: " + reason);
            }
        });
    }
    
    @Override
    public synchronized void onAdPlayableChanged(final boolean isAdPlayable) {
        Log.d(BuildConfig.LOG_TAG, "Ad playable changed to "
                + isAdPlayable);
        
        available = isAdPlayable;
    }
    
    @Override
    public void onVideoView(
            final boolean isCompletedView,
            int watchedMillis, 
            int videoMillis) {
        
        completeTask.setResult(isCompletedView);
    }
    
    synchronized boolean isAvailable() {
        return available;
    }
    
    private static final class CompleteTask implements Callable<Boolean> {
        
        private final CountDownLatch latch = new CountDownLatch(1);
        private final AtomicBoolean complete = new AtomicBoolean();
        
        @Override
        public Boolean call() throws Exception {
            latch.await();
            return complete.get();
        }
        
        void setResult(boolean complete) {
            this.complete.set(complete);
            latch.countDown();
        }
    }
}
