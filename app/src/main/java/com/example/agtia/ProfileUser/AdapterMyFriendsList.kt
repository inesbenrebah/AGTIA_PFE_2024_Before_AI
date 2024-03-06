package com.example.agtia.ProfileUser


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agtia.R
import com.example.agtia.todofirst.Data.MyFriendsList

class AdapterMyFriendsList(private val friendList: ArrayList<MyFriendsList> , private val onRemoveClickListener: OnRemoveClickListener) :
    RecyclerView.Adapter<AdapterMyFriendsList.FriendViewHolder>() {

    interface OnRemoveClickListener {
        fun onRemoveClick(email: String)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.friends_list, parent, false)
        return FriendViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val currentItem = friendList[position]
        holder.emailTextView.text = currentItem.email
        holder.removeButton.setOnClickListener {
            onRemoveClickListener.onRemoveClick(currentItem.email ?: "")
        }
    }


    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var emailTextView: TextView = itemView.findViewById(R.id.email)
        var removeButton: Button = itemView.findViewById(R.id.remove)

    }




    override fun getItemCount(): Int {
        return friendList.size
    }

}

