package com.example.agtia.ProfileUser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agtia.R

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class Profile_Adapter(private val onRemoveClickListener: (String) -> Unit) :
    ListAdapter<String, Profile_Adapter.FriendViewHolder>(FriendDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.friends_list, parent, false)
        return FriendViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friendEmail = getItem(position)
        holder.bind(friendEmail)
        holder.removeButton.setOnClickListener {
            onRemoveClickListener(friendEmail)
        }

    }

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val friendEmailTextView: TextView = itemView.findViewById(R.id.email)
        val removeButton: Button = itemView.findViewById(R.id.remove)

        fun bind(email: String) {
            friendEmailTextView.text = email
        }
    }

    private class FriendDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
