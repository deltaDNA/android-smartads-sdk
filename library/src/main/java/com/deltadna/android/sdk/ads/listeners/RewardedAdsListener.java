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

package com.deltadna.android.sdk.ads.listeners;

import com.deltadna.android.sdk.ads.RewardedAd;

/**
 * Listener for events within the rewarded ads lifecycle.
 */
public interface RewardedAdsListener {
    
    /**
     * Called when a rewarded ad has been loaded and is ready to show.
     *
     * @param ad    the rewarded ad
     */
    void onLoaded(RewardedAd ad);
    
    /**
     * Called when a rewarded ad is no longer available to show.
     *
     * @param ad    the rewarded ad
     */
    void onExpired(RewardedAd ad);
    
    /**
     * Called when a rewarded ad has been opened.
     *
     * @param ad    the rewarded ad
     */
    void onOpened(RewardedAd ad);
    
    /**
     * Called when a rewarded ad has failed to open.
     *
     * @param ad        the rewarded ad
     * @param reason    the reason for the failure
     */
    void onFailedToOpen(RewardedAd ad, String reason);
    
    /**
     * Called when a rewarded as has closed.
     *
     * @param ad        the rewarded ad
     * @param completed whether the full ad was watched, and whether the reward
     *                  should be given
     */
    void onClosed(RewardedAd ad, boolean completed);
}
