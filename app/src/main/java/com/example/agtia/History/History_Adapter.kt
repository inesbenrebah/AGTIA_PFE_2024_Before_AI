package com.example.agtia.History
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.agtia.databinding.ActivityHistoBinding
import com.example.agtia.todofirst.Data.History
import com.example.agtia.todofirst.Data.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class History_Adapter(
    private val context: Context,
    private val list: MutableList<History>
) : RecyclerView.Adapter<History_Adapter.HistoriqueViewHolder>() {

    private var taskListFull: MutableList<History> = ArrayList()

    init {
        taskListFull.addAll(list)
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(history: History, position: Int)
        fun onReturnTaskClick(history: History, position: Int)
    }

    private var listener: OnDeleteClickListener? = null

    fun setOnDeleteClickListener(listener: OnDeleteClickListener) {
        this.listener = listener
    }

    inner class HistoriqueViewHolder(val binding:ActivityHistoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.deleteTask.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onDeleteClick(list[position], position)
                }
            }

            binding.returnTask.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onReturnTaskClick(list[position], position)
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoriqueViewHolder {
        val binding =ActivityHistoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoriqueViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: HistoriqueViewHolder, position: Int) {
        with(holder) {
            with(list[position]) {
                binding.todoTask.text = this.task
                binding.todoDesc.text = this.desc
                binding.todoDate.text = this.date
                binding.todoTime.text = formatTime(this.reminderTime)
                if (!this.imageUri.isNullOrEmpty()) {
                    Glide.with(context)
                        .load(this.imageUri)
                        .into(binding.todoImage)
                    binding.todoImage.visibility = View.VISIBLE
                } else {
                    binding.todoImage.visibility = View.GONE
                }
            }
        }
    }

    fun updateList(updatedList: List<History>) {
        list.clear()
        list.addAll(updatedList)
        notifyDataSetChanged()
    }





    private fun formatTime(timeInMillis: Long): String {

         val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timeInMillis))
         return formattedTime

    }
}
