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

package com.deltadna.android.sdk.ads

import com.deltadna.android.sdk.Engagement
import com.deltadna.android.sdk.ads.core.listeners.InterstitialAdsListener
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class InterstitialAdTest {
    
    @Test
    fun create() {
        assertThat(InterstitialAd.create())
                .isNotNull()
        assertThat(InterstitialAd.create(mock<InterstitialAdsListener>()))
                .isNotNull()
    }
    
    @Test
    fun createWithFailedEngagement() {
        with(mock<Engagement<*>>()) {
            whenever(isSuccessful()).thenReturn(false)
            
            assertThat(InterstitialAd.create(this)).isNotNull()
        }
    }
    
    @Test
    fun createWithMissingParameters() {
        with(mock<Engagement<*>>()) {
            whenever(isSuccessful()).thenReturn(true)
            val json = with(mock<JSONObject>()) {
                whenever(has("parameters")).thenReturn(false)
                this
            }
            whenever(getJson()).thenReturn(json)
            
            assertThat(InterstitialAd.create(this)).isNotNull()
        }
    }
    
    @Test
    fun createWithoutAdShowPoint() {
        with(mock<Engagement<*>>()) {
            whenever(isSuccessful()).thenReturn(true)
            val parameters = with(mock<JSONObject>()) {
                whenever(has("adShowPoint")).thenReturn(false)
                this
            }
            val json = with(mock<JSONObject>()) {
                whenever(has("parameters")).thenReturn(true)
                whenever(getJSONObject("parameters")).thenReturn(parameters)
                this
            }
            whenever(getJson()).thenReturn(json)
            
            assertThat(InterstitialAd.create(this)).isNotNull()
        }
    }
    
    @Test
    fun createWithAdShowPointFalse() {
        with(mock<Engagement<*>>()) {
            whenever(isSuccessful()).thenReturn(true)
            val parameters = with(mock<JSONObject>()) {
                whenever(has("adShowPoint")).thenReturn(true)
                whenever(getBoolean("adShowPoint")).thenReturn(false)
                this
            }
            val json = with(mock<JSONObject>()) {
                whenever(has("parameters")).thenReturn(true)
                whenever(getJSONObject("parameters")).thenReturn(parameters)
                this
            }
            whenever(getJson()).thenReturn(json)
            
            assertThat(InterstitialAd.create(this)).isNull()
        }
    }
    
    @Test
    fun createWithAdShowPointTrue() {
        with(mock<Engagement<*>>()) {
            whenever(isSuccessful()).thenReturn(true)
            val parameters = with(mock<JSONObject>()) {
                whenever(has("adShowPoint")).thenReturn(true)
                whenever(getBoolean("adShowPoint")).thenReturn(true)
                this
            }
            val json = with(mock<JSONObject>()) {
                whenever(has("parameters")).thenReturn(true)
                whenever(getJSONObject("parameters")).thenReturn(parameters)
                this
            }
            whenever(getJson()).thenReturn(json)
            
            assertThat(InterstitialAd.create(this)).isNotNull()
        }
    }
}
