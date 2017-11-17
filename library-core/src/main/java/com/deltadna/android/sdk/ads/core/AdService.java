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

@UnityInterOp
public interface AdService {
    
    void init(boolean sessionUpdated, String decisionPoint);
    
    boolean isInterstitialAdAllowed(
            @Nullable String decisionPoint,
            @Nullable JSONObject engagementParameters);
    boolean isRewardedAdAllowed(
            @Nullable String decisionPoint,
            @Nullable JSONObject engagementParameters);
    
    boolean isInterstitialAdAvailable();
    boolean isRewardedAdAvailable();
    
    void showInterstitialAd(@Nullable String adPoint);
    void showRewardedAd(@Nullable String adPoint);
    
    void onPause();
    void onResume();
    void onDestroy();
}
