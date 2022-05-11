package com.example.android.happybirthdates.contacttracker


import android.app.Notification
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
    companion object {
        //---------- (v)s for Notification.
        private const val NOTIFICATION_ID = 0
        private const val PRIMARY_CHANNEL_ID = "primary_notification_channel"
        //---------- (v)s for |resource|.
        private const val RESOURCE_GIFT_PACKAGE_NAME = "ic_gift_box_foreground"
        private const val RESOURCE_TYPE = "mipmap"
    }

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

        //---------- (c) NotificationManager Service.
        mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //---------- |DB| ContactDatabase.
        val database = ContactDatabase.getInstance(context).contactDatabaseDao

        //---------- (c) SupervisorJob & (c) CoroutineScope & (m) launch new coroutine without blocking current thread => |DB| -[Notification]->.
        val serviceJob = SupervisorJob()
        val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
        serviceScope.launch {
            //--- 1. |DB| ContactDatabase.
            val birthDate: String = prepareBirthDateForContactPersonListSelect()
            val birthdayPersonListFromDatabase = getBirthdayPersonListFromDatabase(database, "$birthDate.%%%%")
            //--- 2.  Notification.
            if (birthdayPersonListFromDatabase != null && birthdayPersonListFromDatabase.isNotEmpty()) {
                    val birthdayPersonList : List<String>? = birthdayPersonListFromDatabase?.map { it.name }
                    buildAndDisplayNotification(context, birthdayPersonList)
            }
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

    /**
     * Created and display push notification.
     *
     * @param context Context in which the receiver is running.
     * @param birthdayPersonList List of names of contacts (persones or companies) wihich have birthdays soon.
     */
    private suspend fun  buildAndDisplayNotification(context: Context, birthdayPersonList: List<String>?) {

        Log.i(TAG, "(m) buildAndDeliverNotification. Received birthdayPersonList: $birthdayPersonList")

        //---- 1. (m) building (c) Notification containing info about (c) Contacts with coming Birthdays.
        var notification = buildNotification(context, birthdayPersonList)
        //---- 2. (m) building (c) NotificationChannel
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
            val notificationChannel = buildNotificationChannel()
        //---- 3. (c) NotificationManager <- (c) NotificationChannel.
            mNotificationManager!!.createNotificationChannel(notificationChannel)
        }
        //---- 4. (c) NotificationManager <- (c) Notification. (m) displaying push notification.
        mNotificationManager!!.notify(NOTIFICATION_ID, notification)

    }

    /**
     * Create (c) NotificationChannel if Android ver >= 'O' (OREO).
     * (c) NotificationChannel is mandatory for push notifications.
     *
     * @return notificationChannel with specific attributes.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildNotificationChannel(): NotificationChannel {
        val notificationChannel = NotificationChannel(PRIMARY_CHANNEL_ID,"Birthdays notification", NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.enableVibration(true)
        notificationChannel.description = "Notifies about Birthdays"
        return notificationChannel
    }

    /**
     * Build notification to be displayed.
     *
     * @param context Context in which the receiver is running.
     * @param birthdayPersonList List of names of contacts (persones or companies) wihich have birthdays soon.
     *
     * @return notification with complete description and image.
     */
    private fun buildNotification(context: Context, birthdayPersonList: List<String>?): Notification? {
        //---------- |resource| ic_gift_box_foreground.
        val imageGiftBoxId: Int = context.resources.getIdentifier(RESOURCE_GIFT_PACKAGE_NAME, RESOURCE_TYPE, context.packageName)
        val drawableGiftBox = context.resources.getDrawable(imageGiftBoxId)
        val bitmapGiftBox = convertDrawableToBitmap(drawableGiftBox) // Alternative to not working line: val bitmap = BitmapFactory.decodeResource(context.resources, frame1Id);

        //---- (c) [Notification]Builder.
        var notificationBuilder = NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_gift_box)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(formContentText(birthdayPersonList))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setLargeIcon(bitmapGiftBox) // (c) [Notification]Builder <- |resource| ic_gift_box_foreground.
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmapGiftBox).bigLargeIcon(null))

        //---- (c) Notification.
        var notification = notificationBuilder.build()
        return notification
    }

    /**
     * Image format conversion form 'Drawable' to 'Bitmap'.
     *
     * @param drawableImage image of the format 'Drawable' to be converted.
     * @return bitmapImage  image with converted format 'Bitmap'.
     */
    private fun convertDrawableToBitmap(drawableImage: Drawable): Bitmap? {
        //---  A.
        if (drawableImage is BitmapDrawable) { return drawableImage.bitmap }
        //---  B.
        val bitmapImage = Bitmap.createBitmap(drawableImage.intrinsicWidth, drawableImage.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapImage)
        drawableImage.setBounds(0, 0, canvas.width, canvas.height)
        drawableImage.draw(canvas)
        return bitmapImage
    }

    /**
     * Write readable message for app users about (c) Contacts with incoming Birthdays.
     *
     * @param birthdayPersonList List of names of contacts (persones or companies) wihich have birthdays soon.
     * @return contentText Readable text for notification message.
     */
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


}