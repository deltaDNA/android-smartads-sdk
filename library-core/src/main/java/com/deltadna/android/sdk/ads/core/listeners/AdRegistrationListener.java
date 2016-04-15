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

package com.deltadna.android.sdk.ads.core.listeners;

/**
 * Listener for ad registration events.
 */
public interface AdRegistrationListener {
    
    /**
     * Called when registering for interstitial ads has succeeded.
     */
    void onRegisteredForInterstitial();
    
    /**
     * Called when registering for interstitial ads has failed.
     *
     * @param reason the reason for the failure
     */
    void onFailedToRegisterForInterstitial(String reason);
    
    /**
     * Called when registering for rewarded ads has succeeded.
     */
    void onRegisteredForRewarded();
    
    /**
     * Called when registering for rewarded ads has failed.
     *
     * @param reason the reason for the failure
     */
    void onFailedToRegisterForRewarded(String reason);
}
