package com.example.agtia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agtia.todofirst.Data.User

class MyAdapterEmailFriend (
    private val userList: MutableList<User>,
    private val onDeleteClickListener: OnDeleteClickListener
): RecyclerView.Adapter<MyAdapterEmailFriend.MyViewHolder>() {

    interface OnDeleteClickListener {
        fun sendFriendRequest(email: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.each_user_in_add_friend, parent, false)
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
            onDeleteClickListener.sendFriendRequest(user.email ?: "")
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    public class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val email: TextView = itemView.findViewById(R.id.email)
        val btnDelete: Button = itemView.findViewById(R.id.direct)
    }
}
