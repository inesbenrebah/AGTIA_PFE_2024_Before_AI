package com.example.agtia.Share_Task.Requests_Shared_Tasks

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agtia.AddTask.Add_ToDoList_Adapter
import com.example.agtia.databinding.SharedRequestsEachItemBinding
import com.example.agtia.todofirst.Data.ShareData

class Request_Tasks_Adapter(private val list: MutableList<ShareData>,private val emailTo :String):
  RecyclerView.Adapter<Request_Tasks_Adapter.ShareViewHolder>() {

    private var listener: ToDoAdapterClicksInterface? = null

    fun setListener(listener:ToDoAdapterClicksInterface) {
        this.listener = listener
    }
    inner class ShareViewHolder( val binding: SharedRequestsEachItemBinding): RecyclerView.ViewHolder(binding.root)
    interface ToDoAdapterClicksInterface {

        fun AcceptRequest(toDoData: ShareData,emailTo:String, position: Int, context: Context)
        fun RejectRequest(toDoData: ShareData, position: Int)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Request_Tasks_Adapter.ShareViewHolder {
       val binding=SharedRequestsEachItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShareViewHolder(binding)    }

    override fun onBindViewHolder(holder: Request_Tasks_Adapter.ShareViewHolder, position: Int) {
        val currentItem = list[position]

        holder.binding.apply {
            RequestFrom.text = currentItem.emailTo
            todoTask.text = currentItem.task
            val imageView = todoImage
            if (!currentItem.userPhotoUrl.isNullOrEmpty()) {
                Glide.with(root.context)
                    .load(currentItem.userPhotoUrl)
                    .into(imageView)
                imageView.visibility = View.VISIBLE
            } else {
                imageView.visibility = View.GONE
            }
            approveBtn.setOnClickListener { listener?.AcceptRequest(currentItem, emailTo,position, holder.itemView.context) }
            rejectBtn.setOnClickListener { listener?.RejectRequest(currentItem, position) }

    }}


    override fun getItemCount(): Int {
        return list.size
    }

}