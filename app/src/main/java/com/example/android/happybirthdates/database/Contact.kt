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
        var imageId: String = "Unnamed",

        @ColumnInfo(name = "image_bytes")
        var imageBytes: ByteArray = byteArrayOf(0b00000001, 0b00000010)


) {
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Contact

                if (id != other.id) return false
                if (name != other.name) return false
                if (birthDate != other.birthDate) return false
                if (imageId != other.imageId) return false
                if (!imageBytes.contentEquals(other.imageBytes)) return false

                return true
        }

        override fun hashCode(): Int {
                var result = id.hashCode()
                result = 31 * result + name.hashCode()
                result = 31 * result + birthDate.hashCode()
                result = 31 * result + imageId.hashCode()
                result = 31 * result + imageBytes.contentHashCode()
                return result
        }
}
