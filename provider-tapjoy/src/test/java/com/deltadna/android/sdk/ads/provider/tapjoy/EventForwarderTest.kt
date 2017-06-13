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

package com.deltadna.android.sdk.ads.provider.tapjoy

import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.nhaarman.mockito_kotlin.*
import com.tapjoy.TJError
import com.tapjoy.TJPlacement
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class EventForwarderTest {
    
    private val adapter = mock<MediationAdapter>()
    private val listener = mock<MediationListener>()
    private val placement = { p: String -> mock<TJPlacement>().apply {
        whenever(name).then { p }
    }}
    
    private var uut = EventForwarder(adapter, listener, PLACEMENT)
    
    @Before
    fun before() {
        uut = EventForwarder(adapter, listener, PLACEMENT)
    }
    
    @After
    fun after() {
        reset(adapter, listener)
    }
    
    @Test
    fun notCompletedRewarded() {
        with (placement(PLACEMENT)) {
            uut.onRequestSuccess(this)
            uut.onContentReady(this)
            uut.onContentShow(this)
            uut.onVideoStart(this)
            uut.onContentDismiss(this)
        }
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(false))
        }
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun completedRewarded() {
        with (placement(PLACEMENT)) {
            uut.onRequestSuccess(this)
            uut.onContentReady(this)
            uut.onContentShow(this)
            uut.onVideoStart(this)
            uut.onVideoComplete(this)
            uut.onContentDismiss(this)
        }
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun completedInterstitial() {
        with (placement(PLACEMENT)) {
            uut.onRequestSuccess(this)
            uut.onContentReady(this)
            uut.onContentShow(this)
            uut.onContentDismiss(this)
        }
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun failed() {
        with (placement(PLACEMENT)) {
            uut.onRequestFailure(this, TJError(1, "message"))
        }
        
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.NoFill),
                any())
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun checksPlacement() {
        with(placement("other")) {
            uut.onRequestSuccess(this)
            uut.onContentReady(this)
            uut.onContentShow(this)
            uut.onVideoStart(this)
            uut.onVideoComplete(this)
            uut.onContentDismiss(this)
        }
        
        verifyZeroInteractions(listener)
    }
    
    private companion object {
        
        val PLACEMENT = "placement"
    }
}
