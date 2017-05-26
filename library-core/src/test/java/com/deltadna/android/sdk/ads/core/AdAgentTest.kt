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

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import com.deltadna.android.sdk.ads.bindings.AdClosedResult
import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.*
import org.json.JSONObject
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class,
        sdk = intArrayOf(Build.VERSION_CODES.LOLLIPOP))
class AdAgentTest {
    
    private val listener = mock<AdAgentListener>()
    private val activity = Robolectric.setupActivity(Activity::class.java)
    private val config = JSONObject()
    
    @After
    fun after() {
        reset(listener)
    }
    
    @Test
    fun requestAdLoadSucceeds() {
        withAgent(spiedAdapters(1)) { adapters ->
            doAnswer {
                onAdLoaded(adapters[0])
            }.whenever(adapters[0]).requestAd(activity, this, config)
            
            requestAd(activity, config)
            
            assertThat(isAdLoaded).isTrue()
            assertThat(adapters[0].requests).isEqualTo(1)
            verify(listener).onAdLoaded(same(this), same(adapters[0]), any())
            verifyNoMoreInteractions(listener)
        }
    }
    
    @Test
    fun requestAdLoadFails() {
        withAgent(spiedAdapters(1)) { adapters ->
            doAnswer {
                onAdFailedToLoad(adapters[0], AdRequestResult.NoFill, "no fill")
            }.whenever(adapters[0]).requestAd(activity, this, config)
            
            requestAd(activity, config)
            
            assertThat(isAdLoaded).isFalse()
            assertThat(adapters[0].requests).isEqualTo(0)
            verify(listener).onAdFailedToLoad(
                    same(this), 
                    same(adapters[0]),
                    any(),
                    any(),
                    eq(AdRequestResult.NoFill))
            verifyNoMoreInteractions(listener)
        }
    }
    
    @Test
    fun requestAdLoadFailsRequestsFromNext() {
        withAgent(spiedAdapters(2)) { adapters ->
            doAnswer {
                onAdFailedToLoad(adapters[0], AdRequestResult.NoFill, "no fill")
            }.whenever(adapters[0]).requestAd(activity, this, config)
            doAnswer {
                onAdLoaded(adapters[1])
            }.whenever(adapters[1]).requestAd(activity, this, config)
            
            requestAd(activity, config)
            
            assertThat(isAdLoaded).isTrue()
            verify(listener).onAdLoaded(same(this), same(adapters[1]), any())
        }
    }
    
    @Test
    fun requestAdLoadTimesOut() {
        withAgent(spiedAdapters(2)) { adapters ->
            doAnswer {
                // fake timeout on adapter[0]
            }.whenever(adapters[0]).requestAd(activity, this, config)
            doAnswer {
                onAdLoaded(adapters[1])
            }.whenever(adapters[1]).requestAd(activity, this, config)
            
            requestAd(activity, config)
            
            Robolectric.getForegroundThreadScheduler().advanceBy(15000)
            
            assertThat(isAdLoaded).isTrue()
            inOrder(listener) {
                verify(listener).onAdFailedToLoad(
                        same(this@withAgent),
                        same(adapters[0]),
                        any(),
                        any(),
                        eq(AdRequestResult.Timeout))
                verify(listener).onAdLoaded(
                        same(this@withAgent),
                        same(adapters[1]),
                        any())
            }
        }
    }
    
    @Test
    fun requestAdTwiceFromSameAdapter() {
        withAgent(spiedAdapters(2), 2) { adapters ->
            doAnswer {
                onAdLoaded(adapters[0])
                onAdClosed(adapters[0], true)
            }.whenever(adapters[0]).requestAd(activity, this, config)
            doAnswer {
                onAdLoaded(adapters[1])
            }.whenever(adapters[1]).requestAd(activity, this, config)
            
            requestAd(activity, config)
            
            assertThat(adapters[0].requests).isEqualTo(2)
            inOrder(listener) {
                // first adapter first run
                verify(listener).onAdLoaded(
                        same(this@withAgent), same(adapters[0]), any())
                verify(listener).onAdClosed(
                        same(this@withAgent), same(adapters[0]), eq(true))
                // first adapter second run
                verify(listener).onAdLoaded(
                        same(this@withAgent), same(adapters[0]), any())
                verify(listener).onAdClosed(
                        same(this@withAgent), same(adapters[0]), eq(true))
                // second adapter
                verify(listener).onAdLoaded(
                        same(this@withAgent), same(adapters[1]), any())
            }
        }
    }
    
    @Test
    fun requestAdUnexpectedAdapterReportsLoaded() {
        withAgent(spiedAdapters(2)) { adapters ->
            doAnswer {
                onAdLoaded(adapters[1])
                onAdLoaded(adapters[0])
            }.whenever(adapters[0]).requestAd(activity, this, config)
            
            requestAd(activity, config)
            
            verify(listener).onAdLoaded(
                    same(this@withAgent), same(adapters[0]), any())
            verifyNoMoreInteractions(listener)
        }
    }
    
    @Test
    fun requestAdWithNetworkNotConnected() {
        with(Shadows.shadowOf(service<ConnectivityManager>(
                Context.CONNECTIVITY_SERVICE))) {
            val info = mock<NetworkInfo>()
            whenever(info.isConnected).thenReturn(false)
            activeNetworkInfo = info
        }
        
        withAgent(spiedAdapters(1)) { adapters ->
            requestAd(activity, config)
            
            assertThat(isAdLoaded).isFalse()
            verify(listener).onAdFailedToLoad(
                    same(this),
                    same(adapters[0]),
                    any(),
                    any(),
                    eq(AdRequestResult.Network))
        }
    }
    
    @Test
    fun requestAdWithoutNetwork() {
        with(Shadows.shadowOf(service<ConnectivityManager>(
                Context.CONNECTIVITY_SERVICE))) {
            activeNetworkInfo = null
        }
        
        withAgent(spiedAdapters(1)) { adapters ->
            requestAd(activity, config)
            
            assertThat(isAdLoaded).isFalse()
            verify(listener).onAdFailedToLoad(
                    same(this),
                    same(adapters[0]),
                    any(),
                    any(),
                    eq(AdRequestResult.Network))
        }
    }
    
    @Test
    fun requestAdMaxPerSessionReached() {
        withAgent(spiedAdapters(1), maxPerSession = 1) { adapters ->
            doAnswer {
                onAdLoaded(adapters[0])
                onAdShowing(adapters[0])
                onAdClosed(adapters[0], true)
            }.whenever(adapters[0]).requestAd(activity, this, config)
            
            requestAd(activity, config)
            
            assertThat(isAdLoaded).isFalse()
            verify(listener).onAdLoaded(same(this), same(adapters[0]), any())
            verify(listener).onAdOpened(same(this), same(adapters[0]))
            verify(listener).onAdClosed(same(this), same(adapters[0]), any())
            verifyNoMoreInteractions(listener)
        }
    }
    
    @Test
    fun showAdWhenLoaded() {
        withAgent(spiedAdapters(1)) { adapters ->
            doAnswer {
                onAdLoaded(adapters[0])
                onAdShowing(adapters[0])
            }.whenever(adapters[0]).requestAd(activity, this, config)
            
            requestAd(activity, config)
            showAd("adpoint")
            
            assertThat(isAdLoaded).isFalse()
            inOrder(listener) {
                verify(listener).onAdLoaded(
                        same(this@withAgent), same(adapters[0]), any())
                verify(listener).onAdOpened(
                        same(this@withAgent), same(adapters[0]))
            }
        }
    }
    
    @Test
    fun showAdCalledTwiceReportedOnlyOnce() {
        withAgent(spiedAdapters(1)) { adapters ->
            doAnswer {
                onAdLoaded(adapters[0])
                onAdShowing(adapters[0])
                onAdShowing(adapters[0])
            }.whenever(adapters[0]).requestAd(activity, this, config)
            
            requestAd(activity, config)
            showAd("adpoint")
            
            verify(listener).onAdOpened(same(this@withAgent), same(adapters[0]))
        }
    }
    
    @Test
    fun showAdWhenNotLoaded() {
        withAgent(spiedAdapters(1)) { adapters ->
            showAd("adpoint")
            
            verify(listener).onAdFailedToOpen(
                    same(this),
                    same(adapters[0]),
                    any(),
                    eq(AdClosedResult.NOT_READY))
        }
    }
    
    @Test
    fun showAdFails() {
        withAgent(spiedAdapters(2)) { adapters ->
            doAnswer {
                onAdLoaded(adapters[0])
            }.whenever(adapters[0]).requestAd(activity, this, config)
            doAnswer {
                onAdFailedToShow(adapters[0], AdClosedResult.ERROR)
            }.whenever(adapters[0]).showAd()
            doAnswer {
                onAdLoaded(adapters[1])
            }.whenever(adapters[1]).requestAd(activity, this, config)
            
            requestAd(activity, config)
            showAd("adpoint")
            
            inOrder(listener) {
                verify(listener).onAdFailedToOpen(
                        same(this@withAgent),
                        same(adapters[0]),
                        any(),
                        eq(AdClosedResult.ERROR))
                verify(listener).onAdLoaded(
                        same(this@withAgent), same(adapters[1]), any())
            }
        }
    }
    
    @Test
    fun waitsWhenWaterfallExhausted() {
        withAgent(spiedAdapters(2)) { adapters ->
            doAnswer {
                onAdFailedToLoad(adapters[0], AdRequestResult.NoFill, "")
            }.whenever(adapters[0]).requestAd(activity, this, config)
            doAnswer {
                onAdFailedToLoad(adapters[1], AdRequestResult.NoFill, "")
            }.whenever(adapters[1]).requestAd(activity, this, config)
            
            requestAd(activity, config)
            
            verify(listener).onAdFailedToLoad(
                    same(this),
                    same(adapters[0]),
                    any(),
                    any(),
                    eq(AdRequestResult.NoFill))
            verify(listener).onAdFailedToLoad(
                    same(this),
                    same(adapters[1]),
                    any(),
                    any(),
                    eq(AdRequestResult.NoFill))
            verifyNoMoreInteractions(listener)
            
            reset(listener)
            with(Robolectric.getForegroundThreadScheduler()) {
                assertThat(size()).isEqualTo(1)
                advanceBy(60000)
            }
            
            verify(listener).onAdFailedToLoad(
                    same(this),
                    same(adapters[0]),
                    any(),
                    any(),
                    eq(AdRequestResult.NoFill))
            verify(listener).onAdFailedToLoad(
                    same(this),
                    same(adapters[1]),
                    any(),
                    any(),
                    eq(AdRequestResult.NoFill))
            verifyNoMoreInteractions(listener)
        }
    }
    
    private fun withAgent(
            adapters: List<MediationAdapter>,
                maxPerNetwork: Int = 1,
            maxPerSession: Int = -1,
            block: AdAgent.(List<MediationAdapter>) -> Unit) {
        block.invoke(
                AdAgent(listener,
                        Waterfall(adapters, maxPerNetwork),
                        maxPerSession),
                adapters)
    }
    
    private fun spiedAdapters(n: Int) = stubbedAdapters(n).map { spy(it) }
}
