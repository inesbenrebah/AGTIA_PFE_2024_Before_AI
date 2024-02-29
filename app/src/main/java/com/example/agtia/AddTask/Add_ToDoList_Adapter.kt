package com.example.agtia.AddTask

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agtia.R
import com.example.agtia.databinding.ActivityEachTodoItemBinding
import com.example.agtia.todofirst.Data.Priority
import com.example.agtia.todofirst.Data.ToDoData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Add_ToDoList_Adapter(private val list: MutableList<ToDoData>) :
    RecyclerView.Adapter<Add_ToDoList_Adapter.TodoViewHolder>() {

    private var listener: ToDoAdapterClicksInterface? = null

    fun setListener(listener: ToDoAdapterClicksInterface) {
        this.listener = listener
    }

    inner class TodoViewHolder(val binding: ActivityEachTodoItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = ActivityEachTodoItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TodoViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val currentItem = list[position]
        holder.binding.apply {
            todoTask.text = currentItem.task
            todoDesc.text = currentItem.desc
            todoDate.text = "Date: ${currentItem.date}"

            val imageView = todoImage
            if (!currentItem.imageUri.isNullOrEmpty()) {
                imageView.visibility = View.VISIBLE
                Glide.with(root.context)
                    .load(currentItem.imageUri)
                    .placeholder(R.drawable.baseline_sentiment_very_dissatisfied_24) // Placeholder ki tabda t loadi
                    .error(R.drawable.green_aesthetic_wallpaper) // ken fama error fl photo
                    .into(imageView)
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

            done.setImageResource(
                when (currentItem.priority) {
                    Priority.HIGH -> R.drawable.icon_priority
                    Priority.NORMAL -> R.drawable.orange
                    Priority.LOW -> R.drawable.baseline_blur_circular_24
                }
            )
            editTask.setOnClickListener {
                listener?.onEditItemClicked(currentItem, position, holder.itemView.context)
            }
            deleteTask.setOnClickListener {
                listener?.onDeleteItemClicked(currentItem, holder.adapterPosition)
            }
            done.setOnClickListener {

                listener?.onDissatisfiedIconClicked(currentItem, holder.adapterPosition)
                done.setImageResource(R.drawable.icon_finish)
            }
            root.setOnClickListener {
                listener?.onItemClicked(currentItem, holder.adapterPosition)
            }
        }
    }

    interface ToDoAdapterClicksInterface {
        fun onDeleteItemClicked(toDoData: ToDoData, position: Int)
        fun onEditItemClicked(toDoData: ToDoData, position: Int, context: Context)
        fun onDissatisfiedIconClicked(toDoData: ToDoData, position: Int)
        fun onItemClicked(toDoData: ToDoData, position: Int)
    }

    fun updateList(updatedList: List<ToDoData>) {
        list.clear()
        list.addAll(updatedList)
        notifyDataSetChanged()
    }
}
