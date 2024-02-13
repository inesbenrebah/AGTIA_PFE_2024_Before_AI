// TodoAppWidget.kt
package com.example.agtia
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.ArrayAdapter
import android.widget.RemoteViews
import com.example.agtia.todofirst.utils.ToDoData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TodoAppWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val databaseRef = FirebaseDatabase.getInstance().reference
                .child("Tasks").child(currentUser?.uid.toString())

            val views = RemoteViews(context.packageName, R.layout.todo_app_widget)

            databaseRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasksList = mutableListOf<String>()
                    for (taskSnapshot in snapshot.children) {
                        val taskName = taskSnapshot.child("task").getValue(String::class.java)
                        taskName?.let { tasksList.add(it) }
                    }

                    // Set up the adapter for the ListView
                    val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, tasksList)
                    views.setRemoteAdapter(R.id.listView, Intent())
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle any errors
                }
            })
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(android.content.ComponentName(context, TodoAppWidget::class.java))
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}
