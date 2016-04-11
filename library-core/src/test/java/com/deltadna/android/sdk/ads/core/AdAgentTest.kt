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
import android.os.Build
import com.deltadna.android.sdk.ads.bindings.AdClosedResult
import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.google.common.truth.Truth.assertThat
import org.json.JSONObject
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.CountDownLatch

@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class,
        sdk = intArrayOf(Build.VERSION_CODES.LOLLIPOP))
class AdAgentTest {
    
    private val listener = mock(AdAgentListener::class.java)
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
            }.`when`(adapters[0]).requestAd(activity, this, config)
            
            requestAd(activity, config)
            
            assertThat(isAdLoaded)
            verify(listener).onAdLoaded(same(this), same(adapters[0]), anyLong())
            verifyNoMoreInteractions(listener)
        }
    }
    
    @Test
    fun requestAdLoadFails() {
        withAgent(spiedAdapters(1)) { adapters ->
            doAnswer {
                onAdFailedToLoad(adapters[0], AdRequestResult.NoFill, "no fill")
            }.`when`(adapters[0]).requestAd(activity, this, config)
            
            requestAd(activity, config)
            
            assertThat(!isAdLoaded)
            verify(listener).onAdFailedToLoad(
                    same(this), 
                    same(adapters[0]),
                    eq("no fill"),
                    anyLong(),
                    same(AdRequestResult.NoFill))
            verifyNoMoreInteractions(listener)
        }
    }
    
    @Test
    fun requestAdLoadFailsRequestsFromNext() {
        withAgent(spiedAdapters(2)) { adapters ->
            doAnswer {
                onAdFailedToLoad(adapters[0], AdRequestResult.NoFill, "no fill")
            }.`when`(adapters[0]).requestAd(activity, this, config)
            doAnswer {
                onAdLoaded(adapters[1])
            }.`when`(adapters[1]).requestAd(activity, this, config)
            
            requestAd(activity, config)
            
            assertThat(isAdLoaded)
            verify(listener).onAdLoaded(same(this), same(adapters[1]), anyLong())
        }
    }
    
    @Test
    fun requestAdLoadTimesOut() {
        withAgent(spiedAdapters(2)) { adapters ->
            val latch = CountDownLatch(1)
            doAnswer {
                // fake a timeout
                Thread({
                    Thread.sleep(15000 + 500)
                    latch.countDown()
                }).start()
            }.`when`(adapters[0]).requestAd(activity, this, config)
            doAnswer {
                onAdLoaded(adapters[1])
            }.`when`(adapters[1]).requestAd(activity, this, config)
            
            requestAd(activity, config)
            latch.await()
            
            Robolectric.flushForegroundThreadScheduler()
            
            assertThat(isAdLoaded)
            inOrder(listener) {
                verify(listener).onAdFailedToLoad(
                        same(this@withAgent),
                        same(adapters[0]),
                        anyString(),
                        anyLong(),
                        same(AdRequestResult.Timeout))
                verify(listener).onAdLoaded(
                        same(this@withAgent),
                        same(adapters[1]),
                        anyLong())
            }
        }
    }
    
    @Test
    fun requestAdTwiceFromSameAdapter() {
        withAgent(spiedAdapters(2), 2) { adapters ->
            doAnswer {
                onAdLoaded(adapters[0])
                onAdClosed(adapters[0], true)
            }.`when`(adapters[0]).requestAd(activity, this, config)
            doAnswer {
                onAdLoaded(adapters[1])
            }.`when`(adapters[1]).requestAd(activity, this, config)
            
            requestAd(activity, config)
            
            inOrder(listener) {
                // first adapter first run
                verify(listener).onAdLoaded(
                        same(this@withAgent), same(adapters[0]), anyLong())
                verify(listener).onAdClosed(
                        same(this@withAgent), same(adapters[0]), eq(true))
                // first adapter second run
                verify(listener).onAdLoaded(
                        same(this@withAgent), same(adapters[0]), anyLong())
                verify(listener).onAdClosed(
                        same(this@withAgent), same(adapters[0]), eq(true))
                // second adapter
                verify(listener).onAdLoaded(
                        same(this@withAgent), same(adapters[1]), anyLong())
            }
        }
    }
    
    @Test
    fun showAdWhenLoaded() {
        withAgent(spiedAdapters(1)) { adapters ->
            doAnswer {
                onAdLoaded(adapters[0])
                onAdShowing(adapters[0])
            }.`when`(adapters[0]).requestAd(activity, this, config)
            
            requestAd(activity, config)
            showAd("adpoint")
            
            assertThat(!isAdLoaded)
            inOrder(listener) {
                verify(listener).onAdLoaded(
                        same(this@withAgent), same(adapters[0]), anyLong())
                verify(listener).onAdOpened(
                        same(this@withAgent), same(adapters[0]))
            }
        }
    }
    
    @Test
    fun showAdWhenNotLoaded() {
        withAgent(spiedAdapters(1)) { adapters ->
            showAd("adpoint")
            
            verify(listener).onAdFailedToOpen(
                    same(this),
                    same(adapters[0]),
                    anyString(), same(AdClosedResult.NOT_READY))
        }
    }
    
    @Test
    fun showAdFails() {
        withAgent(spiedAdapters(2)) { adapters ->
            doAnswer {
                onAdLoaded(adapters[0])
            }.`when`(adapters[0]).requestAd(activity, this, config)
            doAnswer {
                onAdFailedToShow(adapters[0], AdClosedResult.ERROR)
            }.`when`(adapters[0]).showAd()
            doAnswer {
                onAdLoaded(adapters[1])
            }.`when`(adapters[1]).requestAd(activity, this, config)
            
            requestAd(activity, config)
            showAd("adpoint")
            
            inOrder(listener) {
                verify(listener).onAdFailedToOpen(
                        same(this@withAgent),
                        same(adapters[0]),
                        anyString(),
                        same(AdClosedResult.ERROR))
                verify(listener).onAdLoaded(
                        same(this@withAgent), same(adapters[1]), anyLong())
            }
        }
    }
    
    private fun withAgent(
            adapters: List<MediationAdapter>,
            maxPerNetwork: Int = 1,
            block: AdAgent.(List<MediationAdapter>) -> Unit) {
        block.invoke(
                AdAgent(listener, Waterfall(adapters), maxPerNetwork),
                adapters)
    }
    
    private fun spiedAdapters(n: Int) = stubbedAdapters(n).map { spy(it) }
}
