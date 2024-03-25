package com.example.agtia.Share_Task.By_Me

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agtia.databinding.HorizBinding
import com.example.agtia.databinding.SharedByMeBinding
import com.example.agtia.todofirst.Data.GotFinished
import com.example.agtia.todofirst.Data.ShareData
import java.text.SimpleDateFormat
import java.util.*
class Adapter_Shared_By_Me(
    private val mList: MutableList<ShareData>,
    private val bList: MutableList<GotFinished>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listener: ToDoAdapterClicksInterface? = null

    fun setListener(listener: ToDoAdapterClicksInterface) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_VERTICAL -> {
                val binding = SharedByMeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                VerticalViewHolder(binding)
            }
            VIEW_TYPE_HORIZONTAL -> {
                val binding = HorizBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class VerticalViewHolder(private val binding: SharedByMeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ShareData) {
            binding.apply {
                emailFrom.text = item.taskId
                todoTask.text = item.task
                todoDesc.text = item.desc
                todoDate.text = "Date: ${item.date}"

                val imageView = todoImage
                if (!item.imageUri.isNullOrEmpty()) {
                    Glide.with(root.context)
                        .load(item.imageUri)
                        .into(imageView)
                    imageView.visibility = View.VISIBLE
                } else {
                    imageView.visibility = View.GONE
                }
                if (item.reminderTime > 0) {
                    val reminderTimeFormatted = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.reminderTime))
                    todoalarm.text = "  Time: $reminderTimeFormatted"
                    todoalarm.visibility = View.VISIBLE
                } else {
                    todoalarm.visibility = View.GONE
                }
                deleteTask.setOnClickListener {
                    listener?.onDeleteItemClicked(item, adapterPosition)
                }

                root.setOnClickListener {
                    listener?.onItemClicked(item, adapterPosition)
                }
            }
        }
    }

    inner class HorizontalViewHolder(private val binding: HorizBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GotFinished) {
            binding.apply {
                emailFrom.text = item.emailTo
                todoTask.text = item.task
                todoDate.text = item.date
                ratingBar.rating = item.rating
                ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                    // Update the rating value in the data model
                    item.rating = rating

                    // Notify the adapter about the rating change
                    listener?.CalculateRating(item, adapterPosition)
                }
            root.setOnClickListener {
                listener?.onItemClicked2(item, adapterPosition)
            }}
        }
    }

    interface ToDoAdapterClicksInterface {
        fun onDeleteItemClicked(toDoData: ShareData, position: Int)
        fun onItemClicked(toDoData: ShareData, position: Int)
        fun onItemClicked2(toDoData: GotFinished, position: Int)
        fun CalculateRating(toDoData: GotFinished,position: Int)

    }

    companion object {
        private const val VIEW_TYPE_VERTICAL = 0
        private const val VIEW_TYPE_HORIZONTAL = 1
    }
}
