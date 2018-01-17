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

import android.util.Log;

import com.deltadna.android.sdk.ads.bindings.MediationAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class WaterfallFactory {
    
    static Waterfall create(
            JSONArray providers,
            int adFloorPrice,
            int demoteOnCode,
            int maxRequests,
            AdProviderType type) {
        
        final List<MediationAdapter> adapters =
                new ArrayList<>(providers.length());
        
        for (int i = 0; i < providers.length(); i++) {
            final JSONObject config;
            final AdProvider provider;
            try {
                config = providers.getJSONObject(i);
                provider = AdProvider.valueOf(config, type);
            } catch (JSONException | IllegalArgumentException e) {
                Log.w(BuildConfig.LOG_TAG,
                        String.format(
                                Locale.US,
                                "Failed to find ad network at index %d",
                                i),
                        e);
                continue;
            }
            
            if (!provider.present()) {
                Log.d(BuildConfig.LOG_TAG,
                        "Ad network " + provider + " is not built into app");
                continue;
            }
            
            try {
                final int eCpm = config.getInt("eCPM");
                if (eCpm > adFloorPrice) {
                    adapters.add(provider.createAdapter(
                            eCpm,
                            adFloorPrice,
                            demoteOnCode,
                            i,
                            config));
                    
                    Log.d(BuildConfig.LOG_TAG, "Added ad network " + provider);
                } else {
                    Log.d(BuildConfig.LOG_TAG,
                            "Ad network " + provider + " not being added as eCPM < adFloorPrice");
                }
            } catch (JSONException e) {
                Log.w(BuildConfig.LOG_TAG,
                        "Failed to build adapter for ad network " + provider,
                        e);
            }
        }
        
        return new Waterfall(adapters, maxRequests);
    }
    
    private WaterfallFactory() {}
}
