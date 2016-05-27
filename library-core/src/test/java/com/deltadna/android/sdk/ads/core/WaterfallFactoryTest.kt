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

package com.deltadna.android.sdk.ads.core

import com.deltadna.android.sdk.ads.provider.adcolony.AdColonyAdapter
import com.deltadna.android.sdk.ads.provider.admob.AdMobAdapter
import com.deltadna.android.sdk.ads.provider.amazon.AmazonAdapter
import com.google.common.truth.Truth.assertThat
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class WaterfallFactoryTest {
    
    @Test(expected = IllegalArgumentException::class)
    fun noAdaptersBuilt() {
        WaterfallFactory.create(
                JSONArray(),
                1,
                0,
                1,
                AdProviderType.INTERSTITIAL)
    }
    
    @Test
    fun adaptersBuilt() {
        with(WaterfallFactory.create(
                JSONArray()
                        .put(buildAdColony(2))
                        .put(buildAdMob(1))
                        .put(buildAmazon(3)),
                1,
                0,
                1,
                AdProviderType.INTERSTITIAL)) {
            assertThat(adapters.size).isEqualTo(2)
            
            val first = resetAndGetFirst()!!
            assertThat(first).isInstanceOf(AdColonyAdapter::class.java)
            assertThat(first.eCPM).isEqualTo(2)
            assertThat(first.waterfallIndex).isEqualTo(0)
            
            val second = next!!
            assertThat(second).isNotEqualTo(first)
            assertThat(second).isInstanceOf(AmazonAdapter::class.java)
            assertThat(second.eCPM).isEqualTo(3)
            assertThat(second.waterfallIndex).isEqualTo(1)
            
            assertThat(next).isNull()
        }
    }
    
    @Test
    fun badAdapterConfig() {
        with(WaterfallFactory.create(
                JSONArray()
                        .put(buildInvalidProvider())
                        .put(buildAdMob(2))
                        .put(buildMisconfiguredProvider()),
                1,
                0,
                1,
                AdProviderType.INTERSTITIAL)) {
            assertThat(adapters.size).isEqualTo(1)
            assertThat(resetAndGetFirst()).isInstanceOf(AdMobAdapter::class.java)
        }
    }
    
    private fun buildAdColony(eCpm: Int) = JSONObject()
            .put("adProvider", AdProvider.ADCOLONY.legacyName())
            .put("eCPM", eCpm).put("appId", "appId")
            .put("clientOptions", "clientOptions")
            .put("zoneId", "zoneId")
    
    private fun buildAdMob(eCpm: Int) = JSONObject()
            .put("adProvider", AdProvider.ADMOB.legacyName())
            .put("eCPM", eCpm)
            .put("adUnitId", "adUnitId")
    
    private fun buildAmazon(eCpm: Int) = JSONObject()
            .put("adProvider", AdProvider.AMAZON.legacyName())
            .put("eCPM", eCpm)
            .put("appKey", "appKey")
    
    private fun buildInvalidProvider() = JSONObject()
            .put("adProvider", "invalid")
            .put("eCPM", 0)
    
    private fun buildMisconfiguredProvider() = JSONObject()
            .put("adProvider", AdProvider.ADCOLONY.legacyName())
            .put("eCPM", 0)
}
