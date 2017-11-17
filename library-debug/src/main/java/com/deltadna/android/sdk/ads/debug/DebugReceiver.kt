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

package com.deltadna.android.sdk.ads.debug

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.deltadna.android.sdk.ads.bindings.Actions.*

class DebugReceiver : BroadcastReceiver() {
    
    private var interstitial = ""
    private var rewarded = ""
    
    override fun onReceive(context: Context, intent: Intent) {
        fun Int.load() = context.getString(this)
        fun Int.load(vararg args: Any) = context.getString(this, *args)
        
        val action = intent.action
        if (!action.isNullOrEmpty() && !hidden) {
            val notifications = context.getSystemService(
                    Context.NOTIFICATION_SERVICE) as NotificationManager
            
            if (action == DELETE_ACTION) {
                notifications.cancel(TAG, ID)
                hidden = true
            } else {
                val notification = NotificationCompat.Builder(context, CHANNEL)
                        .setSmallIcon(R.drawable.ddna_ic_stat_logo)
                        .setContentTitle(R.string.ddna_title.load())
                        .setDeleteIntent(PendingIntent.getBroadcast(
                                context,
                                DELETE_RC,
                                deleteIntent(context),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                        .setDefaults(0)
                        .setSound(null)
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    notification.setChannelId(channel(context).id)
                
                val agent = intent.getSerializableExtra(AGENT) as Agent?
                val text = when (action) {
                    SESSION_UPDATED -> R.string.ddna_session_updated.load()
                    
                    FAILED_TO_REGISTER -> intent.getStringExtra(REASON)
                    
                    LOADED -> R.string.ddna_loaded.load(
                            agent!!.name.toLowerCase(),
                            intent.getStringExtra(NETWORK))
                    
                    SHOWING -> R.string.ddna_showing.load(
                            agent!!.name.toLowerCase(),
                            intent.getStringExtra(NETWORK))
                    
                    SHOWN -> R.string.ddna_shown.load(
                            agent!!.name.toLowerCase(),
                            intent.getStringExtra(NETWORK))
                    
                    SHOWN_AND_LOADED -> R.string.ddna_shown_and_loaded.load(
                            agent!!.name.toLowerCase(),
                            intent.getStringExtra(NETWORK_SHOWN),
                            intent.getStringExtra(NETWORK_LOADED))
                    
                    else -> throw IllegalStateException("Unknown $action")
                }
                
                if (agent == null) {
                    notification.setContentText(text)
                } else {
                    when (agent) {
                        Agent.INTERSTITIAL -> interstitial = text
                        Agent.REWARDED -> rewarded = text
                    }
                    
                    notification.setStyle(NotificationCompat.BigTextStyle()
                            .bigText(when {
                                rewarded.isEmpty() -> interstitial
                                interstitial.isEmpty() -> rewarded
                                else -> "$interstitial\n$rewarded"
                            }))
                }
                
                notifications.notify(
                        TAG,
                        ID,
                        notification.build())
            }
        }
    }
    
    @TargetApi(Build.VERSION_CODES.O)
    private fun channel(context: Context) = NotificationChannel(
                CHANNEL,
                context.getString(R.string.ddna_channel_name),
                NotificationManager.IMPORTANCE_MIN).apply {
        (context.getSystemService(
                Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(this)}
    
    private fun deleteIntent(context: Context) = Intent()
            .setClass(context, DebugReceiver::class.java)
            .setAction(DELETE_ACTION)
    
    private companion object {
        
        const val TAG = "com.deltadna.android.sdk.ads.debug"
        const val ID = 1
        const val CHANNEL = "com.deltadna.debug"
        
        const val DELETE_ACTION = "com.deltadna.android.sdk.ads.debug.ACTION_DELETE"
        const val DELETE_RC = 1
        
        var hidden = false
    }
}
