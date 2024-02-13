interface DialogNextBtnClickListener {
    fun onSaveTask(todo: String, todoDesc:TextInputEditText, todoEt: TextInputEditText)
    fun onUpdateTask(toDoData: ToDoData, todoDesc: TextInputEditText, todoEt: TextInputEditText)
}
