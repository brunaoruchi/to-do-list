package com.example.todolist

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ListView
import android.widget.Toast
import com.example.todolist.databinding.ActivityMainBinding
import com.google.firebase.database.*

class MainActivity : AppCompatActivity(),  UpdateAndDelete{
    lateinit var database:DatabaseReference
    var toDoList: MutableList<ToDoModel>?=null
    lateinit var adapter: ToDoAdapter
    private var listViewItem: ListView?=null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listViewItem = findViewById<ListView>(R.id.item_listView)

        database = FirebaseDatabase.getInstance().reference

        binding.fab.setOnClickListener{
            val todoTitle = binding.textInput.text.toString()
            if(todoTitle.isNotEmpty()) {
                val todoItemData = ToDoModel.createList()
                todoItemData.itemData = todoTitle
                todoItemData.done = false

                val newItemData = database.child("todo").push()
                todoItemData.UID = newItemData.key

                newItemData.setValue(todoItemData)
                binding.textInput.text = null

                binding.textTask.clearFocus()

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(it.windowToken,0)

                Toast.makeText(this, "Task saved", Toast.LENGTH_LONG).show()
            }

        }

        toDoList = mutableListOf<ToDoModel>()
        adapter = ToDoAdapter(this,toDoList!!)
        listViewItem!!.adapter=adapter
        database.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext,"No item Added", Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                toDoList!!.clear()
                addItemToList(snapshot)
            }
        })
    }

    private fun addItemToList(snapshot: DataSnapshot) {
        val items = snapshot.children.iterator()

        if(items.hasNext()){

            val toDoIndexedValue = items.next()
            val itemsIterator = toDoIndexedValue.children.iterator()

            while (itemsIterator.hasNext()){

                val currentItem = itemsIterator.next()
                val toDoItemData = ToDoModel.createList()
                val map = currentItem.getValue() as HashMap<String, Any>

                toDoItemData.UID = currentItem.key
                toDoItemData.done = map.get("done") as Boolean?
                toDoItemData.itemData = map.get("itemData") as String?
                toDoList!!.add(toDoItemData)
            }
        }

        adapter.notifyDataSetChanged()
    }

    override fun modifyItem(itemUID: String, isDone: Boolean) {
        val itemReference = database.child("todo").child(itemUID)
        itemReference.child("done").setValue(isDone)
    }

    override fun onItemDelete(itemUID: String) {
        val itemReference = database.child("todo").child(itemUID)
        itemReference.removeValue()
        adapter.notifyDataSetChanged()
    }
}