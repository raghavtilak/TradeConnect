package com.raghav.digitalpaymentsbook

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.raghav.digitalpaymentsbook.data.network.RetrofitHelper
import com.raghav.digitalpaymentsbook.ui.activity.MainActivity
import com.raghav.digitalpaymentsbook.ui.activity.MyInvitationsActivity
import com.raghav.digitalpaymentsbook.ui.activity.MyOrderActivity
import com.raghav.digitalpaymentsbook.ui.activity.PendingOrdersActivity
import com.raghav.digitalpaymentsbook.util.Constants
import com.raghav.digitalpaymentsbook.util.PreferenceManager
import com.raghav.digitalpaymentsbook.util.getAuthToken
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class PushNotificationService : FirebaseMessagingService() {

    private val job = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + job)

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification!!.title
        val text = message.notification!!.body
        if(text!!.isNotEmpty() && text.contains("order")){
            createNotification(title, text,"order")
        }else if(text.isNotEmpty() && text.contains("connection")){
            createNotification(title, text,"connection")
        }else{
            createNotification(title, text,"none")
        }
        super.onMessageReceived(message)
    }

    private fun createNotification(title: String?, text: String?,type: String) {

        // Create an Intent for the activity you want to start
//        Intent resultIntent = new Intent(this, MainActivity.class);
        val resultIntent: Intent

        when (type) {
            "order" -> {
                resultIntent = Intent(this, PendingOrdersActivity::class.java)
            }
            "connection" -> {
                resultIntent = Intent(this, MyInvitationsActivity::class.java)
            }
            else -> {
                resultIntent = Intent(this, MainActivity::class.java)
            }
        }

        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntentWithParentStack(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val CHANNEL_ID = "HEADS_UP_NOTIFICATION"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Heads Up Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            val notification = Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
            NotificationManagerCompat.from(this).notify(1, notification.build())
        } else {
            val notificationPopup = Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .build()
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1, notificationPopup)
        }
    }

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d("TAG", "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.

        if(PreferenceManager.getInstance(this).getAuthToken()!="") {

            val jo = JSONObject()
            jo.put("registrationToken", token)

            val body =
                jo.toString().toRequestBody("application/json".toMediaTypeOrNull())


            ioScope.launch {
                val job = async {
                    RetrofitHelper.getInstance(this@PushNotificationService)
                        .updateNotificationToken(body)
                }
                val res = job.await()
                if (res.isSuccessful && res.body() != null) {
                    Log.d("TAG", "Updated token")
                } else {
                    Log.d("TAG", "Can't update token")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel("Service destroyed")
        ioScope.cancel("Service destroyed")
    }

}