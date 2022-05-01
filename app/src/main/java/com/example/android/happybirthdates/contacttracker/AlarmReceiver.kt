package com.example.android.happybirthdates.contacttracker

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
//import android.support.v4.app.NotificationCompat
import androidx.core.app.NotificationCompat
import com.example.android.happybirthdates.R
import android.graphics.drawable.BitmapDrawable

import android.graphics.drawable.Drawable
import java.util.ArrayList
import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.Transformations
import com.example.android.happybirthdates.database.ContactDatabase


/**
 * Broadcast receiver for the alarm, which delivers the notification.
 */
class AlarmReceiver : BroadcastReceiver() {

    //---------- (v) NotificationManager
    private var mNotificationManager: NotificationManager? = null

    /**
     * Called when BroadcastReceiver receives Intent broadcast.
     *
     * @param context Context in which the receiver is running.
     * @param intent Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val msgArrayList = intent.getStringArrayListExtra("MsgArrayList")

        //----------------------------------------------------------------------------------------->
        //----------  |DB| Contact
        val dataSource = ContactDatabase.getInstance(context).contactDatabaseDao
        val person = dataSource.getAllPersons()
        //-----------------------------------------------------------------------------------------<

        deliverNotification(context, msgArrayList)
    }

    /**
     * Builds and delivers the notification.
     *
     * @param context, activity context.
     */
    private fun deliverNotification(context: Context, msgArrayList: ArrayList<String>?) { // Create the content intent for the notification, which launches this activity
        //val contentIntent = Intent(context, MainActivity::class.java)
        //val contentPendingIntent = PendingIntent.getActivity(context, NOTIFICATION_ID, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val imageGiftBoxId: Int = context.resources.getIdentifier(RESOURCE_GIFT_PACKAGE_NAME, RESOURCE_TYPE, context.packageName)
        val drawable = context.resources.getDrawable(imageGiftBoxId)
        val bitmap =  drawableToBitmap(drawable) // Alternative to not working:  val bitmap = BitmapFactory.decodeResource(context.resources, frame1Id);

        var dynamicMsg = ""
        if (!msgArrayList.isNullOrEmpty()){
            dynamicMsg = "Your friend "+ msgArrayList[0] +" has Birthday today."
        }

        var notificationBuilder = NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_gift_box)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(dynamicMsg) //<---------------------------------- Make dynamic
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setLargeIcon(bitmap)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bitmap).bigLargeIcon(null))

        //--- (c) NotificationManager-Notification->
        mNotificationManager!!.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
    companion object {
        //---------- (v)s for Notification.
        private const val NOTIFICATION_ID = 0
        private const val PRIMARY_CHANNEL_ID = "primary_notification_channel"

        //---------- (v)s for |resource|.
        private const val RESOURCE_GIFT_PACKAGE_NAME = "ic_gift_box_foreground"
        private const val RESOURCE_TYPE = "mipmap"
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        //--- A
        if (drawable is BitmapDrawable) { return drawable.bitmap }
        //--- B
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

}