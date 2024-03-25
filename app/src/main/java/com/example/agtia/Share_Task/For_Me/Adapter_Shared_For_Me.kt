package com.example.agtia.Share_Task.For_Me

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agtia.R
import com.example.agtia.databinding.HorizBinding
import com.example.agtia.databinding.RequestedToGetDeletedBinding
import com.example.agtia.databinding.SharedForMeBinding
import com.example.agtia.todofirst.Data.GotDeleted
import com.example.agtia.todofirst.Data.GotFinished
import com.example.agtia.todofirst.Data.Priority
import com.example.agtia.todofirst.Data.ShareData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Adapter_Shared_For_Me(
    private val mList: MutableList<ShareData>,
    private val bList: MutableList<GotDeleted>,
    private val cList: MutableList<GotDeleted>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listener: ToDoAdapterClicksInterface? = null

    fun setListener(listener: ToDoAdapterClicksInterface) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_VERTICAL -> {
                val binding = SharedForMeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                VerticalViewHolder(binding)
            }
            VIEW_TYPE_HORIZONTAL -> {
                val binding = HorizBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HorizontalViewHolder(binding)
            }
            VIEW_TYPE_HORIZONTAL2 -> {
                val binding = RequestedToGetDeletedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HorizontalViewHolder2(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_VERTICAL -> {
                (holder as VerticalViewHolder).bind(mList[position])
            }
            VIEW_TYPE_HORIZONTAL -> {
                (holder as HorizontalViewHolder).bind(bList[position - mList.size])
            }
            VIEW_TYPE_HORIZONTAL2 -> {

                    (holder as HorizontalViewHolder2).bind(cList[position - mList.size - bList.size])

            }
        }
    }

    override fun getItemCount(): Int {
        return mList.size + bList.size + cList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position < mList.size -> VIEW_TYPE_VERTICAL
            position < mList.size + bList.size -> VIEW_TYPE_HORIZONTAL
            else -> VIEW_TYPE_HORIZONTAL2
        }
    }

    inner class VerticalViewHolder(private val binding: SharedForMeBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShareData) {

            binding.apply {
                emailTo.text = item.emailTo
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
                    val reminderTimeFormatted =
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(item.reminderTime))
                    todoalarm.text = "  Time: $reminderTimeFormatted"
                    todoalarm.visibility = View.VISIBLE
                } else {
                    todoalarm.visibility = View.GONE
                }

                done.setImageResource(
                    when (item.priority) {
                        Priority.HIGH -> R.drawable.icon_priority
                        Priority.NORMAL -> R.drawable.orange
                        Priority.LOW -> R.drawable.baseline_blur_circular_24
                    }
                )

                done.setOnClickListener {
                    listener?.onDissatisfiedIconClicked(item, adapterPosition)
                    done.setImageResource(R.drawable.icon_finish)
                }

                root.setOnClickListener {
                    listener?.onItemClicked(item, adapterPosition)
                }
            }
        }
    }

    inner class HorizontalViewHolder(private val binding: HorizBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GotDeleted) {
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
                }
            }
        }
    }

    inner class HorizontalViewHolder2(private val binding: RequestedToGetDeletedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GotDeleted) {
            binding.apply {
                emailFrom.text = item.emailTo
                todoTask.text = item.task
                todoDate.text = item.date
                button.setOnClickListener {
                    listener?.approveTheDelete(item, adapterPosition)
                    listener?.appproveDeleteInShared(item, toDoData2 = ShareData(),adapterPosition)
                }

            }
        }
    }

    interface ToDoAdapterClicksInterface {
        fun onDeleteItemClicked(toDoData: ShareData, position: Int)
        fun onDissatisfiedIconClicked(toDoData: ShareData, position: Int)
        fun onEditItemClicked(toDoData: ShareData, position: Int, context: Context)
        fun onItemClicked(toDoData: ShareData, position: Int)
        fun onItemClicked2(gotDeleted: GotDeleted, position: Int)
        fun approveTheDelete(toDoData: GotDeleted, position: Int)
        fun appproveDeleteInShared(toDoData:GotDeleted,toDoData2: ShareData,position: Int)
        fun CalculateRating(toDoData: GotDeleted, position: Int)
    }

    fun updateList(updatedList: List<ShareData>) {
        mList.clear()
        mList.addAll(updatedList)
        notifyDataSetChanged()
    }

    companion object {
        private const val VIEW_TYPE_VERTICAL = 0
        private const val VIEW_TYPE_HORIZONTAL = 1
        private const val VIEW_TYPE_HORIZONTAL2 = 2
    }
}
