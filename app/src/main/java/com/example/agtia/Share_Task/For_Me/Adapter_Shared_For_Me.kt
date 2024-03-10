package com.example.agtia.Share_Task.For_Me

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agtia.R
import com.example.agtia.databinding.HorizBinding
import com.example.agtia.databinding.SharedForMeBinding
import com.example.agtia.todofirst.Data.GotDeleted
import com.example.agtia.todofirst.Data.Priority
import com.example.agtia.todofirst.Data.ShareData
import com.example.agtia.todofirst.Data.SharedForMe
import com.example.agtia.todofirst.Data.ToDoData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Adapter_Shared_For_Me(
    private val mList: MutableList<ShareData>,
    private val bList: MutableList<GotDeleted>
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

                root.setOnClickListener {
                    listener?.onItemClicked2(item, adapterPosition)
                }
            }
        }
    }

    interface ToDoAdapterClicksInterface {
        fun onDeleteItemClicked(toDoData: ShareData, position: Int)
        fun onDissatisfiedIconClicked(toDoData: ShareData, position: Int)
        fun onEditItemClicked(toDoData: ShareData, position: Int, context: Context)
        fun onItemClicked(toDoData: ShareData, position: Int)
        fun onItemClicked2(gotDeleted: GotDeleted, position: Int) // Added this method
    }
    fun updateList(updatedList: List<ShareData>) {
        mList.clear()
        mList.addAll(updatedList)
        notifyDataSetChanged()
    }


    companion object {
        private const val VIEW_TYPE_VERTICAL = 0
        private const val VIEW_TYPE_HORIZONTAL = 1
    }
}
