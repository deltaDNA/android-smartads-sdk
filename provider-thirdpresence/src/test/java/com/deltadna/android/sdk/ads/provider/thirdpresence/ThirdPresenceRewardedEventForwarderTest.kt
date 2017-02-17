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

package com.deltadna.android.sdk.ads.provider.thirdpresence

import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.deltadna.android.sdk.ads.bindings.MediationAdapter
import com.deltadna.android.sdk.ads.bindings.MediationListener
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.*
import com.thirdpresence.adsdk.sdk.VideoAd
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ThirdPresenceRewardedEventForwarderTest {
    
    private val listener = mock<MediationListener>()
    private val adapter = mock<MediationAdapter>()
    private val ad = mock<ThirdPresenceRewardedEventForwarder>()
    
    private var uut = ThirdPresenceRewardedEventForwarder(listener, adapter)
    
    @Before
    fun before() {
        uut = ThirdPresenceRewardedEventForwarder(listener, adapter)
    }
    
    @After
    fun after() {
        reset(listener, adapter, ad)
    }
    
    @Test
    fun loadedWithoutCompleting() {
        uut.onAdEvent(VideoAd.Events.AD_LOADED, null, null, null)
        uut.onAdEvent(VideoAd.Events.AD_PLAYING, null, null, null)
        uut.onAdEvent(VideoAd.Events.AD_SKIPPED, null, null, null)
        uut.onAdEvent(VideoAd.Events.AD_STOPPED, null, null, null)
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
        }
        assertThat(uut.hasCompleted()).isFalse()
    }
    
    @Test
    fun loadedAndCompleted() {
        uut.onAdEvent(VideoAd.Events.AD_LOADED, null, null, null)
        uut.onAdEvent(VideoAd.Events.AD_PLAYING, null, null, null)
        uut.onAdEvent(VideoAd.Events.AD_VIDEO_COMPLETE, null, null, null)
        uut.onAdEvent(VideoAd.Events.AD_STOPPED, null, null, null)
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
        }
        assertThat(uut.hasCompleted()).isTrue()
    }
    
    @Test
    fun noFill() {
        uut.onAdEvent(VideoAd.Events.AD_ERROR, "No fill", null, null)
        
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.NoFill),
                eq("No fill"))
        assertThat(uut.hasCompleted()).isFalse()
    }
    
    @Test
    fun leftApplication() {
        uut.onAdEvent(VideoAd.Events.AD_LOADED, null, null, null)
        uut.onAdEvent(VideoAd.Events.AD_PLAYING, null, null, null)
        uut.onAdEvent(VideoAd.Events.AD_LEFT_APPLICATION, null, null, null)
        
        inOrder(listener) {
            verify(listener).onAdLoaded(same(adapter))
            verify(listener).onAdShowing(same(adapter))
            verify(listener).onAdLeftApplication(same(adapter))
        }
        assertThat(uut.hasCompleted()).isFalse()
    }
    
    @Test
    fun onError_noFill() {
        uut.onError(VideoAd.ErrorCode.NO_FILL, "message")
        
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.NoFill),
                eq("message"))
        assertThat(uut.hasCompleted()).isFalse()
    }
    
    @Test
    fun onError_network() {
        uut.onError(VideoAd.ErrorCode.NETWORK_FAILURE, "message1")
        uut.onError(VideoAd.ErrorCode.NETWORK_TIMEOUT, "message2")
        
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.Network),
                eq("message1"))
        verify(listener).onAdFailedToLoad(
                same(adapter),
                eq(AdRequestResult.Network),
                eq("message2"))
    }
    
    private fun inOrder(vararg mocks: Any, block: org.mockito.InOrder.() -> Unit) {
        block.invoke(org.mockito.Mockito.inOrder(*mocks))
    }
}
