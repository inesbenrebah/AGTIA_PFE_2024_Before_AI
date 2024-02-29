package com.example.agtia.Admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import android.widget.Button
import com.example.agtia.R
import com.example.agtia.todofirst.Data.User


class MyAdapterAdmin(private val userList: MutableList<User>, private val onDeleteClickListener: OnDeleteClickListener): RecyclerView.Adapter<MyAdapterAdmin.MyViewHolder>() {

    interface OnDeleteClickListener {
        fun onDeleteClick(email: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return MyViewHolder(itemView)
    }
    fun filterList(updatedList: List<User>) {
        userList.clear()
        userList.addAll(updatedList)
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user: User = userList[position]
        holder.email.text = user.email
        holder.btnDelete.setOnClickListener {
            onDeleteClickListener.onDeleteClick(user.email ?: "")
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    public class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val email: TextView = itemView.findViewById(R.id.email)
        val btnDelete: Button = itemView.findViewById(R.id.direct)
    }
}