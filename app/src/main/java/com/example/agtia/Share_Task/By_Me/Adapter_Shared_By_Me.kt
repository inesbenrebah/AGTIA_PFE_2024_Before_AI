package com.example.agtia.Share_Task.By_Me

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agtia.databinding.SharedByMeBinding
import com.example.agtia.todofirst.Data.ShareData
import java.text.SimpleDateFormat
import java.util.*

class Adapter_Shared_By_Me(private val list: MutableList<ShareData>,private val emailFrom :String):
  RecyclerView.Adapter<Adapter_Shared_By_Me.ShareViewHolder>() {

   private var listener: ToDoAdapterClicksInterface? = null

   fun setListener(listener: ToDoAdapterClicksInterface) {
       this.listener = listener
   }

   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareViewHolder {
       val binding = SharedByMeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
       return ShareViewHolder(binding)
   }

   override fun onBindViewHolder(holder: ShareViewHolder, position: Int) {
       val currentItem = list[position]

       holder.binding.apply {
           emailTo.text = currentItem.emailTo
           todoTask.text = currentItem.task
           todoDesc.text = currentItem.desc
           todoDate.text = "Date: ${currentItem.date}"

           val imageView = todoImage
           if (!currentItem.imageUri.isNullOrEmpty()) {
               Glide.with(root.context)
                   .load(currentItem.imageUri)
                   .into(imageView)
               imageView.visibility = View.VISIBLE
           } else {
               imageView.visibility = View.GONE
           }
           if (currentItem.reminderTime > 0) {
               val reminderTimeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                   Date(currentItem.reminderTime)
               )
               todoalarm.text = "  Time: $reminderTimeFormatted"
               todoalarm.visibility = View.VISIBLE
           } else {
               todoalarm.visibility = View.GONE
           }
           editTask.setOnClickListener {
               listener?.onEditItemClicked(currentItem, position, holder.itemView.context)
           }
           deleteTask.setOnClickListener {
               listener?.onDeleteItemClicked(currentItem, holder.adapterPosition)
           }

           root.setOnClickListener {
               listener?.onItemClicked(currentItem, holder.adapterPosition)
           }
       }
   }

   override fun getItemCount(): Int {
       return list.size
   }

   inner class ShareViewHolder( val binding: SharedByMeBinding) : RecyclerView.ViewHolder(binding.root)



   interface ToDoAdapterClicksInterface {
       fun onDeleteItemClicked(toDoData: ShareData, position: Int)
       fun onEditItemClicked(toDoData: ShareData, position: Int, context: Context)
       fun onItemClicked(toDoData: ShareData, position: Int)
   }

   fun updateList(updatedList: List<ShareData>) {
       list.clear()
       list.addAll(updatedList)
       notifyDataSetChanged()
   }
}
