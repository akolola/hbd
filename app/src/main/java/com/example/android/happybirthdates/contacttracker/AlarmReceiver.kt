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
import com.example.android.happybirthdates.database.Contact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "AlarmReceiver"

/**
 * Broadcast receiver for the alarm, which delivers Notification. For Android ver > 'Oreo'.
 */
class AlarmReceiver : BroadcastReceiver() {

    //--------------------------- Notification -----------------------------------------------------
    companion object {
        //---------- (v)s for Notification.
        private const val CHANNEL_ID = "ForegroundServiceChannel"
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

            // Every 1 step in the loop (from today to tomorrow and to after tomorrow) => 1 -[(c) Notification]->.
            for (addedDaysFromTodayAndNoficationId in 0..2){
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
                    //---- 1. (m) building (c) NotificationChannel
                    val notificationChannel = buildNotificationChannel()
                    //---- 2. (c) NotificationManager <- (c) NotificationChannel.
                    mNotificationManager!!.createNotificationChannel(notificationChannel)
                }
                // |DB| ContactDatabase & prepare (c) Notification's text.
                val birthdayContactListFromDatabase = getBirthdayContactListFromDatabase(database, prepareBirthDateForContactListSelect(addedDaysFromTodayAndNoficationId) + ".%%%%")
                if(!birthdayContactListFromDatabase.isNullOrEmpty()) {
                    val formedContentText = formContentText(context,birthdayContactListFromDatabase, addedDaysFromTodayAndNoficationId==0)
                    // (m) building (c) Notification containing info about (c) Contacts.
                    var notification = buildNotification(context, formedContentText)
                    // (c) NotificationManager <- (c) Notification. (m) displaying push notification.
                    mNotificationManager!!.notify(addedDaysFromTodayAndNoficationId, notification)
                }
            }

        }

    }

    /**
     * Write readable message for app users about (c) Contacts with incoming Birthdays.
     *
     * @param birthdayContactList List of names of contacts (persones or companies) which have birthdays soon.
     * @return contentText Readable text for notification message.
     */
    private fun formContentText(context: Context, birthdayPersonListFromDatabase: List<Contact>?, isToday : Boolean): String {
            val birthdayContactList: List<String>? = birthdayPersonListFromDatabase?.map { it.name }
            var contentText = ""
            if (!birthdayContactList.isNullOrEmpty()) {
                if (birthdayContactList.size == 1) {
                    contentText = context.getString(R.string.your_friend)+" "+birthdayContactList[0].trim()+" "+context.getString(R.string.has_birthday)+" "+if(isToday) context.getString(R.string.today) else context.getString(R.string.soon)
                } else {
                    var contentTextBuffer = ""
                    for ((index, value) in birthdayContactList.withIndex()) {
                        contentTextBuffer = contentTextBuffer.plus(value.trim())
                        if (index != birthdayContactList.size - 1) {
                            contentTextBuffer = contentTextBuffer.plus(", ")
                        }
                    }
                    contentText = context.getString(R.string.your_friends)+" "+contentTextBuffer.trim()+" "+context.getString(R.string.have_birthday)+" "+if(isToday) context.getString(R.string.today) else context.getString(R.string.soon)
                }
            }
            return contentText
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun prepareBirthDateForContactListSelect(addedDaysFromToday: Int): String {
        var date = Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DATE, addedDaysFromToday)
        date = calendar.time
        val comingDate: String = SimpleDateFormat("dd.MM", Locale.getDefault()).format(date)
        return comingDate
    }

    /**
     * Create (c) NotificationChannel if Android ver >= 'O' (OREO).
     * (c) NotificationChannel is mandatory for push notifications.
     *
     * @return notificationChannel with specific attributes.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildNotificationChannel(): NotificationChannel {
        val notificationChannel = NotificationChannel(CHANNEL_ID,"Birthdays notification", NotificationManager.IMPORTANCE_HIGH)
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
     * @param contentText Description about persons or companies having birthdays soon.
     *
     * @return notification with complete description and image.
     */
    private fun buildNotification(context: Context, contentText: String): Notification? {
        //---------- |resource| ic_gift_box_foreground.
        val imageGiftBoxId: Int = context.resources.getIdentifier(RESOURCE_GIFT_PACKAGE_NAME, RESOURCE_TYPE, context.packageName)
        val drawableGiftBox = context.resources.getDrawable(imageGiftBoxId)
        val bitmapGiftBox = convertDrawableToBitmap(drawableGiftBox) // Alternative to not working line: val bitmap = BitmapFactory.decodeResource(context.resources, frame1Id);

        //---- (c) [Notification]Builder.
        var notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_gift_box)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(contentText)
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


    //-------------------- DB query (m).
    private suspend fun getBirthdayContactListFromDatabase(database: ContactDatabaseDao, chosenBirthDate: String): List<Contact>? {
        return database.getContactListWithGivenBirthday(chosenBirthDate)
    }


}