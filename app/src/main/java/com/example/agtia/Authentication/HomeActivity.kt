package com.example.agtia.Authentication


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.agtia.AddTask.My_ToDoList_Fragment
import com.example.agtia.Friends.Add_Friends_Fragment

import com.example.agtia.History.History_Of_Tasks_Fragment
import com.example.agtia.ProfileUser.Profile_Fragment
import com.example.agtia.R
import com.example.agtia.databinding.ActivityHomeBinding
import com.example.agtia.Share_Task.Main_Share.Share_Task_Fragment

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragments(My_ToDoList_Fragment())



        binding.bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.mine -> replaceFragments(Profile_Fragment())
                R.id.shared -> replaceFragments(Share_Task_Fragment())
                R.id.history -> replaceFragments(History_Of_Tasks_Fragment())
                R.id.list -> replaceFragments(My_ToDoList_Fragment())
                R.id.friends -> replaceFragments(Add_Friends_Fragment())
            }
            true
        }
    }
    private fun replaceFragments(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}
