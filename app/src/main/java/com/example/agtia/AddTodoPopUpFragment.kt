package com.example.agtia
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.agtia.databinding.FragmentAddTodoPopUpBinding
import com.example.agtia.todofirst.utils.ToDoData
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar
import kotlin.math.log

class AddTodoPopUpFragment : DialogFragment() {

    private lateinit var listener: DialogNextBtnClickListener
    private lateinit var binding: FragmentAddTodoPopUpBinding
private var toDoData: ToDoData? =null
    fun setListener(listener: DialogNextBtnClickListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentAddTodoPopUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var selectedDate: String = ""

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(requireContext(), { view, year, monthOfYear, dayOfMonth ->
            // Set the selected date in the description field
            selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year)

        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        datePicker.show()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments
        if (args != null) {
            val taskId = args.getString("taskId")
            val task = args.getString("task")
            val taskDesc = args.getString("taskDesc") // Assuming the key for the description is "taskDesc"
            if (taskId != null && task != null && taskDesc!= null ) {
                toDoData =
                    taskDesc?.let { ToDoData(taskId, task, taskDesc) } // Assuming ToDoData has a constructor that accepts taskId, task, and taskDesc
                binding.todoEt.setText(toDoData?.task)
                binding.todoDesc.setText(toDoData?.desc)

            }
        }
        registerEvent()
        binding.selectDateButton.setOnClickListener {
            showDatePicker()
        }

    }

    companion object{
        const val Tag="ADDTODOPOPUPFRAGMENT"
        @JvmStatic
        fun newInstance(taskId: String, task: String, desc: String) = AddTodoPopUpFragment().apply {
            arguments = Bundle().apply {
                putString("taskId", taskId)
                putString("task", task)
                putString("taskDesc", desc) // Change the key to "taskDesc"
            }
        }
    }


    private fun registerEvent() {
        binding.todoNextBtn.setOnClickListener {
            val todoTask = binding.todoEt.text.toString()
            val todoDesc = binding.todoDesc.text.toString()

            if (todoTask.isNotEmpty() && todoDesc.isNotEmpty()) {
                if (toDoData == null) {
                    listener.onSaveTask(todoTask, binding.todoDesc, binding.todoEt, selectedDate) // Pass the selected date
                    dismiss()

                } else {
                    toDoData?.let {
                        it.task = todoTask
                        it.desc = todoDesc
                        listener.onUpdateTask(it, binding.todoEt, binding.todoDesc)
                    } ?: run {
                        Toast.makeText(context, "ToDoData is null", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Please type some task and description", Toast.LENGTH_SHORT).show()
            }
        }
        binding.todoClose.setOnClickListener {
            dismiss()
        }
    }


    interface DialogNextBtnClickListener {
        fun onSaveTask(todo: String, todoDesc: TextInputEditText, todoEt: TextInputEditText, date: String)
        fun onUpdateTask(toDoData: ToDoData, todoDesc: TextInputEditText, todoEt: TextInputEditText)

    }
}