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

package com.deltadna.android.sdk.ads.bindings

import android.app.Activity
import com.google.common.truth.Truth.assertThat
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MediationAdapterTest {
    
    @Test
    fun waterfallIndex() {
        with(StubbedAdapter(1)) {
            assertThat(this.waterfallIndex).isEqualTo(1)
            reset(2)
            assertThat(this.waterfallIndex).isEqualTo(2)
        }
    }
    
    @Test
    fun requests() {
        with(StubbedAdapter(0)) {
            assertThat(requests).isEqualTo(0)
            incrementRequests()
            assertThat(requests).isEqualTo(1)
            reset(0)
            assertThat(requests).isEqualTo(1)
        }
    }
    
    @Test
    fun comparisons() {
        assertThat(StubbedAdapter(0).compareTo(StubbedAdapter(0))).isEqualTo(0)
        assertThat(StubbedAdapter(1).compareTo(StubbedAdapter(0))).isEqualTo(1)
        assertThat(StubbedAdapter(0).compareTo(StubbedAdapter(1))).isEqualTo(-1)
        
        with(StubbedAdapter(0)) {
            updateScore(AdRequestResult.NoFill)
            assertThat(compareTo(StubbedAdapter(0))).isEqualTo(1)
        }
        with(StubbedAdapter(1)) {
            updateScore(AdRequestResult.NoFill)
            assertThat(compareTo(StubbedAdapter(0))).isEqualTo(1)
        }
        with(StubbedAdapter(0)) {
            updateScore(AdRequestResult.NoFill)
            assertThat(compareTo(StubbedAdapter(1))).isEqualTo(1)
        }
    }

    private inner class StubbedAdapter(waterfallIndex: Int) :
            MediationAdapter(0, 0b1, waterfallIndex) {
        
        override fun requestAd(
                activity: Activity?,
                listener: MediationListener?,
                configuration: JSONObject?) {
            throw UnsupportedOperationException()
        }
        
        override fun showAd() {
            throw UnsupportedOperationException()
        }
        
        override fun getProviderString(): String? {
            throw UnsupportedOperationException()
        }
        
        override fun getProviderVersionString(): String? {
            throw UnsupportedOperationException()
        }
        
        override fun onDestroy() {
            throw UnsupportedOperationException()
        }
        
        override fun onPause() {
            throw UnsupportedOperationException()
        }
        
        override fun onResume() {
            throw UnsupportedOperationException()
        }
    }
}
