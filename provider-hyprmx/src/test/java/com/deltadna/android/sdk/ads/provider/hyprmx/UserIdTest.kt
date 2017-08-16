/*
 * Copyright (c) 2017 deltaDNA Ltd. All rights reserved.
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

package com.deltadna.android.sdk.ads.provider.hyprmx

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.deltadna.android.sdk.ads.bindings.Preferences
import com.google.common.truth.Truth.*
import com.nhaarman.mockito_kotlin.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class UserIdTest {
    
    @Test
    fun getExisting() {
        assertThat(UserId.get(mock<Preferences>().apply {
            whenever(get()).then {
                mock<SharedPreferences>().apply {
                    whenever(contains(eq("user_id"))).then { true }
                    whenever(getString(eq("user_id"), isNull())).then { "value" }
                }
            }
        })).isEqualTo("value")
    }
    
    @Test
    @SuppressLint("CommitPrefEdits")
    fun getNew() {
        val captor = argumentCaptor<String>()
        var saved = false
        
        assertThat(UserId.get(mock<Preferences>().apply {
            whenever(get()).then {
                mock<SharedPreferences>().apply {
                    whenever(contains(eq("user_id"))).then { false }
                }
            }
            whenever(edit()).then { mock<SharedPreferences.Editor>().apply {
                whenever(putString(eq("user_id"), captor.capture())).then { this }
                whenever(apply()).then { saved = true; null }
            }}
        })).isEqualTo(captor.firstValue)
        assertThat(saved).isTrue()
    }
}
