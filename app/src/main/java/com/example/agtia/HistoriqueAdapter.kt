package com.example.agtia

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.agtia.databinding.ActivityHistoBinding

class HistoriqueAdapter(private val list: MutableList<History>) :
    RecyclerView.Adapter<HistoriqueAdapter.HistoriqueViewHolder>() {

    interface OnDeleteClickListener {
        fun onDeleteClick(history: History, position: Int)
    }

    private var listener: OnDeleteClickListener? = null

    fun setOnDeleteClickListener(listener: OnDeleteClickListener) {
        this.listener = listener
    }

    inner class HistoriqueViewHolder(val binding: ActivityHistoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.deleteTask.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onDeleteClick(list[position], position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoriqueViewHolder {
        val binding = ActivityHistoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
                binding.todoDate.text=this.date
            }
        }
    }
}
