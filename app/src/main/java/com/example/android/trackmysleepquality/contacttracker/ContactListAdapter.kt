/*
 * Copyright 2021, The Android Open Source Project
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

package com.example.android.trackmysleepquality.contacttracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.ContactPerson
import com.example.android.trackmysleepquality.databinding.ViewContactListGridItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val ITEM_VIEW_TYPE_HEADER = 0
private val ITEM_VIEW_TYPE_ITEM = 1


//--------------------------- (c) Adapter ----------------------------------------------------------
//---------- (cr) Std
class ContactListAdapter(val clickListener: ContactListListener) : ListAdapter<DataItem, RecyclerView.ViewHolder>(ContactListDiffCallback()) {

    //--------------------------- 1 Section --------------------------------------------------------
    private val adapterScope = CoroutineScope(Dispatchers.Default)

    //---------- (m) Non std
    fun addHeaderAndSubmitList(list: List<ContactPerson>?) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(DataItem.Header)
                else -> listOf(DataItem.Header) + list.map { DataItem.ContactItem(it) }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    //---------- (m) Std
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }

    class TextViewHolder(view: View): RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.header, parent, false)
                return TextViewHolder(view)
            }
        }
    }

    //---------- (m) Std
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val nightItem = getItem(position) as DataItem.ContactItem
                holder.bind(clickListener, nightItem.contactPerson)
            }
        }
    }

    //---------- (m) Std
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.ContactItem -> ITEM_VIEW_TYPE_ITEM
        }
    }



    //--------------------------- (c) ViewHolder ---------------------------------------------------
    class ViewHolder private constructor(val binding: ViewContactListGridItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: ContactListListener, item: ContactPerson) {
            binding.contactPerson = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ViewContactListGridItemBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

//--------------------------- (c) Data -------------------------------------------------------------
sealed class DataItem {
    data class ContactItem(val contactPerson: ContactPerson): DataItem() {
        override val id = contactPerson.personId
    }

    object Header: DataItem() {
        override val id = Long.MIN_VALUE
    }

    abstract val id: Long
}

//--------------------------- (c) Listener ---------------------------------------------------------
/**
 * (c) Listener for (c)  ContactTrackerFragment => (c) ContactDetailsFragment
 */
class ContactListListener(val clickListener: (contactId: Long) -> Unit) {
    fun onClick(contactPerson: ContactPerson) = clickListener(contactPerson.personId)
}

//--------------------------- (c) Callback ---------------------------------------------------------
/**
 * Callback for calculating the diff between two non-null items in a list.
 *
 * Used by ListAdapter to calculate the minumum number of changes between and old list and a new
 * list that's been passed to `submitList`.
 */
class ContactListDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}



