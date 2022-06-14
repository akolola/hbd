package com.example.android.happybirthdates.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "contact_table")
data class Contact(

        @PrimaryKey(autoGenerate = true)
        var id: Long = 0L,

        @ColumnInfo(name = "name")
        var name: String = "Unnamed",

        @ColumnInfo(name = "birth_date")
        var birthDate: String = "01-01-0001",

        @ColumnInfo(name = "image_id")
        var imageId: String = "Unnamed"

)
