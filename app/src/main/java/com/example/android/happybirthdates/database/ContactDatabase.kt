package com.example.android.happybirthdates.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Contact::class], version = 3, exportSchema = false)
abstract class ContactDatabase : RoomDatabase() {

    abstract val contactDatabaseDao: ContactDatabaseDao

    companion object {

        @Volatile
        private var INSTANCE: ContactDatabase? = null

        /**
         * Helper (m) to get |DB|.
         *
         * @param context App context Singleton, used to get access to filesystem.
         */
        fun getInstance(context: Context): ContactDatabase {

            synchronized(this) {

                var instance = INSTANCE
                // If (v) instance is null make a new database instance.
                if (instance == null) {
                    instance = Room.databaseBuilder(context.applicationContext, ContactDatabase::class.java, "special_day_database")
                            .fallbackToDestructiveMigration()
                            .build()
                    // Assign INSTANCE to the newly created database.
                    INSTANCE = instance
                }
                // Return  (v) instance; smart cast to be non-null.
                return instance
            }
        }
    }
}
