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

package com.deltadna.android.sdk.ads.provider.vungle

import android.os.Build
import com.deltadna.android.sdk.ads.bindings.AdClosedResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class,
        sdk = intArrayOf(Build.VERSION_CODES.LOLLIPOP))
class VungleEventForwarderTest {
    
    private val listener = mock<MediationListener>()
    private val adapter = mock<MediationAdapter>()
    
    private var uut = VungleEventForwarder(listener, adapter)
    
    @Before
    fun before() {
        uut = VungleEventForwarder(listener, adapter)
    }
    
    @After
    fun after() {
        reset(listener, adapter)
    }
    
    @Test
    fun onAdUnavailable() {
        uut.onAdUnavailable("reason")
        
        verify(listener).onAdFailedToShow(
                same(adapter),
                eq(AdClosedResult.EXPIRED))
    }
    
    @Test
    fun notCompletedShowing() {
        uut.onAdStart()
        uut.onVideoView(false, 0, 0)
        uut.onAdEnd(false)
        
        with(inOrder(listener)) {
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(false))
        }
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun completedShowing() {
        uut.onAdStart()
        uut.onVideoView(true, 0, 0)
        uut.onAdEnd(false)
        
        with(inOrder(listener)) {
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
        verifyNoMoreInteractions(listener)
    }
    
    @Test
    fun completedShowingAndClicked() {
        uut.onAdStart()
        uut.onVideoView(true, 0, 0)
        uut.onAdEnd(true)
        
        with(inOrder(listener)) {
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdClicked(same(adapter))
            verify(listener).onAdClosed(same(adapter), eq(true))
        }
    }
}
