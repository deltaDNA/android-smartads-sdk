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

import android.support.annotation.Nullable;

import org.json.JSONObject;

import java.util.Date;

@UnityInterOp
public interface AdService {
    
    void registerForAds(String decisionPoint);
    void onNewSession();
    
    boolean isInterstitialAdAllowed(
            @Nullable String decisionPoint,
            @Nullable JSONObject parameters,
            boolean checkTime);
    boolean isRewardedAdAllowed(
            @Nullable String decisionPoint,
            @Nullable JSONObject parameters,
            boolean checkTime);
    int timeUntilRewardedAdAllowed(
            @Nullable String decisionPoint,
            @Nullable JSONObject parameters);
    
    boolean hasLoadedInterstitialAd();
    boolean hasLoadedRewardedAd();
    
    void showInterstitialAd(
            @Nullable String decisionPoint,
            @Nullable JSONObject parameters);
    void showRewardedAd(
            @Nullable String decisionPoint,
            @Nullable JSONObject parameters);
    
    @Nullable
    Date getLastShown(String decisionPoint);
    int getSessionCount(String decisionPoint);
    int getDailyCount(String decisionPoint);
    
    void onPause();
    void onResume();
    void onDestroy();
}
