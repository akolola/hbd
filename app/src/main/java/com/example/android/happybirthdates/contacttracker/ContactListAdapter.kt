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

package com.example.android.happybirthdates.contacttracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.happybirthdates.R
import com.example.android.happybirthdates.database.Contact
import com.example.android.happybirthdates.databinding.FragmentContactTrackerViewContactListGridItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1


//--------------------------- (c) Adapter ----------------------------------------------------------
//---------- (cr) Std
/**
 * This (c) contains components necessary for (c) RecyclerView.
 *
 * @constructor Creates (c) <Entity>ListListener, in order to make every View item clickable.   (1)
 *
 * This (c) extending (c) ListAdapter, which needs:
 * (c) <Entity>Data. Here it is 'wrapped' (c) ContactPerson;                                    (2)
 * (c) ViewHolder with (4) as param;                                                            (3)
 * (c) <Entity>ListDiffCallback.                                                                (4)
 */
class ContactListAdapter constructor(val clickListener: ContactListListener) : ListAdapter<DataItem, RecyclerView.ViewHolder>(ContactListDiffCallback()) {

    //---------- (c) CoroutineScope
    //---------- CoroutineScope & (m) launch 2 new coroutines without blocking current thread => 1. (m) contactPersonList & 2. (m) submitList ->.
    private val adapterScope = CoroutineScope(Dispatchers.Default)
    fun addHeaderAndSubmitList(contactList: List<Contact>?) {
        adapterScope.launch {
            val items = when (contactList) {
                null -> listOf(DataItem.Header)
                else -> listOf(DataItem.Header) + contactList.map { DataItem.ContactItem(it) }
            }
            withContext(Dispatchers.Main) {
                //---------- (c) RecyclerView's std (m)
                submitList(items)
            }
        }
    }

    //---------- (c) RecyclerView's std (m)
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.ContactItem -> ITEM_VIEW_TYPE_ITEM
        }
    }



    //---------- (c) RecyclerView's std (m)
    /**
     *  This (m) creates, inflates view & returns (c) TextViewHolder or (c) ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            // (c) ViewHolder's content may be alternatively defined directly in this (m)
            ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ContactViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }

    //--- ViewHolder (3A)
    /**
     * This (c) holds |fragment layout| fragment_contact_tracker_view_contact_list_grid_item's Views:
     * <ImageView> & <TextView>, i.e. one item.
     *
     * @constructor Creates (o) of (c) extending (c) ViewDataBinding.
     */
    class ContactViewHolder private constructor(val binding: FragmentContactTrackerViewContactListGridItemBinding) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun from(parent: ViewGroup): ContactViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = FragmentContactTrackerViewContactListGridItemBinding.inflate(layoutInflater, parent, false)
                return ContactViewHolder(binding)
            }
        }

        /**
        * This (m) takes the item & clickListener, then -> fragment_contact_tracker_view_contact_list_grid_item
        */
        fun bind(clickListener: ContactListListener, item: Contact) {
            binding.contactPerson = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

    }

    //--- ViewHolder (3B)
    /**
     *   This (c) holds |fragment layout| fragment_contact_tracker_header's Views:
     *   <TextView>, i.e. one item.
     *
     *  @constructor Creates (o) of (c) extending (c) View.
     */
    class TextViewHolder constructor(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.fragment_contact_tracker_header, parent, false)
                return TextViewHolder(view)
            }
        }
    }



    //---------- (c) RecyclerView's std (m)
    /**
     *  The (m) gets one current chosen item, extracts item data, & data -> View.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ContactViewHolder -> {
                val item = getItem(position) as DataItem.ContactItem
                holder.bind(clickListener, item.contact)
            }
        }
    }


}

//--------------------------- (c) Listener (1) -----------------------------------------------------
/**
 * (c) Listener for (c) ContactTrackerFragment => (c) ContactDetailsFragment.
 */
class ContactListListener constructor(val clickListener: (contactId: Long) -> Unit) {
    fun onClick(contact: Contact) = clickListener(contact.id)
}

//--------------------------- (c) Data (2) ---------------------------------------------------------
/**
 * This (c) contains entity data, i.e. Contact.
 */
sealed class DataItem {



    //---------- Data (c) Std
    data class ContactItem constructor(val contact: Contact): DataItem() {
        override val id = contact.id
    }



    //---------- (o) Non std
    object Header: DataItem() {
        override val id = Long.MIN_VALUE
    }

    abstract val id: Long


}

//--------------------------- (c) ViewHolder (3)----------------------------------------------------
// (c) ViewHolder (3A) & (c) TextViewHolder (3B) is inner one of (c) ContactListAdapter


//--------------------------- (c) Callback (4) -----------------------------------------------------
/**
 * (c) <Entity>ListDiffCallback for calculating the diff between two non-null items in a list.
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



