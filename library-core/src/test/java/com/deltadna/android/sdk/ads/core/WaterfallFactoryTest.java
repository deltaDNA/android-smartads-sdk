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

import com.deltadna.android.sdk.ads.bindings.MediationAdapter;
import com.deltadna.android.sdk.ads.provider.adcolony.AdColonyAdapter;
import com.deltadna.android.sdk.ads.provider.admob.AdMobAdapter;
import com.deltadna.android.sdk.ads.provider.amazon.AmazonAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public final class WaterfallFactoryTest {
    
    @Test
    public void noAdaptersBuilt() {
        final Waterfall waterfall = WaterfallFactory.create(
                new JSONArray(), 1, 0, AdProviderType.INTERSTITIAL);
        
        assertThat(waterfall.getAdapters().size()).isEqualTo(0);
        assertThat(waterfall.resetAndGetFirst()).isNull();
        assertThat(waterfall.getNext()).isNull();
    }
    
    @Test
    public void adaptersBuilt() throws JSONException {
        final Waterfall waterfall = WaterfallFactory.create(
                new JSONArray()
                        .put(buildAdColony(2))
                        .put(buildAdMob(1))
                        .put(buildAmazon(3)),
                1,
                0,
                AdProviderType.INTERSTITIAL);
        
        assertThat(waterfall.getAdapters().size()).isEqualTo(2);
        
        final MediationAdapter first = waterfall.resetAndGetFirst();
        assertThat(first).isInstanceOf(AdColonyAdapter.class);
        assertThat(first.eCPM).isEqualTo(2);
        assertThat(first.getWaterfallIndex()).isEqualTo(0);
        
        final MediationAdapter second = waterfall.getNext();
        assertThat(second).isNotEqualTo(first);
        assertThat(second).isInstanceOf(AmazonAdapter.class);
        assertThat(second.eCPM).isEqualTo(3);
        assertThat(second.getWaterfallIndex()).isEqualTo(1);
        
        assertThat(waterfall.getNext()).isNull();
    }
    
    @Test
    public void badAdapterConfig() throws JSONException {
        final Waterfall waterfall = WaterfallFactory.create(
                new JSONArray()
                        .put(buildInvalidProvider())
                        .put(buildAdMob(2))
                        .put(buildMisconfiguredProvider()),
                1,
                0,
                AdProviderType.INTERSTITIAL);
        
        assertThat(waterfall.getAdapters().size()).isEqualTo(1);
        assertThat(waterfall.resetAndGetFirst()).isInstanceOf(AdMobAdapter.class);
    }
    
    private static JSONObject buildAdColony(int eCpm) throws JSONException {
        return new JSONObject()
                .put("adProvider", AdProvider.ADCOLONY.legacyName())
                .put("eCPM", eCpm)
                .put("appId", "appId")
                .put("clientOptions", "clientOptions")
                .put("zoneId", "zoneId");
    }
    
    private static JSONObject buildAdMob(int eCpm) throws JSONException {
        return new JSONObject()
                .put("adProvider", AdProvider.ADMOB.legacyName())
                .put("eCPM", eCpm)
                .put("adUnitId", "adUnitId");
    }
    
    private static JSONObject buildAmazon(int eCpm) throws JSONException {
        return new JSONObject()
                .put("adProvider", AdProvider.AMAZON.legacyName())
                .put("eCPM", eCpm)
                .put("appKey", "appKey");
    }
    
    private static JSONObject buildInvalidProvider() throws JSONException {
        return new JSONObject()
                .put("adProvider", "invalid")
                .put("eCPM", 0);
    }
    
    private static JSONObject buildMisconfiguredProvider() throws JSONException {
        return new JSONObject()
                .put("adProvider", AdProvider.ADCOLONY.legacyName())
                .put("eCPM", 0);
    }
}
