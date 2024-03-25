package com.example.agtia.Friends

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.agtia.databinding.ActivityRequestsBinding
import com.example.agtia.databinding.FragmentAddFriendsActivityBinding
import com.example.agtia.todofirst.Data.Friend

import com.example.agtia.databinding.RejectedFriendRequestBinding
import com.example.agtia.todofirst.Data.GotFinished

import com.example.agtia.todofirst.Data.RejectedRequest

class FriendRequestAdapter(
    private val mList: MutableList<Friend>,
    private val bList: MutableList<RejectedRequest>,
    private val recipientEmail: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listener: RequestAdapterClicksInterface? = null

    fun setListener(listener: RequestAdapterClicksInterface) {
        this.listener = listener
    }

    inner class VerticalViewHolder(private val binding: ActivityRequestsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Friend) {
            binding.apply {
                if (item.recipientEmail == recipientEmail) {
                    senderemail.text = item.senderEmail

                    root.setOnClickListener {
                        listener?.onItemClicked(item)
                    }
                    remove.setOnClickListener {
                        listener?.onDeleteItemClicked(item, adapterPosition)
                    }
                    add.setOnClickListener {
                        listener?.AddToMyFriendList(item, adapterPosition)
                    }
                }
            }
        }
    }

    inner class HorizontalViewHolder(private val binding: RejectedFriendRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RejectedRequest) {
            binding.apply {
                emailFrom.text = item.emailTo
                todoDate.text = item.date

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_VERTICAL -> {
                val binding = ActivityRequestsBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                VerticalViewHolder(binding)
            }
            VIEW_TYPE_HORIZONTAL -> {
                val binding = RejectedFriendRequestBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HorizontalViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == VIEW_TYPE_VERTICAL) {
            (holder as VerticalViewHolder).bind(mList[position])
        } else {
            (holder as HorizontalViewHolder).bind(bList[position - mList.size])
        }
    }

    override fun getItemCount(): Int {
        return mList.size + bList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < mList.size) {
            VIEW_TYPE_VERTICAL
        } else {
            VIEW_TYPE_HORIZONTAL
        }
    }

    interface RequestAdapterClicksInterface {
        fun onDeleteItemClicked(friend: Friend, position: Int)
        fun onItemClicked(friend: Friend)
        fun AddToMyFriendList(friend: Friend, position: Int)
    }

    companion object {
        private const val VIEW_TYPE_VERTICAL = 0
        private const val VIEW_TYPE_HORIZONTAL = 1
    }
}
