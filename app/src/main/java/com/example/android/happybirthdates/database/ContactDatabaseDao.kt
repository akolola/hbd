/*
 * Copyright 2022, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.happybirthdates.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Defines (m)s for using (c) ContactPerson class with Room.
 */
@Dao
interface ContactDatabaseDao {

    @Insert
    suspend fun insert(night: ContactPerson)

    /**
     * When updating a row with a value already set in a column,
     * replaces the old value with the new one.
     *
     * @param night new value to write
     */
    @Update
    suspend fun update(night: ContactPerson)

    /**
     * Selects and returns the row that matches the supplied start time, which is our key.
     *
     * @param key startTimeMilli to match
     */
    @Query("SELECT * from contact_table WHERE personId = :key")
    suspend fun get(key: Long): ContactPerson?

    /**
     * Deletes Contact with given ID.
     */
    @Query("DELETE FROM contact_table WHERE personId = :key")
    suspend fun deleteById(key: Long)

    /**
     * Selects and returns all rows in the table, sorted by start time in descending order.
     */
    @Query("SELECT * FROM contact_table ORDER BY personId DESC")
    fun getAllPersons(): LiveData<List<ContactPerson>>

    /**
     * Selects and returns latest ContactPerson.
     */
    @Query("SELECT * FROM contact_table ORDER BY personId DESC LIMIT 1")
    suspend fun getLatestPerson(): ContactPerson?

    /**
     * Selects and returns ContactPerson with given personId.
     *
     * @param key personId
     */
    @Query("SELECT * from contact_table WHERE personId = :key")
    fun getContactWithId(key: Long): LiveData<ContactPerson>

    /**
     * Selects and returns ContactPerson list with given Birthday date.
     *
     * @param key  date (string) formatted 'dd.MM.yyyy'  to match
     */
    @Query("SELECT * FROM contact_table WHERE birthdate LIKE :key")
    suspend fun getContactPersonListWithGivenBirthday(key: String): List<ContactPerson>?
}

