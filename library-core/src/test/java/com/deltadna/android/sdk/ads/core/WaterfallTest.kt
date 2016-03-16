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

import com.deltadna.android.sdk.ads.bindings.AdRequestResult
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class WaterfallTest {
    
    @Test
    fun getAdapters() {
        with(Waterfall(emptyList())) {
            assertThat(adapters.isEmpty())
        }
        
        val stubs = stubbedAdapters(2)
        with(Waterfall(stubs)) {
            assertThat(adapters.size).isEqualTo(2)
            assertThat(adapters[0]).isSameAs(stubs[0])
            assertThat(adapters[1]).isSameAs(stubs[1])
        }
    }
    
    @Test
    fun resetAndGetFirst() {
        var stubs = stubbedAdapters(2)
        with(Waterfall(stubs)) {
            assertThat(resetAndGetFirst()).isSameAs(stubs[0])
            assertThat(adapters[0]).isSameAs(stubs[0])
            assertThat(adapters[1]).isSameAs(stubs[1])
        }
        
        stubs = stubbedAdapters(3)
        stubs[0].updateScore(AdRequestResult.NoFill)
        stubs[2].updateScore(AdRequestResult.NoFill)
        with(Waterfall(stubs)) {
            assertThat(resetAndGetFirst()).isSameAs(stubs[1])
            assertThat(adapters[1]).isSameAs(stubs[0])
            assertThat(adapters[2]).isSameAs(stubs[2])
        }
    }
    
    @Test
    fun getNext() {
        val stubs = stubbedAdapters(2)
        with(Waterfall(stubs)) {
            assertThat(next).isSameAs(stubs[1])
            assertThat(next).isNull()
        }
    }
    
    @Test
    fun removeAndGetNext() {
        val stubs = stubbedAdapters(2)
        with(Waterfall(stubs)) {
            assertThat(removeAndGetNext()).isSameAs(stubs[1])
            assertThat(adapters.size).isEqualTo(1)
            
            assertThat(removeAndGetNext()).isNull()
            assertThat(adapters.isEmpty())
        }
    }
}
