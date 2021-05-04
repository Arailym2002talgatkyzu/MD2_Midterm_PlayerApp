package com.example.midtermplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context!!, "notificationChannel")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Player Notification: ")
            .setContentText("IT'S TIME TO STOP.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val manager: NotificationManagerCompat = NotificationManagerCompat.from(context)
        id++
        manager.notify(id, builder.build())
    }
    companion object {
        var id = 0
    }
}