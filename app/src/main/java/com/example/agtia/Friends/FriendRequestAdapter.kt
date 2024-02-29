
package com.example.agtia.Friends

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.agtia.databinding.ActivityRequestsBinding
import com.example.agtia.databinding.FragmentAddFriendsActivityBinding
import com.example.agtia.todofirst.Data.Friend
import com.google.firebase.auth.FirebaseAuth

class FriendRequestAdapter(private val list: MutableList<Friend>, private val recipientEmail: String) :
    RecyclerView.Adapter<FriendRequestAdapter.RequestViewHolder>() {

    private var listener: RequestAdapterClicksInterface? = null

    fun setListener(listener: RequestAdapterClicksInterface) {
        this.listener = listener
    }

    inner class RequestViewHolder(val binding: ActivityRequestsBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FriendRequestAdapter.RequestViewHolder {
        val binding =
            ActivityRequestsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendRequestAdapter.RequestViewHolder, position: Int) {
        val friend = list[position]
        if (friend.recipientEmail == recipientEmail) {
            holder.binding.apply {
                senderemail.text = friend.senderEmail

                root.setOnClickListener {
                    listener?.onItemClicked(friend)
                }
                remove.setOnClickListener {
                    listener?.onDeleteItemClicked(friend, holder.adapterPosition)
                }
                add.setOnClickListener {
                    listener?.AddToMyFriendList(friend, holder.adapterPosition)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface RequestAdapterClicksInterface {
        fun onDeleteItemClicked(friend: Friend, position: Int)
        fun onItemClicked(friend: Friend)
        fun AddToMyFriendList(friend: Friend, position: Int)
    }

    fun updateList(updatedList: List<Friend>) {
        list.clear()
        list.addAll(updatedList)
        notifyDataSetChanged()
    }
}
