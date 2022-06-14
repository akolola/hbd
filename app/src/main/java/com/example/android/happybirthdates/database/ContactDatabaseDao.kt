package com.example.android.happybirthdates.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Defines (m)s for using (c) Contact class with Room.
 */
@Dao
interface ContactDatabaseDao {

    /**
     * Selects and returns latest Contact.
     */
    @Query("SELECT * FROM contact_table ORDER BY id DESC LIMIT 1")
    suspend fun getLatestContact(): Contact?

    /**
     * Selects and returns not LiveData Contact with given personId.
     *
     * @param key contactId
     */
    @Query("SELECT * from contact_table WHERE id = :key LIMIT 1")
    suspend fun getContactWithIdNotLiveData(key: Long): Contact?

    /**
     * Selects and returns Contact with given contactId.
     *
     * @param key contactId
     */
    @Query("SELECT * from contact_table WHERE id = :key")
    fun getContactWithId(key: Long): LiveData<Contact>

    /**
     * Selects and returns Contact list with given Birthday date.
     *
     * @param key  date (string) formatted 'dd.MM.yyyy'  to match
     */
    @Query("SELECT * FROM contact_table WHERE birth_date LIKE :key")
    suspend fun getContactListWithGivenBirthday(key: String): List<Contact>?

    /**
     * Selects and returns all rows, i.e. Contacts, in the table, sorted by start time in descending order.
     */
    @Query("SELECT * FROM contact_table ORDER BY id DESC")
    fun getAllContacts(): LiveData<List<Contact>>

    @Insert
    suspend fun insertContact(contact: Contact)

    /**
     * When updating a row with a value already set in a column,
     * replaces the old value with the new one.
     *
     * @param contact new value to write
     */
    @Update
    suspend fun updateContact(contact: Contact)

    /**
     * Deletes Contact with given ID.
     */
    @Query("DELETE FROM contact_table WHERE id = :key")
    suspend fun deleteContactsById(key: Long)

}

