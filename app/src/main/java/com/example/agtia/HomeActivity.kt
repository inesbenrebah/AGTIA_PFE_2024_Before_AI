package com.example.agtia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.agtia.databinding.ActivityHomeBinding
import com.example.agtia.Todolist

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        replaceFragments(Todolist())



        binding.bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.mine -> replaceFragments(mine())
                R.id.shared -> replaceFragments(shared_todo_list())
                R.id.history -> replaceFragments(historique_of_lists())
                R.id.list -> replaceFragments(Todolist())
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
