// File: Adapter_Rating_Page.kt
package com.example.agtia.ProfileUser

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agtia.R
import com.example.agtia.todofirst.Data.RatingStars

class Adapter_Rating_Page(private val list: ArrayList<RatingStars>):
    RecyclerView.Adapter<Adapter_Rating_Page.RateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RateViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.rate_users_each_item, parent, false)
        return RateViewHolder(itemView)
    }

    inner class RateViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var email: TextView = itemView.findViewById(R.id.email)
        var number: TextView = itemView.findViewById(R.id.number)
        var starsrate: TextView = itemView.findViewById(R.id.starsrate)
    }

    override fun onBindViewHolder(holder: RateViewHolder, position: Int) {
        val currentItem = list[position]
        holder.email.text = currentItem.email
        holder.starsrate.text = currentItem.stars.toString()
        holder.number.text = (position + 1).toString()
    }


    override fun getItemCount(): Int {
        return list.size
    }
}
