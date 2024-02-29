package com.example.agtia.Share_Task.Main_Share

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.agtia.Share_Task.For_Me.Add_Shared_For_Me_Activity
import com.example.agtia.Share_Task.By_Me.Add_Shared_By_Me_Activity
import com.example.agtia.Share_Task.Requests_Shared_Tasks.Add_Requests_Share_Activity
import com.example.agtia.databinding.FragmentSharedTodoListBinding

class Share_Task_Fragment : Fragment(){

    private lateinit var binding:FragmentSharedTodoListBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         binding = FragmentSharedTodoListBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ClickEvent()
    }
    private fun ClickEvent() {
        binding.share.setOnClickListener {
            val intent = Intent(requireActivity(), Add_Sharing_Task::class.java)
            startActivity(intent)
        }
        binding.addSharedByMe.setOnClickListener {
            val intent=Intent(requireActivity(), Add_Shared_By_Me_Activity::class.java)
            startActivity(intent)
        }
        binding.addSharedForMe.setOnClickListener {
            val intent=Intent(requireActivity(), Add_Shared_For_Me_Activity::class.java)
            startActivity(intent)
        }
        binding.addRequests.setOnClickListener {
            val intent=Intent(requireActivity(), Add_Requests_Share_Activity::class.java)
            startActivity(intent)
        }
    }
}