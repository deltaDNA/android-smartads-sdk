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

package com.deltadna.android.sdk.ads.core

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ExceptionHandlerTest {
    
    private lateinit var uut: ExceptionHandler
    
    @Before
    fun before() {
        uut = ExceptionHandler(RuntimeEnvironment.application, "1", 1, "1", "1")
    }
    
    @Test
    fun `does not capture anything without match`() {
        uut.uncaughtException(
                Thread.currentThread(),
                mock<Throwable>().apply {
                    whenever(cause).then { this }
                    whenever(stackTrace).then {
                        arrayOf(element(String::class.qualifiedName!!),
                                element(Integer::class.qualifiedName!!))
                }})
        
        AdProvider.values().forEach {
            assertThat(uut.listCrashes(it)).isEmpty()
        }
    }
    
    @Ignore // fails through `gradlew check`, works in AS
    @Test
    fun `captures adapter exception when class found`() {
        uut.uncaughtException(
                Thread.currentThread(),
                mock<Throwable>().apply {
                    whenever(cause).then { this }
                    whenever(stackTrace).then {
                        arrayOf(element(String::class.qualifiedName!!),
                                element(AdProvider.CHARTBOOST_REWARDED.cls),
                                element(Integer::class.qualifiedName!!))
                }})
        
        assertThat(uut.listCrashes(AdProvider.CHARTBOOST_REWARDED)).hasSize(1)
        AdProvider
                .values()
                .filter { it != AdProvider.CHARTBOOST_REWARDED }
                .forEach { assertThat(uut.listCrashes(it)).isEmpty() }
    }
    
    @Ignore // fails through `gradlew check`, works in AS
    @Test
    fun `captures network exception when namespace found`() {
        uut.uncaughtException(
                Thread.currentThread(),
                mock<Throwable>().apply {
                    whenever(cause).then { this }
                    whenever(stackTrace).then {
                        arrayOf(element(String::class.qualifiedName!!),
                                element(AdProvider.CHARTBOOST.namespace),
                                element(Integer::class.qualifiedName!!))
                }})
        
        assertThat(uut.listCrashes(AdProvider.CHARTBOOST)).hasSize(1)
        assertThat(uut.listCrashes(AdProvider.CHARTBOOST_REWARDED)).hasSize(1)
        AdProvider
                .values()
                .asIterable()
                .minus(arrayOf(AdProvider.CHARTBOOST, AdProvider.CHARTBOOST_REWARDED))
                .forEach { assertThat(uut.listCrashes(it)).isEmpty() }
    }
    
    @Suppress("NestedLambdaShadowedImplicitParameter")
    @Ignore // fails through `gradlew check`, works in AS
    @Test
    fun `captures cause of exception when namespace found`() {
        uut.uncaughtException(
                Thread.currentThread(),
                mock<Throwable>().apply {
                    whenever(cause).then { mock<Throwable>().apply {
                        whenever(cause).then { this }
                        whenever(stackTrace).then {
                            arrayOf(element(String::class.qualifiedName!!),
                                    element(AdProvider.CHARTBOOST.namespace),
                                    element(Integer::class.qualifiedName!!))
                        }
                    }}
                    whenever(stackTrace).then {
                        arrayOf(element(String::class.qualifiedName!!),
                                element(Integer::class.qualifiedName!!))
                    }})
        
        assertThat(uut.listCrashes(AdProvider.CHARTBOOST)).hasSize(1)
        assertThat(uut.listCrashes(AdProvider.CHARTBOOST_REWARDED)).hasSize(1)
        AdProvider
                .values()
                .asIterable()
                .minus(arrayOf(AdProvider.CHARTBOOST, AdProvider.CHARTBOOST_REWARDED))
                .forEach { assertThat(uut.listCrashes(it)).isEmpty() }
    }
    
    @Suppress("NestedLambdaShadowedImplicitParameter")
    @Ignore // fails through `gradlew check`, works in AS
    @Test
    fun `captures exception and cause when namespace found`() {
        uut.uncaughtException(
                Thread.currentThread(),
                mock<Throwable>().apply {
                    whenever(cause).then { mock<Throwable>().apply {
                        whenever(cause).then { this }
                        whenever(stackTrace).then {
                            arrayOf(element(String::class.qualifiedName!!),
                                    element(AdProvider.CHARTBOOST.namespace),
                                    element(Integer::class.qualifiedName!!))
                        }
                    }}
                    whenever(stackTrace).then {
                        arrayOf(element(String::class.qualifiedName!!),
                                element(AdProvider.CHARTBOOST.namespace),
                                element(Integer::class.qualifiedName!!))
                    }})
        
        assertThat(uut.listCrashes(AdProvider.CHARTBOOST)).hasSize(2)
        assertThat(uut.listCrashes(AdProvider.CHARTBOOST_REWARDED)).hasSize(2)
        AdProvider
                .values()
                .asIterable()
                .minus(arrayOf(AdProvider.CHARTBOOST, AdProvider.CHARTBOOST_REWARDED))
                .forEach { assertThat(uut.listCrashes(it)).isEmpty() }
    }
    
    private fun element(cls: String) = StackTraceElement(
            cls,
            "method",
            "fileName",
            0)
}
