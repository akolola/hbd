package com.example.android.happybirthdates.contacttracker


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.android.happybirthdates.R
import com.example.android.happybirthdates.database.ContactDatabase
import com.example.android.happybirthdates.database.ContactDatabaseDao
import com.example.android.happybirthdates.database.ContactPerson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "AlarmReceiver"

/**
 * Broadcast receiver for the alarm, which delivers Notification.
 */
class AlarmReceiver : BroadcastReceiver() {

    //--------------------------- Notification -----------------------------------------------------
    //---------- Technical (v) mNotificationManager
    var mNotificationManager: NotificationManager? = null

    /**
     * Called when BroadcastReceiver receives Intent broadcast.
     *
     * @param context Context in which the receiver is running.
     * @param intent Intent being received.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onReceive(context: Context, intent: Intent) {

        Log.i(TAG, "(m) onReceive. Received intent: $intent")

        //---------- Technical (v) mNotificationManager. Assign val.
        mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //---------- |DB| Contact.
        val database = ContactDatabase.getInstance(context).contactDatabaseDao

        //---------- (c) SupervisorJob & (c) CoroutineScope. Launch new coroutine without blocking current thread => |DB| -[Notification]->.
        val serviceJob = SupervisorJob()
        val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
        serviceScope.launch {

            //--- 1. |DB|
            val birthDate: String = prepareBirthDateForContactPersonListSelect()
            val birthdayPersonListFromDatabase = getBirthdayPersonListFromDatabase(database, "$birthDate.%%%%")
            val birthdayPersonList : List<String>? = birthdayPersonListFromDatabase?.map { it.name }

            //--- 2.  Notification
            deliverNotification(context, birthdayPersonList)

        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun prepareBirthDateForContactPersonListSelect(): String {
        var date = Date()
        val calendar = Calendar.getInstance()
        calendar.setTime(date)
        calendar.add(Calendar.DATE, 2)
        date = calendar.time
        val comingDate: String = SimpleDateFormat("dd.MM", Locale.getDefault()).format(date)
        return comingDate
    }

    private suspend fun  deliverNotification(context: Context, birthdayPersonList: List<String>?) {

        Log.i(TAG, "(m) deliverNotification. Received birthdayPersonList: $birthdayPersonList")

        val imageGiftBoxId: Int = context.resources.getIdentifier(RESOURCE_GIFT_PACKAGE_NAME, RESOURCE_TYPE, context.packageName)
        val drawable = context.resources.getDrawable(imageGiftBoxId)
        val bitmap =  drawableToBitmap(drawable) // Alternative to not working:  val bitmap = BitmapFactory.decodeResource(context.resources, frame1Id);

        var notificationBuilder = NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_gift_box)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(formContentText(birthdayPersonList))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setLargeIcon(bitmap)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap).bigLargeIcon(null))

        //---- (c) NotificationManager -[(c) Notification]->.
        createNotificationChannel()
        mNotificationManager!!.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        //---  A.
        if (drawable is BitmapDrawable) { return drawable.bitmap }
        //---  B.
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    companion object {
        //---------- (v)s for Notification.
        private const val NOTIFICATION_ID = 0
        private const val PRIMARY_CHANNEL_ID = "primary_notification_channel"

        //---------- (v)s for |resource|.
        private const val RESOURCE_GIFT_PACKAGE_NAME = "ic_gift_box_foreground"
        private const val RESOURCE_TYPE = "mipmap"
    }

    private fun formContentText(birthdayPersonList: List<String>?): String {
        var contentText = ""
        if (!birthdayPersonList.isNullOrEmpty()) {
            if (birthdayPersonList.size == 1) {
                contentText = "Your friend " + birthdayPersonList[0] + " has Birthday after tomorrow."
            } else {
                var contentTextBuffer = ""
                for ((index, value) in birthdayPersonList.withIndex()) {
                    contentTextBuffer = contentTextBuffer.plus(value.trim())
                    if (index != birthdayPersonList.size - 1) {
                        contentTextBuffer = contentTextBuffer.plus(", ")
                    }
                }
                contentText = "Your friend $contentTextBuffer have Birthday after tomorrow."
            }
        }
        return contentText
    }

    //-------------------- DB query (m).
    private suspend fun getBirthdayPersonListFromDatabase(database: ContactDatabaseDao, chosenBirthDate: String): List<ContactPerson>? {
        return database.getContactPersonListWithGivenBirthday(chosenBirthDate)
    }

    //--------------------------- Notification -----------------------------------------------------
    //-------------------- NotificationChannel is obligational for Push Notifications
    /**
     *   Create (c) NotificationChannel if >= Android ver OREO.
     */
    private fun createNotificationChannel() {

        //---------- (c) NotificationChannel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //---- (v) notificationChannel. Assign val. Add params.
            val notificationChannel = NotificationChannel(PRIMARY_CHANNEL_ID,"Birthdays notification", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Notifies about Birthdays"
            //---- (c) NotificationManager -[(c) NotificationChannel]->.
            mNotificationManager!!.createNotificationChannel(notificationChannel)
        }

    }


}