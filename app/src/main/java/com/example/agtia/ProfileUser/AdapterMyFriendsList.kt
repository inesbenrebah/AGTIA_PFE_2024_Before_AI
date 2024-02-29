package com.example.agtia.ProfileUser

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.agtia.R
import com.example.agtia.todofirst.Data.MyFriendsList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
class AdapterMyFriendsList(
    private val friendList: ArrayList<MyFriendsList>,
    private val databaseRef: DatabaseReference
) : RecyclerView.Adapter<AdapterMyFriendsList.FriendViewHolder>() {

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var emailTextView: TextView = itemView.findViewById(R.id.email)
        var removeButton: Button = itemView.findViewById(R.id.remove)

        init {
            removeButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val friend = friendList[position]
                    removeFriend(friend, itemView.context)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.friends_list, parent, false)
        return FriendViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val currentItem = friendList[position]
        holder.emailTextView.text = currentItem.email
    }

    override fun getItemCount(): Int {
        return friendList.size
    }

    private fun removeFriend(friend: MyFriendsList, context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Remove friend from the list locally
            friendList.remove(friend)
            notifyDataSetChanged()

            // Remove friend from the database using their email address
            val friendEmail = friend.email
            val encodedEmail = encodeEmail(friendEmail)

            databaseRef.child(encodedEmail).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Friend removed successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to remove friend: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun encodeEmail(email: String): String {
        return email.replace(".", ",")
    }
}
