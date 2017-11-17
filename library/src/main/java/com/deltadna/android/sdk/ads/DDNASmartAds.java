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

package com.deltadna.android.sdk.ads;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.deltadna.android.sdk.ads.listeners.AdRegistrationListener;

import java.lang.ref.WeakReference;

/**
 * Singleton class for accessing the deltaDNA SmartAds SDK.
 * <p>
 * An instance should be retrieved by calling {@link #instance()}.
 * {@link com.deltadna.android.sdk.DDNA} should be initialised and started
 * before registering for ads through {@link #registerForAds(Activity)}. At
 * the point where you would like to display an ad, use one of the static
 * {@code create} methods in the {@link InterstitialAd}/{@link RewardedAd}
 * classes.
 * <p>
 * Listening for ad registration success or failure can be done using
 * {@link #setAdRegistrationListener(AdRegistrationListener)}.
 */
public final class DDNASmartAds {
    
    private static DDNASmartAds instance = null;
    
    @Nullable
    private Ads ads;
    
    private WeakReference<AdRegistrationListener> registrationListener =
            new WeakReference<>(null);
    
    /**
     * Returns the {@link DDNASmartAds} singleton instance, initialising it if
     * called the first time.
     *
     * @return singleton instance
     */
    public static synchronized DDNASmartAds instance() {
        if (instance == null) {
            instance = new DDNASmartAds();
        }
        
        return instance;
    }
    
    /**
     * Registers for ads.
     *
     * @param activity the activity to register with ads
     */
    public void registerForAds(Activity activity) {
        if (ads == null) {
            ads = new Ads(activity);
        }
        
        ads.setAdRegistrationListener(registrationListener.get());
        ads.registerForAds(false);
    }
    
    /**
     * Sets a listener for ad registration status callbacks.
     * <p>
     * The listener is stored as a weak reference so the instance should be
     * attached to an object with a longer lifecycle, for example an
     * {@link Activity}.
     *
     * @param listener the listener for ad registration status
     */
    public void setAdRegistrationListener(
            @Nullable AdRegistrationListener listener) {
        
        registrationListener = new WeakReference<>(listener);
        
        if (ads != null) {
            ads.setAdRegistrationListener(listener);
        }
    }
    
    public void onPause() {
        if (ads != null) {
            ads.onPause();
        }
    }
    
    public void onResume() {
        if (ads != null) {
            ads.onResume();
        }
    }
    
    public void onDestroy() {
        if (ads != null) {
            ads.onDestroy();
            ads = null;
        }
    }
    
    @Nullable
    Ads getAds() {
        return ads;
    }
    
    private DDNASmartAds() {}
}
